package com.financy.controller

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.financy.ApplicationConfig
import com.financy.dbInstance
import com.financy.model.*
import com.financy.utils.Crypto
import com.financy.utils.Exceptions
import org.ktorm.dsl.eq
import org.ktorm.entity.add
import org.ktorm.entity.find
import java.time.LocalDate
import java.util.*
import kotlin.collections.HashMap

object UserController {
  fun getUser(userId: Int): User {
    return dbInstance?.Users?.find { it.id eq userId } ?: throw Error(Exceptions.UserNotFoundException.name)
  }

  fun getUser(email: String): User {
    return dbInstance?.Users?.find { it.email eq email } ?: throw Error(Exceptions.UserNotFoundException.name)
  }

  fun create (credentials: RegistrationCredentials) {
    if (
      credentials.email == null ||
      credentials.password == null ||
      credentials.name == null ||
      credentials.name.length < 2 ||
      !credentials.email.contains("@")
    ) {
      throw Error(Exceptions.InvalidRegistrationCredentialsException.name)
    }

    try {
      val user = User {
        email = credentials.email
        name = credentials.name
        password = Crypto.sha1(credentials.password)
        createdAt = LocalDate.now()
        updatedAt = null
      }

      dbInstance?.Users?.add(user)
    } catch (error: Error) {
      println("Failed to create new user with error ${error.localizedMessage}")

      throw Error(Exceptions.InternalServerException.name)
    }
  }

  fun login (credentials: Credentials): HashMap<String, String> {
    if (credentials.email != null && credentials.password != null) {
      return authenticate(credentials.email, credentials.password)
    } else {
      throw Error(Exceptions.InvalidUserCredentialsException.name)
    }
  }

  private fun authenticate(email: String, password: String): HashMap<String, String> {
    val user = dbInstance?.Users?.find { it.email eq email }

    if (user?.password == Crypto.sha1(password)) {
      val token: String = JWT.create()
        .withIssuer(ApplicationConfig.ktor.issuer)
        .withClaim("email", email)
        .withClaim("user_id", user.id)
        .withExpiresAt(Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))
        .sign(Algorithm.HMAC256(ApplicationConfig.ktor.secret))

      return hashMapOf("token" to token)
    }

    throw Error(Exceptions.UserNotFoundException.name)
  }
}