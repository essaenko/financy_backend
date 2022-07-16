package com.financy.route

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
      val principal = call.principal<JWTPrincipal>()
      val userId = principal!!.payload.getClaim("user_id").asInt()

      if (userId == null) {
        call.respondText(Json.encodeToString(ApiResponse(ApiResponseStatus.Error, "InvalidTokenException", "")))
      } else {
        try {
          val user = UserController.getUser(userId)
          if (user.account == null) {
            throw Error(Exceptions.NoUserAccountException.name)
          }
          val transactionInit = call.receive<TransactionInitData>()

          val transaction = TransactionController.create(user, user.account!!, transactionInit)

          call.respondText { Json.encodeToString(ApiResponse(ApiResponseStatus.Ok, null, TransactionData.getSerializable(transaction))) }
        } catch (error: Error) {
          call.respondText { Json.encodeToString(ApiResponse(ApiResponseStatus.Error, error.localizedMessage, "")) }
        }
      }
    }
    post("/api/v1/transaction/update") {
      val principal = call.principal<JWTPrincipal>()
      val userId = principal!!.payload.getClaim("user_id").asInt()

      if (userId == null) {
        call.respondText(Json.encodeToString(ApiResponse(ApiResponseStatus.Error, "InvalidTokenException", "")))
      } else {
        try {
          val user = UserController.getUser(userId)
          if (user.account == null) {
            throw Error(Exceptions.NoUserAccountException.name)
          }
          val transactionInit = call.receive<TransactionInitData>()

          val transaction = TransactionController.update(user.account!!, transactionInit)

          call.respondText { Json.encodeToString(ApiResponse(ApiResponseStatus.Ok, null, TransactionData.getSerializable(transaction))) }
        } catch (error: Error) {
          call.respondText { Json.encodeToString(ApiResponse(ApiResponseStatus.Error, error.localizedMessage, "")) }
        }
      }
    }
    post("/api/v1/transaction/remove") {
      val principal = call.principal<JWTPrincipal>()
      val userId = principal!!.payload.getClaim("user_id").asInt()

      if (userId == null) {
        call.respondText(Json.encodeToString(ApiResponse(ApiResponseStatus.Error, "InvalidTokenException", "")))
      } else {
        try {
          val user = UserController.getUser(userId)
          if (user.account == null) {
            throw Error(Exceptions.NoUserAccountException.name)
          }
          val transactionInit = call.receive<DefaultInstanceInit>()
          TransactionController.remove(user.account!!, transactionInit)

          call.respondText { Json.encodeToString(ApiResponse(ApiResponseStatus.Ok, null, "")) }
        } catch (error: Error) {
          call.respondText { Json.encodeToString(ApiResponse(ApiResponseStatus.Error, error.localizedMessage, "")) }
        }
      }
    }
    get("/api/v1/transaction") {
      val metrics = RequestMetrics.initializeRequestMetrics()
      val requestTimer = metrics[0].startTimer()
      val principal = call.principal<JWTPrincipal>()
      val userId = principal!!.payload.getClaim("user_id").asInt()

      if (userId == null) {
        call.respondText(Json.encodeToString(ApiResponse(ApiResponseStatus.Error, "InvalidTokenException", "")))
      } else {
        try {
          val userQueryTimer = metrics[1].startTimer()
          val user = UserController.getUser(userId)
          userQueryTimer.observeDuration()

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

          if (user.account == null) {
            throw Error(Exceptions.NoUserAccountException.name)
          }
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
          call.respondText { Json.encodeToString(ApiResponse(ApiResponseStatus.Error, error.localizedMessage, "")) }
        }
      }
    }
    get("/api/v1/transaction/mock") {
      val principal = call.principal<JWTPrincipal>()
      val userId = principal!!.payload.getClaim("user_id").asInt()

      if (userId == null || userId != 7) {
        call.respondText(Json.encodeToString(ApiResponse(ApiResponseStatus.Error, "InvalidTokenException", "")))
      } else {
        try {
          val user = UserController.getUser(userId)
          if (user.account == null) {
            throw Error(Exceptions.NoUserAccountException.name)
          }
          TransactionController.generateTestTransactions(user, user.account!!)

          call.respondText { Json.encodeToString(ApiResponse(ApiResponseStatus.Ok, null, "")) }
        } catch (error: Error) {
          call.respondText { Json.encodeToString(ApiResponse(ApiResponseStatus.Error, error.localizedMessage, "")) }
        }
      }
    }
  }
}