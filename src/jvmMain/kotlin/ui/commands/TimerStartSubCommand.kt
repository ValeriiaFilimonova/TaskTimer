package ui.commands

import DependenciesFactory
import JvmTimerError
import Timer
import alerts.Terminatable
import kotlin.time.ExperimentalTime

@ExperimentalTime
abstract class TimerStartSubCommand : TimerSubCommand() {
    private val services = listOf(
        DependenciesFactory.getPlayer() as Terminatable,
        DependenciesFactory.getSpeaker() as Terminatable
    )

    protected fun createAndStartNewTimer(): Timer {
        try {
            timer?.stop()
        } catch (e: JvmTimerError) {
        }

        return Timer(properties!!.build()).apply {

            onStart = { terminalScreen.setTime(duration.toLong()) }
            onTick = { terminalScreen.setTime(duration.toLong() - elapsedTime.toLong()) }
            onPause = { services.forEach(Terminatable::terminate) }
            onStop = { services.forEach(Terminatable::terminate) }

            start()
        }
    }
}
