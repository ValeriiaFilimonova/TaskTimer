package ui.screen

import com.googlecode.lanterna.TerminalTextUtils
import com.googlecode.lanterna.screen.TerminalScreen

abstract class OutputZone(private val screen: TerminalScreen) : TerminalZone {
    protected var currentRow: Int = topRow

    fun output(byteArray: ByteArray) {
        output(String(byteArray))
    }

    open fun output(text: String) {
        val graphics = screen.newTextGraphics()

        var lines: MutableList<String> = ArrayList()
        val builder = StringBuilder()

        for (char in text) {
            if (char == '\n') {
                lines.add(builder.toString())
                builder.clear()
                continue
            }

            if (TerminalTextUtils.isPrintableCharacter(char)) {
                builder.append(char)
            }
        }

        if (lines.size > rows) {
            lines = lines.takeLast(rows).toMutableList()
        }

        if (currentRow + lines.size > rows) {
            val diff = lines.size - (bottomRow - currentRow)

            screen.scrollLines(topRow, currentRow, diff)
            currentRow -= diff
        }

        for (line in lines) {
            graphics.putString(leftColumn, currentRow++, line)
        }

        if (builder.isNotEmpty()) {
            if (currentRow + 1 > rows) {
                screen.scrollLines(topRow, currentRow, 1)
                currentRow--
                graphics.putString(leftColumn, currentRow, builder.toString())
            }
        }

//        screen.refresh(Screen.RefreshType.DELTA)
    }
}
