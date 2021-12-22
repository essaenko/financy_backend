package com.financy

import com.auth0.jwt.JWT

import com.auth0.jwt.algorithms.Algorithm
import com.financy.route.*
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.serialization.*


fun initWebServer() {
  embeddedServer(Netty, port = 80) {
    install(CORS) {
      host("0.0.0.0:3000")
      host("127.0.0.1:3000")
      host("localhost:3000")
      host("localhost")

      method(HttpMethod.Options)
      method(HttpMethod.Put)
      method(HttpMethod.Patch)
      method(HttpMethod.Delete)
      method(HttpMethod.Post)
      method(HttpMethod.Get)

      header(HttpHeaders.ContentType)
      header(HttpHeaders.Authorization)
    }
    install(ContentNegotiation) {
      json()
    }
    install(Authentication) {
      jwt {
        verifier(JWT
          .require(Algorithm.HMAC256(ApplicationConfig.ktor.secret))
          .withIssuer(ApplicationConfig.ktor.issuer)
          .build()
        )

        validate { credential ->
          if (credential.payload.getClaim("email").asString() != "") {
            JWTPrincipal(credential.payload);
          } else {
            println("Failed credentials ${credential.payload}")
            null
          }
        }
      }
    }

    routing {
      UserControllerRoutes()
      AccountControllerRoutes()
      CategoryControllerRoutes()
      TransactionControllerRoutes()
      PaymentMethodControllerRoutes()
      get("/") {
        call.respondText("API Home page")
      }
    }
  }.start(wait = true)
  println("Web server successfully started");
}