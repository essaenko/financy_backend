package com.financy.controller

import com.financy.dbInstance
import com.financy.model.*
import com.financy.utils.DefaultInstanceInit
import com.financy.utils.Exceptions
import org.ktorm.dsl.eq
import org.ktorm.entity.add
import org.ktorm.entity.filter
import org.ktorm.entity.find
import org.ktorm.entity.toList
import java.time.LocalDate

object PaymentAccountController {
  fun getPaymentAccount(id: Int): PaymentAccount? {
    return dbInstance?.PaymentAccounts?.find { it.id eq id }
  }

  fun getList(acc: Account): List<PaymentAccount> {
    return dbInstance?.PaymentAccounts?.filter { it.accountId eq acc.id }?.toList() ?: listOf()
  }

  fun create(acc: Account, init: PaymentAccountInitData): PaymentAccount {
    val payment = PaymentAccount {
      account = acc
      name = init.name
      description = init.description ?: ""
      remains = init.remains
      active = true
      createdAt = LocalDate.now()
      updatedAt = null
    }

    dbInstance?.PaymentAccounts?.add(payment)

    return payment
  }

  fun update(acc: Account, init: PaymentAccountInitData): PaymentAccount {
    val id: Int = init.id ?: throw Error(Exceptions.UnresolvedPaymentAccountIdException.name)
    val payment: PaymentAccount = dbInstance?.PaymentAccounts?.find { it.id eq id } ?: throw Error(Exceptions.PaymentAccountNotFoundException.name)
    val name: String = init.name
    val description: String = init.description ?: ""

    if (payment.account.id != acc.id) {
      throw Error(Exceptions.NotPermittedOperation.name)
    }

    if (payment.name != name) {
      payment.name = name
    }
    if (payment.description != description) {
      payment.description = description
    }

    payment.flushChanges()

    return payment
  }

  fun remove(acc: Account, init: DefaultInstanceInit) {
    val id: Int = init.id
    val payment: PaymentAccount = dbInstance?.PaymentAccounts?.find { it.id eq id } ?: throw Error(Exceptions.PaymentAccountNotFoundException.name)

    if (payment.account.id != acc.id) {
      throw Error(Exceptions.NotPermittedOperation.name)
    }

    payment.active = false

    payment.flushChanges()
  }
}