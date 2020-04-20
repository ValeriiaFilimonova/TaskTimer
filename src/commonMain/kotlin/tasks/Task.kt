package tasks

interface Task {
    val executionTimeInMillis: Long

    fun execute()
}
