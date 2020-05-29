package ui.screen

import java.io.PrintWriter

interface TerminalScreen {
    fun open()

    fun close()

    fun getOutput(): PrintWriter

    fun readCommand(): String?

    fun print(message: String)

    fun setTime(timeLeftInMilliseconds: Long)
}
