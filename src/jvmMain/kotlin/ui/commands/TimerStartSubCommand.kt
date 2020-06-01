package ui.commands

import Timer
import kotlin.time.ExperimentalTime

@ExperimentalTime
abstract class TimerStartSubCommand : TimerSubCommand() {
    protected fun createAndStartNewTimer(): Timer {
        return Timer(properties!!.build()).apply {

            onStart = { terminalScreen.setTime(duration.toLong()) }
            onTick = { terminalScreen.setTime(duration.toLong() - elapsedTime.toLong()) }

            start()
        }
    }
}
