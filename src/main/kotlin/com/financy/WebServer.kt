package com.financy

import com.auth0.jwt.JWT

import com.auth0.jwt.algorithms.Algorithm
import com.financy.route.*
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.metrics.micrometer.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.serialization.*
import io.micrometer.core.instrument.Clock
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics
import io.micrometer.core.instrument.binder.system.ProcessorMetrics
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import io.prometheus.client.CollectorRegistry

val collectorRegistry = CollectorRegistry()
val appMetricsRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT, collectorRegistry, Clock.SYSTEM)

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
    install(MicrometerMetrics) {
      registry = appMetricsRegistry
      meterBinders = listOf(
        JvmMemoryMetrics(),
        JvmGcMetrics(),
        ProcessorMetrics(),
      )
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
      get("/metrics") {
        call.respond(appMetricsRegistry.scrape())
      }
    }
  }.start(wait = true)
  println("Web server successfully started");
}