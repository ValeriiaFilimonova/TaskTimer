package ui.screen

import com.googlecode.lanterna.terminal.swing.SwingTerminalFontConfiguration
import java.awt.Font

class TimerTerminalFontConfiguration(fontSize: Int) : SwingTerminalFontConfiguration(
    true, BoldMode.EVERYTHING_BUT_SYMBOLS,
    *(filterMonospaced(*selectDefaultFont()).map { f -> Font(f.name, f.style, fontSize) }.toTypedArray())
)
