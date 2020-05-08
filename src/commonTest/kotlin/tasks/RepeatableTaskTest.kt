package tasks

import seconds
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
        val task = RepeatableTask(10.seconds, 0.seconds, alert = fakeAlert)

        assertEquals(0.seconds, task.executionTimeInMillis)

        task.execute()
        assertEquals(0.seconds, task.executionTimeInMillis)

        task.execute()
        assertEquals(10.seconds, task.executionTimeInMillis)

        task.execute()
        assertEquals(20.seconds, task.executionTimeInMillis)
    }

    @Test
    fun execute_withNonZeroFromTime_executionTimeCalculatedProperly() {
        val task = RepeatableTask(10.seconds, 3.seconds, alert = fakeAlert)

        assertEquals(0.seconds, task.executionTimeInMillis)

        task.execute()
        assertEquals(3.seconds, task.executionTimeInMillis)

        task.execute()
        assertEquals(13.seconds, task.executionTimeInMillis)
    }
}
