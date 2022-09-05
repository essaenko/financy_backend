package com.financy.provider

import com.financy.model.User
import com.financy.reader.Reader
import com.financy.reader.ReaderExceptions

class Sber: Provider {
  override fun parseExport(user: User, reader: Reader) {
    val cardRegex = "• [0-9]{4}".toRegex()
    val transactionRegex = "[0-9]{2}\\.[0-9]{2}\\.[0-9]{4} .*+\n[0-9]{2}\\.[0-9]{2}\\.[0-9]{4} .*+".toRegex()

    val table = transactionRegex.findAll(reader.content)
    val card = reader.content
      .slice(cardRegex.find(reader.content)?.range ?: throw Error(ReaderExceptions.UnsopportedFileFormat.name))
      .replace("• ", "*")
    val content = reader.content
    reader.content = ""
    for (match in table) {
      reader.content += "$card ${content.slice(match.range).replace("\n", " ")}"
      reader.content += "\n"
    }

    reader.processContent()
  }
}

val SberProvider = Sber()