package com.financy.utils

import com.financy.model.TransactionType
import kotlinx.serialization.Serializable

data class DefaultCategoryData(
  val name: String,
  val type: TransactionType,
  val mcc: String? = null,
  val tags: String? = null,
  val children: List<DefaultCategoryData>? = null,
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
        type = if (isValidTransactionType) TransactionType.valueOf(type.toString()) else null,
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
        type = if (isValidTransactionType) TransactionType.valueOf(type.toString()) else null,
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
    name = "Другое",
    type = TransactionType.Income
  ),
  DefaultCategoryData(
    name = "Кэшбэк",
    type = TransactionType.Income
  ),
  DefaultCategoryData(
    name = "Зарплата",
    type = TransactionType.Income,
  ),
  DefaultCategoryData(
    name = "Пополнение",
    type = TransactionType.Income,
  ),
  DefaultCategoryData(
    name = "Подработка",
    type = TransactionType.Income,
  ),
  DefaultCategoryData(
    name = "Инвестиции",
    type = TransactionType.Income,
  ),

  DefaultCategoryData(
    name = "Другое",
    type = TransactionType.Outcome,
  ),
  DefaultCategoryData(
    name = "Услуги",
    type = TransactionType.Outcome,
    mcc = "#7221#",
    children = listOf(
      DefaultCategoryData(
        name = "Фото/Видео",
        type = TransactionType.Outcome,
        mcc = "#7395#",
      ),
    )
  ),
  DefaultCategoryData(
    name = "Переводы",
    type = TransactionType.Outcome,
  ),
  DefaultCategoryData(
    name = "Транспорт",
    type = TransactionType.Outcome,
    mcc = "#4121#"
  ),
  DefaultCategoryData(
    name = "Автомобиль",
    type = TransactionType.Outcome,
    children = listOf(
      DefaultCategoryData(
        name = "Топливо",
        type = TransactionType.Outcome,
        mcc = "#5541#",
        tags = "#Топливо#"
      ),
      DefaultCategoryData(
        name = "Другое",
        type = TransactionType.Outcome,
      ),
      DefaultCategoryData(
        name = "Использование",
        type = TransactionType.Outcome,
      ),
      DefaultCategoryData(
        name = "Обслуживание",
        type = TransactionType.Outcome,
      ),
    ),
  ),
  DefaultCategoryData(
    name = "Связь",
    type = TransactionType.Outcome,
    tags = "#Связь#Связь, телеком#Телеком#",
    mcc = "#4814#",
    children = listOf(
      DefaultCategoryData(
        name = "Другое",
        type = TransactionType.Outcome,
      ),
      DefaultCategoryData(
        name = "Телефон",
        type = TransactionType.Outcome,
      ),
      DefaultCategoryData(
        name = "Интернет",
        type = TransactionType.Outcome,
      )
    )
  ),
  DefaultCategoryData(
    name = "Супермаркеты",
    type = TransactionType.Outcome,
    tags = "#Супермаркеты#",
    mcc = "#5499#5411#",
    children = listOf(
      DefaultCategoryData(
        name = "Продукты",
        type = TransactionType.Outcome,
      ),
      DefaultCategoryData(
        name = "Хоз товары",
        type = TransactionType.Outcome,
        mcc = "#5999#",
        tags = "#Разные товары#"
      )
    )
  ),
  DefaultCategoryData(
    name = "Налоги и сборы",
    type = TransactionType.Outcome,
    mcc = "#9311#"
  ),
  DefaultCategoryData(
    name = "Маркетплейсы",
    type = TransactionType.Outcome,
    mcc = "#5399#"
  ),
  DefaultCategoryData(
    name = "Здоровье",
    type = TransactionType.Outcome,
    children = listOf(
      DefaultCategoryData(
        name = "Другое",
        type = TransactionType.Outcome,
      ),
      DefaultCategoryData(
        name = "Аптеки",
        type = TransactionType.Outcome,
        tags = "#Аптеки#",
        mcc = "#5912#"
      ),
      DefaultCategoryData(
        name = "Косметика",
        type = TransactionType.Outcome,
      )
    )
  ),
  DefaultCategoryData(
    name = "Еда",
    type = TransactionType.Outcome,
    children = listOf(
      DefaultCategoryData(
        name = "Фастфуд",
        type = TransactionType.Outcome,
        tags = "#Фастфуд#",
        mcc = "#5814#"
      ),
      DefaultCategoryData(
        name = "Кафе/Рестораны",
        tags = "#Рестораны и кафе#Рестораны#",
        mcc = "#5812#",
        type = TransactionType.Outcome,
      ),
      DefaultCategoryData(
        name = "Другое",
        type = TransactionType.Outcome,
      )
    )
  ),
  DefaultCategoryData(
    name = "Переводы",
    type = TransactionType.Outcome,
    tags = "#Перевод с карты#Переводы/иб#"
  ),
  DefaultCategoryData(
    name = "Одежда",
    type = TransactionType.Outcome,
    mcc = "#5651#",
    tags = "#Одежда, обувь#Одежда#Обувь#"
  ),
  DefaultCategoryData(
    name = "Дом",
    type = TransactionType.Outcome,
    children = listOf(
      DefaultCategoryData(
        name = "Другое",
        type = TransactionType.Outcome,
      ),
      DefaultCategoryData(
        name = "Аренда",
        type = TransactionType.Outcome,
      ),
      DefaultCategoryData(
        name = "Счета",
        type = TransactionType.Outcome,
      ),
      DefaultCategoryData(
        name = "Средства для уборки",
        type = TransactionType.Outcome,
      ),
      DefaultCategoryData(
        name = "Уют",
        type = TransactionType.Outcome,
      )
    )
  )
)