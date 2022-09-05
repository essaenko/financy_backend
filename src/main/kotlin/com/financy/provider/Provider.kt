package com.financy.provider

import com.financy.model.User
import com.financy.reader.Reader

val BankProviders = mapOf(
  "Tinkoff" to TinkoffProvider,
  "SberBank" to SberProvider,
)

interface Provider {
  fun parseExport(user: User, reader: Reader)
}