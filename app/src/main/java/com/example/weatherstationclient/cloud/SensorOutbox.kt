package com.example.weatherstationclient.cloud

data class PendingSensorRecord(val recordId: String, val payload: SensorData)

interface PendingSensorStore {
    fun load(): List<PendingSensorRecord>
    fun save(records: List<PendingSensorRecord>)
}

/** Persist first, then publish in-memory changes. Called from background dispatchers. */
class SensorOutbox(private val store: PendingSensorStore) {
    private var records = store.load().toList()

    val size: Int
        @Synchronized get() = records.size

    @Synchronized
    fun enqueue(record: PendingSensorRecord) {
        val next = records + record
        store.save(next)
        records = next
    }

    @Synchronized
    fun peek(): PendingSensorRecord? = records.firstOrNull()

    @Synchronized
    fun acknowledge(recordId: String) {
        val next = records.filterNot { it.recordId == recordId }
        store.save(next)
        records = next
    }
}
