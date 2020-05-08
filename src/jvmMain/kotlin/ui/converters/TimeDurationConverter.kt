package ui.converters

import days
import hours
import milliseconds
import minutes
import MillisecondsTimeUnit
import seconds
import picocli.CommandLine
import ui.ApplicationUsageError

class TimeDurationConverter : CommandLine.ITypeConverter<MillisecondsTimeUnit> {
    override fun convert(duration: String?): MillisecondsTimeUnit {
        val split = duration?.trim()?.split(".") ?: emptyList()

        if (split.size == 2) {
            val time = split[0].toIntOrNull()
            val timeInMillis = when (split[1]) {
                "ms" -> time?.milliseconds
                "s" -> time?.seconds
                "m" -> time?.minutes
                "h" -> time?.hours
                "d" -> time?.days
                else -> null
            }

            if (timeInMillis != null) {
                return timeInMillis
            }
        }

        throw ApplicationUsageError("Expected time with time unit but got $duration")
    }
}
