package com.bearkingsoftware.loddboka.ui.trekning

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.bearkingsoftware.loddboka.data.Farge
import com.bearkingsoftware.loddboka.viewmodel.Vinner
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class WinnerExportLabels(
    val pdfTitle: String,
    val book: String,
    val ticket: String,
    val drawn: String,
    val chooserTitle: String,
    val colorName: (Farge) -> String,
)

fun shareWinnerResults(
    context: Context,
    winners: List<Vinner>,
    labels: WinnerExportLabels,
    pdfSubtitle: String,
) {
    if (winners.isEmpty()) return
    val timeFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM)
    val rows = winners.mapIndexed { index, winner ->
        listOf(
            (index + 1).toString(),
            "${labels.colorName(winner.loddbok.farge)} ${winner.loddbok.bokstav}",
            winner.loddNummer.toString(),
            timeFormat.format(Date(winner.trukketMillis)),
        )
    }
    val headers = listOf("#", labels.book, labels.ticket, labels.drawn)
    val exportDay = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    val baseName = "Loddboka-winners-$exportDay"
    val dir = File(context.cacheDir, "exports").apply { mkdirs() }
    val pdf = File(dir, "$baseName.pdf")
    val csv = File(dir, "$baseName.csv")
    pdf.writeBytes(winnersPdf(labels.pdfTitle, pdfSubtitle, headers, rows))
    csv.writeBytes(winnersCsv(headers, rows))

    val authority = "${context.packageName}.fileprovider"
    val pdfUri = FileProvider.getUriForFile(context, authority, pdf)
    val csvUri = FileProvider.getUriForFile(context, authority, csv)
    val send = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
        type = "*/*"
        putParcelableArrayListExtra(Intent.EXTRA_STREAM, arrayListOf(pdfUri, csvUri))
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        clipData = ClipData.newUri(context.contentResolver, baseName, pdfUri).apply {
            addItem(ClipData.Item(csvUri))
        }
    }
    try {
        context.startActivity(Intent.createChooser(send, labels.chooserTitle))
    } catch (_: ActivityNotFoundException) {
        // Share sheet has no handler. Stay on the winners screen.
    }
}

internal fun winnersCsv(headers: List<String>, rows: List<List<String>>): ByteArray {
    fun cell(value: String): String {
        val escaped = value.replace("\"", "\"\"")
        return if (escaped.any { it == ',' || it == '"' || it == '\n' || it == '\r' }) "\"$escaped\"" else value
    }
    val text = buildString {
        append(headers.joinToString(",") { cell(it) })
        append("\r\n")
        rows.forEach { row ->
            append(row.joinToString(",") { cell(it) })
            append("\r\n")
        }
    }
    return byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()) + text.toByteArray(Charsets.UTF_8)
}

internal fun winnersPdf(title: String, subtitle: String, headers: List<String>, rows: List<List<String>>): ByteArray {
    val doc = PdfDocument()
    val pageWidth = 595
    val pageHeight = 842
    val margin = 40f
    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 18f
        isFakeBoldText = true
        color = Color.BLACK
    }
    val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 11f
        isFakeBoldText = true
        color = Color.BLACK
    }
    val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 11f
        color = Color.BLACK
    }
    val linePaint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 0.6f
    }
    val colRowEnd = margin + 24f
    val colBook = margin + 36f
    val colTicket = 260f
    val colDrawn = 350f
    val rowHeaderPaint = Paint(headerPaint).apply { textAlign = Paint.Align.RIGHT }
    val rowBodyPaint = Paint(bodyPaint).apply { textAlign = Paint.Align.RIGHT }
    var pageNumber = 1
    var page = doc.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
    var canvas = page.canvas
    var y = margin + 8f

    fun newPage() {
        doc.finishPage(page)
        pageNumber += 1
        page = doc.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
        canvas = page.canvas
        y = margin + 8f
    }

    fun ensure(space: Float) {
        if (y + space > pageHeight - margin) newPage()
    }

    canvas.drawText(title, margin, y, titlePaint)
    y += 20f
    canvas.drawText(subtitle, margin, y, bodyPaint)
    y += 20f

    fun drawHeaders() {
        canvas.drawText(headers.getOrElse(0) { "" }, colRowEnd, y, rowHeaderPaint)
        canvas.drawText(headers.getOrElse(1) { "" }, colBook, y, headerPaint)
        canvas.drawText(headers.getOrElse(2) { "" }, colTicket, y, headerPaint)
        canvas.drawText(headers.getOrElse(3) { "" }, colDrawn, y, headerPaint)
        y += 6f
        canvas.drawLine(margin, y, pageWidth - margin, y, linePaint)
        y += 16f
    }

    drawHeaders()
    for (row in rows) {
        if (y + 18f > pageHeight - margin) {
            newPage()
            drawHeaders()
        }
        canvas.drawText(row.getOrElse(0) { "" }, colRowEnd, y, rowBodyPaint)
        canvas.drawText(row.getOrElse(1) { "" }, colBook, y, bodyPaint)
        canvas.drawText(row.getOrElse(2) { "" }, colTicket, y, bodyPaint)
        canvas.drawText(row.getOrElse(3) { "" }, colDrawn, y, bodyPaint)
        y += 18f
    }
    doc.finishPage(page)
    val out = ByteArrayOutputStream()
    doc.writeTo(out)
    doc.close()
    return out.toByteArray()
}
