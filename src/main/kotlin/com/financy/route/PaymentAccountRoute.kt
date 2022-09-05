package com.financy.route

import com.financy.Session
import com.financy.controller.PaymentAccountController
import com.financy.controller.PaymentMethodController
import com.financy.controller.UserController
import com.financy.model.*
import com.financy.utils.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.logging.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun Route.PaymentAccountControllerRoutes() {
  authenticate {
    get("/api/v1/payment/account") {
      try {
        val session = Session?.getUserSession(call)
        if (session == null) {
          call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Error, "InvalidTokenException", "")))

          return@get
        }
        val user = UserController.getUser(session.userId, true)
        val paymentAccounts = PaymentAccountController.getList(user.account!!)

        call.respondText { Json.encodeToString(ApiResponse(ApiResponseStatus.Ok, null, paymentAccounts.map { PaymentAccountData.getSerializable(it) })) }
      } catch(error: Error) {
        com.financy.Logger.error(error)
        call.respondText { Json.encodeToString(ApiResponse(ApiResponseStatus.Error, error.localizedMessage, "")) }
      }
    }
    post("/api/v1/payment/account/remove") {
      try {
        val session = Session?.getUserSession(call)
        if (session == null) {
          call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Error, "InvalidTokenException", "")))

          return@post
        }
        val user = UserController.getUser(session.userId, true)
        val paymentInit = call.receive<DefaultInstanceInit>()
        

        PaymentAccountController.remove(user.account!!, paymentInit)

        call.respondText { Json.encodeToString(ApiResponse(ApiResponseStatus.Ok, null, "")) }
      } catch(error: Error) {
        com.financy.Logger.error(error.localizedMessage, error)
        call.respondText { Json.encodeToString(ApiResponse(ApiResponseStatus.Error, error.localizedMessage, "")) }
      }
    }
    post("/api/v1/payment/account/update") {
      try {
        val session = Session?.getUserSession(call)
        if (session == null) {
          call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Error, "InvalidTokenException", "")))

          return@post
        }
        val user = UserController.getUser(session.userId, true)
        val paymentInit = call.receive<PaymentAccountInitData>()
        val newPayment = PaymentAccountController.update(user.account!!, paymentInit)

        call.respondText { Json.encodeToString(ApiResponse(ApiResponseStatus.Ok, null, PaymentAccountData.getSerializable(newPayment))) }
      } catch(error: Error) {
        com.financy.Logger.error(error.localizedMessage, error)
        call.respondText { Json.encodeToString(ApiResponse(ApiResponseStatus.Error, error.localizedMessage, "")) }
      }
    }
    post("/api/v1/payment/account/create") {
      try {
        val session = Session?.getUserSession(call)
        if (session == null) {
          call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Error, "InvalidTokenException", "")))

          return@post
        }
        val user = UserController.getUser(session.userId, true)
        val paymentInit = call.receive<PaymentAccountInitData>()
        val newPayment = PaymentAccountController.create(user.account!!, paymentInit)

        call.respondText { Json.encodeToString(ApiResponse(ApiResponseStatus.Ok, null, PaymentAccountData.getSerializable(newPayment))) }
      } catch(error: Error) {
        com.financy.Logger.error(error.localizedMessage, error)
        call.respondText { Json.encodeToString(ApiResponse(ApiResponseStatus.Error, error.localizedMessage, "")) }
      }
    }
  }
}