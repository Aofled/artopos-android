# Rules for the main App that uses this :core:model module.
# This file tells the App's R8/ProGuard not to change (obfuscate) our data classes.

# This is important for JSON libraries (like Moshi, Gson, Kotlinx Serialization)
# that need original field names to work correctly in Release builds.
