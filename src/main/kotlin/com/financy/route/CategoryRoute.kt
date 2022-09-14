package com.financy.route

import com.financy.Session
import com.financy.controller.CategoryController
import com.financy.controller.UserController
import com.financy.model.*
import com.financy.utils.ApiResponse
import com.financy.utils.ApiResponseStatus
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun Route.CategoryControllerRoutes() {
  authenticate {
    post("/api/v1/category/create") {
      try {
        val session = Session?.getUserSession(call)
        if (session == null) {
          call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Error, "InvalidTokenException", "")))

          return@post
        }
        val user = UserController.getUser(session.userId, true)
        

        val category = call.receive<CategoryInitData>()
        val categoryInstance = CategoryController.create(user, category)

        call.respondText(Json.encodeToString(ApiResponse(ApiResponseStatus.Ok, null, CategoryData.getSerializable(categoryInstance))))
      } catch (error: Error) {
        com.financy.Logger.error(error.localizedMessage, error)
        call.respondText(Json.encodeToString(ApiResponse(ApiResponseStatus.Error, error.localizedMessage, "")))
      }
    }
    post("/api/v1/category/update") {
      try {
        val session = Session?.getUserSession(call)
        if (session == null) {
          call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Error, "InvalidTokenException", "")))

          return@post
        }
        val user = UserController.getUser(session.userId, true)


        val category = call.receive<CategoryInitData>()
        val categoryInstance = CategoryController.update(user, category)

        call.respondText(Json.encodeToString(ApiResponse(ApiResponseStatus.Ok, null, CategoryData.getSerializable(categoryInstance))))
      } catch (error: Error) {
        com.financy.Logger.error(error.localizedMessage, error)
        call.respondText(Json.encodeToString(ApiResponse(ApiResponseStatus.Error, error.localizedMessage, "")))
      }
    }
    get("/api/v1/category") {
      try {
        val session = Session?.getUserSession(call)
        if (session == null) {
          call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Error, "InvalidTokenException", "")))

          return@get
        }
        val user = UserController.getUser(session.userId, true)
        val categories = CategoryController.getAll(user.account!!)

        call.respondText { Json.encodeToString(ApiResponse(ApiResponseStatus.Ok, null, categories.map { CategoryData.getSerializable(it) })) }
      } catch (error: Error) {
        com.financy.Logger.error(error.localizedMessage, error)
        call.respondText(Json.encodeToString(ApiResponse(ApiResponseStatus.Error, error.localizedMessage, "")))
      }
    }
  }
}