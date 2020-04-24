package alerts.voice

import MillisecondsTimeUnit
import alerts.AlertGenerationError
import alerts.AlertGenerator
import tasks.AfterElapsedTimeTask
import tasks.AlertTask
import tasks.BeforeTimeLeftTask
import tasks.RepeatableTask
import kotlin.time.*

@ExperimentalTime
class SayTimeAlertGenerator : AlertGenerator {
    override fun generate(task: AlertTask, vararg params: Any): VoiceAlert {
        val text = when (task) {
            is AfterElapsedTimeTask -> "${getStringRepresentation(task.executionTimeInMillis)}elapsed"
            is BeforeTimeLeftTask -> "${getStringRepresentation(task.beforeTime)}left"
            is RepeatableTask -> "${getStringRepresentation(task.executionTimeInMillis)}passed"
            else -> throw AlertGenerationError("Unsupported task type ${task::class.simpleName}")
        }

        return VoiceAlert(text)
    }

    // TODO migrate everything to Duration.class when not experimental
    companion object {
        fun getStringRepresentation(time: MillisecondsTimeUnit): String = buildString {
            var leftDuration = time.toLong().milliseconds

            val (durationInDays, daysText) = getStringFor(leftDuration.inDays, DurationUnit.DAYS)
            if (durationInDays != Duration.ZERO) {
                append(daysText).append(" ")
                leftDuration -= durationInDays
            }

            val (durationInHours, hoursText) = getStringFor(leftDuration.inHours, DurationUnit.HOURS)
            if (durationInHours != Duration.ZERO) {
                append(hoursText).append(" ")
                leftDuration -= durationInHours
            }

            val (durationInMins, minsText) = getStringFor(leftDuration.inMinutes, DurationUnit.MINUTES)
            if (durationInMins != Duration.ZERO) {
                append(minsText).append(" ")
                leftDuration -= durationInMins
            }

            val (durationInSecs, secsText) = getStringFor(leftDuration.inSeconds, DurationUnit.SECONDS)
            if (durationInSecs != Duration.ZERO) {
                append(secsText).append(" ")
                leftDuration -= durationInSecs
            }
        }

        private fun getStringFor(unitsNumber: Double, unit: DurationUnit): Pair<Duration, String> {
            if (unitsNumber < 1) {
                return Pair(Duration.ZERO, "")
            }

            val intUnitsNumber = unitsNumber.toInt()
            val units = getUnitNameInProperForm(intUnitsNumber, unit.fullName())

            if (unitsNumber - 0.5 == intUnitsNumber.toDouble()) {
                return Pair(unitsNumber.toDuration(unit), "$intUnitsNumber and a half ${unit.fullName()}s")
            }

            return Pair(intUnitsNumber.toDuration(unit), "$intUnitsNumber $units")
        }

        private fun getUnitNameInProperForm(numberOfUnits: Int, unitNameSingular: String) =
            if (numberOfUnits > 1) "${unitNameSingular}s" else unitNameSingular

        fun DurationUnit.fullName(): String = when (this) {
            DurationUnit.NANOSECONDS -> "nanosecond"
            DurationUnit.MICROSECONDS -> "microsecond"
            DurationUnit.MILLISECONDS -> "millisecond"
            DurationUnit.SECONDS -> "second"
            DurationUnit.MINUTES -> "minute"
            DurationUnit.HOURS -> "hour"
            DurationUnit.DAYS -> "day"
        }
    }
}
