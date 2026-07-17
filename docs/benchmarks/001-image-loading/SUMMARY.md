# Performance Analysis: SubcomposeAsyncImage vs. Custom AsyncImage Wrapper

## 1. (Executive Summary)
Contrary to the initial hypothesis, the **`SubcomposeAsyncImage`** implementation outperformed the custom **`AsyncImage` + `Box`** implementation by **15–30%** across all test scenarios.

The custom implementation caused excessive main thread work due to **State Hoisting overhead** and redundant parent recompositions. Therefore, I have decided to stick with the `SubcomposeAsyncImage` approach.

## 2. Context & Problem Statement
My feature requires displaying a `LazyVerticalStaggeredGrid` of images with complex UI states:
1.  **Loading:** Animated Shimmer effect (Composable).
2.  **Error:** Interactive UI with a "Retry" button (Composable).
3.  **Success:** The image itself.

**The Hypothesis:**
I assumed that standard `SubcomposeAsyncImage` was causing scroll lag due to the cost of subcomposition (delaying composition until constraints are known). I attempted to refactor this using a lightweight `AsyncImage` wrapped in a `Box`, managing the Loading/Error states manually via a `mutableState` in the parent composable.

## 3. Methodology
We used **Jetpack Macrobenchmark** to measure `FrameTimingMetric` during an aggressive scroll scenario (`scrollListAggressive`).

*   **Metric:** Frame Duration (CPU time to produce a frame). Target is **< 16.66ms** (60 FPS).
*   **Scenarios:**
    1.  **Cached:** Images loaded from memory/disk cache.
    2.  **Network:** Frequent data updates and network fetching.
*   **Devices:**
    *   Low-end: Samsung Galaxy Tab A (SM-T595, Snapdragon 450).
    *   High-end: Samsung Galaxy S10+ (SM-G9750, Snapdragon 855).

## 4. Benchmark Results

### 4.1. Scenario: Cached Scroll (Static Data)
*Condition: Images are loaded from cache. Network activity is minimal.*

**Low-end Device (SM-T595):**

| Metric | SubcomposeAsyncImage | AsyncImage + Box | Delta |
| :--- | :--- | :--- | :--- |
| **P50 (Median)** | **7.9 ms** | 9.1 ms | AsyncImage is **~15% slower** |
| **P90** | **10.3 ms** | 12.7 ms | AsyncImage is **~23% slower** |
| **P95** | **12.2 ms** | 13.8 ms | AsyncImage is **~13% slower** |
| **P99 (Jank)** | **18.0 ms** | 19.5 ms | AsyncImage is **~8% slower** |
| **Frame Count (Median)** | ~885 | **~894** | - |

**High-end Device (S10+):**

| Metric | SubcomposeAsyncImage | AsyncImage + Box | Delta |
| :--- | :--- | :--- | :--- |
| **P50 (Median)** | **5.05 ms** | 5.45 ms | AsyncImage is **~8% slower** |
| **P90** | **7.45 ms** | 8.8 ms | AsyncImage is **~18% slower** |
| **P95** | **8.85 ms** | 10.5 ms | AsyncImage is **~19% slower** |
| **P99 (Jank)** | **12.2 ms** | 16.1 ms | AsyncImage is **~31% slower** |
| **Frame Count (Median)** | ~842 | **~847** | - |

> **Observation:** Even with cached data, the `AsyncImage + Box` implementation adds consistent overhead. On the high-end device, this overhead pushes the P99 metric dangerously close to the limit, causing frame drops (positive overrun).

### 4.2. Scenario: High Load (Network & Frequent Updates)
*Condition: Images are loaded from the network with state transitions (Loading -> Success).*

**Low-end Device (SM-T595):**

| Metric | SubcomposeAsyncImage | AsyncImage + Box | Delta |
| :--- | :--- | :--- | :--- |
| **P50 (Median)** | **9.1 ms** | 11.3 ms | AsyncImage is **~24% slower** |
| **P90** | **14.0 ms** | 19.6 ms | AsyncImage is **~40% slower** |
| **P95** | **17.5 ms** | 25.5 ms | AsyncImage is **~45% slower** |
| **P99 (Jank)** | **25.9 ms** | 30.0 ms | AsyncImage is **~16% slower** |
| **Frame Count (Median)** | ~963 | **~969** | - |

> **Observation:** The gap widens significantly under load. The `AsyncImage + Box` implementation exhibits severe instability (P90 > 16ms), resulting in a "jelly" scroll effect, likely due to the recomposition storm triggered by state updates.

## 5. Root Cause Analysis

The refactor failed due to **Recomposition Scope** and **State Management** issues, which outweighed the theoretical cost of Subcomposition.

### 1. State Hoisting Overhead
In the `AsyncImage + Box` implementation:
```kotlin
// Custom Implementation
var imageState by remember { mutableStateOf(...) } // State lifted to Parent
Box {
    CoilImage(onStateChange = { imageState = it }) // Callback triggers parent
    if (imageState is Loading) Shimmer()
}
```
Every time Coil updated its internal state (Start -> Loading -> Success), it modified `imageState`. Since this state was read in the parent `Box`, it triggered a **recomposition of the entire Item Layout**. In a list with many items loading simultaneously, this caused a "recomposition storm."

### 2. Encapsulation (Smart Recomposition)
`SubcomposeAsyncImage` manages its state (`Painter.State`) internally. When the state changes from Loading to Success:
*   Only the internal node of the image recomposes.
*   The parent `LazyGrid` item is **not** notified and does not recompose.
*   This isolation proved more efficient than manual state management.

### 3. Layout Node Count
The `AsyncImage` implementation used a `Box` with Z-index stacking (Shimmer + Image + Error). Even if invisible, these nodes add overhead to the Layout and Measure passes. `SubcomposeAsyncImage` uses a Slot API approach, replacing nodes dynamically, keeping the UI tree flatter.

## 6. Conclusion & Decision

**Decision:** We will use the **`SubcomposeAsyncImage`** implementation (revert to `ArtworkImage(old)`).

**Key Takeaways:**
1.  **Measure, Don't Guess:** The theoretical overhead of Subcomposition was negligible compared to the real-world overhead of extra recompositions in the manual implementation.
2.  **Use Case Matters:** `AsyncImage` is preferred for simple, static images (using `Painter` placeholders). However, for complex requirements involving **Composable** placeholders (animated Shimmers, interactive Error views), `SubcomposeAsyncImage` is the superior choice due to better state encapsulation.
