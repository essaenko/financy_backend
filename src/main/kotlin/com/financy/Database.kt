package com.financy

import org.ktorm.database.Database
import org.ktorm.logging.ConsoleLogger
import org.ktorm.logging.LogLevel
import org.ktorm.schema.SqlType
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types

var dbInstance: Database? = null

fun initDataBase() {
  Logger.info("Database init")

  dbInstance = Database.connect(
    "jdbc:postgresql://localhost:5432/financy_db",
    user = ApplicationConfig.database.user,
    password = ApplicationConfig.database.password,
    logger = ConsoleLogger(threshold = LogLevel.INFO)
  )
  Logger.info("Database successfully started")
}