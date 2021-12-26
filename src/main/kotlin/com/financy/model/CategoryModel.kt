package com.financy.model

import com.financy.dbInstance
import com.financy.model.UsersSchema.bindTo
import kotlinx.serialization.Serializable
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.entity.Entity
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.*
import java.time.LocalDate

val Database.Categories get() = this.sequenceOf(CategoriesSchema);

enum class CategoryType {
  Income,
  Outcome,
}

object CategoriesSchema: Table<Category>("t_categories") {
  val id = int("id").primaryKey().bindTo { it.id }
  val parentId = int("parent_id").bindTo { it.parent?.id }
  val accountId = int("account_id").references(AccountsSchema) { it.account }
  val name = varchar("name").bindTo { it.name }
  val type = enum<CategoryType>("type").bindTo { it.type }
  val createdAt = date("created_at").bindTo { it.createdAt }
  val updatedAt = date("updated_at").bindTo { it.updatedAt }
}

@Serializable
data class CategoryData(
  val id: Int,
  val parent: CategoryData?,
  val account: AccountData,
  val name: String,
  val type: CategoryType,
  val createdAt: String,
  val updatedAt: String?,
) {
  companion object {
    fun getSerializable(category: Category): CategoryData {
      return CategoryData(
        id = category.id,
        parent = if (category.parent != null) dbInstance?.Categories?.find { it.id eq category.parent!!.id }
          ?.let { getSerializable(it) } else null,
        account = AccountData.getSerializable(category.account, false),
        name = category.name,
        type = category.type,
        createdAt = category.createdAt.toString(),
        updatedAt = category.updatedAt?.toString(),
      )
    }
  }

}

@Serializable
data class CategoryInitData(
  val name: String,
  val type: String,
  val parent: Int? = null,
)

interface Category: Entity<Category> {
  companion object: Entity.Factory<Category>()

  val id: Int
  var parent: Category?
  var account: Account
  var name: String
  var type: CategoryType
  var createdAt: LocalDate
  var updatedAt: LocalDate?
}