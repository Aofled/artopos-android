# Artopos 🎨

[![Android CI](https://github.com/Aofled/artopos-android/actions/workflows/ci_debug.yml/badge.svg)](https://github.com/Aofled/artopos-android/actions/workflows/ci_debug.yml)
[![Android CI](https://github.com/Aofled/artopos-android/actions/workflows/ci_release.yml/badge.svg)](https://github.com/Aofled/artopos-android/actions/workflows/ci_release.yml)
[![Kotlin 2.0](https://img.shields.io/badge/Kotlin-2.0%2B-blue.svg)](https://kotlinlang.org)
[![Min API 26](https://img.shields.io/badge/Min%20API-26-green.svg)](https://apilevels.com/)

**Artopos** is a modern, offline-first Android application for exploring the world's finest masterpieces using the public [Harvard Art Museums API](https://github.com/harvardartmuseums/api-docs).

Built as a showcase of modern Android development, this app demonstrates strict architecture, advanced UI performance optimizations, and on-device Machine Learning integration.

<div align="center">
  <img src="https://github.com/user-attachments/assets/be2e6cbe-a340-4178-909b-6cd1c088efda" width="19%" />
  <img src="https://github.com/user-attachments/assets/a58cfabc-01d2-4005-b386-0168e9ea044e" width="19%" />
  <img src="https://github.com/user-attachments/assets/4fa16b1b-3f6d-470f-ab02-503c7d393959" width="19%" />
  <img src="https://github.com/user-attachments/assets/3153abd0-cd5a-4941-b4f3-6be1143ca015" width="19%" />
  <img src="https://github.com/user-attachments/assets/2e06899d-9980-4f22-8ade-222e3376b54d" width="19%" />
</div>

<br/>

<div align="center">
  <a href="https://play.google.com/store/apps/details?id=ru.createsmart.artopos">
    <img alt="Get it on Google Play" src="https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png" height="60"/>
  </a>
</div>

---

## 🛠 Tech Stack

**TL;DR:**
* **Architecture:** Clean Architecture, Modular Monolith (20+ modules), Strict MVI.
* **UI:** Jetpack Compose (Material 3, Edge-to-Edge), Navigation Compose (Type-Safe).
* **Data & Network:** Room, Paging 3, Retrofit, Kotlinx Serialization, Coil 3.
* **Advanced:** Google ML Kit, Baseline Profiles, Macrobenchmark, GitHub Actions.

<details>
<summary><b>View Detailed Tech Stack & Libraries</b></summary>
<br/>

### Architecture & Design Patterns
*   **Clean Architecture** (Strict separation of Data / Domain / Presentation layers).
*   **Modular Monolith** (Feature-based + Layer-based modules to optimize build times and enforce encapsulation).
*   **Offline-First** (Database as a Single Source of Truth).
*   **MVI** (Unidirectional Data Flow with `Sealed Interfaces` for Intents and UI States).
*   **Dependency Injection** (Hilt / Dagger).

### Modern Android & Compose
*   **Kotlin 2.0+** (K2 Compiler).
*   **Jetpack Compose** (Material 3, Edge-to-Edge, Immutable Collections, Modifier.Node API).
*   **Type-Safe Navigation** (Jetpack Navigation Compose with Kotlin Serialization for compile-time route safety).
*   **Coroutines & Flow** (Heavy usage of `combine`, `stateIn`, `flatMapLatest`).
*   **Paging 3** (RemoteMediator, Room Integration).
*   **DataStore Preferences** (Async reactive user settings storage with corruption recovery).

### Network, Data & ML
*   **Retrofit 2** + **Kotlin Serialization** (Resilient JSON parsing with default coercing).
*   **OkHttp** (Custom Interceptors for forced cache-control).
*   **Room Database** (Normalized schema, Cascade Foreign Keys, Composite Indexes, Transactions).
*   **Coil 3** (Custom DiskCache SSOT, hardware-aware downsampling).
*   **Google ML Kit** (On-Device Translation for dynamic content).
*   **Zoomable** (`net.engawapg.lib:zoomable` for image pan & zoom gestures).

### Build System & Quality
*   **Gradle Kotlin DSL** (KTS) with **Project Isolation** support.
*   **Version Catalog** (`libs.versions.toml`).
*   **Convention Plugins** (Custom `build-logic` automating dependency management, Namespace generation, and ProGuard rules).
*   **Detekt** (Static code analysis with strict CI verification).
*   **R8/ProGuard** (Optimized release builds with consumer-rules aggregation).
*   **Baseline Profiles & Macrobenchmark** (AOT compilation and UI performance metrics).
*   **Unit Testing & Test Harness** (Dedicated `:core:testing` module, JUnit 4, Mockk, Coroutines Test, Turbine).
*   **GitHub Actions CI** (Automated pipeline for static analysis, unit testing, and secure APK building).
</details>

---

## ✨ Engineering Highlights

<details>
<summary><b>1. Compose Performance & "Jump-Free" Grid</b></summary>
<br/>

* **Modern Modifier.Node Shimmer:** The shimmer animation is built using the low-level `Modifier.Node` API (`DrawModifierNode`). It executes strictly in the Draw phase without allocations or triggering recomposition passes, eliminating Garbage Collector overhead during high-speed scrolling.
* **Gesture-state Tracking:** `derivedStateOf` is used to observe scroll and pinch-to-zoom (`zoomState`) thresholds, ensuring recomposition triggers only when a logical state boundary is crossed rather than on every frame.
* **Layout-shift Prevention:** `LazyVerticalStaggeredGrid` reserves exact space for placeholders using aspect ratios provided by the API, preventing disruptive layout jumps while images load asynchronously.
* **Centralized UI Event Coordination:** Features a single top-level `SnackbarHost` synchronized with `BottomBar` and `FloatingActionButton` offsets via frame-based animations (`FastOutSlowInEasing`). This ensures snackbars smoothly adapt to navigation visibility and persist across screen transitions without being destroyed.
* **Baseline Profiles:** Critical user journeys (app startup, feed rendering, and scroll paths) are captured via Macrobenchmark into pre-compiled AOT binary profiles (`baseline-prof.txt`). This mitigates Android Runtime (ART) JIT compilation overhead, ensuring smooth frame delivery from the very first launch.
</details>

<details>
<summary><b>2. Hardware-Aware Image Decoding & Media</b></summary>
<br/>

* **RAM-Based Downsampling:** Decoding resolution is dynamically capped based on the physical device RAM (`ActivityManager.isLowRamDevice` and total RAM thresholds, scaling from 4000px up to 10000px). This keeps zoomed artworks crisp on flagship displays while avoiding GPU `GL_MAX_TEXTURE_SIZE` limits and `OutOfMemoryError` on budget hardware.
* **Transaction-Safe Scoped Storage:** Artworks are saved via `MediaStore`. Android 10+ (API 29+) uses Scoped Storage with `IS_PENDING` flags, while legacy versions request runtime permissions. Any I/O error or coroutine cancellation automatically deletes incomplete entries, preventing orphaned 0-byte files in the gallery.
* **Single Disk Cache Source of Truth:** A dedicated 512 MB Coil `DiskCache` acts as the exclusive on-disk cache for media, eliminating duplicate file storage between OkHttp and Coil and providing instant cache-size calculation.
</details>

<details>
<summary><b>3. On-Device Machine Learning (Google ML Kit)</b></summary>
<br/>

* **Offline Translation:** Artwork descriptions and metadata are translated entirely on-device via Google ML Kit, requiring no external cloud roundtrips.
* **Progressive UI Pattern:** A dual-phase loading strategy powered by `withTimeoutOrNull`: the UI instantly renders cached dictionary translations; if the on-device ML model takes longer than 300ms to warm up, an asynchronous background task seamlessly upgrades the text once complete.
* **Bounded Concurrency:** ML Kit operations are strictly bounded via `Dispatchers.IO.limitedParallelism` tailored to `ActivityManager.isLowRamDevice`, preventing CPU saturation and native C++ memory exhaustion on constrained hardware.
</details>

<details>
<summary><b>4. Robust Offline-First Architecture</b></summary>
<br/>

* **Normalized Schema & Clean SSOT:** The Discover feed utilizes a normalized junction table (`discover_feed`) linked to the primary `artworks` catalog via `ForeignKey(onDelete = CASCADE)`. This completely decouples feed pagination from the details screen, preventing stale items from leaking into the feed during concurrent background refreshes.
* **B-Tree Database Indexing:** Room tables are equipped with composite indexes on `(sorting_index)` and `(type, count)`, replacing slow Full Table Scans with logarithmic index searches.
* **DataStore Corruption Recovery:** Configured with `ReplaceFileCorruptionHandler` and explicit `IOException` fallbacks, safeguarding the app against fatal startup crash loops caused by corrupted preference files.
* **Smart Catalog Synchronization (30-day TTL):** Museum categories and filters are cached locally with a 30-day Time-To-Live policy evaluated on startup via an abstracted `TimeProvider`. Updates execute inside an atomic SQLite `@Transaction`, ensuring UI state consistency during connection drops.
* **Lightweight Cursor Projections:** Employs Room projections (`ArtworkFeedProjectionDBO`) to omit heavy columns during feed scrolling, staying well below SQLite `CursorWindow` memory limits.
* **Pure Domain Contracts:** Network and serialization errors are mapped at the Data layer into a pure Kotlin `AppError` hierarchy that preserves root causes (`cause: Throwable?`) for transparent production diagnostics.
</details>

<details>
<summary><b>5. DevSecOps & CI/CD Pipelines</b></summary>
<br/>

* **Convention Plugins & Project Isolation:** Modular build logic using Gradle Composite Builds (`build-logic`), ensuring compatibility with Gradle Project Isolation and Configuration Caching across 19 modules.
* **Automated CI Workflows:**
  * `ci_debug.yml`: Runs Detekt static analysis, Android Lint, and Unit Tests on pull requests with auto-correction disabled to strictly enforce style compliance.
  * `ci_release.yml`: Automated release pipeline triggered by Git tags that produces signed, obfuscated AAB bundles, packages them into encrypted archives, and delivers artifacts directly via the Telegram Bot API.
</details>

---

## 🏗 Project Structure

<details>
<summary><b>View Folder Tree (Modular Monolith)</b></summary>

```text
root
├── app                 # Application entry point, NavHost, Edge-to-Edge setup, Hilt DI assembly
├── build-logic         # Custom Gradle Convention Plugins
├── benchmark           # Macrobenchmark UI Performance Tests & Baseline Profile Generator
├── core
│   ├── model           # Domain models, Enums, AppErrors (Pure Kotlin, no Android dependencies)
│   ├── network         # Retrofit API interfaces, Serialization DTOs, Auth Interceptors
│   ├── database        # Room Database, DBOs, Projections, DAOs, TypeConverters
│   ├── data            # Repositories, Paging RemoteMediator, Domain-to-Data Mappers
│   ├── domain          # UseCases, Repository Interfaces (Strict Business Logic)
│   ├── datastore       # Jetpack DataStore Preferences for user settings
│   ├── designsystem    # Core UI tokens (Theme, Typography with Google Fonts, Colors)
│   ├── ui-components   # Reusable UI widgets (Shimmers, ArtworkCard, Placeholders, Buttons)
│   ├── translation     # Google ML Kit integration wrapper with thread limiting
│   ├── navigation      # Type-safe @Serializable routes for Compose Navigation
│   ├── image-loader    # Custom OkHttpClient and Coil disk caching integration
│   ├── testing         # Shared testing infrastructure (MainDispatcherRule, Test Dispatchers)
│   └── common          # General Utilities (NetworkMonitor, Locale Helper, Dictionaries)
└── feature
    ├── discover        # "Discover" Infinite Feed screen with Paging 3 and Filter BottomSheet
    ├── details         # Deep-dive screen with full description, Zoomable gallery, and ML translation
    ├── favorites       # Local saved artworks screen (Offline SSOT)
    └── settings        # App preferences (Theme, Language, Cache clearing)
```
</details>

## 🚀 How to Run
### 1. Obtain an API Key (Required)
Artopos uses the public Harvard Art Museums API. You need a free key to prevent `401 Unauthorized` errors.

1.  Visit the [Harvard Art Museums API documentation](https://github.com/harvardartmuseums/api-docs).
2.  Locate the "Access to the API" section (Google Form link). Fill it out, and you will receive the key instantly via email.

### 2. Clone the Repository
```bash  
git clone https://github.com/Aofled/artopos-android.git
```  

### 3. Configure the Project
Create a `local.properties` file in the project root directory (if it wasn't created automatically) and add your key:

```properties  
# local.properties  
sdk.dir=/path/to/your/android/sdk
HARVARD_API_KEY=your-received-api-key-here
```  
> **⚠️ Important note regarding memory usage:**  
> By default, the `gradle.properties` file allocates up to **12 GB of RAM** for build processes.
> If you are working on a machine with limited memory and experience OS freezes, please reduce the `-Xmx` arguments in `gradle.properties`.

### 4. Verify & Run
Run static analysis to ensure the environment is set up correctly:
```bash
./gradlew detektAll
```
Select the `app` configuration in Android Studio and run it on an Emulator or Physical Device.

---  

## ⚖️ Attribution & License
Data and Images are provided by the **Harvard Art Museums API**.  
This application is a personal portfolio project and is not affiliated with or endorsed by Harvard University.

* API Documentation: [Harvard Art Museums API](https://github.com/harvardartmuseums/api-docs)
* All artwork images and metadata belong to their respective owners and the Harvard Art Museums.

*** 
