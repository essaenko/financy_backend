package com.financy.model

import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import kotlin.math.roundToInt

data class StatisticBatch(
  val date: LocalDateTime,
  var value: Double,
  val key: Int?,
)

@Serializable
data class StatisticBatchData(
  val date: String,
  val value: Double,
  val key: Int?,
) {
  companion object {
    fun getSerializable (batch: StatisticBatch): StatisticBatchData {
      return StatisticBatchData(
        date = batch.date.toString(),
        value = (batch.value * 100.0).roundToInt() / 100.0,
        key = batch.key,
      )
    }
  }
}

@Serializable
data class StatisticData(
  val income: List<StatisticBatchData>?,
  val outcome: List<StatisticBatchData>?,
  val remains: List<StatisticBatchData>?,
  val structure: Map<Int, Double>?,
) {
  companion object {
    fun getSerializable(
      income: List<StatisticBatchData>?,
      outcome: List<StatisticBatchData>?,
      remains: List<StatisticBatchData>?,
      structure: Map<Int, Double>?,
    ): StatisticData {
      return StatisticData(income, outcome, remains, structure)
    }
  }
}