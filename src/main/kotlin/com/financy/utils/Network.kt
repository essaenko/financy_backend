package com.financy.utils

import kotlinx.serialization.Serializable

enum class ApiResponseStatus {
  Ok,
  Error,
}

@Serializable
data class ApiResponse<T>(
  val status: ApiResponseStatus? = null,
  val error: String? = null,
  val payload: T? = null,
)