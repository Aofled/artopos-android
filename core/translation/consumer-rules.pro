# --- ML KIT TRANSLATION RULES ---

# Google ML Kit uses Reflection and Native Code (C++).
# These rules ensure R8 doesn't break model loading or JNI calls.

# 1. Keep ML Kit entry points
# This prevents R8 from removing classes that are called from native libraries.
-keep class com.google.mlkit.** { *; }
-keep class com.google.android.gms.** { *; }

# 2. Keep TensorFlow Lite (Used internally by ML Kit)
# If TFLite classes are renamed, the model interpreter will crash.
-keep class org.tensorflow.** { *; }

# 3. Keep Protobuf messages (Used for model configuration)
# Protobuf relies heavily on generated classes that are accessed dynamically.
-keepclassmembers class * extends com.google.protobuf.GeneratedMessageLite {
  <fields>;
}

# 4. Keep Metadata for Native Methods
# Essential for JNI communication between Java/Kotlin and C++.
-keepattributes EnclosingMethod
-keepattributes InnerClasses
-keepattributes Signature
