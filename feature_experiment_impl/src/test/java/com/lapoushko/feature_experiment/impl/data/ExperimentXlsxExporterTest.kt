package com.lapoushko.feature_experiment.impl.data

import com.lapoushko.feature_experiment.api.domain.ExperimentCorrelation
import com.lapoushko.feature_experiment.api.domain.ExperimentEvent
import com.lapoushko.feature_experiment.api.domain.ExperimentSegment
import com.lapoushko.feature_experiment.api.domain.ExperimentSession
import com.lapoushko.feature_experiment.api.domain.ExperimentSummary
import com.lapoushko.feature_experiment.api.domain.PathLossModel
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.util.zip.ZipInputStream
import javax.xml.parsers.DocumentBuilderFactory

class ExperimentXlsxExporterTest {

    private val session = ExperimentSession(
        id = 1,
        title = "Тест «замер» с кириллицей и, запятой",
        startedAtEpochMillis = 0L,
        endedAtEpochMillis = 60_000L
    )

    private fun pingsAndCheckpoints(): List<ExperimentEvent> = listOf(
        ExperimentEvent.Checkpoint(timestampEpochMillis = 0, label = "5 м", rssiDbm = -40, distanceMeters = 5.0),
        ExperimentEvent.Ping(timestampEpochMillis = 700, success = true, rttMillis = 30, rssiDbm = -42),
        ExperimentEvent.Ping(timestampEpochMillis = 1400, success = false, rttMillis = 5000, rssiDbm = -45),
        ExperimentEvent.Checkpoint(timestampEpochMillis = 2000, label = "10 м, за углом", rssiDbm = -60, distanceMeters = 10.0),
        ExperimentEvent.Ping(timestampEpochMillis = 2700, success = true, rttMillis = 40, rssiDbm = -62),
        ExperimentEvent.Ping(timestampEpochMillis = 3400, success = true, rttMillis = 35, rssiDbm = null)
    )

    /** Собрано вручную по фикстуре [pingsAndCheckpoints] — тест проверяет экспортёр, а не агрегацию. */
    private fun summaryWithData() = ExperimentSummary(
        segments = listOf(
            ExperimentSegment(
                label = "5 м",
                startedAtEpochMillis = 0,
                distanceMeters = 5.0,
                pingCount = 2,
                successCount = 1,
                averageRttMillis = 2515,
                averageRssiDbm = -43,
                minRssiDbm = -45,
                maxRssiDbm = -42
            ),
            ExperimentSegment(
                label = "10 м, за углом",
                startedAtEpochMillis = 2000,
                distanceMeters = 10.0,
                pingCount = 2,
                successCount = 2,
                averageRttMillis = 37,
                averageRssiDbm = -62,
                minRssiDbm = -62,
                maxRssiDbm = -62
            )
        ),
        totalPings = 4,
        totalSuccess = 3,
        correlation = ExperimentCorrelation(rssiVsRtt = -0.5, rssiVsLoss = 0.5),
        pathLossModel = PathLossModel(pathLossExponent = 2.1, rssiAt1mDbm = -30.0, rSquared = 0.9, pointCount = 2)
    )

    private fun emptySummary() = ExperimentSummary(segments = emptyList(), totalPings = 0, totalSuccess = 0)

    @Test
    fun `produces a valid zip with well-formed xml parts and expected charts`() {
        val events = pingsAndCheckpoints()
        val summary = summaryWithData()

        val bytes = buildExperimentXlsx(session, events, summary)
        assertTrue("xlsx should not be empty", bytes.isNotEmpty())

        val entries = mutableMapOf<String, ByteArray>()
        ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                entries[entry.name] = zip.readBytes()
                entry = zip.nextEntry
            }
        }

        val requiredParts = listOf(
            "[Content_Types].xml",
            "_rels/.rels",
            "xl/workbook.xml",
            "xl/_rels/workbook.xml.rels",
            "xl/styles.xml",
            "xl/worksheets/sheet1.xml",
            "xl/worksheets/sheet2.xml",
            "xl/worksheets/sheet3.xml",
            "xl/worksheets/_rels/sheet2.xml.rels",
            "xl/worksheets/_rels/sheet3.xml.rels",
            "xl/drawings/drawing1.xml",
            "xl/drawings/drawing2.xml",
            "xl/drawings/_rels/drawing1.xml.rels",
            "xl/drawings/_rels/drawing2.xml.rels",
            "xl/charts/chart1.xml",
            "xl/charts/chart2.xml",
            "xl/charts/chart3.xml",
            "xl/charts/chart4.xml",
            "xl/charts/chart5.xml"
        )
        requiredParts.forEach { part ->
            assertTrue("missing part: $part", entries.containsKey(part))
        }

        val builder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        entries.forEach { (name, content) ->
            if (name.endsWith(".xml")) {
                runCatching { builder.parse(ByteArrayInputStream(content)) }
                    .onFailure { error -> throw AssertionError("malformed xml in $name: ${error.message}", error) }
            }
        }

        val segmentSheet = String(entries.getValue("xl/worksheets/sheet2.xml"), Charsets.UTF_8)
        assertTrue("cyrillic label lost in sheet2", segmentSheet.contains("5 м"))
        assertTrue("cyrillic label lost in sheet2", segmentSheet.contains("10 м, за углом"))
    }

    @Test
    fun `handles empty session without crashing`() {
        val bytes = buildExperimentXlsx(session, emptyList(), emptySummary())
        assertTrue(bytes.isNotEmpty())

        val entries = mutableSetOf<String>()
        ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                entries += entry.name
                entry = zip.nextEntry
            }
        }
        assertTrue(entries.contains("xl/worksheets/sheet1.xml"))
        assertTrue(entries.contains("xl/worksheets/sheet2.xml"))
        assertTrue(entries.contains("xl/worksheets/sheet3.xml"))
        assertTrue("should not reference a non-existent drawing", !entries.contains("xl/drawings/drawing1.xml"))
    }
}
