package com.financy.controller

import com.financy.dbInstance
import com.financy.model.*
import com.financy.utils.Exceptions
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.entity.add
import org.ktorm.entity.filter
import org.ktorm.entity.find
import org.ktorm.entity.toList
import java.time.LocalDateTime

object FamilyRequestController {
  fun getFamilyRequests(user: User, account: Account): List<FamilyRequest> {
    if (user.id == account.owner) {
      return dbInstance?.FamilyRequests?.filter { (it.account eq account.id) and (it.isActive eq true) }?.toList() ?: listOf()
    }

    throw Error(Exceptions.NotPermittedOperation.name)
  }

  fun getFamilyRequest(user: User): FamilyRequest? {
    return dbInstance?.FamilyRequests?.find { it.user eq user.id and it.isActive eq true }
  }

  fun createFamilyRequest(email: String, fUser: User): FamilyRequest {
    val accountOwner = dbInstance?.Users?.find { it.email eq email }

    if (accountOwner?.account != null) {
      val request = FamilyRequest {
        account = accountOwner.account!!.id
        user = fUser.id
        createdAt = LocalDateTime.now()
        updatedAt = LocalDateTime.now()
        isActive = true
      }
      dbInstance?.FamilyRequests?.add(request)

      request.flushChanges()

      return request
    }

    throw Error(Exceptions.UserNotFoundException.name)
  }

  fun rejectFamilyRequest(user: User, account: Account, request: Int): Boolean {
    if (user.id == account.owner) {
      val instance = dbInstance?.FamilyRequests?.find { it.id eq request }

      if (instance != null) {
        instance.isActive = false
        instance.flushChanges()

        return true
      }

      return false
    }

    throw Error(Exceptions.NotPermittedOperation.name)
  }

  fun acceptFamilyRequest(user: User, account: Account, request: Int): Boolean {
    if (user.id == account.owner) {
      val instance = dbInstance?.FamilyRequests?.find { it.id eq request }

      if (instance != null) {
        val newFamilyUser = dbInstance?.Users?.find { it.id eq instance.user }

        if (newFamilyUser != null) {
          newFamilyUser.account = account

          newFamilyUser.flushChanges()
          instance.delete()

          return true
        }

        return false
      }

      return false
    }

    throw Error(Exceptions.NotPermittedOperation.name)
  }
}