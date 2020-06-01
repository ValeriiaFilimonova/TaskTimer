package ui.screen

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalTextUtils
import com.googlecode.lanterna.graphics.TextGraphics
import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.screen.Screen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.io.PrintWriter
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.abs
import kotlin.time.ExperimentalTime
import kotlin.time.milliseconds
import com.googlecode.lanterna.TerminalSize as LanternaTerminalSize
import com.googlecode.lanterna.screen.TerminalScreen as LanternaTerminalScreen

@ExperimentalTime
object JvmTerminalScreen : TerminalScreen {
    private const val FIXED_NUMBER_OF_TIMER_COLUMNS = 22
    private const val FIXED_NUMBER_OF_TIMER_ROWS = 7
    private const val PLACEHOLDER = "timer>"

    private lateinit var resizeListeners: MutableList<(size: TerminalSize) -> Unit>
    private var resizeFuture: ScheduledFuture<*>? = null

    private val outputStream = PipedOutputStream()
    private val inputStream = PipedInputStream(outputStream)
    private val printWriter = PrintWriter(outputStream)

    private val terminalFactory = DefaultTerminalFactory().apply {
        setTerminalEmulatorTitle("Task Timer")
        setInitialTerminalSize(LanternaTerminalSize(80, 50))
    }
    private val terminal = terminalFactory.createTerminal()
    private val screen = LanternaTerminalScreen(terminal)
    private val graphics: TextGraphics = screen.newTextGraphics()
    private var terminalSize = TerminalSize(screen.terminalSize.columns, screen.terminalSize.rows)
    private var cursorPosition
        get() = screen.cursorPosition
        set(cursorPosition) {
            screen.cursorPosition = cursorPosition
        }

    private val timeOutputZone = TimeOutputZone()
    private val printOutputZone = PrintAlertOutputZone()
    private val commandsZone = CommandsZone()

    private val refreshOfTypeNeeded = AtomicReference(RefreshType.NONE)

    init {
        watchOnResizeEvents()
        watchOnOutputStream()
        watchIfScreenRefreshNeeded()
        clear()
    }

    override fun clear() {
        screen.clear()
        resizeListeners.forEach { l -> l.invoke(terminalSize) }
        timeOutputZone.displayTime(0)
        commandsZone.printPlaceholder()
        refreshOfTypeNeeded.set(RefreshType.COMPLETE)
    }

    override fun open() {
        screen.startScreen()
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
            .also { refreshOfTypeNeeded.set(RefreshType.PARTIAL) }
    }

    @ExperimentalTime
    override fun setTime(timeLeftInMilliseconds: Long) {
        timeOutputZone.displayTime(timeLeftInMilliseconds)
            .also { refreshOfTypeNeeded.set(RefreshType.PARTIAL) }
    }

    override fun getOutput(): PrintWriter {
        return printWriter
    }

    override fun getSize(): TerminalSize {
        return TerminalSize(terminalSize.columns, terminalSize.rows)
    }

    override fun setResizeListener(listener: (size: TerminalSize) -> Unit) {
        resizeListeners.add(listener)
    }

    private fun watchIfScreenRefreshNeeded() {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate({
            when (refreshOfTypeNeeded.get()) {
                RefreshType.PARTIAL -> {
                    screen.refresh(Screen.RefreshType.DELTA)
                    refreshOfTypeNeeded.set(RefreshType.NONE)
                }
                RefreshType.COMPLETE -> {
                    screen.refresh(Screen.RefreshType.COMPLETE)
                    refreshOfTypeNeeded.set(RefreshType.NONE)
                }
                else -> {
                }
            }
        }, 500, 100, TimeUnit.MILLISECONDS)
    }

    private fun watchOnOutputStream() {
        Executors.newSingleThreadExecutor().execute {
            val bytesBuffer = mutableListOf<Byte>()

            while (true) {
                val available = inputStream.available()
                if (available == 0) {
                    if (bytesBuffer.isNotEmpty()) {
                        commandsZone.output(bytesBuffer)
                        bytesBuffer.clear()
                        refreshOfTypeNeeded.set(RefreshType.PARTIAL)
                    }
                    Thread.sleep(100)
                    continue
                }

                val bytesChunk = inputStream.readNBytes(available)

                if (bytesChunk.isNotEmpty()) {
                    bytesBuffer.addAll(bytesChunk.toList())
                }
            }
        }
    }

    private fun watchOnResizeEvents() {
        val executor = Executors.newSingleThreadScheduledExecutor()

        resizeListeners = listOf(timeOutputZone, printOutputZone, commandsZone)
            .map { z -> z.resizeListener }
            .toMutableList()

        terminal.addResizeListener { _, newSize ->
            if (resizeFuture?.isDone == false) {
                resizeFuture!!.cancel(true)
            }

            resizeFuture = executor.schedule({
                terminalSize = TerminalSize(newSize.columns, newSize.rows)
                clear()
                screen.doResizeIfNecessary()
            }, 700, TimeUnit.MILLISECONDS)
        }
    }

    //region Helpers

    private enum class RefreshType {
        NONE,
        PARTIAL,
        COMPLETE
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

        val resizeListener: (terminalSize: TerminalSize) -> Unit
    }

    private data class Line(val string: String, val returnCarriage: Boolean)

    private abstract class OutputZone : TerminalZone {
        protected abstract var currentRow: Int

        fun output(byteList: List<Byte>) {
            output(byteList.toByteArray())
        }

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

        override val resizeListener: (terminalSize: TerminalSize) -> Unit = {}

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

        override val resizeListener: (terminalSize: TerminalSize) -> Unit = {
            rightColumn = terminalSize.columns
            currentRow = topRow
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

        override val resizeListener: (terminalSize: TerminalSize) -> Unit = {
            rightColumn = terminalSize.columns
            bottomRow = terminalSize.rows
            topRow = FIXED_NUMBER_OF_TIMER_ROWS + 1
            currentRow = topRow
        }

        init {
            resetCommandsIterator()
        }

        override fun output(text: String) {
            super.output(text)
            printPlaceholder()
        }

        fun pollCommand(): String? {
            val keyStroke = screen.pollInput() ?: return null


            if (keyStroke.isCtrlDown && keyStroke.character == 'l') {
                clear()
                return null
            }

            when (keyStroke.keyType) {
                KeyType.Enter -> return getLastCommand()
                KeyType.Backspace -> removeCharacter()
                KeyType.Character -> printCharacter(keyStroke.character)
                KeyType.ArrowUp -> showPreviousCommand()
                KeyType.ArrowDown -> showNextCommand()
            }

            return null
        }

        fun printPlaceholder() {
            super.output("\n$PLACEHOLDER")
            cursorPosition = TerminalPosition(PLACEHOLDER.length + 1, currentRow)
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

        private fun resetCommandsIterator() {
            commandsIterator = commands.asReversed().listIterator()
        }
    }
    //endregion
}
