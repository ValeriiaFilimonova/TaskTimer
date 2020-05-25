import alerts.Alert
import alerts.AlertGenerator
import alerts.AlertGenerators
import alerts.sound.Sound
import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.junit.jupiter.api.*
import tasks.AlertTask
import kotlin.time.ExperimentalTime

@Tags(Tag("wait"))
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class TimerTest {
    private val executionOffset = Offset.offset(25L)

    @BeforeAll
    fun mockDependencies() {
        mockkObject(DependenciesFactory)

        every { DependenciesFactory.getPlayer() } returns mockk(relaxed = true)
        every { DependenciesFactory.getSpeaker() } returns mockk(relaxed = true)
    }

    @AfterAll
    fun clearMocks() {
        unmockkAll()
    }

    @Test
    fun runTimer_withOnTickMethodAndNoFinalAlarm_shouldCallSpecifiedOnTickTwoTimes() {
        val player = DependenciesFactory.getPlayer()
        val timer = simpleTimer(410.milliseconds)

        val captor = captureTime {
            timer.onTick = { track("onTick") {} }

            registerStart {
                timer.start()
                Thread.sleep(timer.duration.toLong())
                timer.stop()
            }
        }

        verify(exactly = 0) {
            player.playOnce(Sound.WISE_MASTER)
        }

        val onTickCaptures = captor["onTick"]

        assertThat(onTickCaptures.size).isEqualTo(2)
        assertThat(onTickCaptures[0].executionTime()).isCloseTo(200L, executionOffset)
        assertThat(onTickCaptures[1].executionTime()).isCloseTo(400L, executionOffset)
    }

    @Test
    fun runTimer_withFinalAlarmSoundOverridden_shouldCallPlayerToPlaySound() {
        val player = DependenciesFactory.getPlayer()
        val timer = simpleTimer(213.milliseconds, finalAlarm = Sound.PLAYFUL_MOOD)

        val captor = captureTime {
            every { player.playOnce(any()) } answers { track("playFinalSound") }

            registerStart {
                timer.start()
                Thread.sleep(timer.duration.toLong())
                timer.stop()
            }
        }

        verify(exactly = 1) {
            player.playOnce(Sound.PLAYFUL_MOOD)
        }

        val actualExecutionTime = captor["playFinalSound"].firstExecution()
        val expectedExecutionTime = timer.duration.toLong()
        assertThat(actualExecutionTime).isCloseTo(expectedExecutionTime, executionOffset)
    }

    @Test
    fun runTimer_withAfterElapsedTimeTask_shouldCallSpeakerAtProperTime() {
        val speaker = DependenciesFactory.getSpeaker()
        val timer = timerWithTask(300.milliseconds)
            .afterPassed(150.milliseconds, AlertGenerators.getVoiceAlertGenerator("the text"))
            .timer()

        val captor = captureTime {
            every { speaker.say(any()) } answers { track("sayText") }

            registerStart {
                timer.start()
                Thread.sleep(timer.duration.toLong())
                timer.stop()
            }
        }

        verify(exactly = 1) {
            speaker.say("the text")
        }

        val actualExecutionTime = captor["sayText"].firstExecution()
        assertThat(actualExecutionTime).isCloseTo(150, executionOffset)
    }

    @Test
    fun runTimer_withBeforeLeftTimeTask_shouldRunTaskAtProperTime() {
        val captor = captureTime {
            val timer = timerWithTask(400.milliseconds)
                .beforeLeft(70.milliseconds, trackAlert("beforeLeftAlert"))
                .timer()

            registerStart {
                timer.start()
                Thread.sleep(timer.duration.toLong())
                timer.stop()
            }
        }

        val actualExecutionTime = captor["beforeLeftAlert"].firstExecution()
        assertThat(actualExecutionTime).isCloseTo(330L, executionOffset)
    }

    @Test
    fun runTimer_withRepeatableTaskAndDefaultDelay_shouldRunTaskMultipleTimes() {
        val captor = captureTime {
            val timer = timerWithTask(450.milliseconds)
                .repeatEvery(150.milliseconds, generator = trackAlert("repeatDefaultDelay"))
                .timer()

            registerStart {
                timer.start()
                Thread.sleep(timer.duration.toLong())
                timer.stop()
            }
        }

        val repeatCaptures = captor["repeatDefaultDelay"]
        assertThat(repeatCaptures.size).isEqualTo(3)
        assertThat(repeatCaptures[0].executionTime()).isCloseTo(150L, executionOffset)
        assertThat(repeatCaptures[1].executionTime()).isCloseTo(300L, executionOffset)
        assertThat(repeatCaptures[2].executionTime()).isCloseTo(450L, executionOffset)
    }

    @Test
    fun runTimer_withRepeatableTaskAndCustomDelay_shouldRunTaskMultipleTimes() {
        val captor = captureTime {
            val timer = timerWithTask(500.milliseconds)
                .repeatEvery(120.milliseconds, 100.milliseconds, generator = trackAlert("repeatCustomDelay"))
                .timer()

            registerStart {
                timer.start()
                Thread.sleep(timer.duration.toLong())
                timer.stop()
            }
        }

        val repeatCaptures = captor["repeatCustomDelay"]
        assertThat(repeatCaptures.size).isEqualTo(4)
        assertThat(repeatCaptures[0].executionTime()).isCloseTo(100L, executionOffset)
        assertThat(repeatCaptures[1].executionTime()).isCloseTo(220L, executionOffset)
        assertThat(repeatCaptures[2].executionTime()).isCloseTo(340L, executionOffset)
        assertThat(repeatCaptures[3].executionTime()).isCloseTo(460L, executionOffset)
    }

    @Test
    fun runTimer_withRepeatableTaskAndCustomFinishTime_shouldFinishTaskBeforeTimerEnds() {
        val captor = captureTime {
            val timer = timerWithTask(300.milliseconds)
                .repeatEvery(90.milliseconds, 100.milliseconds, 270.milliseconds, trackAlert("repeatCustomFinish"))
                .timer()

            registerStart {
                timer.start()
                Thread.sleep(timer.duration.toLong())
                timer.stop()
            }
        }

        val repeatCaptures = captor["repeatCustomFinish"]
        assertThat(repeatCaptures.size).isEqualTo(2)
        assertThat(repeatCaptures[0].executionTime()).isCloseTo(100L, executionOffset)
        assertThat(repeatCaptures[1].executionTime()).isCloseTo(190L, executionOffset)
    }

    @Test
    fun runTimer_withRepeatableAfterFinishTask_shouldExecuteTaskMultipleTimesAfterTimesFinish() {
        val player = DependenciesFactory.getPlayer()
        val captor = captureTime {
            val timer = timerWithTask(200.milliseconds, finalAlarm = Sound.MORNING_DEW_SHORT)
                .remindAfterFinishEvery(130.milliseconds, trackAlert("remindAfterFinish"))
                .timer()

            every { player.playOnce(any()) } answers { track("playFinalSound") }

            registerStart {
                timer.start()
                Thread.sleep(500)
                timer.stop()
            }
        }

        verify(exactly = 1) {
            player.playOnce(Sound.MORNING_DEW_SHORT)
        }

        assertThat(captor["playFinalSound"].firstExecution()).isCloseTo(200L, executionOffset)

        val remindCaptures = captor["remindAfterFinish"]
        assertThat(remindCaptures.size).isEqualTo(2)
        assertThat(remindCaptures[0].executionTime()).isCloseTo(330L, executionOffset)
        assertThat(remindCaptures[1].executionTime()).isCloseTo(460L, executionOffset)
    }

    @Test
    fun runTimer_willAllPossibleSettings_shouldExecuteAllTasksAtProperTime() {
        val player = DependenciesFactory.getPlayer()
        val captor = captureTime {
            val properties = TimerPropertiesBuilder(500.milliseconds)
                .finalAlarm(Sound.NIGHT_IS_COMING_SHORT)
                .tickInterval(50.milliseconds)
                .afterPassed(310.milliseconds, trackAlert("afterPassed"))
                .beforeLeft(40.milliseconds, trackAlert("beforeLeft"))
                .repeatEvery(160.milliseconds, generator = trackAlert("repeatEvery"))
                .remindAfterFinishEvery(150.milliseconds, trackAlert("afterFinish"))
                .build()
            val timer = Timer(properties).also { it.onTick = { track("onTick") } }

            every { player.playOnce(any()) } answers { track("playFinalSound") }

            registerStart {
                timer.start()
                Thread.sleep(660)
                timer.stop()
            }
        }

        verify(exactly = 1) {
            player.playOnce(Sound.NIGHT_IS_COMING_SHORT)
        }

        val onTickCaptures = captor["onTick"]
        // TODO decide on after finish behavior for onTick method
        assertThat(onTickCaptures.size).isEqualTo(13)
        assertThat(onTickCaptures.firstExecution()).isCloseTo(50L, executionOffset)
        assertThat(onTickCaptures.lastExecution()).isCloseTo(650L, executionOffset)

        val repeatTaskCaptures = captor["repeatEvery"]
        assertThat(repeatTaskCaptures.size).isEqualTo(3)
        assertThat(repeatTaskCaptures.firstExecution()).isCloseTo(160L, executionOffset)
        assertThat(repeatTaskCaptures.lastExecution()).isCloseTo(480L, executionOffset)

        val afterFinishTaskCaptures = captor["afterFinish"]
        assertThat(afterFinishTaskCaptures.size).isEqualTo(1)
        assertThat(afterFinishTaskCaptures.firstExecution()).isCloseTo(650L, executionOffset)

        assertThat(captor["afterPassed"].firstExecution()).isCloseTo(310L, executionOffset)
        assertThat(captor["beforeLeft"].firstExecution()).isCloseTo(460L, executionOffset)
        assertThat(captor["playFinalSound"].firstExecution()).isCloseTo(500L, executionOffset)
    }

    @Test
    fun pauseTimerAndResume_withOnTickTask_shouldRunOnTickCorrectlyAfterEachResume() {
        val pause = 100L
        val captor = captureTime {
            val timer = simpleTimer(500.milliseconds, 90.milliseconds)
                .also { it.onTick = { track("onTick") } }
                .also { it.onStart = { track("start") } }

            timer.start()
            Thread.sleep(100)
            timer.pause()
            Thread.sleep(pause)
            timer.resume()
            Thread.sleep(200)
            timer.pause()
            Thread.sleep(pause)
            timer.resume()
            Thread.sleep(200)
            timer.stop()
        }

        val startCapture = captor["start"].first()
        val onTickCaptures = captor["onTick"]

        assertThat(onTickCaptures.size).isEqualTo(5)
        assertThat(onTickCaptures[0].executionTimeTowards(startCapture)).isCloseTo(90L, executionOffset)
        assertThat(onTickCaptures[1].executionTimeTowards(startCapture)).isCloseTo(180L + pause, executionOffset)
        assertThat(onTickCaptures[2].executionTimeTowards(startCapture)).isCloseTo(270L + pause, executionOffset)
        assertThat(onTickCaptures[3].executionTimeTowards(startCapture)).isCloseTo(360L + 2 * pause, executionOffset)
        assertThat(onTickCaptures[4].executionTimeTowards(startCapture)).isCloseTo(450L + 2 * pause, executionOffset)
    }

    @Test
    fun pauseTimerAndResume_withTwoAfterPassedTasks_shouldRunOneTaskBeforePauseAndAfterResume() {
        val pause = 200L
        val captor = captureTime {
            val timer = timerWithTask(500.milliseconds)
                .afterPassed(150.milliseconds, trackAlert("beforePause"))
                .afterPassed(400.milliseconds, trackAlert("afterPause"))
                .timer()
                .also { it.onStart = { track("start") } }
                .also { it.onResume = { track("resume") } }

            timer.start()
            Thread.sleep(200)
            timer.pause()
            Thread.sleep(pause)
            timer.resume()
            Thread.sleep(300)
            timer.stop()
        }

        val startCapture = captor["start"].first()
        val resumeCapture = captor["resume"].first()
        val beforePauseCapture = captor["beforePause"].first()
        val afterPauseCapture = captor["afterPause"].first()

        assertThat(beforePauseCapture.executionTimeTowards(startCapture)).isCloseTo(150L, executionOffset)
        assertThat(afterPauseCapture.executionTimeTowards(resumeCapture)).isCloseTo(200L, executionOffset)
        assertThat(afterPauseCapture.executionTimeTowards(startCapture)).isCloseTo(400L + pause, executionOffset)
    }

    @Test
    fun pauseTimerAndResume_withTwoBeforeLeftTasks_shouldRunOneTaskBeforePauseAndAfterResume() {
        val pause = 200L
        val captor = captureTime {
            val timer = timerWithTask(500.milliseconds)
                .beforeLeft(300.milliseconds, trackAlert("beforePause"))
                .beforeLeft(100.milliseconds, trackAlert("afterPause"))
                .timer()
                .also { it.onStart = { track("start") } }
                .also { it.onResume = { track("resume") } }

            timer.start()
            Thread.sleep(300)
            timer.pause()
            Thread.sleep(pause)
            timer.resume()
            Thread.sleep(200)
            timer.stop()
        }

        val startCapture = captor["start"].first()
        val resumeCapture = captor["resume"].first()
        val beforePauseCapture = captor["beforePause"].first()
        val afterPauseCapture = captor["afterPause"].first()

        assertThat(beforePauseCapture.executionTimeTowards(startCapture)).isCloseTo(200L, executionOffset)
        assertThat(afterPauseCapture.executionTimeTowards(resumeCapture)).isCloseTo(100L, executionOffset)
        assertThat(afterPauseCapture.executionTimeTowards(startCapture)).isCloseTo(400L + pause, executionOffset)
    }

    @Test
    fun pauseTimerAndResume_withRepeatableTask_shouldRunTaskCorrectlyAfterEachResume() {
        val pause = 100L
        val captor = captureTime {
            val timer = timerWithTask(300.milliseconds)
                .repeatEvery(60.milliseconds, 40.milliseconds, generator = trackAlert("repeat"))
                .timer()
                .also { it.onStart = { track("start") } }
                .also { it.onResume = { track("resume") } }

            timer.start()
            Thread.sleep(150)
            timer.pause()
            Thread.sleep(pause)
            timer.resume()
            Thread.sleep(100)
            timer.pause()
            Thread.sleep(pause)
            timer.resume()
            Thread.sleep(100)
            timer.stop()
        }

        val startCapture = captor["start"].first()
        val resumeCaptures = captor["resume"]
        val repeatCaptures = captor["repeat"]

        /*
            0...............150.............250......300
            ...40....100......160......220......280......340
         */
        assertThat(repeatCaptures.size).isEqualTo(5)
        assertThat(repeatCaptures[0].executionTimeTowards(startCapture)).isCloseTo(40L, executionOffset)
        assertThat(repeatCaptures[1].executionTimeTowards(startCapture)).isCloseTo(100L, executionOffset)
        assertThat(repeatCaptures[2].executionTimeTowards(startCapture)).isCloseTo(160L + pause, executionOffset)
        assertThat(repeatCaptures[2].executionTimeTowards(resumeCaptures.first())).isCloseTo(10L, executionOffset)
        assertThat(repeatCaptures[3].executionTimeTowards(startCapture)).isCloseTo(220L + pause, executionOffset)
        assertThat(repeatCaptures[3].executionTimeTowards(resumeCaptures.first())).isCloseTo(70L, executionOffset)
        assertThat(repeatCaptures[4].executionTimeTowards(startCapture)).isCloseTo(280L + 2 * pause, executionOffset)
        assertThat(repeatCaptures[4].executionTimeTowards(resumeCaptures.last())).isCloseTo(30L, executionOffset)
    }

    @Test
    fun pauseTimerAndResume_withRepeatableTaskAndPauseOnZeroExecutionTime_shouldRunTaskCorrectlyAfterResume() {
        val pause = 100L
        val captor = captureTime {
            val timer = timerWithTask(400.milliseconds, 100.milliseconds)
                .repeatEvery(200.milliseconds, 150.milliseconds, generator = trackAlert("repeat"))
                .timer()
                .also { it.onStart = { track("start") } }
                .also { it.onResume = { track("resume") } }

            timer.start()
            Thread.sleep(100)
            timer.pause()
            Thread.sleep(pause)
            timer.resume()
            Thread.sleep(300)
            timer.stop()
        }

        val startCapture = captor["start"].first()
        val repeatCaptures = captor["repeat"]

        assertThat(repeatCaptures.size).isEqualTo(2)
        assertThat(repeatCaptures[0].executionTimeTowards(startCapture)).isCloseTo(150L + pause, executionOffset)
        assertThat(repeatCaptures[1].executionTimeTowards(startCapture)).isCloseTo(350L + pause, executionOffset)
    }

    @Test
    fun pauseTimerAndResume_withAfterFinishTask_shouldRunTaskCorrectlyAfterResume() {
        val pause = 100L
        val captor = captureTime {
            val timer = timerWithTask(100.milliseconds, 50.milliseconds)
                .remindAfterFinishEvery(60.milliseconds, trackAlert("afterFinish"))
                .timer()
                .also { it.onStart = { track("start") } }

            timer.start()
            Thread.sleep(90)
            timer.pause()
            Thread.sleep(pause)
            timer.resume()
            Thread.sleep(150)
            timer.stop()
        }

        val startCapture = captor["start"].first()
        val afterFinishCaptures = captor["afterFinish"]

        assertThat(afterFinishCaptures.size).isEqualTo(2)
        assertThat(afterFinishCaptures[0].executionTimeTowards(startCapture)).isCloseTo(160L + pause, executionOffset)
        assertThat(afterFinishCaptures[1].executionTimeTowards(startCapture)).isCloseTo(220L + pause, executionOffset)
    }

    @Test
    @ExperimentalTime
    fun start_theSameTimerTwice_shouldSayTimeCorrectlyForRepeatableTask() {
        val speaker = DependenciesFactory.getSpeaker()
        val player = DependenciesFactory.getPlayer()
        val timer = timerWithTask(200.milliseconds, finalAlarm = Sound.PRE_DAWN_SLEEP)
            .repeatEvery(84.milliseconds, generator = AlertGenerators.getSayTimeAlertGenerator())
            .timer()

        timer.start()
        Thread.sleep(timer.duration.toLong())
        timer.stop()
        timer.start()
        Thread.sleep(timer.duration.toLong())
        timer.stop()

        verify(exactly = 2) {
            speaker.say("84 milliseconds passed")
            speaker.say("168 milliseconds passed")
            player.playOnce(Sound.PRE_DAWN_SLEEP)
        }
    }

    private fun TimeCaptor.trackAlert(label: String): AlertGenerator {
        return object : AlertGenerator {
            override fun generate(task: AlertTask, vararg params: Any): Alert {
                return object : Alert {
                    override fun alert() {
                        track(label)
                    }
                }
            }
        }
    }

    private fun TimerPropertiesBuilder.timer(): Timer {
        return Timer(build())
    }

    private fun timerWithTask(
        duration: MillisecondsTimeUnit,
        interval: MillisecondsTimeUnit = 200.milliseconds,
        finalAlarm: Sound? = null
    ): TimerPropertiesBuilder {
        return TimerPropertiesBuilder(duration)
            .tickInterval(interval)
            .finalAlarm(finalAlarm)
    }

    private fun simpleTimer(
        duration: MillisecondsTimeUnit,
        interval: MillisecondsTimeUnit = 200.milliseconds,
        finalAlarm: Sound? = null
    ): Timer {
        val properties = TimerPropertiesBuilder(duration)
            .tickInterval(interval)
            .finalAlarm(finalAlarm)
            .build()
        return Timer(properties)
    }
}
