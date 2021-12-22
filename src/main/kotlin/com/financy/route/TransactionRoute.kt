package com.financy.route

import com.financy.controller.AccountController
import com.financy.controller.TransactionController
import com.financy.controller.UserController
import com.financy.model.TransactionData
import com.financy.model.TransactionInitData
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

fun Route.TransactionControllerRoutes() {
  authenticate {
    post("/api/v1/transaction/create") {
      val principal = call.principal<JWTPrincipal>()
      val userId = principal!!.payload.getClaim("user_id").asInt()

      if (userId == null) {
        call.respondText(Json.encodeToString(ApiResponse(ApiResponseStatus.Error, "InvalidTokenException", "")))
      } else {
        try {
          val user = UserController.getUser(userId)
          if (user.accountId == null) {
            throw Error(Exceptions.NoUserAccountException.name)
          }

          val account = AccountController.getAccount(user.accountId!!)
          val transactionInit = call.receive<TransactionInitData>()

          val transaction = TransactionController.create(user, account, transactionInit)

          call.respondText { Json.encodeToString(ApiResponse(ApiResponseStatus.Ok, null, TransactionData(
            id = transaction.id,
            type = transaction.type,
            comment = transaction.comment,
            cost = transaction.cost,
            createdAt = transaction.createdAt.toString(),
            updatedAt = transaction.updatedAt.toString(),
            accountId = transaction.accountId,
            userId = transaction.userId,
            categoryId = transaction.categoryId,
            from = transaction.from,
            to = transaction.to,
          ))) }
        } catch (error: Error) {
          call.respondText { Json.encodeToString(ApiResponse(ApiResponseStatus.Error, error.localizedMessage, "")) }
        }
      }
    }
    get("/api/v1/transaction") {
      val principal = call.principal<JWTPrincipal>()
      val userId = principal!!.payload.getClaim("user_id").asInt()

      if (userId == null) {
        call.respondText(Json.encodeToString(ApiResponse(ApiResponseStatus.Error, "InvalidTokenException", "")))
      } else {
        try {
          val user = UserController.getUser(userId)
          if (user.accountId == null) {
            throw Error(Exceptions.NoUserAccountException.name)
          }

          val account = AccountController.getAccount(user.accountId!!)
          val transactions = TransactionController.getAll(account)

          call.respondText { Json.encodeToString(ApiResponse(ApiResponseStatus.Ok, null, transactions.map { TransactionData(
              id = it.id,
              type = it.type,
              comment = it.comment,
              cost = it.cost,
              createdAt = it.createdAt.toString(),
              updatedAt = it.updatedAt.toString(),
              accountId = it.accountId,
              userId = it.userId,
              categoryId = it.categoryId,
              from = it.from,
              to = it.to,
            )}
          ))}
        } catch (error: Error) {
          call.respondText { Json.encodeToString(ApiResponse(ApiResponseStatus.Error, error.localizedMessage, "")) }
        }
      }
    }
  }
}