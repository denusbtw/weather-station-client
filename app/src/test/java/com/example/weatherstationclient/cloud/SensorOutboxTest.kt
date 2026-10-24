package com.example.weatherstationclient.cloud

import java.io.IOException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.fail
import org.junit.Test

class SensorOutboxTest {
    private class MemoryStore : PendingSensorStore {
        var saved = emptyList<PendingSensorRecord>()
        var failWrites = false
        override fun load() = saved
        override fun save(records: List<PendingSensorRecord>) {
            if (failWrites) throw IOException("disk unavailable")
            saved = records.toList()
        }
    }

    private fun reading(id: String, time: Long = 123L) = PendingSensorRecord(
        id, SensorData(deviceId = "device", timestamp = time, value = 1007.5f)
    )

    @Test
    fun pendingMeasurementSurvivesRestartWithOriginalIdAndTimestamp() {
        val store = MemoryStore()
        SensorOutbox(store).enqueue(reading("first"))
        val restored = SensorOutbox(store)
        assertEquals("first", restored.peek()?.recordId)
        assertEquals(123L, restored.peek()?.payload?.timestamp)
    }

    @Test
    fun acknowledgementDoesNotDropANewMeasurementQueuedDuringUpload() {
        val store = MemoryStore()
        val outbox = SensorOutbox(store)
        outbox.enqueue(reading("first"))
        val uploading = outbox.peek()!!
        outbox.enqueue(reading("second", 456L))
        outbox.acknowledge(uploading.recordId)
        assertEquals(listOf("second"), store.saved.map { it.recordId })
        assertEquals("second", SensorOutbox(store).peek()?.recordId)
    }

    @Test
    fun failedAcknowledgementPersistenceRetainsTheRecordForIdempotentRetry() {
        val store = MemoryStore()
        val outbox = SensorOutbox(store)
        outbox.enqueue(reading("same-put-path"))
        store.failWrites = true
        try {
            outbox.acknowledge("same-put-path")
            fail("Expected persistence failure")
        } catch (_: IOException) {
            assertEquals("same-put-path", outbox.peek()?.recordId)
            assertEquals("same-put-path", SensorOutbox(store).peek()?.recordId)
        }
        store.failWrites = false
        outbox.acknowledge("same-put-path")
        assertNull(SensorOutbox(store).peek())
    }

    @Test
    fun failedEnqueueIsNotReportedAsDurablyPending() {
        val store = MemoryStore().apply { failWrites = true }
        val outbox = SensorOutbox(store)
        try {
            outbox.enqueue(reading("unsaved"))
            fail("Expected persistence failure")
        } catch (_: IOException) {
            assertEquals(0, outbox.size)
            assertNull(SensorOutbox(store).peek())
        }
    }
}
