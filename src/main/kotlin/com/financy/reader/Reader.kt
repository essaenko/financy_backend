package com.financy.reader
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.charset.Charset

enum class Readers {
  CSV,
  XLS,
  PDF,
}

enum class ReaderExceptions {
  UnresolvedColumnDeclarationName,
  UnresolvedFileCharset,
  UnsopportedFileFormat
}

val Logger: Logger = LoggerFactory.getLogger("Reader")

open class Reader(
  private val file: ByteArray,
  reader: Readers,
  readFile: Boolean = true
) {
  private val charsets = listOf(
    Charsets.UTF_8,
    Charset.forName("Windows-1251"),
    Charsets.ISO_8859_1,
    Charsets.US_ASCII,
    Charsets.UTF_16,
    Charsets.UTF_16BE,
    Charsets.UTF_16LE,
    Charsets.UTF_32,
    Charsets.UTF_32BE,
    Charsets.UTF_32LE,
  )
  private var declarationRow: Int? = null
  private var declaration: MutableMap<String, Int> = mutableMapOf()

  val type: Readers = reader
  lateinit var content: String
  lateinit var rows: List<String>
  lateinit var columns: List<List<String>>

  init {
    if (readFile) {
      content = resolveCharSet()
      processContent()
    }
  }

  fun processContent() {
    rows = parseRows(content)
    columns = rows.map { parseColumns(it) }
  }

  fun setDeclarationRow(index: Int) {
    declarationRow = index

    parseDeclarationRow()
  }

  fun getColumn(line: String, name: String): String {
    if (declaration.containsKey(name)) {
      return parseColumns(line)[declaration[name]!!]
    }

    throw Error(ReaderExceptions.UnresolvedColumnDeclarationName.name)
  }

  private fun parseDeclarationRow() {
    if (declarationRow != null) {
      columns[declarationRow!!].forEachIndexed { index, value ->
        declaration[value] = index
      }
    }
  }

  fun parseRows(content: String): List<String> {
    return content.split("\n")
  }

  fun parseColumns(row: String): List<String> {
    return row
      .split(";")
      .map {
        it.replace("\"", "")
      }
  }

  private fun resolveCharSet(charsetIndex: Int = 0): String {
    if (charsetIndex == charsets.size) {
      throw Error(ReaderExceptions.UnresolvedFileCharset.name)
    }
    val result = file.toString(charsets[charsetIndex])
    Logger.info("-----Tried to parse file with ${charsets[charsetIndex]} charset, result is: \n${result.slice(0..600)}\n")
    if (result == "" || result.contains("�") || result.contains("??")) {
      return resolveCharSet(charsetIndex + 1)
    }

    return result
  }

  open fun eachLine(start: Int = 0, callback: (line: String) -> Unit) {
    content.split("\n").listIterator(start).forEach { callback(it) }
  }
}