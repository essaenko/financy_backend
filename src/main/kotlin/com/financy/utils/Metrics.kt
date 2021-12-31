package com.financy.utils

import com.financy.collectorRegistry
import io.prometheus.client.Histogram

open class Metrics {
  fun buildHistogram(name: String, desc: String? = "empty"): Histogram {
    return Histogram.
      build().
      name(name).
      help(desc).
      buckets(0.001, 0.005, 0.01, 0.015, 0.02, 0.05, 0.1, 0.2, 0.5, 1.0).
      register(collectorRegistry)
  }
}

object TransactionMetrics: Metrics() {
  private var histograms = listOf<Histogram>();

  fun initializeListMetrics(): List<Histogram> {
    if (histograms.isEmpty()) {
      this.histograms = listOf(
        this.buildHistogram("default_query"),
        this.buildHistogram("type_filter_query"),
        this.buildHistogram("date_from_filter_query"),
        this.buildHistogram("date_to_filter_query"),
        this.buildHistogram("date_filter_query"),
        this.buildHistogram("category_filter_query"),
        this.buildHistogram("result_query"),
        this.buildHistogram("map_sequence_to_data")
      );
    }

    return this.histograms;
  }
}

object RequestMetrics: Metrics() {
  private var histograms = listOf<Histogram>()

  fun initializeRequestMetrics(): List<Histogram> {
    if (this.histograms.isEmpty()) {
      this.histograms = listOf(
        this.buildHistogram("request_time"),
        this.buildHistogram("user_query_time"),
        this.buildHistogram("db_query_time"),
        this.buildHistogram("encoding_response_time"),
      )
    }

    return this.histograms
  }
}