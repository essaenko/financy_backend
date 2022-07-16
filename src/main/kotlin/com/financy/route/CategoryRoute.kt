package com.financy.route

import com.financy.controller.CategoryController
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
import org.slf4j.LoggerFactory

fun Route.CategoryControllerRoutes() {
  authenticate {
    post("/api/v1/category/create") {
      val principal = call.principal<JWTPrincipal>()
      val userId = principal!!.payload.getClaim("user_id").asInt()

      if (userId == null) {
        call.respondText(Json.encodeToString(ApiResponse(ApiResponseStatus.Error, "InvalidTokenException", "")))
      }

      if (userId !== null) {
        try {
          val user = UserController.getUser(userId)
          val category = call.receive<CategoryInitData>()

          if (user.account == null) {
            throw Error(Exceptions.NoUserAccountException.name)
          }
          val categoryInstance = CategoryController.create(category.name, user.account!!.id, CategoryType.valueOf(category.type), category.parent)

          call.respondText(Json.encodeToString(ApiResponse(ApiResponseStatus.Ok, null, CategoryData.getSerializable(categoryInstance))))
        } catch (error: Error) {
          call.respondText(Json.encodeToString(ApiResponse(ApiResponseStatus.Error, error.localizedMessage, "")))
        }
      }
    }
    get("/api/v1/category") {
      val principal = call.principal<JWTPrincipal>()
      val userId = principal!!.payload.getClaim("user_id").asInt()

      if (userId == null) {
        call.respondText(Json.encodeToString(ApiResponse(ApiResponseStatus.Error, "InvalidTokenException", "")))
      }

      if (userId != null) {
        try {
          val user = UserController.getUser(userId)
          if (user.account == null) {
            throw Error(Exceptions.NoUserAccountException.name)
          }

          val categories = CategoryController.getAll(user.account!!)

          call.respondText { Json.encodeToString(ApiResponse(ApiResponseStatus.Ok, null, categories.map { CategoryData.getSerializable(it) })) }
        } catch (error: Error) {
          val logger = LoggerFactory.getLogger("Financy")
          logger.warn(error.stackTraceToString())
          call.respondText(Json.encodeToString(ApiResponse(ApiResponseStatus.Error, error.localizedMessage, "")))
        }
      }
    }
  }
}