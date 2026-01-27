
# Artopos 🎨

**Artopos** is a modern Android application for exploring the world's finest masterpieces using the [Harvard Art Museums API](https://github.com/harvardartmuseums/api-docs).

## 🎨 Design Concept

The UI follows a **minimalist, gallery-like aesthetic**, inspired by platforms like Pinterest and Google Arts & Culture.

---  

## 🛠 Tech Stack

### Architecture & Design
*   **Clean Architecture** (Data / Domain / Presentation layers).
*   **Modularization** (Feature-based + Layer-based modules).
*   **Offline-First** (Database as a Single Source of Truth).
*   **MVVM + MVI** (Unidirectional Data Flow with `Sealed Interfaces`).

### Modern Android
*   **Kotlin** 2.0+ (K2 Compiler).
*   **Jetpack Compose** (Material 3, Edge-to-Edge, Staggered Grid).
*   **Coroutines & Flow** (Heavy usage of `combine`, `stateIn`, `flowOn`).
*   **Hilt** (Dependency Injection).

### Network & Data
*   **Retrofit 2** + **Kotlin Serialization**.
*   **OkHttp** (Custom Interceptors for aggressive caching & auth).
*   **Room** (Local storage, Embedded entities, Transactions).
*   **Coil** (Advanced image loading, Shimmer effects).

### Build & Quality
*   **Gradle Kotlin DSL** (KTS).
*   **Version Catalog** (`libs.versions.toml`).
*   **Convention Plugins** (Custom `build-logic` for sharing build configurations).
*   **Detekt** (Static code analysis with auto-formatting).
*   **R8/ProGuard** (Optimized release builds).

---  

## 🚀 How to Run
### 1. Obtain an API Key (Required)
Artopos uses the public Harvard Art Museums API. You need a free key to prevent `401 Unauthorized` errors.

1.  Visit the [Harvard Art Museums API documentation](https://github.com/harvardartmuseums/api-docs).
2.  Locate the registration section (Google Form link).
3.  Fill out the form with your email. You will receive the key instantly.

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

```./gradlew detektAll```

Select the `app` configuration in Android Studio and run it on an Emulator or Physical Device.
  
---  

## ⚖️ Attribution & Credits

Data and Images are provided by the **Harvard Art Museums API**.  
This application is a personal portfolio project and is not affiliated with or endorsed by Harvard University.

* API Documentation: [Harvard Art Museums API](https://github.com/harvardartmuseums/api-docs)
* All artwork images and metadata belong to their respective owners and the Harvard Art Museums.

---
