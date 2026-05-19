***

# Artopos 🎨

[![Android CI](https://github.com/Aofled/artopos-android/actions/workflows/ci_debug.yml/badge.svg)](https://github.com/Aofled/artopos-android/actions/workflows/ci_debug.yml)
[![Android CI](https://github.com/Aofled/artopos-android/actions/workflows/ci_release.yml/badge.svg)](https://github.com/Aofled/artopos-android/actions/workflows/ci_release.yml)
[![Kotlin 2.0](https://img.shields.io/badge/Kotlin-2.0%2B-blue.svg)](https://kotlinlang.org)
[![Min API 28](https://img.shields.io/badge/Min%20API-28-green.svg)](https://apilevels.com/)

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
* **Architecture:** Clean Architecture, Modular Monolith (15+ modules), Strict MVI.
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
*   **Convention Plugins** (Custom `build-logic` automating dependency management, Namespace generation, and ProGuard rules across 15+ modules).
*   **Detekt** (Static code analysis with auto-formatting).
*   **R8/ProGuard** (Optimized release builds with consumer-rules aggregation).
*   **Baseline Profiles & Macrobenchmark** (AOT compilation and UI performance metrics).
*   **Unit Testing** (JUnit 4, Mockk, Coroutines Test, Turbine).
*   **GitHub Actions CI** (Automated pipeline for static analysis, unit testing, and secure APK building).
</details>

---

## ✨ Engineering Highlights

<details>
<summary><b>1. Compose Performance & "Jump-Free" Grid</b></summary>
<br/>

* **Zero-Jank Shimmer Animations:** Custom shimmer effects utilize the `drawBehind` modifier to run exclusively in the Draw phase, bypassing expensive Composition/Layout phases and guaranteeing stable 60/120 FPS.
* **Smart Gesture Tracking:** Heavy usage of `derivedStateOf` to monitor scroll and pinch-to-zoom (`zoomState`) thresholds, completely eliminating unnecessary recompositions during continuous gestures.
* **CLS Prevention:** The `LazyVerticalStaggeredGrid` reserves exact space for placeholders using pre-calculated aspect ratios from the API, completely eliminating UI layout shifts as images load asynchronously.
* **Baseline Profiles:** Bundled with AOT compilation rules (`baseline-prof.txt`) generated via Macrobenchmark, reducing JIT-compilation jank and improving startup time by ~30%.
</details>

<details>
<summary><b>2. Hardware-Aware Image Decoding & Media</b></summary>
<br/>

* **Dynamic RAM-based Downsampling:** A custom `ImageRequest` interceptor calculates safe resolution caps (e.g., 2048px for <2GB RAM, up to 8192px for >8GB RAM flagships). This maximizes image crispness during zoom while preventing OpenGL `MAX_TEXTURE_SIZE` violations and `OutOfMemory` crashes on budget devices.
* **Scoped Storage & Legacy Permissions:** Safely integrates Android's `MediaStore` to save artworks. Automatically uses Scoped Storage on Android 10+ (API 29+) while managing `WRITE_EXTERNAL_STORAGE` runtime permissions gracefully on legacy devices.
* **Aggressive Caching:** Implements a dedicated `OkHttpClient` for Coil with a custom `NetworkInterceptor` to enforce 7-day disk caching, overriding restrictive server `Cache-Control` headers.
</details>

<details>
<summary><b>3. On-Device Machine Learning (Google ML Kit)</b></summary>
<br/>

* **Local Text Translation:** Translates complex art historical descriptions completely offline using heavy C++ ML models.
* **Progressive UI:** Implemented a two-step UX using Coroutines `withTimeoutOrNull`. If the heavy ML model takes too long to warm up, the UI instantly shows a "Fast" dictionary-based translation, dynamically updating to the "Deep" ML translation in the background without freezing the screen.
* **Hardware-Aware Concurrency:** Dynamically limits the number of parallel C++ ML Kit threads (`Dispatchers.IO.limitedParallelism`) based on the device's `ActivityManager.isLowRamDevice` to prevent system starvation.
</details>

<details>
<summary><b>4. Robust Offline-First & Architecture</b></summary>
<br/>

* **Database SSOT:** The UI strictly observes the local Room database (`Flow<PagingData>`), ensuring instant data display on launch.
* **Memory Optimization:** Uses lightweight Room projections (`ArtworkFeedProjectionDBO`) to exclude heavy columns during feed scrolling, avoiding `CursorWindow` limits.
* **Strict API Contracts:** Enabled Kotlin `ExplicitApiMode.Strict` for pure JVM modules (`core:domain`, `core:model`) to prevent accidental exposure of internal classes and enforce robust architectural boundaries.
* **Pure Domain Error Handling:** Retrofit's `HttpException` and serialization errors are caught at the Data layer and mapped into a pure Kotlin `AppError` sealed class hierarchy, keeping the Presentation layer 100% framework-agnostic.
</details>

<details>
<summary><b>5. DevSecOps & CI/CD Pipelines</b></summary>
<br/>

* **Gradle Convention Plugins:** Automated dependency management, internal R8/ProGuard consumer-rules aggregation, and dynamic `namespace` generation across 15+ modules.
* **Split CI/CD Workflows:**
    * `ci_debug.yml`: Runs Detekt and Unit Tests on PRs.
    * `ci_release.yml`: Semi-automated production pipeline triggered by Git tags.
* **Secure Artifact Delivery:** Production AABs are never stored publicly. The CI pipeline securely delivers signed, obfuscated release bundles directly via **Telegram Bot API** and generates password-protected ZIP backups.
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
│   ├── designsystem    # Core UI tokens (Theme, Typography with Downloadable Fonts, Colors)
│   ├── ui-components   # Reusable UI widgets (Optimized Shimmers, Placeholders, Buttons)
│   ├── artwork-card    # Isolated UI component for rendering images with Coil and Retry logic
│   ├── translation     # Google ML Kit integration wrapper
│   ├── navigation      # Type-safe @Serializable routes for Compose Navigation
│   ├── image-loader    # Custom OkHttpClient provider for Coil disk caching
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
