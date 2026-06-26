# LocalSkill

## Local Setup

This project uses Firebase and local secret files.

Required local files:

- `app/google-services.json`
- `secrets.properties` if API keys or secret properties are needed
- `local.properties` for the Android SDK path (created by Android Studio)

These files are ignored by Git and should not be pushed to GitHub.

Use the provided example files as templates:

- `.env.example`
- `secrets.properties.example`

If you need Firebase configuration, place your own `google-services.json` into `app/google-services.json`.
