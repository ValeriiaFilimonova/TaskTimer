package tasks

import SECONDS
import alerts.Alert
import kotlin.test.Test
import kotlin.test.assertEquals

open class RepeatableTaskTest {
    private val fakeAlert = object : Alert {
        override fun alert() {
            // do nothing
        }
    }

    @Test
    fun execute_withZeroFromTime_executionTimeCalculatedProperly() {
        val task = RepeatableTask(10.SECONDS, 0.SECONDS, alert = fakeAlert)

        assertEquals(0.SECONDS, task.executionTimeInMillis)

        task.execute()
        assertEquals(0.SECONDS, task.executionTimeInMillis)

        task.execute()
        assertEquals(10.SECONDS, task.executionTimeInMillis)

        task.execute()
        assertEquals(20.SECONDS, task.executionTimeInMillis)
    }

    @Test
    fun execute_withNonZeroFromTime_executionTimeCalculatedProperly() {
        val task = RepeatableTask(10.SECONDS, 3.SECONDS, alert = fakeAlert)

        assertEquals(0.SECONDS, task.executionTimeInMillis)

        task.execute()
        assertEquals(3.SECONDS, task.executionTimeInMillis)

        task.execute()
        assertEquals(13.SECONDS, task.executionTimeInMillis)
    }
}
