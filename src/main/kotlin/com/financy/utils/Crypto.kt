package com.financy.utils

import java.security.MessageDigest

object Crypto {
  fun sha1(string: String): String {
    val bytes = MessageDigest.getInstance("SHA-1").digest(string.toByteArray());

    return bytes.joinToString("") {
      "%02x".format(it)
    }
  }
}