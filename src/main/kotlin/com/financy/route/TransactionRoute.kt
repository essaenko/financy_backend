package com.financy.route

import com.financy.controller.AccountController
import com.financy.controller.TransactionController
import com.financy.controller.UserController
import com.financy.model.TransactionData
import com.financy.model.TransactionInitData
import com.financy.utils.ApiResponse
import com.financy.utils.ApiResponseStatus
import com.financy.utils.DefaultInstanceInit
import com.financy.utils.Exceptions
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
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

          val transaction = TransactionController.update(user.account!!, transactionInit);

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
          TransactionController.remove(user.account!!, transactionInit);

          call.respondText { Json.encodeToString(ApiResponse(ApiResponseStatus.Ok, null, "")) }
        } catch (error: Error) {
          call.respondText { Json.encodeToString(ApiResponse(ApiResponseStatus.Error, error.localizedMessage, "")) }
        }
      }
    }
    get("/api/v1/transaction") {
      val principal = call.principal<JWTPrincipal>()
      val userId = principal!!.payload.getClaim("user_id").asInt()

      if (userId == null) {
        call.respondText(Json.encodeToString(ApiResponse(ApiResponseStatus.Error, "InvalidTokenException", "")))
      } else {
        try {
          val user = UserController.getUser(userId)
          val page = call.request.queryParameters["p"]?.toInt() ?: 1;
          if (user.account == null) {
            throw Error(Exceptions.NoUserAccountException.name)
          }
          val transactions = TransactionController.getList(user.account!!, page)

          call.respondText { Json.encodeToString(ApiResponse(ApiResponseStatus.Ok, null, transactions.map { TransactionData.getSerializable(it) }
          ))}
        } catch (error: Error) {
          call.respondText { Json.encodeToString(ApiResponse(ApiResponseStatus.Error, error.localizedMessage, "")) }
        }
      }
    }
  }
}