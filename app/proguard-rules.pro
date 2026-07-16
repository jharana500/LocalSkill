# LocalSkill release shrinker notes
#
# Release minification remains disabled until assembleRelease and regression
# testing verify R8 behavior. These rules are intentionally narrow and avoid
# blanket keeps such as `-keep class ** { *; }`.

# Firebase Realtime Database deserializes model classes through generated/reflected
# no-arg constructors and public properties.
-keepclassmembers class com.example.localskill.model.** {
    public <init>();
}

# Preserve generic signatures used by Firebase/Tasks callbacks and Kotlin metadata
# needed by some reflection-based libraries.
-keepattributes Signature,*Annotation*
