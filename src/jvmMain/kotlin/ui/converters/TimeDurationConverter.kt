package ui.converters

import DAYS
import HOURS
import MILLISECONDS
import MINUTES
import MillisecondsTimeUnit
import SECONDS
import picocli.CommandLine
import ui.ApplicationUsageError

class TimeDurationConverter : CommandLine.ITypeConverter<MillisecondsTimeUnit> {
    override fun convert(duration: String?): MillisecondsTimeUnit {
        val split = duration?.trim()?.split(".") ?: emptyList()

        if (split.size == 2) {
            val time = split[0].toIntOrNull()
            val timeInMillis = when (split[1]) {
                "ms" -> time?.MILLISECONDS
                "s" -> time?.SECONDS
                "m" -> time?.MINUTES
                "h" -> time?.HOURS
                "d" -> time?.DAYS
                else -> null
            }

            if (timeInMillis != null) {
                return timeInMillis
            }
        }

        throw ApplicationUsageError("Expected time with time unit but got $duration")
    }
}
