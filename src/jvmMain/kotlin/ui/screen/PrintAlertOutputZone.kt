package ui.screen

import com.googlecode.lanterna.screen.TerminalScreen
import kotlin.math.roundToInt

internal class PrintAlertOutputZone(private val screen: TerminalScreen): OutputZone(screen) {
    override var leftColumn: Int = 23
    override var rightColumn: Int = 0
    override var topRow: Int = 0
    override var bottomRow: Int = 0

    init {
        val terminalSize = screen.terminalSize
        reactOnTerminalSizeChange(terminalSize.columns, terminalSize.rows)
    }

    override fun reactOnTerminalSizeChange(columns: Int, rows: Int) {
        rightColumn = columns
        bottomRow = rows - (rows / TerminalZone.GOLDEN_RATIO).roundToInt()
    }

    override fun output(text: String) {
        super.output(text + "\n")
    }
}
