# Phase 5 Remaining Infrastructure Limitations

- Remote push delivery requires a trusted backend or Firebase Cloud Function; the Android app only registers tokens, receives typed payloads, creates channels, and opens the app safely.
- Storage company ownership cannot be fully proven from Storage rules without trusted custom claims. Use Play Integrity/App Check plus backend-assigned claims for production.
- Connected Android tests require a device or emulator.
- App Check release enforcement must be configured in Firebase Console; debug tokens must stay local.
