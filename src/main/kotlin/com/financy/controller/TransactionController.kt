package com.financy.controller

import com.financy.dbInstance
import com.financy.model.*
import com.financy.utils.*
import io.ktor.application.*
import io.ktor.response.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.ktorm.dsl.*
import org.ktorm.entity.*
import org.ktorm.schema.typeOf
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.random.Random

object TransactionController {
  private fun processTransaction(transaction: Transaction) {
    val payment: PaymentMethod = transaction.from;
    val transfer: PaymentMethod? = transaction.to;
    if (transfer != null) {
      transaction.to = transfer
      transfer.remains = if (transaction.type == TransactionType.Outcome) transfer.remains + transaction.cost else transfer.remains - transaction.cost

      transfer.flushChanges()
    }

    payment.remains = if (transaction.type == TransactionType.Income) payment.remains + transaction.cost else payment.remains - transaction.cost

    payment.flushChanges()
  }

  private fun undoTransaction(transaction: Transaction) {
    val payment: PaymentMethod = transaction.from;
    val transfer: PaymentMethod? = transaction.to;
    if (transfer != null) {
      transaction.to = transfer
      transfer.remains = if (transaction.type == TransactionType.Outcome) transfer.remains - transaction.cost else transfer.remains + transaction.cost
      transfer.flushChanges()
    }

    payment.remains = if (transaction.type == TransactionType.Income) payment.remains - transaction.cost else payment.remains + transaction.cost

    payment.flushChanges()
  }

  fun remove(acc: Account, init: DefaultInstanceInit) {
    val id: Int = init.id
    val transaction: Transaction = dbInstance?.Transactions?.find { it.id eq id } ?: throw Error(Exceptions.UnresolvedTransactionException.name);
    if (transaction.account.id != acc.id) {
      throw Error(Exceptions.UnauthorizedOperationException.name);
    }

    this.undoTransaction(transaction);

    transaction.delete()
  }

  fun update(acc: Account, init: TransactionInitData): Transaction {
    val id: Int = init.id ?: throw Error(Exceptions.UnresolvedTransactionIdException.name)
    val transaction: Transaction = dbInstance?.Transactions?.find { it.id eq id } ?: throw Error(Exceptions.UnresolvedTransactionException.name);

    if (transaction.account.id != acc.id) {
      throw Error(Exceptions.UnauthorizedOperationException.name);
    }

    this.undoTransaction(transaction)

    if (init.to != null) {
      transaction.to = dbInstance?.Payments?.find { it.id eq init.to }
    }
    if (transaction.from.id != init.from) {
      transaction.from = dbInstance?.Payments?.find { it.id eq init.from } ?: throw Error(Exceptions.UnresolvedTransactionPaymentMethodException.name)
    }
    if (transaction.type != TransactionType.valueOf(init.type)) {
      transaction.type = TransactionType.valueOf(init.type);
    }
    if (transaction.category.id != init.category) {
      transaction.category = dbInstance?.Categories?.find { it.id eq init.category } ?: throw Error(Exceptions.UnresolvedTransactionCategoryException.name)
    }
    if (transaction.cost != init.cost) {
      transaction.cost = init.cost
    }
    if (transaction.comment != init.comment) {
      transaction.comment = init.comment
    }

    transaction.flushChanges();

    this.processTransaction(transaction);

    return transaction
  }
  fun create(usr: User, acc: Account, init: TransactionInitData): Transaction {
    val cat: Category? = dbInstance?.Categories?.find { it.id eq init.category }

    if (cat != null) {
      val payment = dbInstance?.Payments?.find { it.id eq init.from } ?: dbInstance?.Payments?.find { it.accountId eq acc.id }!!
      val transaction = Transaction {
        account = acc
        user = usr
        type = TransactionType.valueOf(init.type)
        comment = init.comment
        from = payment
        cost = init.cost
        category = cat
        createdAt = if (init.date != null) LocalDate.ofInstant(Instant.ofEpochMilli(init.date), ZoneId.systemDefault()) else LocalDate.now()
        updatedAt = null
      }

      dbInstance?.Transactions?.add(transaction)

      this.processTransaction(transaction);

      return transaction
    } else {
      throw Error(Exceptions.UnresolvedTransactionCategoryException.name)
    }
  }

  fun getList(account: Account, filters: TransactionFiltersInit): CollectionResponsePayload<TransactionData> {
    val metrics = TransactionMetrics.initializeListMetrics()
    val defaultQueryTimer = metrics[0].startTimer()
    val resultQueryTimer = metrics[6].startTimer()

    val defaultQuery = dbInstance?.
      Transactions?.
      filter { it.accountId eq account.id }?.
      sortedBy { it.createdAt.desc() }
    defaultQueryTimer.observeDuration();

    var result = defaultQuery;

    if (filters.type != null) {
      val type: TransactionType = filters.type;
      val timer = metrics[1].startTimer()

      result = result?.filter { it.type eq type }
      timer.observeDuration();
    }

    if (filters.dateFrom != null && filters.date == null) {
      val dateFrom: LocalDate = Instant.ofEpochMilli(filters.dateFrom).atZone(ZoneId.systemDefault()).toLocalDate();
      val timer = metrics[2].startTimer()

      result = result?.filter { it.createdAt greaterEq dateFrom }
      timer.observeDuration()
    }

    if (filters.dateTo != null && filters.date == null) {
      val dateTo: LocalDate = Instant.ofEpochMilli(filters.dateTo).atZone(ZoneId.systemDefault()).toLocalDate();
      val timer = metrics[3].startTimer()

      result = result?.filter { it.createdAt lessEq dateTo }
      timer.observeDuration()
    }

    if (filters.date != null) {
      val date: LocalDate = Instant.ofEpochMilli(filters.date).atZone(ZoneId.systemDefault()).toLocalDate();
      val timer = metrics[4].startTimer()

      result = result?.filter { it.createdAt eq date }
      timer.observeDuration()
    }

    if (filters.category != null) {
      val timer = metrics[5].startTimer()
      result = result?.filter { it.categoryId eq filters.category };
      timer.observeDuration()
    }

    val total = result?.count() ?: 0;

    result = result?.drop(filters.perPage * (filters.page - 1))?.take(filters.perPage)

    val list = result?.toList()
    resultQueryTimer.observeDuration()
    val mapTimer = metrics[7].startTimer()
    val map = list?.map { TransactionData.getSerializable(it) }
    mapTimer.observeDuration()

    return CollectionResponsePayload(
      total,
      elements = filters.perPage,
      list = map ?: listOf()
    )
  }

  fun generateTestTransactions(usr: User, acc: Account) {
    val incomeMaxCost: Int = 150000;
    val incomeMinCost: Int = 10000;
    val outcomeMaxCost: Int = 100000;
    val outcomeMinCost: Int = 1000;

    for (i in 1..50) {
      val cst: Int = Random.nextInt(incomeMinCost, incomeMaxCost);
      val catIds = dbInstance?.Categories?.
        filter { (it.accountId eq acc.id) and (it.type eq CategoryType.Income) }?.
        toList()?.map { it.id } ?: listOf();
      val cat = dbInstance?.Categories?.find { it.id eq (catIds[Random.nextInt(catIds.size)]) }
      val payment = dbInstance?.Payments?.find { it.accountId eq acc.id }
      val transaction = Transaction {
        account = acc
        user = usr
        category = cat!!
        type = TransactionType.Income
        cost = cst
        from = payment!!
        createdAt = LocalDate.now()
      }

      dbInstance?.Transactions?.add(transaction)
    }
    for (i in 1..50) {
      val cst: Int = Random.nextInt(outcomeMinCost, outcomeMaxCost);
      val catIds = dbInstance?.Categories?.
      filter { (it.accountId eq acc.id) and (it.type eq CategoryType.Outcome) }?.
      toList()?.map { it.id } ?: listOf();
      val cat = dbInstance?.Categories?.find { it.id eq (catIds[Random.nextInt(catIds.size)]) }
      val payment = dbInstance?.Payments?.find { it.accountId eq acc.id }
      val transaction = Transaction {
        account = acc
        user = usr
        category = cat!!
        type = TransactionType.Outcome
        cost = cst
        from = payment!!
        createdAt = LocalDate.now()
      }

      dbInstance?.Transactions?.add(transaction)
    }
  }
}