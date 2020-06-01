package ui.screen

import java.io.PrintWriter

interface TerminalScreen {
    fun open()

    fun close()

    fun clear()

    fun getSize(): TerminalSize

    fun setResizeListener(listener: (size: TerminalSize) -> Unit)

    fun getOutput(): PrintWriter

    fun readCommand(): String?

    fun print(message: String)

    fun setTime(timeLeftInMilliseconds: Long)
}

data class TerminalSize(val columns: Int, val rows: Int)
