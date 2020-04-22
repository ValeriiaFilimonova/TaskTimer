val Int.MILLISECONDS: MillisecondsTimeUnit
    get() = MillisecondsTimeUnit(this.toLong())

val Int.SECONDS: MillisecondsTimeUnit
    get() = this.MILLISECONDS * 1000

val Int.MINUTES: MillisecondsTimeUnit
    get() = this.SECONDS * 60

val Int.HOURS: MillisecondsTimeUnit
    get() = this.MINUTES * 60

val Int.DAYS: MillisecondsTimeUnit
    get() = this.HOURS * 24

class MillisecondsTimeUnit(value: Long) : Comparable<MillisecondsTimeUnit> {
    var value: Long

    init {
        if (value < 0) {
            throw RuntimeException("Time must be positive or zero")
        }

        this.value = value
    }

    override fun equals(other: Any?): Boolean {
        if (other is MillisecondsTimeUnit) {
            return other.value == value
        }
        if (other is Long || other is Int) {
            return other == value
        }
        return false
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun compareTo(other: MillisecondsTimeUnit): Int {
        return compareTo(other.value)
    }

    fun toLong(): Long {
        return value
    }

    fun toDouble(): Double {
        return value.toDouble()
    }
}

operator fun MillisecondsTimeUnit.compareTo(other: Int): Int = compareTo(other.toLong())

operator fun MillisecondsTimeUnit.compareTo(other: Long): Int = if (value > other) 1 else if (value == other) 0 else -1

operator fun MillisecondsTimeUnit.plus(other: MillisecondsTimeUnit): MillisecondsTimeUnit = plus(other.value)

operator fun MillisecondsTimeUnit.plus(other: Int): MillisecondsTimeUnit = plus(other.toLong())

operator fun MillisecondsTimeUnit.plus(other: Long): MillisecondsTimeUnit = MillisecondsTimeUnit(value + other)

operator fun MillisecondsTimeUnit.minus(other: MillisecondsTimeUnit): MillisecondsTimeUnit = minus(other.value)

operator fun MillisecondsTimeUnit.minus(other: Int): MillisecondsTimeUnit = minus(other.toLong())

operator fun MillisecondsTimeUnit.minus(other: Long): MillisecondsTimeUnit = MillisecondsTimeUnit(value - other)

operator fun MillisecondsTimeUnit.times(other: MillisecondsTimeUnit): MillisecondsTimeUnit = times(other.value)

operator fun MillisecondsTimeUnit.times(other: Int): MillisecondsTimeUnit = times(other.toLong())

operator fun MillisecondsTimeUnit.times(other: Long): MillisecondsTimeUnit = MillisecondsTimeUnit(value * other)

operator fun MillisecondsTimeUnit.div(other: MillisecondsTimeUnit): MillisecondsTimeUnit = div(other.value)

operator fun MillisecondsTimeUnit.div(other: Long): MillisecondsTimeUnit = MillisecondsTimeUnit(value / other)

operator fun MillisecondsTimeUnit.div(other: Double): Double = value / other

operator fun MillisecondsTimeUnit.rem(other: MillisecondsTimeUnit): Long = rem(other.value)

operator fun MillisecondsTimeUnit.rem(other: Int): Long = rem(other.toLong())

operator fun MillisecondsTimeUnit.rem(other: Long): Long = value % other
