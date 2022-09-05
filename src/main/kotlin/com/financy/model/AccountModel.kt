package com.financy.model

import com.financy.dbInstance
import kotlinx.serialization.Serializable
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.entity.*
import org.ktorm.schema.Table
import org.ktorm.schema.date
import org.ktorm.schema.int
import java.time.LocalDate

val Database.Accounts get() = this.sequenceOf(AccountSchema)

object AccountSchema: Table<Account>("t_accounts") {
  val id = int("id").primaryKey().bindTo { it.id }
  val createdAt = date("created_at").bindTo { it.createdAt }
  val updatedAt = date("updated_at").bindTo { it.updatedAt }
  val ownerId = int("owner_id").bindTo { it.owner }
}

@Serializable
data class AccountData(
  val id: Int,
  var owner: UserData?,
  val createdAt: String,
  var updatedAt: String?
) {
  companion object {
    fun getSerializable(account: Account, recursive: Boolean? = true): AccountData {
      return AccountData(
        id = account.id,
        createdAt = account.createdAt.toString(),
        updatedAt = account.updatedAt?.toString(),
        owner = if (recursive == true) dbInstance?.Users?.find { it.id eq account.owner }?.let { UserData.getSerializable(it, false) } else null
      )
    }
  }
}

interface Account: Entity<Account> {
  companion object: Entity.Factory<Account>()

  val id: Int
  var createdAt: LocalDate
  var updatedAt: LocalDate?
  var owner: Int

  fun getCategories(): List<Category>? {
    return dbInstance?.Categories?.filter { it.accountId eq this.id }?.toList()
  }
}