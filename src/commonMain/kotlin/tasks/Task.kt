package tasks

import MillisecondsTimeUnit

interface Task {
    val executionTimeInMillis: MillisecondsTimeUnit

    fun execute()
}
