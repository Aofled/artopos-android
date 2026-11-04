# Allow R8 to obfuscate and shrink Entity fields,
# since mapping is based on string-based @ColumnInfo annotations
-keepclassmembers,allowobfuscation,allowshrinking class * {
    @androidx.room.Entity <fields>;
}

# Protecting the constructor of generated Room implementations
-keepclassmembers class * extends androidx.room.RoomDatabase {
    <init>(...);
}
