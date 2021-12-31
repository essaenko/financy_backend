package com.financy.route

import com.financy.controller.UserController
import com.financy.model.Credentials
import com.financy.model.RegistrationCredentials
import com.financy.model.UserData
import com.financy.utils.ApiResponse
import com.financy.utils.ApiResponseStatus
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


fun Route.UserControllerRoutes() {
  post("/api/v1/user/login") {
    val payload = call.receive<Credentials>()
    try {
      val token = UserController.login(payload)

      call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Ok, null, token)))
    } catch(error: Error) {
      println("Error while sending token ${error.localizedMessage}");
      call.response.status(HttpStatusCode.Unauthorized);
      call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Error, error.localizedMessage, "")))
    }
  }

  post("/api/v1/user/create") {
    val payload = call.receive<RegistrationCredentials>()
    try {
      UserController.create(payload)

      call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Ok, null, "")))
    } catch(error: Error) {
      println("Error while registering user ${error.localizedMessage}")
      call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Error, error.localizedMessage, "")))
    }
  }

  authenticate {
    get("/api/v1/user") {
      val principal = call.principal<JWTPrincipal>()
      val email = principal!!.payload.getClaim("email").asString()

      try {
        val user = UserController.getUser(email)

        call.respondText(
          Json.encodeToString(
            ApiResponse(status = ApiResponseStatus.Ok, null, UserData.getSerializable(user))
          ));
      } catch(error: Error) {
        println("Error while fetching user ${error.localizedMessage}")
        call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Error, error.localizedMessage, "")))
      }
    }
  }
}