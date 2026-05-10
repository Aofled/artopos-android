# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# --- GENERAL ---

# Keep stack trace data for Crashlytics (Line numbers, files)
-keepattributes SourceFile,LineNumberTable

# Required for libraries using Reflection (Dagger, Retrofit, etc.)
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes Exceptions, InnerClasses

# CRITICAL: Keep Generic types (List<User> -> List<User>)
# Fixes crashes in Retrofit/Gson/Moshi
-keepattributes Signature

# --- KOTLIN ---
-keepclassmembers class ** {
    @org.jetbrains.annotations.Nullable *;
    @org.jetbrains.annotations.NotNull *;
}

# --- KOTLIN SERIALIZATION ---
# Prevent renaming fields in JSON models
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}

# --- RETROFIT ---
# Keep annotations for HTTP methods (@GET, @POST)
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault

# --- NAVIGATION COMPOSE ---
# Prevent crashes when passing arguments between screens
-keepnames class * implements android.os.Parcelable
-keepnames class * implements java.io.Serializable

# --- DEBUGGING SUPPORT (OPTIONAL) ---
# Keep resource IDs. Usually not needed for Release.
-keep class **.R$* {
    <fields>;
}

# Number of compiler cycles
-optimizationpasses 5

# logs
# -printconfiguration build/outputs/mapping/release/full_config.txt
