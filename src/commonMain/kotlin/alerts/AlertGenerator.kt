package alerts

import tasks.AlertTask

interface AlertGenerator {
    fun generate(task: AlertTask, vararg params: Any = emptyArray()): Alert
}
