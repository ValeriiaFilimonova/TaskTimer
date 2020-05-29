package ui.screen

interface TerminalZone {
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

    fun reactOnTerminalSizeChange(columns: Int, rows: Int)

    companion object {
        const val GOLDEN_RATIO = 1.618
    }
}
