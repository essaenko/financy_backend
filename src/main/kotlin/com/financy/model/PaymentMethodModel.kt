package com.financy.model

import kotlinx.serialization.Serializable
import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.Table
import org.ktorm.schema.date
import org.ktorm.schema.int
import org.ktorm.schema.varchar
import java.time.LocalDate

val Database.Payments get() = this.sequenceOf(PaymentMethodSchema);

object PaymentMethodSchema: Table<PaymentMethod>("t_payments") {
  val id = int("id").primaryKey().bindTo { it.id }
  val accountId = int("account_id").bindTo { it.accountId }
  val name = varchar("name").bindTo { it.name }
  val description = varchar("description").bindTo { it.description }
  val remains = int("remains").bindTo { it.remains }
  val createdAt = date("created_at").bindTo { it.createdAt }
  val updatedAt = date("updated_at").bindTo { it.updatedAt }
}

@Serializable
data class PaymentMethodData(
  val id: Int,
  var accountId: Int,
  var name: String,
  var description: String,
  var remains: Int,
  val createdAt: String?,
  var updatedAt: String?
)

@Serializable
data class PaymentMethodInitData(
  val name: String,
  val description: String? = "",
  val remains: Int,
)

interface PaymentMethod: Entity<PaymentMethod> {
  companion object: Entity.Factory<PaymentMethod>()

  val id: Int
  var accountId: Int
  var name: String
  var description: String
  var remains: Int
  var createdAt: LocalDate
  var updatedAt: LocalDate?
}