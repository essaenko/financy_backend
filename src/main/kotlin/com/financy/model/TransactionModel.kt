package com.financy.model

import kotlinx.serialization.Serializable
import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.*
import java.time.LocalDate

val Database.Transactions get() = this.sequenceOf(TransactionsSchema);

enum class TransactionType {
  Income,
  Outcome
}

object TransactionsSchema: Table<Transaction>("t_transactions") {
  val id = int("id").primaryKey().bindTo { it.id }
  val type = enum<TransactionType>("type").bindTo { it.type }
  val cost = int("cost").bindTo { it.cost }
  val comment = varchar("comment").bindTo { it.comment }
  val accountId = int("account_id").bindTo { it.accountId }
  val userId = int("user_id").bindTo { it.userId }
  val categoryId = int("category_id").bindTo { it.categoryId }
  val from = int("from_id").bindTo { it.from }
  val to = int("to_id").bindTo { it.to }
  val createdAt = date("created_at").bindTo { it.createdAt }
  val updatedAt = date("updated_at").bindTo { it.updatedAt }
}

@Serializable
data class TransactionData(
  val id: Int,
  val accountId: Int,
  val userId: Int,
  val categoryId: Int,
  val type: TransactionType,
  val cost: Int,
  val from: Int,
  val to: Int? = null,
  var comment: String,
  val createdAt: String?,
  var updatedAt: String?
)

@Serializable
data class TransactionInitData (
  val type: String,
  val cost: Int,
  val comment: String,
  val from: Int,
  val to: Int? = null,
  val category: Int,
  val date: Long? = null,
)

interface Transaction: Entity<Transaction> {
  companion object: Entity.Factory<Transaction>()

  val id: Int
  var accountId: Int
  var userId: Int
  var categoryId: Int
  var type: TransactionType
  var cost: Int
  var comment: String
  var from: Int
  var to: Int?
  var createdAt: LocalDate
  var updatedAt: LocalDate?
}