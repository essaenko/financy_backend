package com.financy.reader

import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.DataFormatter

class XLSReader(file: ByteArray): Reader(file, Readers.XLS, false) {
  init {
    val book = HSSFWorkbook(file.inputStream())
    val formatter = DataFormatter()
    var result = ""
    val sheet = book.getSheetAt(0)

    sheet.forEach { row ->
      for (i in 0..row.lastCellNum) {
        if (i > 0) result += ";"
        val cell = row.getCell(i)
        result += formatter.formatCellValue(cell)
      }
      result += "\n"
    }

    content = result
    rows = parseRows(content)
    columns = rows.map { parseColumns(it) }
  }
}