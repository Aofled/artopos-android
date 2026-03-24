# --- DATASTORE MODULE RULES ---

# Usually empty.
# Jetpack DataStore (Preferences) works fine with R8 out of the box.

# --- EXCEPTION: Proto DataStore ---
# If you switch from Preferences to Proto DataStore (defining schema in .proto files),
# you MUST keep generated Protobuf classes.
#
# -keep class **.settings.SettingsOuterClass$* { *; }

# --- EXCEPTION: Custom Objects in Preferences ---
# If you save complex objects via JSON/Gson into a String preference,
# keep their class names to avoid serialization issues.
#
# -keep class ru.createsmart.artopos.core.datastore.model.** { *; }
