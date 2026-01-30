# Room library already has basic rules, but we add extra safety for this module.

# 1. Protect Entity fields
# If R8 renames fields (e.g., "val title" -> "val a"), Room might fail to map
# database columns to these fields unless @ColumnInfo is used everywhere.
-keepclassmembers class * {
    @androidx.room.Entity <fields>;
}

# 2. Protect Database implementation
# Room uses Reflection to find the generated "_Impl" class at runtime.
# This ensures R8 doesn't remove the constructor.
-keepclassmembers class * extends androidx.room.RoomDatabase {
    <init>(...);
}
