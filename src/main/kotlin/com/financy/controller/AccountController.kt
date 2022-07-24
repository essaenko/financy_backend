package com.financy.controller

import com.financy.dbInstance
import com.financy.model.Account
import com.financy.model.Accounts
import com.financy.model.User
import com.financy.model.Users
import com.financy.utils.Exceptions
import org.ktorm.dsl.eq
import org.ktorm.entity.add
import org.ktorm.entity.filter
import org.ktorm.entity.find
import org.ktorm.entity.toList
import java.time.LocalDate

object AccountController {
  fun getUsers(account: Account): List<User> {
    return dbInstance?.Users?.filter { it.accountId eq account.id }?.toList() ?: listOf()
  }

  fun removeUserFromAccount(user: User, email: String) {
    if (user.account?.owner != user.id) {
      throw Error(Exceptions.NotPermittedOperation.name)
    }

    val member = dbInstance?.Users?.find { it.email eq email } ?: throw Error(Exceptions.UserNotFoundException.name)
    member.account = null
    member.flushChanges()
  }

  fun create(user: User): Account {
    val account = Account {
      owner = user.id
      createdAt = LocalDate.now()
      updatedAt = null
    }
    dbInstance?.Accounts?.add(account)
    user.account = account

    user.flushChanges()

    CategoryController.createDefaultCategoriesForNewAccount(account)

    return account
  }
}