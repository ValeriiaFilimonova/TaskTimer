package tasks

import MillisecondsTimeUnit

interface Task : Cloneable {
    val executionTimeInMillis: MillisecondsTimeUnit

    fun execute()

    override fun clone(): Task
}