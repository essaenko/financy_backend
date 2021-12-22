package com.financy.controller

import com.financy.dbInstance
import com.financy.model.*
import org.ktorm.dsl.eq
import org.ktorm.entity.add
import org.ktorm.entity.filter
import org.ktorm.entity.toList
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

object TransactionController {
  fun create(user: User, account: Account, init: TransactionInitData): Transaction {
    val transaction = Transaction {
      accountId = account.id
      userId = user.id
      type = TransactionType.valueOf(init.type)
      comment = init.comment
      cost = init.cost
      categoryId = init.category
      createdAt = if (init.date != null) LocalDate.ofInstant(Instant.ofEpochMilli(init.date), ZoneId.systemDefault()) else LocalDate.now()
      updatedAt = null
    }

    dbInstance?.Transactions?.add(transaction)

    return transaction
  }

  fun getAll(account: Account): List<Transaction> {
    return dbInstance?.Transactions?.filter { it.accountId eq account.id }?.toList() ?: listOf()
  }
}