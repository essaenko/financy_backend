package com.financy.controller

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.financy.ApplicationConfig
import com.financy.Session
import com.financy.dbInstance
import com.financy.model.*
import com.financy.utils.Crypto
import com.financy.utils.Exceptions
import com.financy.utils.Mail
import org.ktorm.dsl.eq
import org.ktorm.entity.add
import org.ktorm.entity.find
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.HashMap

object UserController {
  fun getUser(userId: Int, withAccount: Boolean? = false): User {
    val user = dbInstance?.Users?.find { it.id eq userId } ?: throw Error(Exceptions.UserNotFoundException.name)

    if (withAccount == true && user.account == null) {
      throw Error(Exceptions.NoUserAccountException.name)
    }

    return user
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

    if (dbInstance?.Users?.find { it.email eq credentials.email } != null) {
      throw Error(Exceptions.EmailAlreadyRegisteredException.name)
    }

    try {
      val user = User {
        email = credentials.email
        name = credentials.name
        password = Crypto.sha1(credentials.password)
        createdAt = LocalDateTime.now()
        updatedAt = null
      }

      dbInstance?.Users?.add(user)
    } catch (error: Error) {
      com.financy.Logger.error("Failed to create new user with error ${error.localizedMessage}", error)
      throw Error(Exceptions.InternalServerException.name)
    }
  }

  fun login (credentials: Credentials, userAgent: String): HashMap<String, String> {
    if (credentials.email != null && credentials.password != null) {
      return authenticate(credentials.email, credentials.password, userAgent)
    } else {
      throw Error(Exceptions.InvalidUserCredentialsException.name)
    }
  }

  fun createResetPasswordRequest(email: String?) {
    if (email == null) {
      throw Error(Exceptions.InvalidUserCredentialsException.name)
    }
    if (dbInstance?.Users?.find { it.email eq email } == null) {
      return
    }

    val token = Crypto.sha1("$email.${ApplicationConfig.ktor.resetPasswordSecret}.${LocalDateTime.now()}")

    Session?.createResetPasswordRequest(email, token)

    Mail.sendMail(
      email,
      "Restore your password for financy.live",
      mapOf("LINK" to "https://financy.live", "TOKEN" to token),
      "${ApplicationConfig.fileSystem.resourcesRoot}/restore_password_template.html"
    )
  }

  fun updatePassword(user: User, oldPassword: String, newPassword: String) {
    if(Crypto.sha1(oldPassword) != user.password) {
      throw Error(Exceptions.InvalidUserCredentialsException.name)
    }

    user.password = Crypto.sha1(newPassword)
    user.flushChanges()
    Session?.destroyAllSessions(user.id)
  }

  fun resetPassword(password: String?, token: String?) {
    if (password == null || token == null) {
      throw Error(Exceptions.InvalidUserCredentialsException.name)
    }
    val resetPasswordDescription = Session?.getResetPasswordRequest(token)
      ?: throw Error(Exceptions.BadRequestException.name)
    val user = dbInstance?.Users?.find { it.email eq resetPasswordDescription.email }
      ?: throw Error(Exceptions.UserNotFoundException.name)
    user.password = Crypto.sha1(password)
    user.flushChanges()

    Session?.destroyAllSessions(user.id)
  }

  private fun authenticate(email: String, password: String, userAgent: String): HashMap<String, String> {
    val user = dbInstance?.Users?.find { it.email eq email }

    if (user?.password == Crypto.sha1(password)) {
      val session: SessionDescription = Session?.createSession(user.id, userAgent) as SessionDescription
      val token: String = JWT.create()
        .withIssuer(ApplicationConfig.ktor.issuer)
        .withClaim("session", session._id.toString())
        .withExpiresAt(Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))
        .sign(Algorithm.HMAC256(ApplicationConfig.ktor.secret))

      if (Session?.findSessionFromDevice(user.id, userAgent) == null) {
        Mail.sendMail(
          user.email,
          "Financy: New login",
          "Found login from new device.\nDevice: $userAgent\nIf it's not your device, immediatly change your password for security reasons."
        )
      }

      return hashMapOf("token" to token)
    }

    throw Error(Exceptions.UserNotFoundException.name)
  }
}