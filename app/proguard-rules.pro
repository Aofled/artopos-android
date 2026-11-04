# ==============================================================================
# ARTOPOS RELEASE R8 OPTIMIZATION RULES
# ==============================================================================

# 1. Aggressive optimization (5 optimizer passes)
-optimizationpasses 5

# 2. Flattening of package structure (collapses paths in DEX, saving space in the String Pool)
-repackageclasses ""
-allowaccessmodification

# 3. Preserving metadata for readable crashes in Firebase Crashlytics
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# 4. Preserving signatures for generic types and Retrofit/Kotlin reflection
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# 5. Stripping out `android.util.Log` debug logs in release builds
# (Completely removes `Log.d/v/i` calls and their associated strings from the bytecode)
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
}

# 6. Kotlin Serialization Security (only what’s actually necessary)
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}
-keepclassmembers class * extends kotlinx.serialization.internal.GeneratedSerializer {
    <fields>;
}

# 7. Navigation Compose & Safe Args
-keepnames class * implements android.os.Parcelable
-keepnames class * implements java.io.Serializable

# 8. Suppressing false warnings from third-party libraries
-dontwarn javax.annotation.**
-dontwarn org.codehaus.mojo.animal_sniffer.**
