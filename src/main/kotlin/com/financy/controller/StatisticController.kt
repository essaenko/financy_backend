package com.financy.controller

import com.financy.dbInstance
import com.financy.model.*
import com.financy.utils.StatisticParametersInit
import org.ktorm.dsl.*
import org.ktorm.entity.count
import org.ktorm.entity.filter
import org.ktorm.entity.filterColumns
import org.ktorm.entity.toList
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import kotlin.math.ceil
import kotlin.math.roundToInt

object StatisticController {
  fun get(params: StatisticParametersInit, account: Account): StatisticData {
    val dateFrom = Instant.ofEpochMilli(params.dateFrom).atZone(ZoneId.systemDefault()).toLocalDateTime()
    val dateTo = Instant.ofEpochMilli(params.dateTo).atZone(ZoneId.systemDefault()).toLocalDateTime()

    val transactionsQuery = dbInstance?.Transactions?.filter {
      var query = (it.createdAt greaterEq dateFrom) and (it.createdAt lessEq dateTo) and (it.accountId eq account.id)

      if (params.category != null) {
        query = query and (it.categoryId eq params.category)
      }
      if (params.type != null) {
        query = query and (it.type eq params.type)
      }
      if (params.user != null) {
        query = query and (it.userId eq params.user)
      }

      query
    }


    val remainsQuery = dbInstance?.Remains?.filter { remains ->
      var query = (remains.createdAt greaterEq dateFrom.toLocalDate()) and (remains.createdAt lessEq dateTo.toLocalDate())

      val payments = dbInstance?.PaymentAccounts?.filter { it.accountId eq account.id }?.filterColumns { listOf(it.id) }?.query

      if (payments != null) {
        query = query and (remains.payment.inList(payments))
      }

      query
    }

    val transactions = transactionsQuery?.toList() ?: listOf()

    if (transactions.isEmpty()) {
      return StatisticData(null, null, null, null)
    }

    val incomeTransactions = transactions.filter { it.type == TransactionType.Income }
    val outcomeTransactions = transactions.filter { it.type == TransactionType.Outcome }

    val incomeData: MutableList<StatisticBatch> = mutableListOf()
    val outcomeData: MutableList<StatisticBatch> = mutableListOf()
    val remainsData: MutableList<StatisticBatch> = mutableListOf()

    if (params.type == null || params.type == TransactionType.Income) {
      this.mapBatchData(incomeData, incomeTransactions, dateFrom, dateTo)
    }
    if (params.type == null || params.type == TransactionType.Outcome) {
      this.mapBatchData(outcomeData, outcomeTransactions, dateFrom, dateTo)
    }

    if (remainsQuery != null && remainsQuery.count() > 0) {
      this.mapRemainsData(remainsData, remainsQuery.toList())
    }

    val transactionsStructure = mutableMapOf<Int, Double>()

    this.mapStructureData(transactionsStructure, transactions)

    return StatisticData(
      incomeData.map { StatisticBatchData.getSerializable(it) },
      outcomeData.map { StatisticBatchData.getSerializable(it) },
      remainsData.map { StatisticBatchData.getSerializable(it) },
      transactionsStructure,
    )
  }

  private fun mapStructureData(map: MutableMap<Int, Double>, transactions: List<Transaction>) {
    transactions.forEach {
      map[it.category.id] = ((map.getOrDefault(it.category.id, 0.0) + it.cost) * 100.0).roundToInt() / 100.0
    }
  }

  private fun mapRemainsData(
    map: MutableList<StatisticBatch>,
    remains: List<Remains>
  ) {
    remains.forEach {
      map.add(
        StatisticBatch(
          date= it.createdAt.atStartOfDay(),
          value = it.remains.toDouble(),
          key = it.payment
        )
      )
    }
  }

  private fun mapBatchData(
    list: MutableList<StatisticBatch>,
    transactions: List<Transaction>,
    dateFrom: LocalDateTime,
    dateTo: LocalDateTime
  ) {
    val dateDiff: Int = (
            (dateTo.toEpochSecond(ZoneOffset.UTC) - dateFrom.toEpochSecond(ZoneOffset.UTC)) /
                    60 / // Minutes
                    60 / // Hours
                    24   // Days
            ).toInt()
    if (dateDiff < 32) {
      for(i in 0..dateDiff) {
        list.add(
          StatisticBatch(
            date = dateFrom.plusDays(i.toLong()),
            value = 0.0,
            key = null,
          )
        )
      }
    } else if (dateDiff < 182){
      for(i in 0..ceil(dateDiff.toFloat() / 7).toInt()) {
        list.add(
          StatisticBatch(
            date = dateFrom.plusWeeks(i.toLong()),
            value = 0.0,
            key = null
          )
        )
      }
    } else {
      for(i in 0..ceil(dateDiff.toFloat() / 30).toInt()) {
        list.add(
          StatisticBatch(
            date = dateFrom.plusMonths(i.toLong()),
            value = 0.0,
            key = null,
          )
        )
      }
    }

    transactions.forEach { transaction ->
      val batch: StatisticBatch? = if (dateDiff < 32) {
        list.find { it.date.dayOfYear == transaction.createdAt.dayOfYear }
      } else if (dateDiff < 182) {
        val tDate: Int = transaction.createdAt.dayOfYear
        val batchDate = dateFrom.plusWeeks(((tDate - dateFrom.dayOfYear) % 7).toLong())
        list.find { it.date.dayOfYear == batchDate.dayOfYear }
      } else {
        val tDate: Int = transaction.createdAt.dayOfYear
        val batchDate = dateFrom.plusMonths(((tDate - dateFrom.dayOfYear) % 30).toLong())
        list.find { it.date.dayOfYear == batchDate.dayOfYear }
      }

      batch?.value = batch?.value?.plus(transaction.cost) ?: 0.0
    }
  }
}