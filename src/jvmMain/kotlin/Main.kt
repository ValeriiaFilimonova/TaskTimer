import alerts.AlertGenerators
import alerts.sound.Sound.MORNING_DEW_LONG
import alerts.voice.JvmSpeechSynthesizer
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.screen.Screen
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import com.googlecode.lanterna.terminal.Terminal
import ui.lantera.TimerTerminalFontConfiguration
import kotlin.math.abs
import kotlin.time.ExperimentalTime
import kotlin.time.milliseconds

@ExperimentalTime
fun main() {
    val fontConfiguration = TimerTerminalFontConfiguration(30)
    val terminalFactory = DefaultTerminalFactory().apply {
        setTerminalEmulatorTitle("Task Timer")
        setTerminalEmulatorFontConfiguration(fontConfiguration)
        setInitialTerminalSize(TerminalSize(29, 9))
    }
    val terminal: Terminal = terminalFactory.createTerminal()
    val screen: Screen = TerminalScreen(terminal).apply { cursorPosition = null }

    screen.startScreen()

    val properties = TimerPropertiesBuilder(15.SECONDS)
        .tickInterval(1.SECONDS)
        .repeatEvery(5.SECONDS, generator =  AlertGenerators.getSayTimeAlertGenerator())
        .remindAfterFinishEvery(5.SECONDS, AlertGenerators.getVoiceAlertGenerator("stop the timer"))
        .finalAlarm(MORNING_DEW_LONG)
        .build()

    val timer = Timer(properties)
        .also {
            it.onStart = {
                display(screen, it.duration.toLong())
            }
            it.onTick = {
                display(screen, (it.duration.toLong() - it.elapsedTime.toLong()))
            }
        }

    timer.start()
    Thread.sleep(12.MINUTES.toLong())
    timer.stop()

    (DependenciesFactory.getSpeaker() as JvmSpeechSynthesizer).close()
}

@ExperimentalTime
private fun display(screen: Screen, timeLeft: Long) {
    abs(timeLeft).milliseconds.toComponents { days, hours, minutes, seconds, _ ->
        var text = "${days.pad()}:${hours.pad()}:${minutes.pad()}:${seconds.pad()}"

        if (timeLeft < 0) {
            text = "-$text"
        }

        val size = screen.terminalSize
        val column = (size.columns - text.length) / 2
        val row = size.rows / 2

        screen.newTextGraphics().putString(column, row, text)
        screen.refresh()
    }
}

private fun Int.pad(): String = toString().padStart(2, '0')
