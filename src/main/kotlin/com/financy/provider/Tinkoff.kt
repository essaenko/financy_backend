package com.financy.provider

import com.financy.controller.TransactionController
import com.financy.dbInstance
import com.financy.model.*
import com.financy.reader.Reader
import org.ktorm.entity.add
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.abs

val Logger: Logger = LoggerFactory.getLogger("TinkoffProvider")

class Tinkoff: Provider {
  override fun parseExport(usr: User, reader: Reader) {
    val payments = usr.getPaymentMethods()
    val accountCategories = usr.account?.getCategories()
    reader.setDeclarationRow(0)
    reader.eachLine(1) { row ->
      val columns = reader.parseColumns(row)

      if (columns.isNotEmpty()) {
        val paymentMethod = payments.find { it.name.contains(reader.getColumn(row, "Номер карты")) }
        val pCost = reader.getColumn(row, "Сумма операции").replace(",",".").toFloat()
//        val currency = reader.getColumn(row, "Валюта операции")
        val pType = if (pCost > 0) TransactionType.Income else TransactionType.Outcome
        val pComment = reader.getColumn(row, "Описание")
        val pCreatedAt = LocalDateTime.parse(
          reader.getColumn(row, "Дата операции"),
          DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")
        )
        Logger.info("Transaction MCC code: ${reader.getColumn(row, "MCC").toIntOrNull()}")
        var pCategory = accountCategories?.find {
          it.mcc?.contains("#${reader.getColumn(row, "MCC")}#")
            ?: false
        }

        if (pCategory == null) {
          pCategory = accountCategories?.find { it.name == "Другое" && it.type == pType }
        }
        if (pCategory == null) {
          pCategory = accountCategories?.find { it.type == pType }
        }
        if (pCategory == null) {
          Logger.info("Can't process transaction with comment: $pComment cause category is null")

          return@eachLine
        }
        if (paymentMethod == null) {
          Logger.info("Can't process transaction with comment: $pComment cause paymentMethod is null")

          return@eachLine
        }

        val transaction = Transaction {
          account = usr.account!!
          user = paymentMethod.owner
          type = pType
          comment = pComment
          from = paymentMethod
          cost = abs(pCost)
          category = pCategory
          createdAt = pCreatedAt
          updatedAt = LocalDateTime.now()
        }
        dbInstance?.Transactions?.add(transaction)

        TransactionController.processTransaction(transaction)
        Logger.info("Transaction successfully processed")
      }
    }
  }
}

val TinkoffProvider = Tinkoff()