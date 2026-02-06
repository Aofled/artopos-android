package ru.createsmart.benchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val BENCHMARK_ITERATIONS = 20
private const val FIND_OBJECT_TIMEOUT_MS = 5000L
private const val SWIPE_SPEED = 15 // Pixels per step (Lower is faster)
private const val MARGIN_DIVIDER = 10

@Suppress("MagicNumber")
@RunWith(AndroidJUnit4::class)
class DiscoverScrollBenchmark {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun scrollListAggressive() {
        benchmarkRule.measureRepeated(
            packageName = "ru.createsmart.artopos",
            metrics = listOf(FrameTimingMetric()),
            iterations = BENCHMARK_ITERATIONS,

            // Performance: Use CompilationMode.Full() to measure PURE code speed.
            // This eliminates JIT compilation noise from the results.
            compilationMode = CompilationMode.Full(),
            startupMode = StartupMode.WARM,
            setupBlock = {
                pressHome()
                startActivityAndWait()
            },
        ) {
            val list = device.wait(Until.findObject(By.scrollable(true)), FIND_OBJECT_TIMEOUT_MS)
                ?: error("List not found!")

            // We exclude the launch of system gestures (for example, the "Back" swipe) at the edges of the screen.
            list.setGestureMargin(device.displayWidth / MARGIN_DIVIDER)

            val centerX = device.displayWidth / 2
            val startY = (device.displayHeight * 0.85).toInt()
            val endY = (device.displayHeight * 0.15).toInt()
            val jitter = (device.displayHeight * 0.20).toInt()

            // --- STRESS TEST ---

            val swipeSpeed = SWIPE_SPEED
            // 1. Aggressive Scroll Down
            repeat(5) {
                device.swipe(centerX, startY, centerX, endY, swipeSpeed)
                device.waitForIdle()
            }

            // 2. Aggressive Scroll Up
            // Tests recycling of items that went off-screen (Top Cache)
            repeat(5) {
                device.swipe(centerX, endY, centerX, startY, swipeSpeed)
            }

            // 3. Jitter (Shake)
            // Tests recomposition stability when content moves slightly
            repeat(10) {
                device.swipe(centerX, startY, centerX, startY - jitter, swipeSpeed)
                device.swipe(centerX, startY - jitter, centerX, startY, swipeSpeed)
            }

            // 4. Chaos Mode (Random directions)
            // Forces Prefetcher to change direction constantly (Worst case scenario)
            repeat(10) {
                device.swipe(centerX, startY, centerX, endY, swipeSpeed)
                device.swipe(centerX, endY, centerX, startY, swipeSpeed)
            }

            // 5. Cleanup: Long Fling and Wait
            list.fling(Direction.DOWN)
            device.waitForIdle()
        }
    }
}
