package com.financy

import org.ktorm.database.Database
import org.ktorm.logging.ConsoleLogger
import org.ktorm.logging.LogLevel

var dbInstance: Database? = null

fun initDataBase() {
  println("Database init")

  dbInstance = Database.connect(
    "jdbc:postgresql://localhost:5432/financy_db",
    user = ApplicationConfig.database.user,
    password = ApplicationConfig.database.password,
    logger = ConsoleLogger(threshold = LogLevel.INFO)
  )
  println("Database successfully started")
}