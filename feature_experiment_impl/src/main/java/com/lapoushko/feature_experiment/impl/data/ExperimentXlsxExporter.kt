package com.lapoushko.feature_experiment.impl.data

import com.lapoushko.feature_experiment.api.domain.ExperimentEvent
import com.lapoushko.feature_experiment.api.domain.ExperimentSegment
import com.lapoushko.feature_experiment.api.domain.ExperimentSession
import com.lapoushko.feature_experiment.api.domain.ExperimentSummary
import java.io.ByteArrayOutputStream
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * XLSX (OOXML) с готовыми диаграммами — CSV как формат не может их хранить, поэтому это
 * отдельный экспорт. Собирается вручную (zip + XML по схеме SpreadsheetML/DrawingML) без
 * Apache POI: POI на Android ломается из-за отсутствия javax.xml.stream в системном рантайме.
 *
 * Листы: "Сводка" (числа без графиков), "Сегменты" (агрегаты по меткам + до 3 диаграмм),
 * "События" (сырые пинги по времени + до 2 диаграмм). Раскладка захардкожена под эти три
 * листа — под более общий случай не абстрагируем, так как других сценариев экспорта нет.
 */
fun buildExperimentXlsx(
    session: ExperimentSession,
    events: List<ExperimentEvent>,
    summary: ExperimentSummary
): ByteArray {
    val pings = events.filterIsInstance<ExperimentEvent.Ping>()
    val segments = summary.segments

    val segmentCharts = mutableListOf<ChartSpec>()
    if (segments.isNotEmpty()) {
        val lastRow = 1 + segments.size
        val labels = segments.map { it.label }
        segmentCharts += ChartSpec(
            kind = ChartKind.BAR,
            title = "Потери, % по сегментам",
            seriesName = "Потери, %",
            sheetName = SEGMENTS_SHEET,
            categoryColumn = "A",
            valueColumn = "E",
            firstRow = 2,
            lastRow = lastRow,
            categories = labels,
            values = segments.map { it.lossPercent.toDouble() }
        )
        segmentCharts += ChartSpec(
            kind = ChartKind.LINE,
            title = "RTT ср., мс по сегментам",
            seriesName = "RTT, мс",
            sheetName = SEGMENTS_SHEET,
            categoryColumn = "A",
            valueColumn = "F",
            firstRow = 2,
            lastRow = lastRow,
            categories = labels,
            values = segments.map { (it.averageRttMillis ?: 0L).toDouble() }
        )
        if (segments.any { it.averageRssiDbm != null }) {
            segmentCharts += ChartSpec(
                kind = ChartKind.LINE,
                title = "RSSI ср., дБм по сегментам",
                seriesName = "RSSI, дБм",
                sheetName = SEGMENTS_SHEET,
                categoryColumn = "A",
                valueColumn = "G",
                firstRow = 2,
                lastRow = lastRow,
                categories = labels,
                values = segments.map { (it.averageRssiDbm ?: 0).toDouble() }
            )
        }
    }

    val eventCharts = mutableListOf<ChartSpec>()
    if (pings.isNotEmpty()) {
        val lastRow = 1 + pings.size
        val startedAt = session.startedAtEpochMillis
        val elapsedLabels = pings.map { formatNumber((it.timestampEpochMillis - startedAt) / 1000.0) }
        eventCharts += ChartSpec(
            kind = ChartKind.LINE,
            title = "RTT, мс по времени",
            seriesName = "RTT, мс",
            sheetName = EVENTS_SHEET,
            categoryColumn = "A",
            valueColumn = "B",
            firstRow = 2,
            lastRow = lastRow,
            categories = elapsedLabels,
            values = pings.map { it.rttMillis.toDouble() }
        )
        if (pings.any { it.rssiDbm != null }) {
            eventCharts += ChartSpec(
                kind = ChartKind.LINE,
                title = "RSSI, дБм по времени",
                seriesName = "RSSI, дБм",
                sheetName = EVENTS_SHEET,
                categoryColumn = "A",
                valueColumn = "C",
                firstRow = 2,
                lastRow = lastRow,
                categories = elapsedLabels,
                values = pings.map { (it.rssiDbm ?: 0).toDouble() }
            )
        }
    }

    return assembleWorkbook(
        summaryRows = buildSummarySheetRows(session, summary),
        segmentRows = buildSegmentSheetRows(segments),
        segmentCharts = segmentCharts,
        eventRows = buildEventSheetRows(pings, session.startedAtEpochMillis),
        eventCharts = eventCharts
    )
}

private const val SUMMARY_SHEET = "Сводка"
private const val SEGMENTS_SHEET = "Сегменты"
private const val EVENTS_SHEET = "События"

private val COLS = ('A'..'Z').map { it.toString() }

private sealed interface Cell
private data class TextCell(val text: String) : Cell
private data class NumberCell(val value: Double) : Cell

private enum class ChartKind { BAR, LINE }

private data class ChartSpec(
    val kind: ChartKind,
    val title: String,
    val seriesName: String,
    val sheetName: String,
    val categoryColumn: String,
    val valueColumn: String,
    val firstRow: Int,
    val lastRow: Int,
    val categories: List<String>,
    val values: List<Double>
)

private fun formatNumber(value: Double): String = String.format(Locale.US, "%.4f", value)

private fun xmlEscape(text: String): String = text
    .replace("&", "&amp;")
    .replace("<", "&lt;")
    .replace(">", "&gt;")
    .replace("\"", "&quot;")
    .replace("'", "&apos;")

private fun buildRow(rowIndex: Int, cells: List<Cell>): String {
    val sb = StringBuilder("<row r=\"").append(rowIndex).append("\">")
    cells.forEachIndexed { i, cell ->
        val ref = COLS[i] + rowIndex
        when (cell) {
            is TextCell -> sb.append("<c r=\"").append(ref).append("\" t=\"inlineStr\"><is><t xml:space=\"preserve\">")
                .append(xmlEscape(cell.text)).append("</t></is></c>")
            is NumberCell -> sb.append("<c r=\"").append(ref).append("\"><v>")
                .append(formatNumber(cell.value)).append("</v></c>")
        }
    }
    return sb.append("</row>").toString()
}

private fun formatOrDash2(value: Double?): String = value?.let { String.format(Locale.US, "%.2f", it) } ?: "—"
private fun formatOrDash1(value: Double?): String = value?.let { String.format(Locale.US, "%.1f", it) } ?: "—"

private fun buildSummarySheetRows(session: ExperimentSession, summary: ExperimentSummary): List<String> {
    val model = summary.pathLossModel
    val entries = listOf(
        "Метрика" to "Значение",
        "Название замера" to session.title,
        "Всего пингов" to summary.totalPings.toString(),
        "Успешных пингов" to summary.totalSuccess.toString(),
        "Потери, %" to summary.overallLossPercent.toString(),
        "Корреляция RSSI↔RTT" to formatOrDash2(summary.correlation.rssiVsRtt),
        "Корреляция RSSI↔потери" to formatOrDash2(summary.correlation.rssiVsLoss),
        "Коэффициент затухания n" to formatOrDash2(model?.pathLossExponent),
        "RSSI на 1 м, дБм" to formatOrDash1(model?.rssiAt1mDbm),
        "R² модели затухания" to formatOrDash2(model?.rSquared),
        "Точек в модели затухания" to (model?.pointCount?.toString() ?: "0")
    )
    return entries.mapIndexed { index, (label, value) ->
        buildRow(index + 1, listOf(TextCell(label), TextCell(value)))
    }
}

private fun buildSegmentSheetRows(segments: List<ExperimentSegment>): List<String> {
    val header = buildRow(
        1,
        listOf(
            "Метка", "Расстояние, м", "Пингов", "Успешно", "Потери, %",
            "RTT ср., мс", "RSSI ср., дБм", "RSSI мин, дБм", "RSSI макс, дБм"
        ).map { TextCell(it) }
    )
    val dataRows = segments.mapIndexed { index, segment ->
        buildRow(
            index + 2,
            listOf(
                TextCell(segment.label),
                segment.distanceMeters?.let { NumberCell(it) } ?: TextCell(""),
                NumberCell(segment.pingCount.toDouble()),
                NumberCell(segment.successCount.toDouble()),
                NumberCell(segment.lossPercent.toDouble()),
                segment.averageRttMillis?.let { NumberCell(it.toDouble()) } ?: TextCell(""),
                segment.averageRssiDbm?.let { NumberCell(it.toDouble()) } ?: TextCell(""),
                segment.minRssiDbm?.let { NumberCell(it.toDouble()) } ?: TextCell(""),
                segment.maxRssiDbm?.let { NumberCell(it.toDouble()) } ?: TextCell("")
            )
        )
    }
    return listOf(header) + dataRows
}

private fun buildEventSheetRows(pings: List<ExperimentEvent.Ping>, startedAtEpochMillis: Long): List<String> {
    val header = buildRow(1, listOf("Время, с", "RTT, мс", "RSSI, дБм").map { TextCell(it) })
    val dataRows = pings.mapIndexed { index, ping ->
        buildRow(
            index + 2,
            listOf(
                NumberCell((ping.timestampEpochMillis - startedAtEpochMillis) / 1000.0),
                NumberCell(ping.rttMillis.toDouble()),
                ping.rssiDbm?.let { NumberCell(it.toDouble()) } ?: TextCell("")
            )
        )
    }
    return listOf(header) + dataRows
}

private fun sheetXml(rows: List<String>, drawingRelId: String?): String {
    val sb = StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>")
    sb.append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" ")
        .append("xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">")
    sb.append("<sheetData>")
    rows.forEach { sb.append(it) }
    sb.append("</sheetData>")
    if (drawingRelId != null) sb.append("<drawing r:id=\"").append(drawingRelId).append("\"/>")
    sb.append("</worksheet>")
    return sb.toString()
}

private fun cellRange(sheetName: String, column: String, firstRow: Int, lastRow: Int): String =
    "'" + sheetName + "'!$" + column + "$" + firstRow + ":$" + column + "$" + lastRow

private fun chartXml(spec: ChartSpec, axId1: Int, axId2: Int): String {
    val catRef = cellRange(spec.sheetName, spec.categoryColumn, spec.firstRow, spec.lastRow)
    val valRef = cellRange(spec.sheetName, spec.valueColumn, spec.firstRow, spec.lastRow)

    val catCache = StringBuilder("<c:strCache><c:ptCount val=\"").append(spec.categories.size).append("\"/>")
    spec.categories.forEachIndexed { i, v ->
        catCache.append("<c:pt idx=\"").append(i).append("\"><c:v>").append(xmlEscape(v)).append("</c:v></c:pt>")
    }
    catCache.append("</c:strCache>")

    val valCache = StringBuilder("<c:numCache><c:formatCode>General</c:formatCode><c:ptCount val=\"")
        .append(spec.values.size).append("\"/>")
    spec.values.forEachIndexed { i, v ->
        valCache.append("<c:pt idx=\"").append(i).append("\"><c:v>").append(formatNumber(v)).append("</c:v></c:pt>")
    }
    valCache.append("</c:numCache>")

    val chartTag = if (spec.kind == ChartKind.BAR) "barChart" else "lineChart"
    val groupingTag = if (spec.kind == ChartKind.BAR) {
        "<c:barDir val=\"col\"/><c:grouping val=\"clustered\"/>"
    } else {
        "<c:grouping val=\"standard\"/>"
    }
    val markerTag = if (spec.kind == ChartKind.LINE) "<c:marker><c:symbol val=\"circle\"/><c:size val=\"5\"/></c:marker>" else ""
    val smoothTag = if (spec.kind == ChartKind.LINE) "<c:smooth val=\"0\"/>" else ""
    val seriesMarkerFlag = if (spec.kind == ChartKind.LINE) "<c:marker val=\"1\"/>" else ""

    val sb = StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>")
    sb.append("<c:chartSpace xmlns:c=\"http://schemas.openxmlformats.org/drawingml/2006/chart\" ")
        .append("xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\" ")
        .append("xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">")
    sb.append("<c:chart>")
    sb.append("<c:title><c:tx><c:rich><a:bodyPr/><a:lstStyle/><a:p><a:r><a:t>")
        .append(xmlEscape(spec.title)).append("</a:t></a:r></a:p></c:rich></c:tx><c:overlay val=\"0\"/></c:title>")
    sb.append("<c:autoTitleDeleted val=\"0\"/>")
    sb.append("<c:plotArea><c:layout/>")
    sb.append("<c:").append(chartTag).append(">")
    sb.append(groupingTag)
    sb.append("<c:varyColors val=\"0\"/>")
    sb.append("<c:ser><c:idx val=\"0\"/><c:order val=\"0\"/>")
    sb.append("<c:tx><c:v>").append(xmlEscape(spec.seriesName)).append("</c:v></c:tx>")
    sb.append(markerTag)
    sb.append("<c:cat><c:strRef><c:f>").append(xmlEscape(catRef)).append("</c:f>").append(catCache).append("</c:strRef></c:cat>")
    sb.append("<c:val><c:numRef><c:f>").append(xmlEscape(valRef)).append("</c:f>").append(valCache).append("</c:numRef></c:val>")
    sb.append(smoothTag)
    sb.append("</c:ser>")
    sb.append(seriesMarkerFlag)
    sb.append("<c:axId val=\"").append(axId1).append("\"/><c:axId val=\"").append(axId2).append("\"/>")
    sb.append("</c:").append(chartTag).append(">")
    sb.append("<c:catAx><c:axId val=\"").append(axId1).append("\"/><c:scaling><c:orientation val=\"minMax\"/></c:scaling>")
        .append("<c:delete val=\"0\"/><c:axPos val=\"b\"/><c:crossAx val=\"").append(axId2).append("\"/></c:catAx>")
    sb.append("<c:valAx><c:axId val=\"").append(axId2).append("\"/><c:scaling><c:orientation val=\"minMax\"/></c:scaling>")
        .append("<c:delete val=\"0\"/><c:axPos val=\"l\"/><c:crossAx val=\"").append(axId1).append("\"/></c:valAx>")
    sb.append("</c:plotArea>")
    sb.append("<c:legend><c:legendPos val=\"b\"/></c:legend>")
    sb.append("<c:plotVisOnly val=\"1\"/>")
    sb.append("</c:chart></c:chartSpace>")
    return sb.toString()
}

private fun drawingXml(chartCount: Int, relIdOffset: Int): String {
    val anchors = StringBuilder()
    for (i in 0 until chartCount) {
        val rowFrom = i * 16
        val rowTo = rowFrom + 15
        val relId = "rId" + (relIdOffset + i)
        anchors.append("<xdr:twoCellAnchor>")
        anchors.append("<xdr:from><xdr:col>0</xdr:col><xdr:colOff>0</xdr:colOff><xdr:row>")
            .append(rowFrom).append("</xdr:row><xdr:rowOff>0</xdr:rowOff></xdr:from>")
        anchors.append("<xdr:to><xdr:col>8</xdr:col><xdr:colOff>0</xdr:colOff><xdr:row>")
            .append(rowTo).append("</xdr:row><xdr:rowOff>0</xdr:rowOff></xdr:to>")
        anchors.append("<xdr:graphicFrame macro=\"\">")
        anchors.append("<xdr:nvGraphicFramePr><xdr:cNvPr id=\"").append(i + 2).append("\" name=\"Chart")
            .append(i + 1).append("\"/><xdr:cNvGraphicFramePr/></xdr:nvGraphicFramePr>")
        anchors.append("<xdr:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\"0\" cy=\"0\"/></xdr:xfrm>")
        anchors.append("<a:graphic><a:graphicData uri=\"http://schemas.openxmlformats.org/drawingml/2006/chart\">")
        anchors.append("<c:chart xmlns:c=\"http://schemas.openxmlformats.org/drawingml/2006/chart\" ")
            .append("xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" r:id=\"")
            .append(relId).append("\"/>")
        anchors.append("</a:graphicData></a:graphic>")
        anchors.append("</xdr:graphicFrame>")
        anchors.append("<xdr:clientData/>")
        anchors.append("</xdr:twoCellAnchor>")
    }
    return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
        "<xdr:wsDr xmlns:xdr=\"http://schemas.openxmlformats.org/drawingml/2006/spreadsheetDrawing\" " +
        "xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\">" +
        anchors + "</xdr:wsDr>"
}

private fun relationshipsXml(relationships: List<Pair<String, String>>): String {
    val sb = StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>")
    sb.append("<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">")
    relationships.forEachIndexed { index, (type, target) ->
        sb.append("<Relationship Id=\"rId").append(index + 1).append("\" Type=\"").append(type)
            .append("\" Target=\"").append(target).append("\"/>")
    }
    sb.append("</Relationships>")
    return sb.toString()
}

private const val REL_WORKSHEET = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet"
private const val REL_STYLES = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles"
private const val REL_DRAWING = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/drawing"
private const val REL_CHART = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/chart"
private const val REL_OFFICE_DOCUMENT =
    "http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument"

private const val CT_WORKBOOK = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"
private const val CT_STYLES = "application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"
private const val CT_WORKSHEET = "application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"
private const val CT_DRAWING = "application/vnd.openxmlformats-officedocument.drawing+xml"
private const val CT_CHART = "application/vnd.openxmlformats-officedocument.drawingml.chart+xml"

private fun assembleWorkbook(
    summaryRows: List<String>,
    segmentRows: List<String>,
    segmentCharts: List<ChartSpec>,
    eventRows: List<String>,
    eventCharts: List<ChartSpec>
): ByteArray {
    val allCharts = segmentCharts + eventCharts
    // axId должен быть уникален только внутри своего chart-файла — используем индекс с запасом,
    // чтобы не пересекался catAx/valAx одной и той же диаграммы.
    val chartXmlByIndex = allCharts.mapIndexed { i, spec -> chartXml(spec, 100000 + i * 2, 100001 + i * 2) }

    val hasSegmentDrawing = segmentCharts.isNotEmpty()
    val hasEventDrawing = eventCharts.isNotEmpty()

    val entries = LinkedHashMap<String, String>()

    entries["[Content_Types].xml"] = buildContentTypesXml(hasSegmentDrawing, hasEventDrawing, allCharts.size)
    entries["_rels/.rels"] = relationshipsXml(listOf(REL_OFFICE_DOCUMENT to "xl/workbook.xml"))
    entries["xl/workbook.xml"] = buildWorkbookXml()
    entries["xl/_rels/workbook.xml.rels"] = relationshipsXml(
        listOf(
            REL_WORKSHEET to "worksheets/sheet1.xml",
            REL_WORKSHEET to "worksheets/sheet2.xml",
            REL_WORKSHEET to "worksheets/sheet3.xml",
            REL_STYLES to "styles.xml"
        )
    )
    entries["xl/styles.xml"] = STYLES_XML

    entries["xl/worksheets/sheet1.xml"] = sheetXml(summaryRows, drawingRelId = null)
    entries["xl/worksheets/sheet2.xml"] = sheetXml(segmentRows, drawingRelId = if (hasSegmentDrawing) "rId1" else null)
    entries["xl/worksheets/sheet3.xml"] = sheetXml(eventRows, drawingRelId = if (hasEventDrawing) "rId1" else null)

    if (hasSegmentDrawing) {
        entries["xl/worksheets/_rels/sheet2.xml.rels"] =
            relationshipsXml(listOf(REL_DRAWING to "../drawings/drawing1.xml"))
        entries["xl/drawings/drawing1.xml"] = drawingXml(segmentCharts.size, relIdOffset = 1)
        entries["xl/drawings/_rels/drawing1.xml.rels"] = relationshipsXml(
            (0 until segmentCharts.size).map { i -> REL_CHART to "../charts/chart${i + 1}.xml" }
        )
    }
    if (hasEventDrawing) {
        entries["xl/worksheets/_rels/sheet3.xml.rels"] =
            relationshipsXml(listOf(REL_DRAWING to "../drawings/drawing2.xml"))
        entries["xl/drawings/drawing2.xml"] = drawingXml(eventCharts.size, relIdOffset = 1)
        entries["xl/drawings/_rels/drawing2.xml.rels"] = relationshipsXml(
            (0 until eventCharts.size).map { i -> REL_CHART to "../charts/chart${segmentCharts.size + i + 1}.xml" }
        )
    }

    chartXmlByIndex.forEachIndexed { i, xml ->
        entries["xl/charts/chart${i + 1}.xml"] = xml
    }

    val buffer = ByteArrayOutputStream()
    ZipOutputStream(buffer).use { zip ->
        entries.forEach { (path, content) ->
            zip.putNextEntry(ZipEntry(path))
            zip.write(content.toByteArray(Charsets.UTF_8))
            zip.closeEntry()
        }
    }
    return buffer.toByteArray()
}

private fun buildContentTypesXml(hasSegmentDrawing: Boolean, hasEventDrawing: Boolean, chartCount: Int): String {
    val sb = StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>")
    sb.append("<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">")
    sb.append("<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>")
    sb.append("<Default Extension=\"xml\" ContentType=\"application/xml\"/>")
    sb.append("<Override PartName=\"/xl/workbook.xml\" ContentType=\"$CT_WORKBOOK\"/>")
    sb.append("<Override PartName=\"/xl/styles.xml\" ContentType=\"$CT_STYLES\"/>")
    for (i in 1..3) {
        sb.append("<Override PartName=\"/xl/worksheets/sheet$i.xml\" ContentType=\"$CT_WORKSHEET\"/>")
    }
    if (hasSegmentDrawing) sb.append("<Override PartName=\"/xl/drawings/drawing1.xml\" ContentType=\"$CT_DRAWING\"/>")
    if (hasEventDrawing) sb.append("<Override PartName=\"/xl/drawings/drawing2.xml\" ContentType=\"$CT_DRAWING\"/>")
    for (i in 1..chartCount) {
        sb.append("<Override PartName=\"/xl/charts/chart$i.xml\" ContentType=\"$CT_CHART\"/>")
    }
    sb.append("</Types>")
    return sb.toString()
}

private fun buildWorkbookXml(): String =
    "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
        "<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" " +
        "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">" +
        "<sheets>" +
        "<sheet name=\"" + xmlEscape(SUMMARY_SHEET) + "\" sheetId=\"1\" r:id=\"rId1\"/>" +
        "<sheet name=\"" + xmlEscape(SEGMENTS_SHEET) + "\" sheetId=\"2\" r:id=\"rId2\"/>" +
        "<sheet name=\"" + xmlEscape(EVENTS_SHEET) + "\" sheetId=\"3\" r:id=\"rId3\"/>" +
        "</sheets></workbook>"

private val STYLES_XML =
    "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
        "<styleSheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">" +
        "<fonts count=\"1\"><font><sz val=\"11\"/><name val=\"Calibri\"/></font></fonts>" +
        "<fills count=\"1\"><fill><patternFill patternType=\"none\"/></fill></fills>" +
        "<borders count=\"1\"><border><left/><right/><top/><bottom/><diagonal/></border></borders>" +
        "<cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs>" +
        "<cellXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\"/></cellXfs>" +
        "</styleSheet>"
