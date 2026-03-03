# Usually empty if using standard String routes.
# R8 handles string literals automatically.

# If you pass custom objects between screens (e.g. data class UserArgs : Parcelable),
# you MUST keep their names and fields. R8 might rename them, breaking argument restoration.
#
# -keepnames class ru.createsmart.artopos.core.navigation.** { *; }

# --- EXCEPTION: Type-Safe Navigation (Kotlin Serialization) ---
# If using @Serializable classes as routes (Navigation 2.8+),
# keep the @Serializable annotation to ensure correct parsing.
#
# -keepattributes RuntimeVisibleAnnotations
# -keepclassmembers class * {
#     @kotlinx.serialization.Serializable *;
# }
