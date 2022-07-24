package com.financy.route

import com.financy.Session
import com.financy.controller.TransactionController
import com.financy.controller.UserController
import com.financy.model.*
import com.financy.utils.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun Route.TransactionControllerRoutes() {
  authenticate {
    post("/api/v1/transaction/create") {
      try {
        val session = Session?.getUserSession(call)
        if (session == null) {
          call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Error, "InvalidTokenException", "")))

          return@post
        }
        val user = UserController.getUser(session.userId, true)
        val transactionInit = call.receive<TransactionInitData>()
        if (user == null) {
          call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Error, Exceptions.InvalidTokenException.name, "")))

          return@post
        }

        val transaction = TransactionController.create(user, user.account!!, transactionInit)

        call.respondText { Json.encodeToString(ApiResponse(ApiResponseStatus.Ok, null, TransactionData.getSerializable(transaction))) }
      } catch (error: Error) {
        com.financy.Logger.error(error.localizedMessage, error)
        call.respondText { Json.encodeToString(ApiResponse(ApiResponseStatus.Error, error.localizedMessage, "")) }
      }
    }
    post("/api/v1/transaction/update") {
      try {
        val session = Session?.getUserSession(call)
        if (session == null) {
          call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Error, "InvalidTokenException", "")))

          return@post
        }
        val user = UserController.getUser(session.userId, true)
        val transactionInit = call.receive<TransactionInitData>()
        if (user == null) {
          call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Error, Exceptions.InvalidTokenException.name, "")))

          return@post
        }

        val transaction = TransactionController.update(user.account!!, transactionInit)

        call.respondText { Json.encodeToString(ApiResponse(ApiResponseStatus.Ok, null, TransactionData.getSerializable(transaction))) }
      } catch (error: Error) {
        com.financy.Logger.error(error.localizedMessage, error)
        call.respondText { Json.encodeToString(ApiResponse(ApiResponseStatus.Error, error.localizedMessage, "")) }
      }
    }
    post("/api/v1/transaction/remove") {
      try {
        val session = Session?.getUserSession(call)
        if (session == null) {
          call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Error, "InvalidTokenException", "")))

          return@post
        }
        val user = UserController.getUser(session.userId, true)
        val transactionInit = call.receive<DefaultInstanceInit>()
        if (user == null) {
          call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Error, Exceptions.InvalidTokenException.name, "")))

          return@post
        }
        TransactionController.remove(user.account!!, transactionInit)

        call.respondText { Json.encodeToString(ApiResponse(ApiResponseStatus.Ok, null, "")) }
      } catch (error: Error) {
        com.financy.Logger.error(error.localizedMessage, error)
        call.respondText { Json.encodeToString(ApiResponse(ApiResponseStatus.Error, error.localizedMessage, "")) }
      }
    }
    get("/api/v1/transaction") {
      val metrics = RequestMetrics.initializeRequestMetrics()
      val requestTimer = metrics[0].startTimer()

      try {
        val userQueryTimer = metrics[1].startTimer()
        val session = Session?.getUserSession(call)
        if (session == null) {
          call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Error, "InvalidTokenException", "")))
          userQueryTimer.observeDuration()

          return@get
        }
        val user = UserController.getUser(session.userId, true)
        userQueryTimer.observeDuration()
        if (user == null) {
          call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Error, Exceptions.InvalidTokenException.name, "")))

          return@get
        }

        val query: Parameters = call.request.queryParameters
        val filters = TransactionFiltersInit.getValidInstance(
          type = query["type"],
          dateFrom = query["dateFrom"],
          dateTo = query["dateTo"],
          date = query["date"],
          category = query["category"],
          page = query["page"],
          perPage = query["perPage"],
        )
        val dbQueryTimer = metrics[2].startTimer()
        val response = TransactionController.getList(user.account!!, filters)
        dbQueryTimer.observeDuration()

        val responseEncodingTimer = metrics[3].startTimer()
        val responseText = Json.encodeToString(
          ApiResponse(
            ApiResponseStatus.Ok,
            null,
            response
          )
        )
        responseEncodingTimer.observeDuration()

        call.respondText { responseText }
        requestTimer.observeDuration()
      } catch (error: Error) {
        com.financy.Logger.error(error.localizedMessage, error)
        call.respondText { Json.encodeToString(ApiResponse(ApiResponseStatus.Error, error.localizedMessage, "")) }
      }
    }
    get("/api/v1/transaction/mock") {
      try {
        val session = Session?.getUserSession(call)
        if (session == null) {
          call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Error, "InvalidTokenException", "")))

          return@get
        }
        val user = UserController.getUser(session.userId, true)
        if (user == null) {
          call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Error, Exceptions.InvalidTokenException.name, "")))

          return@get
        }
        TransactionController.generateTestTransactions(user, user.account!!)

        call.respondText { Json.encodeToString(ApiResponse(ApiResponseStatus.Ok, null, "")) }
      } catch (error: Error) {
        com.financy.Logger.error(error.localizedMessage, error)
        call.respondText { Json.encodeToString(ApiResponse(ApiResponseStatus.Error, error.localizedMessage, "")) }
      }
    }
  }
}