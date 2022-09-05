package com.financy.controller

import com.financy.dbInstance
import com.financy.model.*
import com.financy.utils.DefaultCategoryData
import com.financy.utils.Exceptions
import com.financy.utils.defaultCategoriesData
import org.ktorm.dsl.eq
import org.ktorm.entity.*
import java.time.LocalDate

object CategoryController {
  fun createDefaultCategoriesForNewAccount(account: Account) {
    for(categoryData in defaultCategoriesData) {
      createDefaultCategoryInstance(account, categoryData)
    }
  }

  fun getAll(account: Account): List<Category> {
    return (dbInstance?.Categories?.filter { it.accountId eq account.id })?.toList() ?: listOf()
  }

  fun create(user: User, categoryInit: CategoryInitData): Category {
    val categoryAcc: Account? = dbInstance?.Accounts?.find { it.id eq user.account!!.id}

    if (categoryAcc != null) {
      val category = Category {
        account = categoryAcc
        parent = if (categoryInit.parent != null) dbInstance?.Categories?.find { it.id eq categoryInit.parent } else null
        name = categoryInit.name
        type = TransactionType.valueOf(categoryInit.type)
        mcc = categoryInit.mcc
        createdAt = LocalDate.now()
        updatedAt = null
      }

      dbInstance?.Categories?.add(category)

      return category
    } else {
      throw Error(Exceptions.UnresolvedCategoryAccountException.name)
    }
  }

  private fun createDefaultCategoryInstance (acc: Account, data: DefaultCategoryData, pnt: Int? = null) {
    val category = Category {
      account = acc
      parent = if (pnt != null) dbInstance?.Categories?.find { it.id eq pnt } else null
      name = data.name
      type = data.type
      mcc = data.mcc
      createdAt = LocalDate.now()
      updatedAt = null
    }

    dbInstance?.Categories?.add(category)

    if (data.children != null) {
      for (child in data.children) {
        createDefaultCategoryInstance(acc, child, category.id)
      }
    }
  }
}