package com.financy.model

import kotlinx.serialization.Serializable
import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.*
import java.time.LocalDateTime

val Database.Transactions get() = this.sequenceOf(TransactionsSchema)

enum class TransactionType {
  Income,
  Outcome
}

object TransactionsSchema: Table<Transaction>("t_transactions") {
  val id = int("id").primaryKey().bindTo { it.id }
  val type = enum<TransactionType>("type").bindTo { it.type }
  val cost = float("cost").bindTo { it.cost }
  val comment = varchar("comment").bindTo { it.comment }
  val accountId = int("account_id").references(AccountSchema) { it.account }
  val userId = int("user_id").references(UsersSchema) { it.user }
  val categoryId = int("category_id").references(CategoriesSchema) { it.category }
  val from = int("from_id").references(PaymentMethodSchema) { it.from }
  val to = int("to_id").references(PaymentMethodSchema) { it.to }
  val createdAt = datetime("created_at").bindTo { it.createdAt }
  val updatedAt = datetime("updated_at").bindTo { it.updatedAt }
}

@Serializable
data class TransactionData(
  val id: Int,
  val account: AccountData,
  val user: UserData,
  val category: CategoryData,
  val type: TransactionType,
  val cost: Float,
  val from: PaymentMethodData,
  val to: PaymentMethodData?,
  val date: String,
  var comment: String,
  val createdAt: String,
  var updatedAt: String?,
) {
  companion object {
    fun getSerializable(transaction: Transaction): TransactionData {
      return TransactionData(
        id = transaction.id,
        account = AccountData.getSerializable(transaction.account),
        user = UserData.getSerializable(transaction.user),
        category = CategoryData.getSerializable(transaction.category),
        type = transaction.type,
        cost = transaction.cost,
        from = PaymentMethodData.getSerializable(transaction.from),
        to = if (transaction.to != null) PaymentMethodData.getSerializable(transaction.to!!) else null,
        comment = transaction.comment,
        date = transaction.createdAt.toString(),
        createdAt = transaction.createdAt.toString(),
        updatedAt = transaction.updatedAt.toString(),
      )
    }
  }
}

@Serializable
data class TransactionInitData (
  val id: Int? = null,
  val to: Int? = null,
  val type: String,
  val category: Int,
  val cost: Float,
  val comment: String = "",
  val from: Int,
  val date: Long? = null,
)

interface Transaction: Entity<Transaction> {
  companion object: Entity.Factory<Transaction>()

  val id: Int
  var account: Account
  var user: User
  var category: Category
  var type: TransactionType
  var cost: Float
  var comment: String
  var from: PaymentMethod
  var to: PaymentMethod?
  var createdAt: LocalDateTime
  var updatedAt: LocalDateTime?
}