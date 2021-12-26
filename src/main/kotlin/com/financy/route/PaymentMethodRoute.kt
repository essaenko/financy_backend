package com.financy.route

import com.financy.controller.AccountController
import com.financy.controller.PaymentMethodController
import com.financy.controller.UserController
import com.financy.model.PaymentMethodData
import com.financy.model.PaymentMethodInitData
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

fun Route.PaymentMethodControllerRoutes() {
  authenticate {
    get("/api/v1/payment") {
      val principal = call.principal<JWTPrincipal>()
      val userId = principal!!.payload.getClaim("user_id").asInt()

      if (userId == null) {
        call.respondText { Json.encodeToString(ApiResponse(ApiResponseStatus.Error, "InvalidTokenException",  "")) }
      }

      if (userId != null) {
        try {
          val user = UserController.getUser(userId);
          if (user.account == null) {
            throw Error(Exceptions.NoUserAccountException.name);
          }
          val paymentMethods = PaymentMethodController.getAll(user.account!!);

          call.respondText { Json.encodeToString(ApiResponse(ApiResponseStatus.Ok, null, paymentMethods.map { PaymentMethodData.getSerializable(it) })) }
        } catch(error: Error) {
          call.respondText { Json.encodeToString(ApiResponse(ApiResponseStatus.Error, error.localizedMessage, "")) }
        }
      }
    }
    post("/api/v1/payment/remove") {
      val principal = call.principal<JWTPrincipal>()
      val userId = principal!!.payload.getClaim("user_id").asInt()

      if (userId == null) {
        call.respondText { Json.encodeToString(ApiResponse(ApiResponseStatus.Error, "InvalidTokenException",  "")) }
      }

      if (userId != null) {
        try {
          val user = UserController.getUser(userId);
          val paymentInit = call.receive<DefaultInstanceInit>()

          if (user.account == null) {
            throw Error(Exceptions.NoUserAccountException.name);
          }

          PaymentMethodController.remove(user.account!!, paymentInit)

          call.respondText { Json.encodeToString(ApiResponse(ApiResponseStatus.Ok, null, "")) }
        } catch(error: Error) {
          call.respondText { Json.encodeToString(ApiResponse(ApiResponseStatus.Error, error.localizedMessage, "")) }
        }
      }
    }
    post("/api/v1/payment/update") {
      val principal = call.principal<JWTPrincipal>()
      val userId = principal!!.payload.getClaim("user_id").asInt()

      if (userId == null) {
        call.respondText { Json.encodeToString(ApiResponse(ApiResponseStatus.Error, "InvalidTokenException",  "")) }
      }

      if (userId != null) {
        try {
          val user = UserController.getUser(userId);
          val paymentInit = call.receive<PaymentMethodInitData>()

          if (user.account == null) {
            throw Error(Exceptions.NoUserAccountException.name);
          }
          val newPayment = PaymentMethodController.update(user.account!!, paymentInit)

          call.respondText { Json.encodeToString(ApiResponse(ApiResponseStatus.Ok, null, PaymentMethodData.getSerializable(newPayment))) }
        } catch(error: Error) {
          call.respondText { Json.encodeToString(ApiResponse(ApiResponseStatus.Error, error.localizedMessage, "")) }
        }
      }
    }
    post("/api/v1/payment/create") {
      val principal = call.principal<JWTPrincipal>()
      val userId = principal!!.payload.getClaim("user_id").asInt()

      if (userId == null) {
        call.respondText { Json.encodeToString(ApiResponse(ApiResponseStatus.Error, "InvalidTokenException",  "")) }
      }

      if (userId != null) {
        try {
          val user = UserController.getUser(userId);
          val paymentInit = call.receive<PaymentMethodInitData>()

          if (user.account == null) {
            throw Error(Exceptions.NoUserAccountException.name);
          }
          val newPayment = PaymentMethodController.create(user.account!!, paymentInit)

          call.respondText { Json.encodeToString(ApiResponse(ApiResponseStatus.Ok, null, PaymentMethodData.getSerializable(newPayment))) }
        } catch(error: Error) {
          call.respondText { Json.encodeToString(ApiResponse(ApiResponseStatus.Error, error.localizedMessage, "")) }
        }
      }
    }
  }
}
