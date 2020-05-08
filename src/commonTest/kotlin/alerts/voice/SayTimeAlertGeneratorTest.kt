package alerts.voice

import days
import hours
import minutes
import seconds
import plus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.ExperimentalTime

@ExperimentalTime
open class SayTimeAlertGeneratorTest {
    @Test
    fun getStringRepresentation_oneMinuteDuration_shouldUseSingularUnitName() {
        val text = SayTimeAlertGenerator.getStringRepresentation(1.minutes)

        assertEquals("1 minute", text)
    }

    @Test
    fun getStringRepresentation_twoHoursDuration_shouldUsePluralUnitName() {
        val text = SayTimeAlertGenerator.getStringRepresentation(2.hours)

        assertEquals("2 hours", text)
    }

    @Test
    fun getStringRepresentation_dayDurationWithHalf_shouldNotSplitOnDaysAndHours() {
        val text = SayTimeAlertGenerator.getStringRepresentation(3.days + 12.hours)

        assertEquals("3 and a half days", text)
    }

    @Test
    fun getStringRepresentation_oneHourDurationInSeconds_shouldUseTheBiggestPossibleTimeUnit() {
        val text = SayTimeAlertGenerator.getStringRepresentation(345600.seconds)

        assertEquals("4 days", text)
    }

    @Test
    fun getStringRepresentation_complexDuration_shouldSplitOnAllPossibleEvenParts() {
        val text = SayTimeAlertGenerator.getStringRepresentation(1.days + 13.hours + 27.minutes + 1.seconds)

        assertEquals("1 day 13 hours 27 minutes 1 second", text)
    }
}
