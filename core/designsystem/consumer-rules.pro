# --- UI MODULE RULES ---

# Usually empty for Jetpack Compose.
# R8/ProGuard handles Composable functions automatically.

# --- EXCEPTION: XML Layouts ---
# If you create Custom Views (class MyView : View) and use them in XML files
# (e.g. <ru.createsmart.artopos.ui.MyView />), you MUST keep them.
# R8 does not scan XML files for class names.

# -keep class ru.createsmart.artopos.core.designsystem.views.** { *; }
