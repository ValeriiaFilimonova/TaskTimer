import java.time.Duration

fun Any.captureTime(block: TimeCaptor.() -> Unit): TimeCaptor {
    val captor = TimeCaptor()
    block.invoke(captor)
    return captor
}

class TimeCaptor {
    private val captures: MutableList<TimeCapture> = mutableListOf()

    private var captureStartTime: Long = System.currentTimeMillis()

    fun setStartTime(time: Long) {
        captureStartTime = time;
    }

    fun getCapture(label: String): List<TimeCapture> {
        return captures.filter { capture -> capture.label == label }
    }

    fun addCapture(label: String, block: (() -> Unit)?) {
        val startTime = System.currentTimeMillis()
        block?.invoke()
        val endTime = System.currentTimeMillis()

        captures.add(TimeCapture(label, startTime, endTime))
    }

    inner class TimeCapture(val label: String, val startTime: Long, val endTime: Long? = null) {
        fun duration(): Duration {
            return Duration.ofMillis(endTime ?: startTime - startTime)
        }

        fun executionTime(): Long {
            return startTime - captureStartTime
        }

        fun executionTimeTowards(capture: TimeCapture): Long {
            return startTime - capture.startTime
        }
    }
}

fun List<TimeCaptor.TimeCapture>.firstExecution(): Long {
    return first().executionTime()
}

fun List<TimeCaptor.TimeCapture>.lastExecution(): Long {
    return last().executionTime()
}

fun TimeCaptor.registerStart(block: () -> Unit) {
    setStartTime(System.currentTimeMillis())
    block.invoke()
}

fun TimeCaptor.track(label: String, block: (() -> Unit)? = null) {
    addCapture(label, block)
}

operator fun TimeCaptor.get(label: String): List<TimeCaptor.TimeCapture> {
    return getCapture(label)
}
