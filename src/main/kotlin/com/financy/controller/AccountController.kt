package com.financy.controller

import com.financy.dbInstance
import com.financy.model.Account
import com.financy.model.Accounts
import com.financy.model.User
import com.financy.utils.Exceptions
import org.ktorm.dsl.eq
import org.ktorm.entity.add
import org.ktorm.entity.find
import java.time.LocalDate

object AccountController {
  fun getAccount(id: Int): Account {
    return dbInstance?.Accounts?.find { it.id eq id } ?: throw Error(Exceptions.AccountNotFoundException.name)
  }

  fun create(user: User): Account {
    val account = Account {
      createdAt = LocalDate.now()
      updatedAt = null
    }
    dbInstance?.Accounts?.add(account)
    user.accountId = account.id

    user.flushChanges()

    CategoryController.createDefaultCategoriesForNewAccount(account)

    return account
  }
}