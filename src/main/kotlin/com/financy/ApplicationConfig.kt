package com.financy

object ApplicationConfig {
  object ktor {
    val resetPasswordSecret = "8iCc^ysz*^roDB"
    val secret = "L3%57QoaLjZT2x"
    val issuer = "https://financy.live"
  }
  object database {
    val user = "financy_owner"
    val password = "j*gN3Zqz\$fF9dX"
  }
  object fileSystem {
    val resourcesRoot =
      if (APP_ENV == "prod")
        "/applications/service/resources"
      else
        "/Users/essaenko/Documents/IdeaProjects/financy_backend/src/main/kotlin/com/financy/resources"
  }
}