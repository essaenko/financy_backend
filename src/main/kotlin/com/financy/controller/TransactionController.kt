package com.financy.controller

import com.financy.dbInstance
import com.financy.model.*
import com.financy.provider.BankProviders
import com.financy.reader.CSVReader
import com.financy.reader.PDFReader
import com.financy.reader.Readers
import com.financy.reader.XLSReader
import com.financy.utils.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.ktorm.dsl.*
import org.ktorm.entity.*
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import kotlin.random.Random

object TransactionController {
  fun processTransaction(transaction: Transaction) {
    val payment: PaymentMethod = transaction.from
    val transfer: PaymentMethod? = transaction.to
    if (transfer != null) {
      transaction.to = transfer
      PaymentMethodController.processTransaction(
        transfer,
        if (transaction.type == TransactionType.Outcome) transfer.account.remains + transaction.cost
        else transfer.account.remains - transaction.cost,
        transaction.createdAt.toLocalDate()
      )
    }

    PaymentMethodController.processTransaction(
      payment,
      if (transaction.type == TransactionType.Income) payment.account.remains + transaction.cost
      else payment.account.remains - transaction.cost,
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
        if (transaction.type == TransactionType.Outcome) transfer.account.remains - transaction.cost
        else transfer.account.remains + transaction.cost,
        transaction.createdAt.toLocalDate()
      )
    }

    PaymentMethodController.processTransaction(
      payment,
      if (transaction.type == TransactionType.Income) payment.account.remains - transaction.cost
      else payment.account.remains + transaction.cost,
      transaction.createdAt.toLocalDate()
    )
  }

  suspend fun processImportFile(user: User, reader: Readers, bytes: ByteArray, provider: String) = coroutineScope {
    try {
      launch {
        if (BankProviders.containsKey(provider)) {
          BankProviders.get(provider)?.parseExport(
            user,
            when(reader) {
              Readers.CSV -> CSVReader(bytes)
              Readers.XLS -> XLSReader(bytes)
              Readers.PDF -> PDFReader(bytes)
            }
          )
        }
      }

    } catch(error: Error) {
      com.financy.Logger.error(error.localizedMessage, error)
    }
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
      transaction.to = dbInstance?.PaymentMethods?.find { it.id eq init.to }
    }
    if (transaction.from.id != init.from) {
      transaction.from = dbInstance?.PaymentMethods?.find { it.id eq init.from } ?: throw Error(Exceptions.UnresolvedTransactionPaymentMethodException.name)
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
      val payment = dbInstance?.PaymentMethods?.find { it.id eq init.from } ?: throw Error(Exceptions.PaymentMethodNotFoundException.name)
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

      if (init.to != null) {
        val transfer = dbInstance?.PaymentMethods?.find { it.id eq init.to } ?: throw Error(Exceptions.PaymentMethodNotFoundException.name)

        transaction.to = transfer
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
        filter { (it.accountId eq acc.id) and (it.type eq TransactionType.Income) }?.
        toList()?.map { it.id } ?: listOf()
      val cat = dbInstance?.Categories?.find { it.id eq (catIds[Random.nextInt(catIds.size)]) }
      val payment = dbInstance?.PaymentMethods?.find { it.accountId eq acc.id }
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
      filter { (it.accountId eq acc.id) and (it.type eq TransactionType.Outcome) }?.
      toList()?.map { it.id } ?: listOf()
      val cat = dbInstance?.Categories?.find { it.id eq (catIds[Random.nextInt(catIds.size)]) }
      val payment = dbInstance?.PaymentMethods?.find { it.accountId eq acc.id }
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