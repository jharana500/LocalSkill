package com.example.localskill.model

data class NotificationModel(
    val id: String = "",
    val recipientId: String = "",
    val senderId: String = "",
    val type: String = "",
    val relatedEntityType: String = "",
    val relatedEntityId: String = "",
    val secondaryEntityId: String = "",
    val isRead: Boolean = false,
    val createdAt: Long = 0L
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "recipientId" to recipientId,
        "senderId" to senderId,
        "type" to type,
        "relatedEntityType" to relatedEntityType,
        "relatedEntityId" to relatedEntityId,
        "secondaryEntityId" to secondaryEntityId,
        "isRead" to isRead,
        "createdAt" to createdAt
    )

    val typedType: NotificationType?
        get() = NotificationType.from(type)

    val typedEntityType: NotificationEntityType?
        get() = NotificationEntityType.from(relatedEntityType)
}
