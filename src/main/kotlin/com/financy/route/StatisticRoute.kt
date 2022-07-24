package com.financy.route

import com.financy.Session
import com.financy.controller.StatisticController
import com.financy.controller.UserController
import com.financy.model.StatisticData
import com.financy.model.User
import com.financy.utils.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun Route.StatisticControllerRoutes() {
  authenticate {
    get("/api/v1/stats") {
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
        val query = call.request.queryParameters

        val statistic: StatisticData = StatisticController.get(
          StatisticParametersInit.getValidInstance(
            dateFrom = query["dateFrom"],
            dateTo = query["dateTo"],
            category = query["category"],
            type = query["type"],
            user = query["user"]
          ),
          user.account!!
        )

        try {
          call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Ok, null, statistic)))
        } catch (error: Error) {
          call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Error, error.localizedMessage, "")))
        }
      } catch(error: Error) {
        com.financy.Logger.error(error.localizedMessage, error)
        call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Error, error.localizedMessage, "")))
      }
    }
  }
}