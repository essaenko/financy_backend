package com.financy

object ApplicationConfig {
  object ktor {
    val secret = "FinancyAppServerSecret"
    val issuer = "http://localhost:8080"
  }
  object database {
    val user = "financy_owner"
    val password = "financy_test_password"
  }
}