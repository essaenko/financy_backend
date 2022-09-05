package com.financy

import com.financy.utils.SessionService
import org.slf4j.LoggerFactory

var APP_ENV = "prod"
val Logger = LoggerFactory.getLogger("com.financy.app")
var Session: SessionService? = null

fun main(args: Array<String>) {
  if (args.isNotEmpty() && args[0] == "-process_env") {
    APP_ENV = args[1]
  }
  Logger.info("Creating web server with env: $APP_ENV")
  Session = SessionService()
  initDataBase()
  initWebServer()
}