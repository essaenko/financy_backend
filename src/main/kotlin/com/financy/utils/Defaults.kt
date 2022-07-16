package com.financy.utils

import com.financy.model.CategoryType
import com.financy.model.TransactionType
import kotlinx.serialization.Serializable

data class DefaultCategoryData(
   val name: String,
   val type: CategoryType,
   val children: Array<DefaultCategoryData>? = null,
)


@Serializable
data class CollectionResponsePayload<T>(
  val total: Int,
  val elements: Int,
  val list: List<T>,
)

@Serializable
data class StatisticParametersInit(
  val dateFrom: Long,
  val dateTo: Long,
  val type: TransactionType?,
  val user: Int?,
  val category: Int?,
) {
  companion object {
    fun getValidInstance(
      dateFrom: String?,
      dateTo: String?,
      type: String?,
      user: String?,
      category: String?,
    ): StatisticParametersInit {
      val isValidTransactionType = TransactionType.values().map { it.toString() }.contains(type.toString())
      val categoryID = category?.toIntOrNull()
      val userID = user?.toIntOrNull()

      if (dateFrom?.toLongOrNull() == null || dateTo?.toLongOrNull() == null) {
        throw Error(Exceptions.BadRequestException.name)
      }

      return StatisticParametersInit(
        type = if(isValidTransactionType) TransactionType.valueOf(type.toString()) else null,
        dateFrom = dateFrom.toLong(),
        dateTo = dateTo.toLong(),
        category = if (categoryID != null && categoryID > 0) categoryID else null,
        user = if (userID != null && userID > 0) userID else null,
      )
    }
  }
}

@Serializable
data class TransactionFiltersInit(
  val type: TransactionType?,
  val dateFrom: Long?,
  val dateTo: Long?,
  val date: Long?,
  val category: Int?,
  val page: Int = 1,
  val perPage: Int = 20,
) {
  companion object {
    fun getValidInstance(
      type: String?,
      dateFrom: String?,
      dateTo: String?,
      date: String?,
      category: String?,
      page: String?,
      perPage: String?,
    ): TransactionFiltersInit {
      val isValidTransactionType = TransactionType.values().map { it.toString() }.contains(type.toString())

      return TransactionFiltersInit(
        type = if(isValidTransactionType) TransactionType.valueOf(type.toString()) else null,
        dateFrom = dateFrom?.toLongOrNull(),
        dateTo = dateTo?.toLongOrNull(),
        date = date?.toLongOrNull(),
        category = category?.toIntOrNull(),
        page = page?.toIntOrNull() ?: 1,
        perPage = perPage?.toIntOrNull() ?: 20,
      )
    }
  }
}

@Serializable
data class DefaultInstanceInit(
  val id: Int,
)

val defaultCategoriesData = arrayOf(
  DefaultCategoryData(
     name = "Unsorted",
     type = CategoryType.Income
  ),
  DefaultCategoryData(
     name = "Salary",
     type = CategoryType.Income,
  ),
  DefaultCategoryData(
     name = "Deposit",
     type = CategoryType.Income,
  ),
  DefaultCategoryData(
     name = "Part-time Job",
     type = CategoryType.Income,
  ),
  DefaultCategoryData(
     name = "Investment",
     type = CategoryType.Income,
  ),

  DefaultCategoryData(
     name = "Unsorted",
     type = CategoryType.Outcome,
  ),
  DefaultCategoryData(
    name = "Transfer",
    type = CategoryType.Outcome,
  ),
  DefaultCategoryData(
     name = "Transport",
     type = CategoryType.Outcome,
  ),
  DefaultCategoryData(
     name = "Car",
     type = CategoryType.Outcome,
     children = arrayOf(
      DefaultCategoryData(
         name = "Unsorted",
         type = CategoryType.Outcome,
      ),
      DefaultCategoryData(
         name = "Use",
         type = CategoryType.Outcome,
      ),
      DefaultCategoryData(
         name = "Service",
         type = CategoryType.Outcome,
      ),
    ),
  ),
  DefaultCategoryData(
     name = "Communication",
     type = CategoryType.Outcome,
     children = arrayOf(
      DefaultCategoryData(
         name = "Unsorted",
         type = CategoryType.Outcome,
      ),
      DefaultCategoryData(
         name = "Mobile phone",
         type = CategoryType.Outcome,
      ),
      DefaultCategoryData(
         name = "WiFi",
         type = CategoryType.Outcome,
      )
    )
  ),
  DefaultCategoryData(
     name = "Food",
     type = CategoryType.Outcome,
  ),
  DefaultCategoryData(
     name = "Healthy",
     type = CategoryType.Outcome,
     children = arrayOf(
      DefaultCategoryData(
         name = "Unsorted",
         type = CategoryType.Outcome,
      ),
      DefaultCategoryData(
         name = "Medicine",
         type = CategoryType.Outcome,
      ),
      DefaultCategoryData(
         name = "Cosmetic",
         type = CategoryType.Outcome,
      )
    )
  ),
  DefaultCategoryData(
     name = "Diner",
     type = CategoryType.Outcome,
     children = arrayOf(
      DefaultCategoryData(
         name = "Fast food",
         type = CategoryType.Outcome,
      ),
      DefaultCategoryData(
         name = "Restaurant",
         type = CategoryType.Outcome,
      ),
      DefaultCategoryData(
         name = "Unsorted",
         type = CategoryType.Outcome,
      )
    )
  ),
  DefaultCategoryData(
     name = "Clothes",
     type = CategoryType.Outcome,
  ),
  DefaultCategoryData(
     name = "Home",
     type = CategoryType.Outcome,
     children = arrayOf(
      DefaultCategoryData(
         name = "Unsorted",
         type = CategoryType.Outcome,
      ),
      DefaultCategoryData(
         name = "Rent",
         type = CategoryType.Outcome,
      ),
      DefaultCategoryData(
         name = "Bills",
         type = CategoryType.Outcome,
      ),
      DefaultCategoryData(
         name = "Chemicals",
         type = CategoryType.Outcome,
      ),
      DefaultCategoryData(
         name = "Comfort",
         type = CategoryType.Outcome,
      )
    )
  )
)