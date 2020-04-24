package ui.screen

import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.screen.Screen
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import com.googlecode.lanterna.terminal.Terminal
import kotlin.math.abs
import kotlin.time.ExperimentalTime
import kotlin.time.milliseconds

object TimerScreen {
    private val terminalFactory: DefaultTerminalFactory
    private lateinit var terminal: Terminal
    private lateinit var screen: Screen

    init {
        val fontConfiguration = TimerTerminalFontConfiguration(30)
        val terminalSize = TerminalSize(29, 9)

        terminalFactory = DefaultTerminalFactory().apply {
            setTerminalEmulatorTitle("Task Timer")
            setTerminalEmulatorFontConfiguration(fontConfiguration)
            setInitialTerminalSize(terminalSize)
        }

    }

    fun show() {
        terminal = terminalFactory.createTerminal()
        screen = TerminalScreen(terminal).apply { cursorPosition = null }
        screen.startScreen()
    }

    fun hide() {
        screen.stopScreen()
        screen.close()
        terminal.close()
    }

    @ExperimentalTime
    fun display(timeLeft: Long) {
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
}
