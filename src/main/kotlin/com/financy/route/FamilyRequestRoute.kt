package com.financy.route

import com.financy.Session
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
      try {
        val session = Session?.getUserSession(call)
        if (session == null) {
          call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Error, "InvalidTokenException", "")))

          return@get
        }
        val user: User? = UserController.getUser(session.userId)
        if (user == null) {
          call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Error, Exceptions.InvalidTokenException.name, "")))

          return@get
        }
        val request: FamilyRequest? = FamilyRequestController.getFamilyRequest(user)

        try {
          if (request != null) {
            call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Ok, null, FamilyRequestData.getSerializable(request))))
          } else {
            call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Ok, null, "")))
          }
        } catch (error: Error) {
          com.financy.Logger.error(error.localizedMessage, error)
          call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Error, error.localizedMessage, "")))
        }
      } catch(error: Error) {
        com.financy.Logger.error(error.localizedMessage, error)
        call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Error, error.localizedMessage, "")))
      }
    }
    get("/api/v1/family/requests") {
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

        try {
          val requests = FamilyRequestController.getFamilyRequests(user, user.account as Account)

          call.respondText(Json.encodeToString(ApiResponse(
            status = ApiResponseStatus.Ok,
            null,
            requests.map { FamilyRequestData.getSerializable(it) }
          )))
        } catch (error: Error) {
          com.financy.Logger.error(error.localizedMessage, error)
          call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Error, error.localizedMessage, "")))
        }
      } catch(error: Error) {
        com.financy.Logger.error(error.localizedMessage, error)
        call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Error, error.localizedMessage, "")))
      }
    }
    post("/api/v1/family/request/create") {
      try {
        val session = Session?.getUserSession(call)
        if (session == null) {
          call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Error, "InvalidTokenException", "")))

          return@post
        }
        val user: User = UserController.getUser(session.userId)
        val request = call.receive<FamilyRequestCreateInitData>()
        try {
          val result = FamilyRequestController.createFamilyRequest(request.email, user)

          call.respondText(Json.encodeToString(ApiResponse(
            status = ApiResponseStatus.Ok,
            null,
            FamilyRequestData.getSerializable(result)
          )))
        } catch (error: Error) {
          com.financy.Logger.error(error.localizedMessage, error)
          call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Error, error.localizedMessage, "")))
        }
      } catch(error: Error) {
        com.financy.Logger.error(error.localizedMessage, error)
        call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Error, error.localizedMessage, "")))
      }
    }
    post("/api/v1/family/request/approve") {
      try {
        val session = Session?.getUserSession(call)
        if (session == null) {
          call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Error, "InvalidTokenException", "")))

          return@post
        }
        val user: User = UserController.getUser(session.userId, true)
        val request = call.receive<FamilyRequestInitData>()
        try {
          val result = FamilyRequestController.acceptFamilyRequest(user, user.account as Account, request.id)

          call.respondText(Json.encodeToString(ApiResponse(
            status = ApiResponseStatus.Ok,
            null,
            result
          )))
        } catch (error: Error) {
          com.financy.Logger.error(error.localizedMessage, error)
          call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Error, error.localizedMessage, "")))
        }
      } catch(error: Error) {
        com.financy.Logger.error(error.localizedMessage, error)
        call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Error, error.localizedMessage, "")))
      }
    }
    post("/api/v1/family/request/reject") {
      try {
        val session = Session?.getUserSession(call)
        if (session == null) {
          call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Error, "InvalidTokenException", "")))

          return@post
        }
        val user: User = UserController.getUser(session.userId, true)
        val request = call.receive<FamilyRequestInitData>()
        try {
          val result = FamilyRequestController.rejectFamilyRequest(user, user.account as Account, request.id)

          call.respondText(Json.encodeToString(ApiResponse(
            status = ApiResponseStatus.Ok,
            null,
            result
          )))
        } catch (error: Error) {
          com.financy.Logger.error(error.localizedMessage, error)
          call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Error, error.localizedMessage, "")))
        }
      } catch(error: Error) {
        com.financy.Logger.error(error.localizedMessage, error)
        call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Error, error.localizedMessage, "")))
      }
    }
  }
}