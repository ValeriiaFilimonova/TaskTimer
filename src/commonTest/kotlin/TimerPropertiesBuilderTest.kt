import alerts.Alert
import alerts.AlertGenerator
import alerts.AlertGenerators
import alerts.sound.Sound
import alerts.sound.SoundAlert
import alerts.voice.VoiceAlert
import tasks.*
import kotlin.test.*
import kotlin.time.ExperimentalTime

open class TimerPropertiesBuilderTest {
    private val fakeGenerator = object : AlertGenerator {
        override fun generate(task: AlertTask, vararg params: Any): Alert {
            TODO("empty generator")
        }
    }

    @Test
    fun buildProperties_withZeroDuration_shouldThrowError() {
        val error = assertFailsWith(TimerPropertiesInitError::class) {
            TimerPropertiesBuilder(0.milliseconds).build()
        }
        assertEquals("Timer duration can't be zero or exceed 20 days", error.message)
    }

    @Test
    fun buildProperties_withTooLongDuration_shouldThrowError() {
        val error = assertFailsWith(TimerPropertiesInitError::class) {
            TimerPropertiesBuilder(21.days).build()
        }
        assertEquals("Timer duration can't be zero or exceed 20 days", error.message)
    }

    @Test
    fun buildProperties_withNotNullFinalAlarm_shouldAddAfterElapsedTask() {
        val timerProperties = TimerPropertiesBuilder(1.minutes).build()

        assertEquals(timerProperties.tasks.count(), 1)

        val finalAlarmTask = timerProperties.tasks.first()
        assertTrue(finalAlarmTask is AfterElapsedTimeTask)
        assertEquals(timerProperties.durationInMillis, finalAlarmTask.executionTimeInMillis)

        val alert = finalAlarmTask.getAlert()
        assertTrue(alert is SoundAlert)
        assertEquals(Sound.WISE_MASTER, alert.sound)
    }

    @Test
    fun tickInterval_withIntervalBiggerThanDuration_shouldThrowError() {
        val error = assertFailsWith(TimerPropertiesInitError::class) {
            TimerPropertiesBuilder(1.seconds).tickInterval(2.seconds).build()
        }
        assertEquals("Tick interval can't exceed timer duration", error.message)
    }

    @Test
    fun finalAlarm_withNullSound_shouldNotAddTaskForFinalAlarm() {
        val timerProperties = TimerPropertiesBuilder(1.minutes).finalAlarm(null).build()
        assertEquals(0, timerProperties.tasks.count())
    }

    @Test
    fun finalAlarm_withSound_shouldAddTaskWithGivenSoundAlert() {
        val expectedSound = Sound.PLAYFUL_MOOD
        val timerProperties = TimerPropertiesBuilder(1.minutes).finalAlarm(expectedSound).build()

        val finalAlarmTask = timerProperties.tasks.first() as AlertTask
        val finalAlarmAlert = finalAlarmTask.getAlert() as SoundAlert

        assertEquals(expectedSound, finalAlarmAlert.sound)
    }

    @Test
    fun afterPassed_withTimeBiggerThanDuration_shouldThrowError() {
        val error = assertFailsWith(TimerPropertiesInitError::class) {
            TimerPropertiesBuilder(1.minutes).afterPassed(2.minutes, fakeGenerator)
        }

        assertEquals("Task execution time can't exceed timer duration", error.message)
    }

    @Test
    fun afterPassed_withProperParameters_shouldAddAfterElapsedTask() {
        val expectedTaskTime = 30.seconds
        val timerProperties = TimerPropertiesBuilder(1.minutes).afterPassed(expectedTaskTime, fakeGenerator).build()

        assertEquals(2, timerProperties.tasks.count())
        assertTrue(timerProperties.tasks.all { t -> t is AfterElapsedTimeTask })
        assertEquals(expectedTaskTime, timerProperties.tasks.first().executionTimeInMillis)
    }

    @Test
    fun beforeLeft_withTimeBiggerThanDuration_shouldThrowError() {
        val error = assertFailsWith(TimerPropertiesInitError::class) {
            TimerPropertiesBuilder(1.minutes).beforeLeft(2.minutes, fakeGenerator)
        }

        assertEquals("Task execution time can't exceed timer duration", error.message)
    }

    @Test
    fun beforeLeft_withProperParameters_shouldAddBeforeLeftTask() {
        val timerProperties = TimerPropertiesBuilder(1.minutes).beforeLeft(15.seconds, fakeGenerator).build()

        assertEquals(2, timerProperties.tasks.count())

        val beforeLeftTask = timerProperties.tasks.first()
        assertTrue(beforeLeftTask is BeforeTimeLeftTask)
        assertEquals(45.seconds, beforeLeftTask.executionTimeInMillis)
    }

    @Test
    fun repeatEvery_withTimeBiggerThanDuration_shouldThrowError() {
        val error = assertFailsWith(TimerPropertiesInitError::class) {
            TimerPropertiesBuilder(1.minutes).repeatEvery(2.minutes, generator = fakeGenerator)
        }

        assertEquals("Task execution time can't be zero or exceed timer duration", error.message)
    }

    @Test
    fun repeatEvery_withDelayBiggerThanDuration_shouldThrowError() {
        val error = assertFailsWith(TimerPropertiesInitError::class) {
            TimerPropertiesBuilder(1.minutes).repeatEvery(10.seconds, 1.minutes, generator = fakeGenerator)
        }

        assertEquals("Task delay time can't exceed timer duration", error.message)
    }

    @Test
    fun repeatEvery_withFinishTimeBiggerThanDuration_shouldThrowError() {
        val error = assertFailsWith(TimerPropertiesInitError::class) {
            TimerPropertiesBuilder(1.minutes).repeatEvery(10.seconds, 5.seconds, 2.minutes, fakeGenerator)
        }

        assertEquals("Task finish time can't exceed timer duration", error.message)
    }

    @Test
    fun repeatEvery_withTimeValueOnly_shouldAddRepeatableTaskWithDefaultDelayAndFinish() {
        val timerProperties =
            TimerPropertiesBuilder(2.minutes).repeatEvery(30.seconds, generator = fakeGenerator).build()

        assertEquals(2, timerProperties.tasks.count())

        val repeatableTask = timerProperties.tasks.first()
        assertTrue(repeatableTask is RepeatableTask)
        assertEquals(30.seconds, repeatableTask.repeatEvery)
        assertEquals(30.seconds, repeatableTask.repeatFrom)
        assertEquals(2.minutes, repeatableTask.repeatTill)
    }

    @Test
    fun repeatEvery_withAllParameters_shouldAddRepeatableTaskWithCustomDelayAndFinish() {
        val timerProperties = TimerPropertiesBuilder(10.minutes)
            .repeatEvery(15.seconds, 10.seconds, 5.minutes, fakeGenerator)
            .build()

        assertEquals(2, timerProperties.tasks.count())

        val repeatableTask = timerProperties.tasks.first()
        assertTrue(repeatableTask is RepeatableTask)
        assertEquals(15.seconds, repeatableTask.repeatEvery)
        assertEquals(10.seconds, repeatableTask.repeatFrom)
        assertEquals(5.minutes, repeatableTask.repeatTill)
    }

    @Test
    fun remindAfterFinishEvery_withZeroExecutionTime_shouldThrowError() {
        val error = assertFailsWith(TimerPropertiesInitError::class) {
            TimerPropertiesBuilder(1.minutes).remindAfterFinishEvery(0.seconds, fakeGenerator)
        }

        assertEquals("Task execution time can't be zero", error.message)
    }

    @Test
    fun remindAfterFinishEvery_withProperParameter_shouldAddRepeatableTask() {
        val timerProperties = TimerPropertiesBuilder(30.seconds)
            .remindAfterFinishEvery(5.seconds, fakeGenerator)
            .build()

        assertEquals(2, timerProperties.tasks.count())

        val repeatableTask = timerProperties.tasks.first()
        assertTrue(repeatableTask is RepeatableTask)
        assertEquals(5.seconds, repeatableTask.repeatEvery)
        assertEquals(35.seconds, repeatableTask.repeatFrom)
        assertNull(repeatableTask.repeatTill)
    }

    @Test
    @ExperimentalTime
    fun buildProperties_withAllPossibleParameters_shouldBuildCorrectPropertiesObject() {
        val task = object : Task {
            override val executionTimeInMillis: MillisecondsTimeUnit = 40.seconds

            override fun execute() {
                TODO("do nothing")
            }
        }
        val (duration, tickInterval, tasks) = TimerPropertiesBuilder(20.minutes)
            .tickInterval(30.seconds)
            .finalAlarm(Sound.PRE_DAWN_SLEEP)
            .task(task)
            .afterPassed(10.minutes, AlertGenerators.getVoiceAlertGenerator("half of the time elapsed"))
            .beforeLeft(1.minutes, AlertGenerators.getSayTimeAlertGenerator())
            .repeatEvery(1.minutes, 1.minutes, 18.minutes, AlertGenerators.getSoundAlertGenerator(Sound.TING_A_LING))
            .remindAfterFinishEvery(10.seconds, AlertGenerators.getSoundAlertGenerator(Sound.FAIRY_DISAPPEARANCE))
            .build()

        assertEquals(20.minutes, duration)
        assertEquals(30.seconds, tickInterval)
        assertEquals(6, tasks.count())

        val customTask = tasks.elementAt(0)
        assertFalse(customTask is AlertTask)
        assertEquals(40.seconds, customTask.executionTimeInMillis)

        val afterPassedTask = tasks.elementAt(1)
        assertTrue(afterPassedTask is AfterElapsedTimeTask)
        assertEquals(10.minutes, afterPassedTask.executionTimeInMillis)
        val afterPassedAlert = afterPassedTask.getAlert()
        assertTrue(afterPassedAlert is VoiceAlert)
        assertEquals("half of the time elapsed", afterPassedAlert.text)

        val beforeLeftTask = tasks.elementAt(2)
        assertTrue(beforeLeftTask is BeforeTimeLeftTask)
        assertEquals(beforeLeftTask.executionTimeInMillis, 19.minutes)
        val beforeLeftAlert = beforeLeftTask.getAlert()
        assertTrue(beforeLeftAlert is VoiceAlert)
        assertEquals("1 minute left", beforeLeftAlert.text)

        val repeatEveryTask = tasks.elementAt(3)
        assertTrue(repeatEveryTask is RepeatableTask)
        assertEquals(repeatEveryTask.repeatEvery, 1.minutes)
        assertEquals(repeatEveryTask.repeatFrom, 1.minutes)
        assertEquals(repeatEveryTask.repeatTill, 18.minutes)
        assertEquals(repeatEveryTask.executionTimeInMillis, 0.milliseconds)
        val repeatEveryAlert = repeatEveryTask.getAlert()
        assertTrue(repeatEveryAlert is SoundAlert)
        assertEquals(Sound.TING_A_LING, repeatEveryAlert.sound)

        val remindAfterTask = tasks.elementAt(4)
        assertTrue(remindAfterTask is RepeatableTask)
        assertEquals(remindAfterTask.repeatEvery, 10.seconds)
        assertEquals(remindAfterTask.repeatFrom, 20.minutes + 10.seconds)
        assertTrue(remindAfterTask.getAlert() is SoundAlert)

        val finalAlarmTask = tasks.elementAt(5)
        assertTrue(finalAlarmTask is AfterElapsedTimeTask)
        assertEquals(duration, finalAlarmTask.executionTimeInMillis)
    }
}
