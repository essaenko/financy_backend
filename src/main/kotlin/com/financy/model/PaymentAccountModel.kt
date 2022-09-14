package com.financy.model

import kotlinx.serialization.Serializable
import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.*
import java.time.LocalDateTime

val Database.PaymentAccounts get() = this.sequenceOf(PaymentAccountSchema)

object PaymentAccountSchema: Table<PaymentAccount>("t_payment_accounts") {
  val id = int("id").primaryKey().bindTo { it.id }
  val accountId = int("account_id").references(AccountSchema) { it.account }
  val name = varchar("name").bindTo { it.name }
  val description = varchar("description").bindTo { it.description }
  val remains = float("remains").bindTo { it.remains }
  val active = boolean("active").bindTo { it.active }
  val createdAt = datetime("created_at").bindTo { it.createdAt }
  val updatedAt = datetime("updated_at").bindTo { it.updatedAt }
}

@Serializable
data class PaymentAccountData(
  val id: Int,
  var account: AccountData,
  var name: String,
  var description: String,
  var remains: Float,
  val createdAt: String,
  var updatedAt: String?,
) {
  companion object {
    fun getSerializable(payment: PaymentAccount): PaymentAccountData {
      return PaymentAccountData(
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
data class PaymentAccountInitData(
  val id: Int? = null,
  val name: String,
  val description: String? = "",
  val remains: Float,
)

interface PaymentAccount: Entity<PaymentAccount> {
  companion object: Entity.Factory<PaymentAccount>()

  val id: Int
  var account: Account
  var name: String
  var description: String
  var remains: Float
  var active: Boolean
  var createdAt: LocalDateTime
  var updatedAt: LocalDateTime?
}