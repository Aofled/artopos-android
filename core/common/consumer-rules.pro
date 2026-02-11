# --- COMMON MODULE RULES ---

# Usually empty.
# This module contains helper functions and extensions.
# R8/ProGuard can safely rename and inline them without issues.

# --- EXCEPTION: Reflection ---
# If you add code that uses Reflection (e.g. Class.forName("...")),
# you must uncomment the line below to protect your classes.
#
# -keep class ru.createsmart.artopos.core.common.** { *; }
