package com.example.localskill.model

enum class NotificationEntityType {
    APPLICATION,
    JOB,
    COMPANY,
    REPORT,
    VERIFICATION,
    USER;

    companion object {
        fun from(value: String): NotificationEntityType? = entries.firstOrNull { it.name == value }
    }
}
