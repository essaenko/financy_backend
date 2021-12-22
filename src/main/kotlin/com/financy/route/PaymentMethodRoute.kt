package com.financy.route

import com.financy.controller.AccountController
import com.financy.controller.PaymentMethodController
import com.financy.controller.UserController
import com.financy.model.PaymentMethodData
import com.financy.model.PaymentMethodInitData
import com.financy.utils.ApiResponse
import com.financy.utils.ApiResponseStatus
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

          if (user.accountId == null) {
            throw Error(Exceptions.NoUserAccountException.name);
          }

          val account = AccountController.getAccount(user.accountId!!);
          val newPayment = PaymentMethodController.create(account, paymentInit)

          call.respondText { Json.encodeToString(ApiResponse(ApiResponseStatus.Ok, null, PaymentMethodData(
            id = newPayment.id,
            accountId = newPayment.accountId,
            name = newPayment.name,
            description = newPayment.description,
            remains = newPayment.remains,
            createdAt = newPayment.createdAt.toString(),
            updatedAt = newPayment.updatedAt.toString(),
          ))) }
        } catch(error: Error) {
          call.respondText { Json.encodeToString(ApiResponse(ApiResponseStatus.Error, error.localizedMessage, "")) }
        }
      }
    }
  }
}
