package com.financy.controller

import com.financy.dbInstance
import com.financy.model.*
import com.financy.utils.DefaultInstanceInit
import com.financy.utils.Exceptions
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.entity.add
import org.ktorm.entity.filter
import org.ktorm.entity.find
import org.ktorm.entity.toList
import java.time.LocalDate

object PaymentMethodController {
  fun create(acc: Account, init: PaymentMethodInitData): PaymentMethod {
    val payment = PaymentMethod {
      account = acc
      name = init.name
      remains = init.remains
      description = init.description ?: ""
      active = true
      createdAt = LocalDate.now()
      updatedAt = null
    }

    dbInstance?.Payments?.add(payment)

    return payment
  }

  fun remove(acc: Account, init: DefaultInstanceInit) {
    val id: Int = init.id
    val payment: PaymentMethod = dbInstance?.Payments?.find { it.id eq id } ?: throw Error(Exceptions.PaymentMethodNotFoundException.name)

    if (payment.account.id != acc.id) {
      throw Error(Exceptions.UnauthorizedOperationException.name)
    }

    payment.active = false

    payment.flushChanges()
  }

  fun update(acc: Account, init: PaymentMethodInitData): PaymentMethod {
    val id: Int = init.id ?: throw Error(Exceptions.UnresolvedPaymentMethodIdException.name)
    val payment: PaymentMethod = dbInstance?.Payments?.find { it.id eq id } ?: throw Error(Exceptions.PaymentMethodNotFoundException.name)
    val name: String = init.name
    val description: String = init.description ?: ""
    val remains: Float = init.remains

    if (payment.account.id != acc.id) {
      throw Error(Exceptions.UnauthorizedOperationException.name)
    }

    if (payment.name != name) {
      payment.name = name
    }
    if (payment.remains != remains) {
      payment.remains = remains
    }
    if (payment.description != description) {
      payment.description = description
    }

    payment.flushChanges()

    return payment
  }

  fun getAll(account: Account): List<PaymentMethod> {
    return dbInstance?.Payments?.filter { it.accountId eq account.id and it.active eq true }?.toList() ?: listOf()
  }

  fun processTransaction(paymentMethod: PaymentMethod, newRemains: Float, date: LocalDate) {
    paymentMethod.remains = newRemains
    var remainsInst = dbInstance?.Remains?.find { (it.payment eq paymentMethod.id) and (it.createdAt eq date.atStartOfDay().toLocalDate()) }

    println("Payment remains: ${paymentMethod.remains}, new remains: $newRemains")
    if (remainsInst == null) {
      remainsInst = Remains {
        payment = paymentMethod.id
        createdAt = date.atStartOfDay().toLocalDate()
        remains = 0.0f
      }
      dbInstance?.Remains?.add(remainsInst)
    }

    remainsInst.remains = paymentMethod.remains

    paymentMethod.flushChanges()
    remainsInst.flushChanges()
  }
}