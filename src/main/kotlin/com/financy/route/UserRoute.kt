package com.financy.route

import com.financy.Session
import com.financy.controller.UserController
import com.financy.model.*
import com.financy.utils.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


fun Route.UserControllerRoutes() {
  post("/api/v1/user/reset") {
    val payload = call.receive<ResetPasswordCredentials>()
    try {
      UserController.resetPassword(payload.password, payload.token)

      call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Ok, null, "")))
    } catch(error: Error) {
      com.financy.Logger.error("Error while sending email ${error.localizedMessage}", error)
      call.response.status(HttpStatusCode.Unauthorized)
      call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Error, error.localizedMessage, "")))
    }
  }
  post("/api/v1/user/restore") {
    val payload = call.receive<Email>()
    try {
      UserController.createResetPasswordRequest(payload.email)

      call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Ok, null, "")))
    } catch(error: Error) {
      com.financy.Logger.error("Error while sending email ${error.localizedMessage}", error)
      call.response.status(HttpStatusCode.Unauthorized)
      call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Error, error.localizedMessage, "")))
    }
  }
  post("/api/v1/user/login") {
    val payload = call.receive<Credentials>()
    try {
      val token = UserController.login(payload, call.request.userAgent() ?: "Unresolved user-agent")

      call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Ok, null, token)))
    } catch(error: Error) {
      com.financy.Logger.error(error.localizedMessage, error)
      call.response.status(HttpStatusCode.Unauthorized)
      call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Error, error.localizedMessage, "")))
    }
  }

  post("/api/v1/user/create") {
    val payload = call.receive<RegistrationCredentials>()
    try {
      UserController.create(payload)

      call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Ok, null, "")))
    } catch(error: Error) {
      com.financy.Logger.error(error.localizedMessage, error)
      call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Error, error.localizedMessage, "")))
    }
  }

  authenticate {
    post("/api/v1/user/change") {
      try {
        val session = Session?.getUserSession(call)
        if (session == null) {
          call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Error, "InvalidTokenException", "")))

          return@post
        }
        val user = UserController.getUser(session.userId)
        val credentials = call.receive<UpdatePasswordCredentials>()
        UserController.updatePassword(user, credentials.currentPassword, credentials.newPassword)

        call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Ok, null, "")))
      } catch(error: Error) {
        com.financy.Logger.error(error.localizedMessage, error)
        call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Error, error.localizedMessage, "")))
      }
    }
    get("/api/v1/user") {
      try {
        val session = Session?.getUserSession(call)
        if (session == null) {
          call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Error, "InvalidTokenException", "")))

          return@get
        }
        val user = UserController.getUser(session.userId)

        call.respondText(
          Json.encodeToString(
            ApiResponse(status = ApiResponseStatus.Ok, null, UserData.getSerializable(user))
          ))
      } catch(error: Error) {
        com.financy.Logger.error(error.localizedMessage, error)
        call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Error, error.localizedMessage, "")))
      }
    }
  }
}