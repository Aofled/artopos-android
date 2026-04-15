# --- IMAGE LOADER MODULE RULES ---

# Usually empty.
# Coil library provides its own ProGuard rules automatically.

# --- EXCEPTION: Coil Interceptors / Custom Fetchers ---
# If you create custom classes that implement Coil interfaces
# (e.g. Interceptor, Fetcher, Decoder) and register them dynamically,
# R8 might remove them if not explicitly referenced.
#
# -keep class ru.createsmart.artopos.core.imageloader.interceptor.** { *; }

# --- EXCEPTION: Accessing Coil Cache via Reflection ---
# If your cache clearing logic uses Reflection to read Coil's internal DiskCache,
# you must keep the Coil cache classes (Not recommended, use public API instead).
#
# -keep class coil.disk.DiskCache { *; }
