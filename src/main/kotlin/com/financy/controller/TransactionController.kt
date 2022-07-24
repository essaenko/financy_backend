package com.financy.controller

import com.financy.dbInstance
import com.financy.model.*
import com.financy.utils.*
import org.ktorm.dsl.*
import org.ktorm.entity.*
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import kotlin.random.Random

object TransactionController {
  private fun processTransaction(transaction: Transaction) {
    val payment: PaymentMethod = transaction.from
    val transfer: PaymentMethod? = transaction.to
    if (transfer != null) {
      transaction.to = transfer
      PaymentMethodController.processTransaction(
        transfer,
        if (transaction.type == TransactionType.Outcome) transfer.remains + transaction.cost
        else transfer.remains - transaction.cost,
        transaction.createdAt.toLocalDate()
      )
    }

    PaymentMethodController.processTransaction(
      payment,
      if (transaction.type == TransactionType.Income) payment.remains + transaction.cost
      else payment.remains - transaction.cost,
      transaction.createdAt.toLocalDate()
    )
  }

  private fun undoTransaction(transaction: Transaction) {
    val payment: PaymentMethod = transaction.from
    val transfer: PaymentMethod? = transaction.to
    if (transfer != null) {
      transaction.to = transfer
      PaymentMethodController.processTransaction(
        transfer,
        if (transaction.type == TransactionType.Outcome) transfer.remains - transaction.cost
        else transfer.remains + transaction.cost,
        transaction.createdAt.toLocalDate()
      )
    }

    PaymentMethodController.processTransaction(
      payment,
      if (transaction.type == TransactionType.Income) payment.remains - transaction.cost
      else payment.remains + transaction.cost,
      transaction.createdAt.toLocalDate()
    )
  }

  fun remove(acc: Account, init: DefaultInstanceInit) {
    val id: Int = init.id
    val transaction: Transaction = dbInstance?.Transactions?.find { it.id eq id } ?: throw Error(Exceptions.UnresolvedTransactionException.name)
    if (transaction.account.id != acc.id) {
      throw Error(Exceptions.NotPermittedOperation.name)
    }

    this.undoTransaction(transaction)

    transaction.delete()
  }

  fun update(acc: Account, init: TransactionInitData): Transaction {
    val id: Int = init.id ?: throw Error(Exceptions.UnresolvedTransactionIdException.name)
    val transaction: Transaction = dbInstance?.Transactions?.find { it.id eq id } ?: throw Error(Exceptions.UnresolvedTransactionException.name)

    if (transaction.account.id != acc.id) {
      throw Error(Exceptions.NotPermittedOperation.name)
    }

    this.undoTransaction(transaction)

    if (init.to != null) {
      transaction.to = dbInstance?.Payments?.find { it.id eq init.to }
    }
    if (transaction.from.id != init.from) {
      transaction.from = dbInstance?.Payments?.find { it.id eq init.from } ?: throw Error(Exceptions.UnresolvedTransactionPaymentMethodException.name)
    }
    if (transaction.type != TransactionType.valueOf(init.type)) {
      transaction.type = TransactionType.valueOf(init.type)
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

    transaction.flushChanges()

    this.processTransaction(transaction)

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
        createdAt = if (init.date != null) LocalDateTime.ofInstant(Instant.ofEpochMilli(init.date), ZoneId.systemDefault()) else LocalDateTime.now()
        updatedAt = null
      }

      dbInstance?.Transactions?.add(transaction)

      this.processTransaction(transaction)

      return transaction
    } else {
      throw Error(Exceptions.UnresolvedTransactionCategoryException.name)
    }
  }

  fun getList(account: Account, filters: TransactionFiltersInit): CollectionResponsePayload<TransactionData> {
    val metrics = TransactionMetrics.initializeListMetrics()
    val defaultQueryTimer = metrics[0].startTimer()
    val resultQueryTimer = metrics[6].startTimer()

    var entity = dbInstance?.
      Transactions?.
      filter { it.accountId eq account.id }
    defaultQueryTimer.observeDuration()


    if (filters.type != null) {
      val type: TransactionType = filters.type
      val timer = metrics[1].startTimer()

      entity = entity?.filter { it.type eq type }
      timer.observeDuration()
    }

    if (filters.dateFrom != null && filters.date == null) {
      val dateFrom: LocalDateTime = Instant.ofEpochMilli(filters.dateFrom).atZone(ZoneId.systemDefault()).toLocalDateTime()
      val timer = metrics[2].startTimer()

      entity = entity?.filter { it.createdAt greaterEq dateFrom }
      timer.observeDuration()
    }

    if (filters.dateTo != null && filters.date == null) {
      val dateTo: LocalDateTime = Instant.ofEpochMilli(filters.dateTo).atZone(ZoneId.systemDefault()).toLocalDateTime()
      val timer = metrics[3].startTimer()

      entity = entity?.filter { it.createdAt lessEq dateTo }
      timer.observeDuration()
    }

    if (filters.date != null) {
      val date: LocalDateTime = Instant.ofEpochMilli(filters.date).atZone(ZoneId.systemDefault()).toLocalDateTime()
      val timer = metrics[4].startTimer()

      entity = entity?.filter { it.createdAt eq date }
      timer.observeDuration()
    }

    if (filters.category != null) {
      val timer = metrics[5].startTimer()
      entity = entity?.filter { it.categoryId eq filters.category }
      timer.observeDuration()
    }

    val total = entity?.count() ?: 0
    val query = entity?.
      query?.
      orderBy(TransactionsSchema.createdAt.desc())?.
      limit(filters.perPage * (filters.page - 1), filters.perPage)

    val list = query?.map { TransactionsSchema.createEntity(it) }
    resultQueryTimer.observeDuration()
    val mapTimer = metrics[7].startTimer()
    val map = list?.map { TransactionData.getSerializable(it) }
    mapTimer.observeDuration()

    return CollectionResponsePayload(
      total,
      elements = list?.size ?: 0,
      list = map ?: listOf()
    )
  }

  fun generateTestTransactions(usr: User, acc: Account) {
    val incomeMaxCost = 100000
    val incomeMinCost = 1000
    val outcomeMaxCost = 100000
    val outcomeMinCost = 1000
    val calendar = Calendar.getInstance()

    for (i in 1..56) {
      val cst: Float = Random.nextInt(incomeMinCost, incomeMaxCost).toFloat()
      val catIds = dbInstance?.Categories?.
        filter { (it.accountId eq acc.id) and (it.type eq CategoryType.Income) }?.
        toList()?.map { it.id } ?: listOf()
      val cat = dbInstance?.Categories?.find { it.id eq (catIds[Random.nextInt(catIds.size)]) }
      val payment = dbInstance?.Payments?.find { it.accountId eq acc.id }
      val transaction = Transaction {
        account = acc
        user = usr
        category = cat!!
        type = TransactionType.Income
        cost = cst
        from = payment!!
        createdAt = LocalDateTime.of(
          calendar.get(Calendar.YEAR),
          calendar.get(Calendar.MONTH),
          kotlin.math.ceil(i.toDouble() / 2).toInt(),
          0,
          0,
          0
        )
      }

      dbInstance?.Transactions?.add(transaction)
      this.processTransaction(transaction)
    }
    for (i in 1..56) {
      val cst: Float = Random.nextInt(outcomeMinCost, outcomeMaxCost).toFloat()
      val catIds = dbInstance?.Categories?.
      filter { (it.accountId eq acc.id) and (it.type eq CategoryType.Outcome) }?.
      toList()?.map { it.id } ?: listOf()
      val cat = dbInstance?.Categories?.find { it.id eq (catIds[Random.nextInt(catIds.size)]) }
      val payment = dbInstance?.Payments?.find { it.accountId eq acc.id }
      val transaction = Transaction {
        account = acc
        user = usr
        category = cat!!
        type = TransactionType.Outcome
        cost = cst
        from = payment!!
        createdAt = LocalDateTime.of(
          calendar.get(Calendar.YEAR),
          calendar.get(Calendar.MONTH),
          kotlin.math.ceil(i.toDouble() / 2).toInt(),
          0,
          0,
          0
        )
      }

      dbInstance?.Transactions?.add(transaction)
      this.processTransaction(transaction)
    }
  }
}