# --- UI MODULE RULES ---

# Usually empty for Jetpack Compose.
# Composable functions are compiled into static code, so R8 obfuscation is safe.

# --- NAVIGATION WARNING ---
# If you pass complex objects (Parcelable/Serializable) as Navigation Arguments,
# you MUST protect them from renaming, otherwise restoration might fail.
#
# -keepnames class ru.createsmart.artopos.core.ui.model.** { *; }
