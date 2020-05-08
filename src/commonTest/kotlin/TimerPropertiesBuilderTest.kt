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
            TimerPropertiesBuilder(0.MILLISECONDS).build()
        }
        assertEquals("Timer duration can't be zero or exceed 20 days", error.message)
    }

    @Test
    fun buildProperties_withTooLongDuration_shouldThrowError() {
        val error = assertFailsWith(TimerPropertiesInitError::class) {
            TimerPropertiesBuilder(21.DAYS).build()
        }
        assertEquals("Timer duration can't be zero or exceed 20 days", error.message)
    }

    @Test
    fun buildProperties_withNotNullFinalAlarm_shouldAddAfterElapsedTask() {
        val timerProperties = TimerPropertiesBuilder(1.MINUTES).build()

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
            TimerPropertiesBuilder(1.SECONDS).tickInterval(2.SECONDS).build()
        }
        assertEquals("Tick interval can't exceed timer duration", error.message)
    }

    @Test
    fun finalAlarm_withNullSound_shouldNotAddTaskForFinalAlarm() {
        val timerProperties = TimerPropertiesBuilder(1.MINUTES).finalAlarm(null).build()
        assertEquals(0, timerProperties.tasks.count())
    }

    @Test
    fun finalAlarm_withSound_shouldAddTaskWithGivenSoundAlert() {
        val expectedSound = Sound.PLAYFUL_MOOD
        val timerProperties = TimerPropertiesBuilder(1.MINUTES).finalAlarm(expectedSound).build()

        val finalAlarmTask = timerProperties.tasks.first() as AlertTask
        val finalAlarmAlert = finalAlarmTask.getAlert() as SoundAlert

        assertEquals(expectedSound, finalAlarmAlert.sound)
    }

    @Test
    fun afterPassed_withTimeBiggerThanDuration_shouldThrowError() {
        val error = assertFailsWith(TimerPropertiesInitError::class) {
            TimerPropertiesBuilder(1.MINUTES).afterPassed(2.MINUTES, fakeGenerator)
        }

        assertEquals("Task execution time can't exceed timer duration", error.message)
    }

    @Test
    fun afterPassed_withProperParameters_shouldAddAfterElapsedTask() {
        val expectedTaskTime = 30.SECONDS
        val timerProperties = TimerPropertiesBuilder(1.MINUTES).afterPassed(expectedTaskTime, fakeGenerator).build()

        assertEquals(2, timerProperties.tasks.count())
        assertTrue(timerProperties.tasks.all { t -> t is AfterElapsedTimeTask })
        assertEquals(expectedTaskTime, timerProperties.tasks.first().executionTimeInMillis)
    }

    @Test
    fun beforeLeft_withTimeBiggerThanDuration_shouldThrowError() {
        val error = assertFailsWith(TimerPropertiesInitError::class) {
            TimerPropertiesBuilder(1.MINUTES).beforeLeft(2.MINUTES, fakeGenerator)
        }

        assertEquals("Task execution time can't exceed timer duration", error.message)
    }

    @Test
    fun beforeLeft_withProperParameters_shouldAddBeforeLeftTask() {
        val timerProperties = TimerPropertiesBuilder(1.MINUTES).beforeLeft(15.SECONDS, fakeGenerator).build()

        assertEquals(2, timerProperties.tasks.count())

        val beforeLeftTask = timerProperties.tasks.first()
        assertTrue(beforeLeftTask is BeforeTimeLeftTask)
        assertEquals(45.SECONDS, beforeLeftTask.executionTimeInMillis)
    }

    @Test
    fun repeatEvery_withTimeBiggerThanDuration_shouldThrowError() {
        val error = assertFailsWith(TimerPropertiesInitError::class) {
            TimerPropertiesBuilder(1.MINUTES).repeatEvery(2.MINUTES, generator = fakeGenerator)
        }

        assertEquals("Task execution time can't be zero or exceed timer duration", error.message)
    }

    @Test
    fun repeatEvery_withDelayBiggerThanDuration_shouldThrowError() {
        val error = assertFailsWith(TimerPropertiesInitError::class) {
            TimerPropertiesBuilder(1.MINUTES).repeatEvery(10.SECONDS, 1.MINUTES, generator = fakeGenerator)
        }

        assertEquals("Task delay time can't exceed timer duration", error.message)
    }

    @Test
    fun repeatEvery_withFinishTimeBiggerThanDuration_shouldThrowError() {
        val error = assertFailsWith(TimerPropertiesInitError::class) {
            TimerPropertiesBuilder(1.MINUTES).repeatEvery(10.SECONDS, 5.SECONDS, 2.MINUTES, fakeGenerator)
        }

        assertEquals("Task finish time can't exceed timer duration", error.message)
    }

    @Test
    fun repeatEvery_withTimeValueOnly_shouldAddRepeatableTaskWithDefaultDelayAndFinish() {
        val timerProperties =
            TimerPropertiesBuilder(2.MINUTES).repeatEvery(30.SECONDS, generator = fakeGenerator).build()

        assertEquals(2, timerProperties.tasks.count())

        val repeatableTask = timerProperties.tasks.first()
        assertTrue(repeatableTask is RepeatableTask)
        assertEquals(30.SECONDS, repeatableTask.repeatEvery)
        assertEquals(30.SECONDS, repeatableTask.repeatFrom)
        assertEquals(2.MINUTES, repeatableTask.repeatTill)
    }

    @Test
    fun repeatEvery_withAllParameters_shouldAddRepeatableTaskWithCustomDelayAndFinish() {
        val timerProperties = TimerPropertiesBuilder(10.MINUTES)
            .repeatEvery(15.SECONDS, 10.SECONDS, 5.MINUTES, fakeGenerator)
            .build()

        assertEquals(2, timerProperties.tasks.count())

        val repeatableTask = timerProperties.tasks.first()
        assertTrue(repeatableTask is RepeatableTask)
        assertEquals(15.SECONDS, repeatableTask.repeatEvery)
        assertEquals(10.SECONDS, repeatableTask.repeatFrom)
        assertEquals(5.MINUTES, repeatableTask.repeatTill)
    }

    @Test
    fun remindAfterFinishEvery_withZeroExecutionTime_shouldThrowError() {
        val error = assertFailsWith(TimerPropertiesInitError::class) {
            TimerPropertiesBuilder(1.MINUTES).remindAfterFinishEvery(0.SECONDS, fakeGenerator)
        }

        assertEquals("Task execution time can't be zero", error.message)
    }

    @Test
    fun remindAfterFinishEvery_withProperParameter_shouldAddRepeatableTask() {
        val timerProperties = TimerPropertiesBuilder(30.SECONDS)
            .remindAfterFinishEvery(5.SECONDS, fakeGenerator)
            .build()

        assertEquals(2, timerProperties.tasks.count())

        val repeatableTask = timerProperties.tasks.first()
        assertTrue(repeatableTask is RepeatableTask)
        assertEquals(5.SECONDS, repeatableTask.repeatEvery)
        assertEquals(35.SECONDS, repeatableTask.repeatFrom)
        assertNull(repeatableTask.repeatTill)
    }

    @Test
    @ExperimentalTime
    fun buildProperties_withAllPossibleParameters_shouldBuildCorrectPropertiesObject() {
        val task = object : Task {
            override val executionTimeInMillis: MillisecondsTimeUnit = 40.SECONDS

            override fun execute() {
                TODO("do nothing")
            }
        }
        val (duration, tickInterval, tasks) = TimerPropertiesBuilder(20.MINUTES)
            .tickInterval(30.SECONDS)
            .finalAlarm(Sound.PRE_DAWN_SLEEP)
            .task(task)
            .afterPassed(10.MINUTES, AlertGenerators.getVoiceAlertGenerator("half of the time elapsed"))
            .beforeLeft(1.MINUTES, AlertGenerators.getSayTimeAlertGenerator())
            .repeatEvery(1.MINUTES, 1.MINUTES, 18.MINUTES, AlertGenerators.getSoundAlertGenerator(Sound.TING_A_LING))
            .remindAfterFinishEvery(10.SECONDS, AlertGenerators.getSoundAlertGenerator(Sound.FAIRY_DISAPPEARANCE))
            .build()

        assertEquals(20.MINUTES, duration)
        assertEquals(30.SECONDS, tickInterval)
        assertEquals(6, tasks.count())

        val customTask = tasks.elementAt(0)
        assertFalse(customTask is AlertTask)
        assertEquals(40.SECONDS, customTask.executionTimeInMillis)

        val afterPassedTask = tasks.elementAt(1)
        assertTrue(afterPassedTask is AfterElapsedTimeTask)
        assertEquals(10.MINUTES, afterPassedTask.executionTimeInMillis)
        val afterPassedAlert = afterPassedTask.getAlert()
        assertTrue(afterPassedAlert is VoiceAlert)
        assertEquals("half of the time elapsed", afterPassedAlert.text)

        val beforeLeftTask = tasks.elementAt(2)
        assertTrue(beforeLeftTask is BeforeTimeLeftTask)
        assertEquals(beforeLeftTask.executionTimeInMillis, 19.MINUTES)
        val beforeLeftAlert = beforeLeftTask.getAlert()
        assertTrue(beforeLeftAlert is VoiceAlert)
        assertEquals("1 minute left", beforeLeftAlert.text)

        val repeatEveryTask = tasks.elementAt(3)
        assertTrue(repeatEveryTask is RepeatableTask)
        assertEquals(repeatEveryTask.repeatEvery, 1.MINUTES)
        assertEquals(repeatEveryTask.repeatFrom, 1.MINUTES)
        assertEquals(repeatEveryTask.repeatTill, 18.MINUTES)
        assertEquals(repeatEveryTask.executionTimeInMillis, 0.MILLISECONDS)
        val repeatEveryAlert = repeatEveryTask.getAlert()
        assertTrue(repeatEveryAlert is SoundAlert)
        assertEquals(Sound.TING_A_LING, repeatEveryAlert.sound)

        val remindAfterTask = tasks.elementAt(4)
        assertTrue(remindAfterTask is RepeatableTask)
        assertEquals(remindAfterTask.repeatEvery, 10.SECONDS)
        assertEquals(remindAfterTask.repeatFrom, 20.MINUTES + 10.SECONDS)
        assertTrue(remindAfterTask.getAlert() is SoundAlert)

        val finalAlarmTask = tasks.elementAt(5)
        assertTrue(finalAlarmTask is AfterElapsedTimeTask)
        assertEquals(duration, finalAlarmTask.executionTimeInMillis)
    }
}
