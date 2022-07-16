package com.financy.route

import com.financy.controller.FamilyRequestController
import com.financy.controller.UserController
import com.financy.model.*
import com.financy.utils.ApiResponse
import com.financy.utils.ApiResponseStatus
import com.financy.utils.Exceptions
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun Route.FamilyRequestRoutes() {
  authenticate {
    get("/api/v1/family/request") {
      val principal = call.principal<JWTPrincipal>()
      val userId: Int? = principal!!.payload.getClaim("user_id").asInt()
      if (userId != null) {
        try {
          val user: User = UserController.getUser(userId)
          val request: FamilyRequest? = FamilyRequestController.getFamilyRequest(user)

          try {
            if (request != null) {
              call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Ok, null, FamilyRequestData.getSerializable(request))))
            } else {
              call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Ok, null, "")))
            }
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
    get("/api/v1/family/requests") {
      val principal = call.principal<JWTPrincipal>()
      val userId: Int? = principal!!.payload.getClaim("user_id").asInt()
      if (userId != null) {
        try {
          val user: User = UserController.getUser(userId)

          if (user.account == null) {
            throw Error(Exceptions.NoUserAccountException.name)
          }

          try {
            val requests = FamilyRequestController.getFamilyRequests(user, user.account as Account)

            call.respondText(Json.encodeToString(ApiResponse(
              status = ApiResponseStatus.Ok,
              null,
              requests.map { FamilyRequestData.getSerializable(it) }
            )))
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
    post("/api/v1/family/request/create") {
      val principal = call.principal<JWTPrincipal>()
      val userId: Int? = principal!!.payload.getClaim("user_id").asInt()
      if (userId != null) {
        try {
          val user: User = UserController.getUser(userId)
          val request = call.receive<FamilyRequestCreateInitData>()

          try {
            val result = FamilyRequestController.createFamilyRequest(request.email, user)

            call.respondText(Json.encodeToString(ApiResponse(
              status = ApiResponseStatus.Ok,
              null,
              FamilyRequestData.getSerializable(result)
            )))
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
    post("/api/v1/family/request/approve") {
      val principal = call.principal<JWTPrincipal>()
      val userId: Int? = principal!!.payload.getClaim("user_id").asInt()
      if (userId != null) {
        try {
          val user: User = UserController.getUser(userId)

          if (user.account == null) {
            throw Error(Exceptions.NoUserAccountException.name)
          }
          val request = call.receive<FamilyRequestInitData>()

          try {
            val result = FamilyRequestController.acceptFamilyRequest(user, user.account as Account, request.id)

            call.respondText(Json.encodeToString(ApiResponse(
              status = ApiResponseStatus.Ok,
              null,
              result
            )))
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
    post("/api/v1/family/request/reject") {
      val principal = call.principal<JWTPrincipal>()
      val userId: Int? = principal!!.payload.getClaim("user_id").asInt()
      if (userId != null) {
        try {
          val user: User = UserController.getUser(userId)

          if (user.account == null) {
            throw Error(Exceptions.NoUserAccountException.name)
          }
          val request = call.receive<FamilyRequestInitData>()

          try {
            val result = FamilyRequestController.rejectFamilyRequest(user, user.account as Account, request.id)

            call.respondText(Json.encodeToString(ApiResponse(
              status = ApiResponseStatus.Ok,
              null,
              result
            )))
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
  }
}