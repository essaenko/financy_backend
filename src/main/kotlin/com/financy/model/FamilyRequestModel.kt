package com.financy.model

import com.financy.dbInstance
import com.financy.utils.Exceptions
import kotlinx.serialization.Serializable
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.entity.Entity
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.*
import java.time.LocalDateTime

val Database.FamilyRequests get() = this.sequenceOf(FamilyRequestsSchema)

object FamilyRequestsSchema: Table<FamilyRequest>("t_requests") {
  val id = int("id").primaryKey().bindTo { it.id }
  val createdAt = datetime("created_at").bindTo { it.createdAt }
  val updatedAt = datetime("updated_at").bindTo { it.updatedAt }
  val user = int("user_id").bindTo { it.user }
  val account = int("account_id").bindTo { it.account }
  val isActive = boolean("is_active").bindTo { it.isActive }
}

@Serializable
data class FamilyRequestInitData(
  val id: Int
)

@Serializable
data class FamilyRequestCreateInitData(
  val email: String
)

@Serializable
data class FamilyRequestData(
  val id: Int,
  var createdAt: String,
  var updatedAt: String?,
  var user: String,
  var account: Int,
  val owner: String,
  var isActive: Boolean
) {
  companion object {
    fun getSerializable(request: FamilyRequest): FamilyRequestData {
      val accountOwnerId = dbInstance?.Accounts?.find { it.id eq request.account }?.owner ?: throw Error(Exceptions.AccountNotFoundException.name)
      val accOwner = dbInstance?.Users?.find { it.id eq accountOwnerId } ?: throw Error(Exceptions.UserNotFoundException.name)
      val user = dbInstance?.Users?.find { it.id eq request.user } ?: throw Error(Exceptions.UserNotFoundException.name)
      return FamilyRequestData(
        id = request.id,
        createdAt = request.createdAt.toString(),
        updatedAt = request.updatedAt?.toString(),
        user = user.email,
        account = request.account,
        owner = accOwner.email,
        isActive = request.isActive,
      )
    }
  }
}

interface FamilyRequest: Entity<FamilyRequest> {
  companion object: Entity.Factory<FamilyRequest>()

  val id: Int
  var createdAt: LocalDateTime
  var updatedAt: LocalDateTime?
  var user: Int
  var account: Int
  var isActive: Boolean
}