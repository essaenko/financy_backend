package com.financy.controller

import com.financy.dbInstance
import com.financy.model.Account
import com.financy.model.Categories
import com.financy.model.Category
import com.financy.model.CategoryType
import com.financy.utils.DefaultCategoryData
import com.financy.utils.defaultCategoriesData
import org.ktorm.dsl.eq
import org.ktorm.entity.*
import java.time.LocalDate

object CategoryController {
  fun createDefaultCategoriesForNewAccount(account: Account) {
    val categories = defaultCategoriesData;

    for(categoryData in categories) {
      createDefaultCategoryInstance(account, categoryData)
    }
  }

  fun getAll(account: Account): List<Category> {
    return (dbInstance?.Categories?.filter { it.accountId eq account.id })?.toList() ?: listOf()
  }

  fun create(categoryName: String, account: Int, categoryType: CategoryType, parent: Int? = null): Category {
    val category = Category {
      accountId = account
      parentId = parent
      name = categoryName
      type = categoryType
      createdAt = LocalDate.now()
      updatedAt = null
    }

    dbInstance?.Categories?.add(category)

    return category
  }

  private fun createDefaultCategoryInstance (account: Account, data: DefaultCategoryData, parent: Int? = null) {
    val category = Category {
      accountId = account.id
      parentId = parent
      name = data.name
      type = data.type
      createdAt = LocalDate.now()
      updatedAt = null
    }

    dbInstance?.Categories?.add(category);

    if (data.children != null) {
      for (child in data.children) {
        createDefaultCategoryInstance(account, child, category.id);
      }
    }
  }
}