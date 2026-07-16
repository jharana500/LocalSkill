# Release Checklist

- Confirm branch is merged through `development`, not directly from a stale feature branch.
- Run `./gradlew clean`.
- Run `./gradlew assembleDebug`.
- Run `./gradlew test` and `./gradlew testDebugUnitTest`.
- Run `./gradlew lintDebug`.
- Run `./gradlew assembleRelease`.
- Run `./gradlew connectedDebugAndroidTest` on an emulator/device or mark not executed.
- Run `git diff --check`.
- Review Firebase Database and Storage rules before deployment.
- Confirm no `google-services.json`, keystores, server keys, service accounts, or signing passwords are committed.
- Confirm release signing is configured outside Git.
- Confirm App Check Play Integrity is configured before production enforcement.
- Confirm FCM remote push sender backend/Cloud Function is deployed before claiming push delivery support.
- Review minification/R8 behavior before enabling release minification.
