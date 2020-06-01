package ui.commands

import JvmTimerError
import Timer
import kotlin.time.ExperimentalTime

@ExperimentalTime
abstract class TimerStartSubCommand : TimerSubCommand() {
    protected fun createAndStartNewTimer(): Timer {
        try {
            timer?.stop()
        } catch (e: JvmTimerError) {
        }

        return Timer(properties!!.build()).apply {

            onStart = { terminalScreen.setTime(duration.toLong()) }
            onTick = { terminalScreen.setTime(duration.toLong() - elapsedTime.toLong()) }

            start()
        }
    }
}
