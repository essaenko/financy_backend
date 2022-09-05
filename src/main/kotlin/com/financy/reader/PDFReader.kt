package com.financy.reader

import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.parser.PdfTextExtractor

class PDFReader(file: ByteArray): Reader(file, Readers.PDF, false) {
  init {
    val reader = PdfReader(file)
    val pages = reader.numberOfPages

    content = ""

    for(i in 1..pages) {
      content += PdfTextExtractor.getTextFromPage(reader, i)
    }

    println("----- MATCH: \\t ${content.contains("\t")} \\n: ${content.contains("\n")}")
  }
}