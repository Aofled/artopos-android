# ==============================================================================
# ML KIT TRANSLATION CONSUMER RULES
# ==============================================================================

# Note: The 'com.google.mlkit:translate' library already contains built-in
# consumer-rules within its AAR, which automatically protect TFLite, JNI, and Protobuf.
# We do NOT need to declare global '-keep class com.google.** { *; }' rules.

# 1. Protecting the data source's public interface from deletion,
# in case it is injected via reflection or Hilt in other modules.
-keep,allowobfuscation,allowshrinking class ru.createsmart.artopos.core.translation.MLKitTranslationDataSource {
    public <init>(...);
    public <methods>;
}

# 2. Attributes required for the proper functioning of JNI and Play Services Tasks (.await())
# Preserving type signatures for Kotlin Coroutines and the Google Tasks API:
-keepattributes Signature
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# 3. Suppressing false R8 warnings when optimizing Play Services
-dontwarn com.google.android.gms.**
-dontwarn com.google.mlkit.**
