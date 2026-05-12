package ru.createsmart.benchmark

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val FIND_OBJECT_TIMEOUT_MS = 5000L
private const val MARGIN_DIVIDER = 10
private const val SCROLL_PERCENTAGE = 0.5f

@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {

    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun generate() {
        baselineProfileRule.collect(
            packageName = "ru.createsmart.artopos",
            maxIterations = 1,
        ) {
            // 1. Start the application
            pressHome()
            startActivityAndWait()

            // 2. Wait for the feed to load (find the scrollable list)
            val list = device.wait(Until.findObject(By.scrollable(true)), FIND_OBJECT_TIMEOUT_MS)

            if (list != null) {
                // 3. Make a small swipe to make Compose render the cards
                list.setGestureMargin(device.displayWidth / MARGIN_DIVIDER)
                list.scroll(Direction.DOWN, SCROLL_PERCENTAGE)
                device.waitForIdle()
            }
        }
    }
}
