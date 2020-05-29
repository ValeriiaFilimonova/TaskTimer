package ui.screen

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TerminalTextUtils
import com.googlecode.lanterna.graphics.TextGraphics
import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.screen.Screen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.io.PrintWriter
import java.util.concurrent.Executors
import kotlin.collections.ArrayList
import kotlin.math.abs
import kotlin.time.ExperimentalTime
import kotlin.time.milliseconds
import com.googlecode.lanterna.screen.TerminalScreen as PicocliTerminalScreen1

@ExperimentalTime
object JvmTerminalScreen: TerminalScreen {
    private const val FIXED_NUMBER_OF_TIMER_COLUMNS = 22
    private const val FIXED_NUMBER_OF_TIMER_ROWS = 7
    private const val PLACEHOLDER = "timer>"

    private val screen: PicocliTerminalScreen1
    private val graphics: TextGraphics

    private var cursorPosition
        get() = screen.cursorPosition
        set(cursorPosition) {
            screen.cursorPosition = cursorPosition
        }

    private val timeOutputZone: TimeOutputZone
    private val printOutputZone: OutputZone
    private val commandsZone: CommandsZone

    private val outputStream = PipedOutputStream()
    private val inputStream = PipedInputStream(outputStream)
    private val printWriter = PrintWriter(outputStream)

    init {
        val terminalFactory = DefaultTerminalFactory().apply {
            setTerminalEmulatorTitle("Task Timer")
            setInitialTerminalSize(TerminalSize(80, 40))
        }
        val terminal = terminalFactory.createTerminal()

        screen = PicocliTerminalScreen1(terminal)
        graphics = screen.newTextGraphics()

        timeOutputZone = TimeOutputZone()
        printOutputZone = PrintAlertOutputZone()
        commandsZone = CommandsZone()

        watchOutput()
    }

    override fun open() {
        screen.startScreen()
        setTime(0)
    }

    override fun close() {
        screen.stopScreen()
        printWriter.close()
    }

    override fun print(message: String) {
        printOutputZone.output(message)
    }

    override fun readCommand(): String? {
        return commandsZone.pollCommand()
    }

    // todo resize handle

    @ExperimentalTime
    override fun setTime(timeLeftInMilliseconds: Long) {
        timeOutputZone.displayTime(timeLeftInMilliseconds)
        screen.refresh(Screen.RefreshType.DELTA)
    }

    override fun getOutput(): PrintWriter {
        return printWriter
    }

    private fun watchOutput() {
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
                    screen.refresh(Screen.RefreshType.DELTA)
                }
            }
        }
    }

    private interface TerminalZone {
        var leftColumn: Int

        var rightColumn: Int

        var topRow: Int

        var bottomRow: Int

        val columns: Int
            get() = rightColumn - leftColumn

        val rows: Int
            get() = bottomRow - topRow

        val middleColumn: Int
            get() = columns / 2 + leftColumn

        val middleRow: Int
            get() = rows / 2 + topRow

        fun reactOnTerminalSizeChange(terminalSize: TerminalSize)
    }

    private data class Line(val string: String, val returnCarriage: Boolean)

    private abstract class OutputZone : TerminalZone {
        protected abstract var currentRow: Int

        fun output(byteArray: ByteArray) {
            output(String(byteArray))
        }

        open fun output(text: String) {
            val lines = getLines(text)

            if (currentRow + lines.size > rows) {
                val diff = lines.size - (bottomRow - currentRow)

                screen.scrollLines(topRow, currentRow, diff)
                currentRow -= diff
            }

            for (line in lines) {
                graphics.putString(leftColumn, currentRow, line.string)

                if (line.returnCarriage) {
                    currentRow++
                }
            }
        }

        private fun getLines(text: String): List<Line> {
            val lines: MutableList<Line> = ArrayList()
            val builder = StringBuilder()

            for (char in text) {
                if (char == '\n') {
                    lines.add(Line(builder.toString(), true))
                    builder.clear()
                    continue
                }

                if (TerminalTextUtils.isPrintableCharacter(char)) {
                    builder.append(char)
                }
            }

            if (builder.isNotEmpty()) {
                lines.add(Line(builder.toString(), false))
            }

            if (lines.size > rows) {
                return lines.takeLast(rows)
            }

            return lines
        }
    }

    @ExperimentalTime
    private class TimeOutputZone : TerminalZone {
        override var leftColumn: Int = 0
        override var rightColumn: Int = FIXED_NUMBER_OF_TIMER_COLUMNS
        override var topRow: Int = 0
        override var bottomRow: Int = FIXED_NUMBER_OF_TIMER_ROWS

        init {
            displayTime(0)
        }

        override fun reactOnTerminalSizeChange(terminalSize: TerminalSize) {
        }

        fun displayTime(timeLeftInMilliseconds: Long) {
            abs(timeLeftInMilliseconds).milliseconds.toComponents { days, hours, minutes, seconds, _ ->
                val text = "${days.pad()}:${hours.pad()}:${minutes.pad()}:${seconds.pad()}"

                graphics.putString(4, middleRow - 1, " ".repeat(12))

                if (timeLeftInMilliseconds < 0) {
                    graphics.putString(4, middleRow, "-$text")
                } else {
                    graphics.putString(5, middleRow, text)
                }
            }
        }

        private fun Int.pad(): String = toString().padStart(2, '0')
    }

    private class PrintAlertOutputZone : OutputZone() {
        override var leftColumn: Int = FIXED_NUMBER_OF_TIMER_COLUMNS + 1
        override var rightColumn: Int = 0
        override var topRow: Int = 0
        override var bottomRow: Int = FIXED_NUMBER_OF_TIMER_ROWS
        override var currentRow: Int = topRow

        init {
            reactOnTerminalSizeChange(screen.terminalSize)
        }

        override fun reactOnTerminalSizeChange(terminalSize: TerminalSize) {
            rightColumn = terminalSize.columns
        }

        override fun output(text: String) {
            super.output(text + "\n")
        }
    }

    private class CommandsZone : OutputZone() {
        override var leftColumn: Int = 0
        override var rightColumn: Int = 0
        override var topRow: Int = 0
        override var bottomRow: Int = 0
        override var currentRow: Int = 0

        private val commands = mutableListOf<String>()
        private var lastCommand = StringBuilder()
        private lateinit var commandsIterator: ListIterator<String>

        init {
            reactOnTerminalSizeChange(screen.terminalSize)
            resetCommandsIterator()
        }

        override fun reactOnTerminalSizeChange(terminalSize: TerminalSize) {
            rightColumn = terminalSize.columns
            bottomRow = terminalSize.rows
            topRow = FIXED_NUMBER_OF_TIMER_ROWS + 1
            currentRow = topRow
        }

        override fun output(text: String) {
            super.output(text)
            printPlaceholder()
        }

        fun pollCommand(): String? {
            val keyStroke = screen.pollInput() ?: return null

            var command: String? = null

            when (keyStroke.keyType) {
                KeyType.Enter -> command = getLastCommand()
                KeyType.Backspace -> removeCharacter()
                KeyType.Character -> printCharacter(keyStroke.character)
                KeyType.ArrowUp -> showPreviousCommand()
                KeyType.ArrowDown -> showNextCommand()
            }

            screen.refresh(Screen.RefreshType.DELTA)

            return command
        }

        private fun getLastCommand(): String {
            return lastCommand.toString().also {
                commands.add(it)
                lastCommand.clear()
                resetCommandsIterator()
                printPlaceholder()
            }
        }

        private fun printCharacter(char: Char) {
            lastCommand.append(char)
            graphics.setCharacter(cursorPosition, char)
            cursorPosition = cursorPosition.withRelativeColumn(1)
        }

        private fun removeCharacter() {
            if (lastCommand.isNotEmpty()) {
                if (lastCommand.deleteCharAt(lastCommand.lastIndex).isEmpty()) {
                    resetCommandsIterator()
                }
                cursorPosition = cursorPosition.withRelativeColumn(-1)
                graphics.putString(cursorPosition, " ")
            }
        }

        private fun showPreviousCommand() {
            if (commandsIterator.hasNext()) {
                switchCommand(commandsIterator.next())
            }
        }

        private fun showNextCommand() {
            if (commandsIterator.hasPrevious()) {
                switchCommand(commandsIterator.previous())

            }
        }

        private fun switchCommand(command: String) {
            if (command.isEmpty()) {
                return
            }

            val placeholder = PLACEHOLDER.length + 1
            val string = command + " ".repeat(columns - placeholder - command.length)
            graphics.putString(placeholder, currentRow, string)
            cursorPosition = TerminalPosition(placeholder + command.length, currentRow)
            lastCommand = StringBuilder(command)
        }

        private fun printPlaceholder() {
            super.output("\n$PLACEHOLDER")
            cursorPosition = TerminalPosition(PLACEHOLDER.length + 1, currentRow)
        }

        private fun resetCommandsIterator() {
            commandsIterator = commands.asReversed().listIterator()
        }
    }
}
