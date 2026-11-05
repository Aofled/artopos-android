# Artopos 🎨

[![Android CI](https://github.com/Aofled/artopos-android/actions/workflows/ci_debug.yml/badge.svg)](https://github.com/Aofled/artopos-android/actions/workflows/ci_debug.yml)
[![Android CI](https://github.com/Aofled/artopos-android/actions/workflows/ci_release.yml/badge.svg)](https://github.com/Aofled/artopos-android/actions/workflows/ci_release.yml)
[![Kotlin 2.0](https://img.shields.io/badge/Kotlin-2.0%2B-blue.svg)](https://kotlinlang.org)
[![Min API 26](https://img.shields.io/badge/Min%20API-26-green.svg)](https://apilevels.com/)

**Artopos** is a modern, offline-first Android application for exploring the world's finest masterpieces using the public [Harvard Art Museums API](https://github.com/harvardartmuseums/api-docs).

Built as a production-grade showcase of modern Android development: strict Clean Architecture, modular monolith, zero-overhead Compose rendering, and on-device ML.

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

* **Architecture:** Clean Architecture, Modular Monolith (20 modules), Strict MVI (UDF), Hilt (pure `javax.inject` in domain).
* **UI & Performance:** Jetpack Compose (Material 3, Edge-to-Edge), Type-Safe Navigation Compose, Immutable Collections, `Modifier.Node` API, Baseline Profiles.
* **Data & Networking:** Room (Normalized SQLite, Composite B-Tree Indexes), Paging 3 (RemoteMediator + Network PagingSource), Retrofit 2, Kotlinx Serialization, Coil 3 (Custom 512 MB DiskCache).
* **Search & ML:** Elasticsearch Query Builder (Wildcards + Phrases), Google ML Kit (On-Device Translation with bounded thread pools).
* **Build & DevOps:** Gradle Kotlin DSL (Composite `build-logic`, Project Isolation ready), Version Catalog, Detekt, GitHub Actions CI/CD.

---

## ⚡️ Key Engineering Highlights

* **Zero-Recomposition Shimmer (`Modifier.Node`):** Shimmer animations run strictly on the low-level `DrawModifierNode` API. By reading animation progress solely in the Draw phase, the rendering pipeline completely bypasses Composition and Layout passes, eliminating GC overhead during 120Hz scrolling.
* **Layout-Shift Free Grid & Stable Anchors:** `LazyVerticalStaggeredGrid` pre-allocates placeholder dimensions using image aspect ratios before bitmap loading. Custom scroll-synchronization resets the scroll anchor only after `LoadState.NotLoading` is emitted, preventing jarring viewport jumps during sorting changes.
* **Hardware-Aware Memory & Image Downsampling:** Decoding resolution is dynamically clamped based on the physical device RAM (`ActivityManager.isLowRamDevice`, scaling from 4,000px to 10,000px). This keeps pinch-to-zoom crisp on flagships while preventing GPU `GL_MAX_TEXTURE_SIZE` crashes and OOMs on budget hardware.
* **Progressive On-Device ML Translation:** Two-phase loading strategy powered by `withTimeoutOrNull(300ms)`: displays instant dictionary translations; if the on-device Google ML Kit model takes longer than 300ms to warm up, an asynchronous background task seamlessly upgrades the text without UI flicker.
* **Resilient Hybrid Paging Strategy:** Decouples offline-first Room pagination (`discover_feed` junction table) from ephemeral search pagination (`SearchPagingSource`). Eliminates Paging 3's `pageEventFlow` channel collision by streaming reactive favorite overlays independently, while persisting clean un-degraded DTO snapshots to SQLite via an in-memory session cache.
* **Fast-Path MediaStream Copying:** When exporting artworks to the gallery, cached bytes are copied directly from Coil’s `DiskCache` snapshot into the `MediaStore` stream, avoiding costly CPU/RAM bitmap re-compression and cleaning up partial files on cancellation.

---

<details>
<summary><b>View Detailed Tech Stack & Libraries</b></summary>
<br/>

### Architecture & Design Patterns
*   **Clean Architecture** (Strict separation of Data / Domain / Presentation layers, pure JVM Domain contracts).
*   **Modular Monolith** (Feature-based + Layer-based modules to optimize build times and enforce strict encapsulation).
*   **Offline-First & Hybrid Data Strategy** (Database SSOT for Feed & Favorites; In-Memory Session Cache for Search).
*   **Strict MVI** (Unidirectional Data Flow with `Sealed Interfaces` for Intents, UI States, and Single-shot Events).
*   **Dependency Injection** (Hilt / Dagger with pure JSR-330 `javax.inject` in pure domain modules).

### Modern Android & Compose
*   **Kotlin 2.0+** (K2 Compiler, Strong Skipping Mode).
*   **Jetpack Compose** (Material 3, Edge-to-Edge, Immutable Collections, Modifier.Node API, `animateItem()`).
*   **Type-Safe Navigation** (Jetpack Navigation Compose with Kotlin Serialization for compile-time route safety).
*   **Coroutines & Flow** (Heavy usage of `combine`, `stateIn`, `flatMapLatest`, `debounce`).
*   **Paging 3** (RemoteMediator for SQLite caching, Network-only PagingSource with reactive Room State overlays).
*   **DataStore Preferences** (Async reactive user settings storage with `ReplaceFileCorruptionHandler`).

### Network, Data & ML
*   **Retrofit 2** + **Kotlin Serialization** (Resilient JSON parsing with default coercing).
*   **OkHttp** (Network Interceptors for forced 7-day Cache-Control).
*   **Room Database** (Normalized junction tables, Cascade Foreign Keys, Composite Indexes, Transactions).
*   **Coil 3** (Custom 512 MB DiskCache SSOT, hardware-aware downsampling, Fast-Path stream copying).
*   **Google ML Kit** (On-Device Translation for dynamic descriptions and metadata).
*   **Zoomable** (`net.engawapg.lib:zoomable` for image pan & pinch gestures).

### Build System & Quality
*   **Gradle Kotlin DSL** (KTS) with **Project Isolation** readiness.
*   **Version Catalog** (`libs.versions.toml`).
*   **Convention Plugins** (Custom `build-logic` composite build automating dependencies, namespaces, and ProGuard rules).
*   **Detekt** (Static code analysis with strict CI verification).
*   **R8/ProGuard** (Optimized release builds with consumer-rules aggregation).
*   **Baseline Profiles & Macrobenchmark** (AOT compilation and UI frame timing metrics).
*   **Unit Testing & Test Harness** (Dedicated `:core:testing` module, In-Memory Room testing, Robolectric, JUnit 4, Mockk, Turbine).
*   **GitHub Actions CI** (Automated pipelines for static analysis, unit testing, and encrypted AAB delivery via Telegram Bot API).
</details>

---

## 🏗 Project Structure

<details>
<summary><b>View Folder Tree (Modular Monolith)</b></summary>

```text
root
├── app                 # Application entry point, NavHost, Composition Root, Hilt assembly
├── build-logic         # Convention Plugins automating dependencies, namespaces & ProGuard
├── benchmark           # Macrobenchmark tests & Baseline Profile generator
├── core
│   ├── model           # Pure Kotlin domain entities, Enums, AppErrors (Zero Android dependencies)
│   ├── domain          # Repository interfaces & UseCases (Strict Business Logic)
│   ├── data            # Repositories, RemoteMediator, SearchPagingSource, Mappers
│   ├── network         # Retrofit API, Serialization DTOs, Auth Interceptors
│   ├── database        # Room Database, DBOs, Projections, DAOs
│   ├── datastore       # Jetpack DataStore Preferences with corruption recovery
│   ├── designsystem    # Core UI tokens, Themes, Downloadable Google Fonts
│   ├── ui-components   # Reusable widgets (ShimmerNode, ArtworkCard, FilterBottomSheet)
│   ├── translation     # Google ML Kit wrapper with limitedParallelism
│   ├── navigation      # Type-safe @Serializable routes
│   ├── image-loader    # Coil 3 & OkHttp configuration, MediaStore downloader
│   ├── common          # Shared utilities (NetworkMonitor, LocaleHelper, Dictionaries)
│   └── testing         # Shared testing harness (MainDispatcherRule)
└── feature
    ├── discover        # Infinite feed with Paging 3 and Filter BottomSheet
    ├── search          # Debounced live search with smart script guidance
    ├── details         # Deep-dive screen with Zoomable gallery and ML translation
    ├── favorites       # Saved artworks screen (Local SQLite SSOT)
    └── settings        # Preferences (Theme, Dynamic Locale, Cache cleaner)
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
> **💡 RAM Allocation & Performance:**  
> By default, the project allocates a safe baseline (~4.5 GB RAM total: 3 GB Gradle + 1.5 GB Kotlin Daemon) to ensure smooth builds on 8–16 GB machines without OS freezes.  
> If you are working on a high-spec workstation (32+ GB RAM), you can safely boost heap limits locally in your global `~/.gradle/gradle.properties` (`C:\Users\<Username>\.gradle\gradle.properties` on Windows):
> ```properties
> org.gradle.jvmargs=-Xmx8192m -XX:+UseG1GC -XX:MaxMetaspaceSize=2048m -Dfile.encoding=UTF-8
> kotlin.daemon.jvmargs=-Xmx4096m
> ```

### 4. Verify & Run
Run static analysis to ensure the environment is set up correctly:
```bash
# 1. Automatically reformat and fix code style across all 20 modules:
./gradlew detektFormat

# 2. Run read-only static analysis (CI simulation):
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
