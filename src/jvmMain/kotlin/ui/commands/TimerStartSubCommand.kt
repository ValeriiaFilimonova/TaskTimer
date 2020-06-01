package ui.commands

import JvmTimerError
import Timer
import alerts.sound.JvmSoundPlayer
import kotlin.time.ExperimentalTime

@ExperimentalTime
abstract class TimerStartSubCommand : TimerSubCommand() {
    private val player = (DependenciesFactory.getPlayer() as JvmSoundPlayer)

    protected fun createAndStartNewTimer(): Timer {
        try {
            timer?.stop()
        } catch (e: JvmTimerError) {
        }

        return Timer(properties!!.build()).apply {

            onStart = { terminalScreen.setTime(duration.toLong()) }
            onTick = { terminalScreen.setTime(duration.toLong() - elapsedTime.toLong()) }
            onPause = { player.terminate() }
            onStop = { player.terminate() }

            start()
        }
    }
}
