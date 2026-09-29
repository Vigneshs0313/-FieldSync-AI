package com.example.crdt

import com.example.data.entity.ChecklistItemEntity
import com.example.data.entity.InspectionEntity

object CrdtEngine {

    /**
     * Parses a vector clock string like "nodeA:3,nodeB:1" into a Map<String, Long>.
     */
    fun parseVectorClock(clockStr: String): Map<String, Long> {
        val map = mutableMapOf<String, Long>()
        if (clockStr.isBlank()) return map
        clockStr.split(",").forEach { part ->
            val kv = part.split(":")
            if (kv.size == 2) {
                map[kv[0].trim()] = kv[1].trim().toLongOrNull() ?: 0L
            }
        }
        return map
    }

    /**
     * Serializes a vector clock map into string format "nodeA:3,nodeB:1".
     */
    fun serializeVectorClock(clockMap: Map<String, Long>): String {
        return clockMap.entries.joinToString(",") { "${it.key}:${it.value}" }
    }

    /**
     * Compares two vector clocks.
     * Returns:
     *  1 if clockA causally dominates clockB (A > B)
     * -1 if clockB causally dominates clockA (B > A)
     *  0 if concurrent / equal (conflict!)
     */
    fun compareVectorClocks(clockAStr: String, clockBStr: String): Int {
        val mapA = parseVectorClock(clockAStr)
        val mapB = parseVectorClock(clockBStr)
        val allKeys = (mapA.keys + mapB.keys).toSet()

        var aGreaterOrEqual = true
        var bGreaterOrEqual = true

        for (key in allKeys) {
            val valA = mapA[key] ?: 0L
            val valB = mapB[key] ?: 0L
            if (valA < valB) aGreaterOrEqual = false
            if (valB < valA) bGreaterOrEqual = false
        }

        return when {
            aGreaterOrEqual && !bGreaterOrEqual -> 1
            bGreaterOrEqual && !aGreaterOrEqual -> -1
            else -> 0 // Concurrent / conflict
        }
    }

    /**
     * LWW (Last-Write-Wins) Register with Lamport/Hybrid Logical Clock & Device ID tie-breaker.
     * Merges two ChecklistItemEntity updates using CRDT semantics.
     */
    fun mergeChecklistItem(local: ChecklistItemEntity, remote: ChecklistItemEntity): ChecklistItemEntity {
        val clockComparison = compareVectorClocks(local.vectorClock, remote.vectorClock)
        return when {
            clockComparison > 0 -> local
            clockComparison < 0 -> remote
            else -> {
                // Concurrent updates (fallback to LWW timestamp + author tie-breaker)
                if (local.updatedAt > remote.updatedAt) {
                    local
                } else if (remote.updatedAt > local.updatedAt) {
                    remote
                } else {
                    if (local.lastModifiedBy >= remote.lastModifiedBy) local else remote
                }
            }
        }
    }

    /**
     * Merges inspection entity metadata using LWW / CRDT vector clock semantics.
     */
    fun mergeInspection(local: InspectionEntity, remote: InspectionEntity): InspectionEntity {
        val clockComparison = compareVectorClocks(local.vectorClock, remote.vectorClock)
        return when {
            clockComparison > 0 -> local
            clockComparison < 0 -> remote
            else -> if (local.updatedAt >= remote.updatedAt) local else remote
        }
    }

    /**
     * Increments node counter in a vector clock string for local mutation.
     */
    fun incrementVectorClock(currentClock: String, nodeId: String): String {
        val map = parseVectorClock(currentClock).toMutableMap()
        val currentVal = map[nodeId] ?: 0L
        map[nodeId] = currentVal + 1L
        return serializeVectorClock(map)
    }
}
