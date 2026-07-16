# Testing

## Local Unit Tests

Run:

```sh
./gradlew test
./gradlew testDebugUnitTest
```

Unit tests use fake repositories and deterministic coroutine dispatchers where applicable. They must not require production Firebase.

Current Phase 5 local coverage includes notification destination mapping, interview date/time formatting, and safe error mapping.

## Compose and Instrumented Tests

Run with a connected emulator/device:

```sh
./gradlew connectedDebugAndroidTest
```

If no device or emulator is available, mark this as not executed rather than passed.

## Build and Lint

```sh
./gradlew assembleDebug
./gradlew lintDebug
./gradlew assembleRelease
```

If dependency download or SDK infrastructure fails, record the exact command and error.
