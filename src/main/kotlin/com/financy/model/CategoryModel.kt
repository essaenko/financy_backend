package com.financy.model

import com.financy.model.UsersSchema.bindTo
import kotlinx.serialization.Serializable
import org.ktorm.database.Database
import org.ktorm.entity.Entity
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
  val parentId = int("parent_id").bindTo { it.parentId }
  val accountId = int("account_id").bindTo { it.accountId }
  val name = varchar("name").bindTo { it.name }
  val type = enum<CategoryType>("type").bindTo { it.type }
  val createdAt = date("created_at").bindTo { it.createdAt }
  val updatedAt = date("updated_at").bindTo { it.updatedAt }
}

@Serializable
data class CategoryData(
  val id: Int,
  var parentId: Int?,
  var accountId: Int?,
  var name: String,
  var type: CategoryType,
  val createdAt: String?,
  var updatedAt: String?
)

@Serializable
data class CategoryInitData(
  val name: String,
  val type: String,
  val parent: Int? = null,
)

interface Category: Entity<Category> {
  companion object: Entity.Factory<Category>()

  val id: Int
  var parentId: Int?
  var accountId: Int?
  var name: String
  var type: CategoryType
  var createdAt: LocalDate
  var updatedAt: LocalDate?
}