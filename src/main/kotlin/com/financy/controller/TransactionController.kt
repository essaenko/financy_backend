package com.financy.controller

import com.financy.dbInstance
import com.financy.model.*
import com.financy.utils.ApiResponse
import com.financy.utils.ApiResponseStatus
import com.financy.utils.DefaultInstanceInit
import com.financy.utils.Exceptions
import io.ktor.application.*
import io.ktor.response.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.ktorm.dsl.eq
import org.ktorm.entity.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

object TransactionController {
  fun remove(acc: Account, init: DefaultInstanceInit) {
    val id: Int = init.id
    val transaction: Transaction = dbInstance?.Transactions?.find { it.id eq id } ?: throw Error(Exceptions.UnresolvedTransactionException.name);
    if (transaction.account.id != acc.id) {
      throw Error(Exceptions.UnauthorizedOperationException.name);
    }

    transaction.delete()
  }
  fun update(acc: Account, init: TransactionInitData): Transaction {
    val id: Int = init.id ?: throw Error(Exceptions.UnresolvedTransactionIdException.name)
    val transaction: Transaction = dbInstance?.Transactions?.find { it.id eq id } ?: throw Error(Exceptions.UnresolvedTransactionException.name);

    if (transaction.account.id != acc.id) {
      throw Error(Exceptions.UnauthorizedOperationException.name);
    }

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

      if (init.to != null) {
        val transfer = dbInstance?.Payments?.find { it.id eq init.to }

        if (transfer != null) {
          transaction.to = transfer
          transfer.remains = if (transaction.type == TransactionType.Outcome) transfer.remains + transaction.cost else transfer.remains - transaction.cost

          transfer.flushChanges()
        }
      }

      payment.remains = if (transaction.type == TransactionType.Income) payment.remains + transaction.cost else payment.remains - transaction.cost

      payment.flushChanges()

      dbInstance?.Transactions?.add(transaction)

      return transaction
    } else {
      throw Error(Exceptions.UnresolvedTransactionCategoryException.name)
    }
  }

  fun getList(account: Account, page: Int = 1): List<Transaction> {
    return dbInstance?.
    Transactions?.
    filter { it.accountId eq account.id }?.
    drop(20 * (page - 1))?.
    take(20)?.
    toList()
      ?: listOf()
  }
}