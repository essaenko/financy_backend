package com.financy.model

import com.financy.dbInstance
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.dsl.inList
import org.ktorm.entity.Entity
import org.ktorm.entity.filter
import org.ktorm.entity.sequenceOf
import org.ktorm.entity.toList
import org.ktorm.schema.*
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import java.time.LocalDateTime

val Database.Users get() = this.sequenceOf(UsersSchema)

object UsersSchema: Table<User>("t_users") {
  val id = int("id").primaryKey().bindTo { it.id }
  var name = varchar("name").bindTo { it.name }
  var email = varchar("email").bindTo { it.email }
  var password = varchar("password").bindTo { it.password }
  var accountId= int("account_id").references(AccountSchema) { it.account }
  var createdAt = datetime("created_at").bindTo { it.createdAt }
  var updatedAt = datetime("updated_at").bindTo { it.updatedAt }
}

@Serializable
data class SessionDescription(
  @Contextual val _id: Id<SessionDescription> = newId(),
  val userAgent: String = "",
  val createdAt: String = "",
  var updatedAt: String = "",
  val userId: Int = 0,
)

@Serializable
data class UpdatePasswordCredentials(
  val currentPassword: String,
  val newPassword: String,
)
@Serializable
data class ResetPasswordDescription(
  val email: String,
  val token: String,
  val createdAt: String,
)

@Serializable
data class ResetPasswordCredentials(
  val password: String? = null,
  val token: String? = null,
)

@Serializable
data class Email(
  val email: String? = null,
)

@Serializable
data class Credentials (
  val email: String? = null,
  val password: String? = null,
)

@Serializable
data class RegistrationCredentials(
  val email: String? = null,
  val name: String? = null,
  val password: String? = null,
)

@Serializable
data class UserData(
  val id: Int,
  var name: String,
  var email: String,
  var account: AccountData?,
  var createdAt: String,
  var updatedAt: String?
) {
  companion object {
    fun getSerializable(user: User, recursive: Boolean = true): UserData {
      return UserData(
        id = user.id,
        name = user.name,
        email = user.email ,
        account = if (user.account != null && recursive) AccountData.getSerializable(user.account!!, false) else null,
        createdAt = user.createdAt.toString(),
        updatedAt = user.updatedAt.toString(),
      )
    }
  }
}

interface User: Entity<User> {
  companion object: Entity.Factory<User>()

  val id: Int
  var name: String
  var email: String
  var password: String
  var account: Account?
  var createdAt: LocalDateTime
  var updatedAt: LocalDateTime?

  fun getPaymentAccounts(): List<PaymentAccount> {
    if (this.account == null) {
      return listOf()
    }

    return dbInstance?.PaymentAccounts?.filter { it.accountId eq this.account!!.id }?.toList() ?: listOf()
  }

  fun getPaymentMethods(): List<PaymentMethod> {
    val paymentAccounts = this.getPaymentAccounts()

    return dbInstance?.PaymentMethods?.filter { it.accountId.inList(paymentAccounts.map { it.id }) }?.toList() ?: listOf()
  }
}