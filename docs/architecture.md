# Architecture

LocalSkill uses a pragmatic single-module Android architecture:

- **View**: Jetpack Compose screens and navigation.
- **ViewModel**: exposes immutable `StateFlow` UI state and one-time event channels.
- **Repository**: owns Firebase reads/writes and validation around canonical platform events.
- **Model**: Firebase-compatible Kotlin data classes/enums used by repositories and UI.
- **Services/Utils**: Android services, validation, formatting, constants, and safe error mapping.

The project deliberately avoids Clean Architecture layers such as domain/usecase/entity modules. This keeps Phase 5 focused on platform integration and release quality without introducing mapping layers that do not add value for this codebase.

## Firebase Boundaries

Composables and ViewModels must not create or retain Firebase references. `AppContainer` constructs repositories once, and repository implementations interact with Firebase Auth, Realtime Database, Storage, and Messaging-related token storage.

## Notifications

In-app notifications are typed using `NotificationType` and `NotificationEntityType`. Records live at `notifications/{recipientId}/{notificationId}`. Notification IDs are deterministic from event keys to avoid duplicate notifications on retried writes.

Navigation from notifications is centralized in `NotificationDestinationMapper`; raw route strings are never trusted from Firebase.
