package com.financy.route

import com.financy.controller.StatisticController
import com.financy.controller.UserController
import com.financy.model.StatisticData
import com.financy.model.User
import com.financy.utils.ApiResponse
import com.financy.utils.ApiResponseStatus
import com.financy.utils.Exceptions
import com.financy.utils.StatisticParametersInit
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
      val principal = call.principal<JWTPrincipal>()
      val userId: Int? = principal!!.payload.getClaim("user_id").asInt()
      val query = call.request.queryParameters
      if (userId != null) {
        try {
          val user: User = UserController.getUser(userId)

          if (user.account == null) {
            throw Error(Exceptions.NoUserAccountException.name)
          }

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
          call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Error, error.localizedMessage, "")))
        }
      } else {
        call.respondText(Json.encodeToString(ApiResponse(status = ApiResponseStatus.Error, "InvalidTokenException", "")))
      }
    }
  }
}