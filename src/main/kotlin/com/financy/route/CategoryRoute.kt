package com.financy.route

import com.financy.controller.AccountController
import com.financy.controller.CategoryController
import com.financy.controller.UserController
import com.financy.model.CategoryData
import com.financy.model.CategoryInitData
import com.financy.model.CategoryType
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

          if (user.accountId == null) {
            throw Error(Exceptions.NoUserAccountException.name)
          }
          val account = AccountController.getAccount(user.accountId!!)

          val categoryInstance = CategoryController.create(category.name, account.id, CategoryType.valueOf(category.type), category.parent)

          call.respondText(Json.encodeToString(ApiResponse(ApiResponseStatus.Ok, null, CategoryData(
            name = categoryInstance.name,
            type = categoryInstance.type,
            id = categoryInstance.id,
            accountId = categoryInstance.accountId,
            parentId = categoryInstance.parentId,
            createdAt = categoryInstance.createdAt.toString(),
            updatedAt = categoryInstance.updatedAt.toString()
          ))))
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
          if (user.accountId == null) {
            throw Error(Exceptions.NoUserAccountException.name)
          }

          val account = AccountController.getAccount(user.accountId!!)
          val categories = CategoryController.getAll(account)

          call.respondText { Json.encodeToString(ApiResponse(ApiResponseStatus.Ok, null, categories.map { CategoryData(
            id = it.id,
            parentId = it.parentId,
            accountId = it.accountId,
            name = it.name,
            type = it.type,
            createdAt = it.createdAt.toString(),
            updatedAt = it.updatedAt.toString(),
          ) })) }
        } catch (error: Error) {
          call.respondText(Json.encodeToString(ApiResponse(ApiResponseStatus.Error, error.localizedMessage, "")))
        }
      }
    }
  }
}