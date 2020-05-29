package ui.screen

import com.googlecode.lanterna.screen.TerminalScreen
import ui.screen.TerminalZone.Companion.GOLDEN_RATIO
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.time.ExperimentalTime
import kotlin.time.milliseconds

@ExperimentalTime
internal class TimeOutputZone(private val screen: TerminalScreen) : TerminalZone {
    override var leftColumn: Int = 0
    override var rightColumn: Int = 22
    override var topRow: Int = 0
    override var bottomRow: Int = 0

    init {
        val terminalSize = screen.terminalSize
        reactOnTerminalSizeChange(terminalSize.columns, terminalSize.rows)
        displayTime(0)
    }

    override fun reactOnTerminalSizeChange(columns: Int, rows: Int) {
        bottomRow = rows - (rows / GOLDEN_RATIO).roundToInt()
    }

    fun displayTime(timeLeftInMilliseconds: Long) {
        abs(timeLeftInMilliseconds).milliseconds.toComponents { days, hours, minutes, seconds, _ ->
            val text = "${days.pad()}:${hours.pad()}:${minutes.pad()}:${seconds.pad()}"

            val graphics = screen.newTextGraphics()
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
