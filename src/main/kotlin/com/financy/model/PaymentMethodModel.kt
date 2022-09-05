package com.financy.model

import kotlinx.serialization.Serializable
import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.*
import java.time.LocalDate

val Database.PaymentMethods get() = this.sequenceOf(PaymentMethodSchema)

object PaymentMethodSchema: Table<PaymentMethod>("t_payments") {
  val id = int("id").primaryKey().bindTo { it.id }
  val accountId = int("account_id").references(PaymentAccountSchema) { it.account }
  val owner = int("owner_id").references(UsersSchema) { it.owner }
  val name = varchar("name").bindTo { it.name }
  val description = varchar("description").bindTo { it.description }
  val active = boolean("active").bindTo { it.active }
  val createdAt = date("created_at").bindTo { it.createdAt }
  val updatedAt = date("updated_at").bindTo { it.updatedAt }
}

@Serializable
data class PaymentMethodData(
  val id: Int,
  var account: PaymentAccountData,
  val owner: UserData,
  var name: String,
  var description: String,
  val createdAt: String,
  var updatedAt: String?,
) {
  companion object {
    fun getSerializable(payment: PaymentMethod): PaymentMethodData {
      return PaymentMethodData(
        id = payment.id,
        account = PaymentAccountData.getSerializable(payment.account),
        owner = UserData.getSerializable(payment.owner),
        name = payment.name,
        description = payment.description,
        createdAt = payment.createdAt.toString(),
        updatedAt = payment.updatedAt?.toString(),
      )
    }
  }
}

@Serializable
data class PaymentMethodInitData(
  val id: Int? = null,
  val account: Int? = null,
  val name: String,
  val description: String? = "",
)

interface PaymentMethod: Entity<PaymentMethod> {
  companion object: Entity.Factory<PaymentMethod>()

  val id: Int
  var account: PaymentAccount
  var owner: User
  var name: String
  var description: String
  var active: Boolean
  var createdAt: LocalDate
  var updatedAt: LocalDate?
}