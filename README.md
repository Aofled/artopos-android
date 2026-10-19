***

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
* **Data & Network:** Room, Paging 3, Retrofit, Kotlinx Serialization, Coil.
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
*   **Jetpack Compose** (Material 3, Edge-to-Edge, Immutable Collections).
*   **Type-Safe Navigation** (Jetpack Navigation Compose 2.8+ with Kotlin Serialization for compile-time route safety).
*   **Coroutines & Flow** (Heavy usage of `combine`, `stateIn`, `flatMapLatest`).
*   **Paging 3** (RemoteMediator, Room Integration).
*   **DataStore Preferences** (Async reactive user settings storage).

### Network, Data & ML
*   **Retrofit 2** + **Kotlin Serialization** (JSON parsing).
*   **OkHttp** (Custom Interceptors for aggressive caching).
*   **Room Database** (Local storage, Embedded entities, `@Relation`, Transactions).
*   **Coil** (Advanced image loading).
*   **Google ML Kit** (On-Device Translation for dynamic content).
*   **Zoomable** (`net.engawapg.lib:zoomable` for flawless image pan & zoom gestures).

### Build System & Quality
*   **Gradle Kotlin DSL** (KTS).
*   **Version Catalog** (`libs.versions.toml`).
*   **Convention Plugins** (Custom `build-logic` automating dependency management, Namespace generation, and ProGuard rules across 20+ modules).
*   **Detekt** (Static code analysis with auto-formatting).
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

* **Draw-phase shimmer:** The shimmer effect is built with `drawBehind` and reads its animation state only in the Draw phase, so the animation does not trigger recomposition or re-layout of list items.
* **Gesture-state tracking:** `derivedStateOf` is used to observe scroll and pinch-to-zoom (`zoomState`) thresholds, so recomposition happens only when a threshold is crossed rather than on every gesture frame.
* **Layout-shift prevention:** `LazyVerticalStaggeredGrid` reserves space for placeholders using aspect ratios provided by the API, so items keep their size while images load asynchronously.
* **Baseline Profiles:** Baseline Profile rules (`baseline-prof.txt`) are generated with Macrobenchmark and bundled with the app. Cold start improved by ~30% in my measurements.
</details>

<details>
<summary><b>2. Hardware-Aware Image Decoding & Media</b></summary>
<br/>

* **RAM-based downsampling:** A custom `ImageRequest` interceptor caps the decode resolution by device RAM (e.g. 2048px below 2 GB, up to 8192px above 8 GB). This keeps zoomed images sharp on high-end devices and helps avoid `MAX_TEXTURE_SIZE` errors and `OutOfMemoryError` on budget ones.
* **Scoped Storage:** Artworks are saved through `MediaStore`. Scoped Storage is used on Android 10+ (API 29+), and the `WRITE_EXTERNAL_STORAGE` runtime permission is requested only on older versions.
* **Disk caching:** A dedicated `OkHttpClient` for Coil with a custom `NetworkInterceptor` enforces 7-day disk caching, overriding restrictive server `Cache-Control` headers.
</details>

<details>
<summary><b>3. On-Device Machine Learning (Google ML Kit)</b></summary>
<br/>

* **Offline translation:** Artwork descriptions are translated fully on-device with ML Kit, with no network round trip.
* **Progressive UI:** A two-step flow built on `withTimeoutOrNull`: if the ML model is slow to warm up, the UI first shows a fast dictionary-based translation and then switches to the ML result in the background.
* **Bounded concurrency:** The number of parallel ML Kit tasks is limited with `Dispatchers.IO.limitedParallelism` based on `ActivityManager.isLowRamDevice`, to reduce memory and CPU pressure on low-RAM devices.
</details>

<details>
<summary><b>4. Robust Offline-First & Architecture</b></summary>
<br/>

* **Database SSOT:** The UI strictly observes the local Room database (`Flow<PagingData>`), ensuring instant data display on launch.
* **Smart Catalog Synchronization (30-day In-App TTL):** Museum categories and filters are cached locally with a 30-day Time-To-Live (TTL) invalidation policy tracked via Jetpack DataStore. Instead of draining battery with periodic background workers for dormant users, the app evaluates cache freshness on startup with an abstracted `TimeProvider`. Updates execute inside an atomic SQLite `@Transaction` (clearing old records only after all parallel network requests succeed), preventing data loss or empty UI states on connection drops.
* **Memory Optimization:** Uses lightweight Room projections (`ArtworkFeedProjectionDBO`) to exclude heavy columns during feed scrolling, avoiding `CursorWindow` limits.
* **Strict API Contracts:** Enabled Kotlin `ExplicitApiMode.Strict` for pure JVM modules (`core:domain`, `core:model`) to prevent accidental exposure of internal classes and enforce robust architectural boundaries.
* **Pure Domain Error Handling:** Retrofit's `HttpException` and serialization errors are caught at the Data layer and mapped into a pure Kotlin `AppError` sealed class hierarchy, keeping the Presentation layer 100% framework-agnostic.
</details>

<details>
<summary><b>5. DevSecOps & CI/CD Pipelines</b></summary>
<br/>

* **Gradle Convention Plugins:** Automated dependency management, internal R8/ProGuard consumer-rules aggregation, and dynamic `namespace` generation across 20+ modules.
* **Convention Plugins:** Dependency management, R8/ProGuard consumer-rules aggregation, and `namespace` generation are shared across 20+ modules.
* **Two CI workflows:**
  * `ci_debug.yml`: runs Detekt and unit tests on pull requests.
  * `ci_release.yml`: semi-automated release pipeline triggered by Git tags.
* **Release artifact delivery:** Signed, obfuscated release bundles are not published as public artifacts. The pipeline delivers them through the Telegram Bot API and also produces password-protected ZIP backups.</details>

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
│   ├── designsystem    # Core UI tokens (Theme, Typography with Downloadable Fonts, Colors)
│   ├── ui-components   # Reusable UI widgets (Optimized Shimmers, Placeholders, Buttons)
│   ├── artwork-card    # Isolated UI component for rendering images with Coil and Retry logic
│   ├── translation     # Google ML Kit integration wrapper
│   ├── navigation      # Type-safe @Serializable routes for Compose Navigation
│   ├── image-loader    # Custom OkHttpClient provider for Coil disk caching
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
