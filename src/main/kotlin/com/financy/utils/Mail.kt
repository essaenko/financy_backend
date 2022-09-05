package com.financy.utils

import com.financy.Logger
import org.apache.commons.mail.EmailException
import org.apache.commons.mail.SimpleEmail
import java.io.File
import java.nio.charset.StandardCharsets.UTF_8

object Mail {
  fun sendMail(
    to: String,
    subject: String,
    keys: Map<String, String>,
    template: String,
  ) {
    try {
      var body = File(template).readText(UTF_8)
      keys.forEach {
        body = body.replace("%${it.key}%", it.value)
      }

      sendMail(to, subject, body, true)
    } catch(error: Error) {
      Logger.error("Failed sending mail with error: ${error.localizedMessage}", error)
    }
  }
  fun sendMail(
    to: String,
    subject: String,
    body: String,
    html: Boolean? = false,
  ) {
    try {
      val message = SimpleEmail()
      message.hostName = "localhost"
      message.setSmtpPort(25)
      message.isSSLOnConnect = true
      message.setFrom("support@financy.live", "Financy.live")
      message.addTo(to)
      message.subject = subject

      if (html == true) {
        message.setContent(body, "text/html; charset=UTF-8")
      } else {
        message.setMsg(body)
      }

      message.send()
    } catch (error: EmailException) {
      Logger.error("Failed sending mail with error: ${error.localizedMessage}", error)
    }
  }
}