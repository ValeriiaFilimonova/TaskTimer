package ui.screen

import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.screen.Screen
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.io.PrintWriter
import java.util.concurrent.Executors
import kotlin.time.ExperimentalTime

@ExperimentalTime
object JvmTerminalScreen {
    private val screen: TerminalScreen

    private val timeOutputZone: TimeOutputZone
    private val printOutputZone: OutputZone
    private val commandsZone: CommandsZone

    private val outputStream = PipedOutputStream()
    private val inputStream = PipedInputStream(outputStream)
    val printStream = PrintWriter(outputStream)

    init {
        val terminalFactory = DefaultTerminalFactory().apply {
            setTerminalEmulatorTitle("Task Timer")
            setInitialTerminalSize(TerminalSize(80, 50))
        }
        val terminal = terminalFactory.createTerminal()

        screen = TerminalScreen(terminal)
        timeOutputZone = TimeOutputZone(screen)
        printOutputZone = PrintAlertOutputZone(screen)
        commandsZone = CommandsZone(screen)

        Executors.newSingleThreadExecutor().execute {
            while (true) {
                val available = inputStream.available()
                if (available == 0) {
                    Thread.sleep(10)
                    continue
                }

                val bytes = inputStream.readNBytes(available)

                if (bytes.isNotEmpty()) {
                    commandsZone.output(bytes)
                }
            }
        }
    }

    fun open() {
        screen.startScreen()
        setTime(0)
    }

    fun close() {
        screen.stopScreen()
        printStream.close()
    }

    fun printMessage(message: String) {
        printOutputZone.output(message)
    }

    fun readCommand(): String? {
        return commandsZone.pollCommand()
    }

    // todo resize handle

    @ExperimentalTime
    fun setTime(timeLeftInMilliseconds: Long) {
        timeOutputZone.displayTime(timeLeftInMilliseconds)
        screen.refresh(Screen.RefreshType.DELTA)
    }
}
