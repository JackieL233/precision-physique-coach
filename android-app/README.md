# I Want to be an IFBB PRO Android App

This is a native Android companion app for the `I Want to be an IFBB PRO` skill. It packages the skill assets inside the APK and lets you call an AI API for training, nutrition, visual form analysis, food-photo nutrition estimates, and linked check-ins.

## What is included

- Kotlin + Jetpack Compose Android app.
- Bundled skill assets in `app/src/main/assets/skill/`.
- Coach modes for new plans, check-ins, training visual analysis, food visual analysis, linked training + nutrition analysis, existing plan audit, and evidence explanations.
- Configurable API key, base URL, and model from the app UI.
- OpenAI Responses API request shape with text and photos.
- Photo picker support for exercise frames, equipment photos, food photos, labels, and menus.

## Build

Use Android Studio with JDK 17 and a normal Android SDK installation.

1. Open `android-app/` in Android Studio.
2. Let Android Studio sync Gradle.
3. Build and run the `app` configuration on an emulator or phone.

This workspace currently does not include a local JDK/Android SDK, so APK compilation is expected to happen in Android Studio or another Android build machine.

## API setup

Open the app and fill in:

- `API key`: your provider key. It is stored locally in Android SharedPreferences and is not committed to source.
- `base URL`: default is `https://api.openai.com/v1`.
- `model`: default is `gpt-4.1-mini`; you can change this to any model your API provider supports.

The network client posts to:

```text
{base URL}/responses
```

It sends:

- `instructions`: the bundled skill prompt and selected references.
- `input_text`: your profile, log, or question.
- `input_image`: optional photos encoded as data URLs.

## Skill assets

The app reads the bundled skill assets from:

```text
app/src/main/assets/skill/
```

When you update the upstream skill, copy the updated files into that assets folder before rebuilding the app.

## Safety

This app is coaching support, not medical care. Photo/video form checks and food-photo nutrition estimates are approximate. The app should not be used for diagnosis, physical therapy, clinical dietetics, drug protocols, or official contest rules.
