package tasks

class TaskPrototype(private val task: Task) {
    fun getTask(): Task {
        return task.clone()
    }
}
