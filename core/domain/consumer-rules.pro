# --- DOMAIN MODULE RULES ---

# Usually empty.
# This module contains pure business logic (UseCases) and Interfaces.
# R8 handles standard Kotlin code perfectly without extra rules.

# NOTE:
# If you use Domain Models directly in UI for JSON parsing (not recommended),
# you might need to keep them.
# -keep class ru.createsmart.artopos.core.domain.model.** { *; }
