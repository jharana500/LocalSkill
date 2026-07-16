# Firebase Setup

## Required Services

Enable these services for package `com.example.localskill`:

1. Authentication: Email/password
2. Realtime Database
3. Firebase Storage
4. Firebase Cloud Messaging
5. App Check

Place the downloaded `google-services.json` at `app/google-services.json`. Do not commit it.

## Rules

Review and deploy:

```sh
firebase deploy --only database
firebase deploy --only storage
```

Database rules include ownership checks for applications, company indexes, notifications, and per-device token records.

## Firebase Messaging

The Android client registers FCM tokens and can receive typed payloads. Sending push notifications requires a trusted backend or Cloud Function using Firebase Admin SDK. Never include service account files, server keys, or Admin SDK credentials in the app.

Suggested Cloud Function responsibilities:

- Validate canonical event writes.
- Fan out remote pushes to active `userDevices` records.
- Avoid duplicate sends using the same deterministic notification event key.

## App Check

Development:

- Enable App Check debug provider in Firebase Console.
- Add debug tokens locally only; never commit them.

Release:

- Enable Play Integrity provider.
- Enforce App Check for Realtime Database, Storage, and any push-sending Cloud Functions.

## Storage Ownership Limitation

Current Storage rules restrict paths, content types, and file sizes. Full company ownership validation for company-scoped paths requires trusted custom claims such as `request.auth.token.companyId`, because Storage rules cannot query Realtime Database directly. Until that backend/custom-claims setup exists, repository validation remains necessary and this is not production-complete storage ownership enforcement.
