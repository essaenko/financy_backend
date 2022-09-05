package com.financy.route

import com.financy.Session
import com.financy.controller.AccountController
import com.financy.controller.UserController
import com.financy.model.AccountData
import com.financy.model.Email
import com.financy.model.User
import com.financy.model.UserData
import com.financy.utils.ApiResponse
import com.financy.utils.ApiResponseStatus
import com.financy.utils.Exceptions
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun Route.AccountControllerRoutes() {
  authenticate {
    get("/api/v1/account") {
      try {
        val session = Session?.getUserSession(call)
        if (session == null) {
          call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Error, Exceptions.InvalidTokenException.name, "")))

          return@get
        }
        val user: User? = UserController.getUser(session.userId, true)

        if (user == null) {
          call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Error, Exceptions.InvalidTokenException.name, "")))

          return@get
        }

        try {
          call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Ok, null, AccountData.getSerializable(user.account!!))))
        } catch (error: Error) {
          call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Error, error.localizedMessage, "")))
        }
      } catch(error: Error) {

        com.financy.Logger.error(error.localizedMessage, error)
        call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Error, error.localizedMessage, "")))
      }
    }
    post("/api/v1/account/create") {
      try {
        val session = Session?.getUserSession(call)
        if (session == null) {
          call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Error, "InvalidTokenException", "")))

          return@post
        }

        val user: User = UserController.getUser(session.userId)
        val account = AccountController.create(user)

        call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Ok, null, AccountData.getSerializable(account))))
      } catch (error: Error) {
        com.financy.Logger.error(error.localizedMessage, error)
        call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Error, error.localizedMessage, "")))
      }
    }

    get("/api/v1/account/users") {
      try {
        val session = Session?.getUserSession(call)
        if (session == null) {
          call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Error, "InvalidTokenException", "")))

          return@get
        }
        val user: User? = UserController.getUser(session.userId, true)

        if (user == null) {
          call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Error, Exceptions.InvalidTokenException.name, "")))

          return@get
        }

        val accountUsers = AccountController.getUsers(user.account!!)

        call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Ok, null, accountUsers.map { UserData.getSerializable(it) })))
      } catch (error: Error) {
        com.financy.Logger.error(error.localizedMessage, error)
        call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Error, error.localizedMessage, "")))
      }
    }
    post("/api/v1/account/users/remove") {
      try {
        val session = Session?.getUserSession(call)
        if (session == null) {
          call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Error, "InvalidTokenException", "")))

          return@post
        }
        val user: User = UserController.getUser(session.userId, true)
        val email = call.receive<Email>()
        if (email.email == null) {
          call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Error, Exceptions.BadRequestException.name, "")))

          return@post
        }
        AccountController.removeUserFromAccount(user, email.email)

        call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Ok, null, "")))
      } catch (error: Error) {
        com.financy.Logger.error(error.localizedMessage, error)
        call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Error, error.localizedMessage, "")))
      }
    }
  }
}