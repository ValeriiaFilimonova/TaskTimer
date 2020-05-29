package ui.screen

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.screen.Screen
import com.googlecode.lanterna.screen.TerminalScreen
import kotlin.math.roundToInt

class CommandsZone(private val screen: TerminalScreen) : OutputZone(screen) {
    override var leftColumn: Int = 0
    override var rightColumn: Int = 0
    override var topRow: Int = 0
    override var bottomRow: Int = 0

    private val input = StringBuilder()

    init {
        val terminalSize = screen.terminalSize
        reactOnTerminalSizeChange(terminalSize.columns, terminalSize.rows)
        currentRow = topRow
    }

    override fun reactOnTerminalSizeChange(columns: Int, rows: Int) {
        rightColumn = columns
        bottomRow = rows
        topRow = rows - (rows / TerminalZone.GOLDEN_RATIO).roundToInt() + 1
    }

    override fun output(text: String) {
        super.output(text)
        super.output("\n" + "timer>")
        screen.cursorPosition = TerminalPosition(7, currentRow)
        screen.refresh(Screen.RefreshType.DELTA)
    }

    fun pollCommand(): String? {
        val keyStroke = screen.pollInput() ?: return null

        if (keyStroke.keyType == KeyType.Enter) {
            val string = input.toString()
            input.clear()
            super.output("\ntimer>")
            screen.cursorPosition = TerminalPosition(7, currentRow)
            screen.refresh(Screen.RefreshType.DELTA)
            return string
        }

        if (keyStroke.keyType == KeyType.Backspace) {
            input.deleteCharAt(input.lastIndex)
            screen.cursorPosition = screen.cursorPosition.withRelativeColumn(-1)
            screen.newTextGraphics().putString(screen.cursorPosition, " ")
            screen.refresh(Screen.RefreshType.DELTA)
        }

        if (keyStroke.keyType == KeyType.Character) {
            input.append(keyStroke.character)
            screen.newTextGraphics()
                .putString(screen.cursorPosition.column, screen.cursorPosition.row, keyStroke.character.toString())
            screen.cursorPosition = screen.cursorPosition.withRelativeColumn(1)
            screen.refresh(Screen.RefreshType.DELTA)
        }

        // todo implement up and down

        return null
    }
}
