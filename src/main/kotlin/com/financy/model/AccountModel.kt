package com.financy.model

import kotlinx.serialization.Serializable
import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.Table
import org.ktorm.schema.date
import org.ktorm.schema.int
import java.time.LocalDate

val Database.Accounts get() = this.sequenceOf(AccountsSchema);

object AccountsSchema: Table<Account>("t_accounts") {
  val id = int("id").primaryKey().bindTo { it.id }
  val createdAt = date("created_at").bindTo { it.createdAt }
  val updatedAt = date("updated_at").bindTo { it.updatedAt }
}

@Serializable
data class AccountData(
  val id: Int,
  val createdAt: String?,
  var updatedAt: String?
)

interface Account: Entity<Account> {
  companion object: Entity.Factory<Account>()

  val id: Int
  var createdAt: LocalDate
  var updatedAt: LocalDate?
}