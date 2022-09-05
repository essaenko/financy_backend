package com.financy

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.financy.route.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.request.*
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
  embeddedServer(Netty, port = 8080) {
    if (APP_ENV == "dev") {
      install(CORS) {
        allowHost("0.0.0.0:3000")
        allowHost("127.0.0.1:3000")
        allowHost("localhost:3000")
        allowHost("localhost")
        allowHost("192.168.0.137")
        allowHost("192.168.0.137:3000")
        allowHost("financy.live")

        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Get)

        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
      }
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
          if (credential.payload.getClaim("session").asString() != "") {
            JWTPrincipal(credential.payload)
          } else {
            Logger.info("Failed JWT credentials: ${credential.payload}")
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
      PaymentAccountControllerRoutes()
      StatisticControllerRoutes()
      FamilyRequestRoutes()
      get("/") {
        call.respondText("API Home page")
      }
      get("/api/metrics") {
        val privateHeader = call.request.header(ApplicationConfig.ktor.metrics.header)
        if (privateHeader != null && privateHeader == ApplicationConfig.ktor.metrics.value) {
          call.respondText(appMetricsRegistry.scrape())
        } else {
          call.response.status(HttpStatusCode.BadRequest)
          call.respond("")
        }
      }
    }
    fun Application.module() {
      install(MicrometerMetrics) {
        registry = appMetricsRegistry
        meterBinders = listOf(
          JvmMemoryMetrics(),
          JvmGcMetrics(),
          ProcessorMetrics(),
        )
      }
    }
  }.start(wait = true)
  Logger.info("Web server successfully started")
}