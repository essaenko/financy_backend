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
    val categories = defaultCategoriesData

    for(categoryData in categories) {
      createDefaultCategoryInstance(account, categoryData)
    }
  }

  fun getAll(account: Account): List<Category> {
    return (dbInstance?.Categories?.filter { it.accountId eq account.id })?.toList() ?: listOf()
  }

  fun create(categoryName: String, acc: Int, categoryType: CategoryType, pnt: Int? = null): Category {
    val categoryAcc: Account? = dbInstance?.Accounts?.find { it.id eq acc}

    if (categoryAcc != null) {
      val category = Category {
        account = categoryAcc
        parent = if (pnt != null) dbInstance?.Categories?.find { it.id eq pnt } else null
        name = categoryName
        type = categoryType
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