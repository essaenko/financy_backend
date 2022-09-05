package com.financy.utils

import com.financy.model.ResetPasswordDescription
import com.financy.model.SessionDescription
import com.mongodb.MongoTimeoutException
import com.mongodb.client.FindIterable
import com.mongodb.client.MongoCollection
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import org.bson.types.ObjectId
import org.litote.kmongo.*
import org.litote.kmongo.id.toId
import java.time.LocalDateTime

enum class ServiceStatusList {
  Untouched,
  Connecting,
  Connected,
  Disconnected,
}

class SessionService {
  private val sessionCollection: MongoCollection<SessionDescription>
  private val resetPasswordCollection: MongoCollection<ResetPasswordDescription>
  private var status: ServiceStatusList = ServiceStatusList.Untouched

  init {
    val client = KMongo.createClient("mongodb://localhost")
    val database = client.getDatabase("financy_db")
    sessionCollection = database.getCollection<SessionDescription>("session")
    resetPasswordCollection = database.getCollection<ResetPasswordDescription>("reset")

    try {
      status = ServiceStatusList.Connecting
      sessionCollection.countDocuments()

      status = ServiceStatusList.Connected
    } catch (error: MongoTimeoutException) {
      status = ServiceStatusList.Disconnected
      client.close()
      com.financy.Logger.error(error.localizedMessage, error)
    }
  }

  fun createSession(id: Int, userAgent: String): SessionDescription {
    if (status == ServiceStatusList.Disconnected) {
      throw Error(Exceptions.InternalServerException.name)
    }
    val session = SessionDescription(
      userAgent = userAgent,
      createdAt = LocalDateTime.now().toString(),
      updatedAt = LocalDateTime.now().toString(),
      userId = id,
    )

    sessionCollection.insertOne(session)

    return session
  }

  fun destroyAllSessions(userId: Int) {
    if (status == ServiceStatusList.Disconnected) {
      throw Error(Exceptions.InternalServerException.name)
    }

    sessionCollection.deleteMany(SessionDescription::userId eq userId)
  }

  fun findSessionFromDevice(userId: Int, userAgent: String): SessionDescription? {
    return sessionCollection.findOne(and(SessionDescription::userId eq userId, SessionDescription::userAgent eq userAgent))
  }

  fun updateSession(token: String) {
    if (status == ServiceStatusList.Disconnected) {
      throw Error(Exceptions.InternalServerException.name)
    }

    sessionCollection.updateOne(
      SessionDescription::_id eq ObjectId(token).toId(),
      setValue(SessionDescription::updatedAt, LocalDateTime.now().toString())
    )
  }

  fun getSession(token: String): SessionDescription? {
    if (status == ServiceStatusList.Disconnected) {
      throw Error(Exceptions.InternalServerException.name)
    }

    val session = sessionCollection.findOneById(ObjectId(token).toId<SessionDescription>())

    if (session != null) {
      updateSession(token)
    }

    return session
  }

  fun createResetPasswordRequest(email: String, token: String) {
    if (status == ServiceStatusList.Disconnected) {
      throw Error(Exceptions.InternalServerException.name)
    }

    resetPasswordCollection.deleteMany(ResetPasswordDescription::email eq email)

    resetPasswordCollection.insertOne(ResetPasswordDescription(
      email,
      token,
      createdAt = LocalDateTime.now().toString()
    ))
  }

  fun getResetPasswordRequest(token: String): ResetPasswordDescription? {
    return resetPasswordCollection.findOne(ResetPasswordDescription::token eq token)
  }

  fun getUserSession(call: ApplicationCall): SessionDescription? {
    if (status == ServiceStatusList.Disconnected) {
      throw Error(Exceptions.InternalServerException.name)
    }

    val principal = call.principal<JWTPrincipal>()
    val token: String? = principal!!.payload.getClaim("session").asString()

    if (token != null) {
      return getSession(token)
    } else {
      return null
    }
  }
}