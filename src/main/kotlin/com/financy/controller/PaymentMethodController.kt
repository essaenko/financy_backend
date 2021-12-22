package com.financy.controller

import com.financy.dbInstance
import com.financy.model.Account
import com.financy.model.PaymentMethod
import com.financy.model.PaymentMethodInitData
import com.financy.model.Payments
import org.ktorm.entity.add
import java.time.LocalDate

object PaymentMethodController {
  fun create(account: Account, init: PaymentMethodInitData): PaymentMethod {
    val payment = PaymentMethod {
      accountId = account.id
      name = init.name
      remains = init.remains
      description = init.description ?: ""
      createdAt = LocalDate.now()
      updatedAt = null
    }

    dbInstance?.Payments?.add(payment);

    return payment
  }
}