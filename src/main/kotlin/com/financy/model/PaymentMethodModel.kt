package com.financy.model

import kotlinx.serialization.Serializable
import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.*
import java.time.LocalDate

val Database.Payments get() = this.sequenceOf(PaymentMethodSchema)

object PaymentMethodSchema: Table<PaymentMethod>("t_payments") {
  val id = int("id").primaryKey().bindTo { it.id }
  val accountId = int("account_id").references(AccountsSchema) { it.account }
  val name = varchar("name").bindTo { it.name }
  val description = varchar("description").bindTo { it.description }
  val remains = int("remains").bindTo { it.remains }
  val active = boolean("active").bindTo { it.active }
  val createdAt = date("created_at").bindTo { it.createdAt }
  val updatedAt = date("updated_at").bindTo { it.updatedAt }
}

@Serializable
data class PaymentMethodData(
  val id: Int,
  var account: AccountData,
  var name: String,
  var description: String,
  var remains: Int,
  val createdAt: String,
  var updatedAt: String?,
) {
  companion object {
    fun getSerializable(payment: PaymentMethod): PaymentMethodData {
      return PaymentMethodData(
        id = payment.id,
        account = AccountData.getSerializable(payment.account),
        name = payment.name,
        description = payment.description,
        remains = payment.remains,
        createdAt = payment.createdAt.toString(),
        updatedAt = payment.updatedAt?.toString(),
      )
    }
  }
}

@Serializable
data class PaymentMethodInitData(
  val id: Int? = null,
  val name: String,
  val description: String? = "",
  val remains: Int,
)

interface PaymentMethod: Entity<PaymentMethod> {
  companion object: Entity.Factory<PaymentMethod>()

  val id: Int
  var account: Account
  var name: String
  var description: String
  var remains: Int
  var active: Boolean
  var createdAt: LocalDate
  var updatedAt: LocalDate?
}