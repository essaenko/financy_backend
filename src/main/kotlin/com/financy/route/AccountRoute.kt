package com.financy.route

import com.financy.controller.AccountController
import com.financy.controller.UserController
import com.financy.model.AccountData
import com.financy.model.User
import com.financy.model.UserData
import com.financy.utils.ApiResponse
import com.financy.utils.ApiResponseStatus
import com.financy.utils.Exceptions
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

fun Route.AccountControllerRoutes() {
  authenticate {
    get("/api/v1/account") {
      val principal = call.principal<JWTPrincipal>()
      val userId: Int? = principal!!.payload.getClaim("user_id").asInt()
      if (userId != null) {
        try {
          val user: User = UserController.getUser(userId)

          if (user.account == null) {
            throw Error(Exceptions.NoUserAccountException.name)
          }

          try {
            call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Ok, null, AccountData.getSerializable(user.account!!))))
          } catch (error: Error) {
            call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Error, error.localizedMessage, "")))
          }
        } catch(error: Error) {
          call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Error, error.localizedMessage, "")))
        }
      } else {
        call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Error, "InvalidTokenException", "")))
      }
    }
    post("/api/v1/account/create") {
      val principal = call.principal<JWTPrincipal>()
      val userId = principal!!.payload.getClaim("user_id").asInt()

      if (userId != null) {
        try {
          val user: User = UserController.getUser(userId)

          val account = AccountController.create(user)

          call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Ok, null, AccountData.getSerializable(account))))
        } catch (error: Error) {
          val log = LoggerFactory.getLogger("com.financy.app")
          log.error(error.localizedMessage)
          call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Error, error.localizedMessage, "")))
        }
      } else {
        call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Error, "InvalidTokenException", "")))
      }
    }

    get("/api/v1/account/users") {
      val principal = call.principal<JWTPrincipal>()
      val userId = principal!!.payload.getClaim("user_id").asInt()

      if (userId != null) {
        try {
          val user: User = UserController.getUser(userId)
          if (user.account == null) {
            throw Error(Exceptions.NoUserAccountException.name)
          }

          val accountUsers = AccountController.getUsers(user.account!!)

          call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Ok, null, accountUsers.map { UserData.getSerializable(it) })))
        } catch (error: Error) {
          val log = LoggerFactory.getLogger("com.financy.app")
          log.error(error.localizedMessage)
          call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Error, error.localizedMessage, "")))
        }
      } else {
        call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Error, "InvalidTokenException", "")))
      }
    }
  }
}