package com.financy.controller

import com.financy.dbInstance
import com.financy.model.*
import com.financy.utils.DefaultInstanceInit
import com.financy.utils.Exceptions
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.entity.*
import java.time.LocalDate
import java.time.LocalDateTime

object PaymentMethodController {
  fun create(user: User, init: PaymentMethodInitData): PaymentMethod {
    val acc = PaymentAccountController.getPaymentAccount(
      init.account
        ?: throw Error(Exceptions.PaymentAccountNotFoundException.name)
    ) ?: throw Error(Exceptions.PaymentAccountNotFoundException.name)
    val payment = PaymentMethod {
      account = acc
      owner = user
      name = init.name
      description = init.description ?: ""
      active = true
      createdAt = LocalDateTime.now()
      updatedAt = null
    }

    dbInstance?.PaymentMethods?.add(payment)

    return payment
  }

  fun remove(acc: Account, init: DefaultInstanceInit) {
    val id: Int = init.id
    val payment: PaymentMethod = dbInstance?.PaymentMethods?.find { it.id eq id } ?: throw Error(Exceptions.PaymentMethodNotFoundException.name)

    if (payment.account.account.id != acc.id) {
      throw Error(Exceptions.NotPermittedOperation.name)
    }

    payment.active = false

    payment.flushChanges()
  }

  fun update(acc: Account, init: PaymentMethodInitData): PaymentMethod {
    val id: Int = init.id ?: throw Error(Exceptions.UnresolvedPaymentMethodIdException.name)
    val payment: PaymentMethod = dbInstance?.PaymentMethods?.find { it.id eq id } ?: throw Error(Exceptions.PaymentMethodNotFoundException.name)
    val name: String = init.name
    val description: String = init.description ?: ""

    if (payment.account.account.id != acc.id) {
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

  fun getList(account: Account): List<PaymentMethod> {
    val paymentMethods = mutableListOf<PaymentMethod>()
    dbInstance?.PaymentAccounts?.filter { it.accountId eq account.id and it.active eq true }?.forEach { paymentAccount ->
      paymentMethods.addAll(
        dbInstance
          ?.PaymentMethods
          ?.filter {
            it.accountId eq paymentAccount.id and it.active eq true
          }?.toList()
          ?: listOf()
      )
    }

    return paymentMethods
  }

  fun processTransaction(paymentMethod: PaymentMethod, newRemains: Float, date: LocalDate) {
    paymentMethod.account.remains = newRemains
    var remainsInst = dbInstance?.Remains?.find { (it.payment eq paymentMethod.account.id) and (it.createdAt eq date.atStartOfDay().toLocalDate()) }

    if (remainsInst == null) {
      remainsInst = Remains {
        payment = paymentMethod.account.id
        createdAt = date.atStartOfDay().toLocalDate()
        remains = 0.0f
      }
      dbInstance?.Remains?.add(remainsInst)
    }

    remainsInst.remains = paymentMethod.account.remains

    paymentMethod.account.flushChanges()
    paymentMethod.flushChanges()
    remainsInst.flushChanges()
  }
}