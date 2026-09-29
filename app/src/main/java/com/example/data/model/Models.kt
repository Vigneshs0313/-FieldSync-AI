package com.example.data.model

data class TechnicianUser(
    val id: String,
    val name: String,
    val role: String,
    val avatarInitials: String
)

enum class NetworkStatus(val label: String) {
    ONLINE("ONLINE"),
    OFFLINE("OFFLINE"),
    SLOW_NETWORK("SLOW 3G"),
    SYNCING("SYNCING")
}

enum class ItemStatus(val label: String) {
    PASS("Pass"),
    FAIL("Fail"),
    NA("N/A"),
    UNCHECKED("Unchecked")
}

enum class Severity(val label: String) {
    NONE("Normal"),
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High"),
    CRITICAL("Critical")
}

enum class SyncStatus {
    PENDING,
    UPLOADING,
    SYNCED,
    FAILED
}

enum class ConflictStatus {
    UNRESOLVED,
    RESOLVED_KEEP_A,
    RESOLVED_KEEP_B,
    RESOLVED_MERGED
}

enum class ConflictResolution {
    KEEP_A,
    KEEP_B,
    MERGE
}
