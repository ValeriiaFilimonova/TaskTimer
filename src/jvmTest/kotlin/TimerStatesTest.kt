import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.*

@Tags(Tag("state"))
internal class TimerStatesTest {
    private lateinit var timer: Timer

    @BeforeEach
    fun createTimer() {
        timer = Timer(TimerProperties(10.hours, 1.minutes, emptyList()))
    }

    @AfterEach
    fun stopTimer() {
        try {
            timer.stop()
        } catch (error: JvmTimerError) {
            // it's ok, so do nothing
        }
    }

    @Test
    fun pauseResumeAndStop_notStartedTimer_shouldThrowError() {
        assertThatExceptionOfType(JvmTimerError::class.java).isThrownBy { timer.pause() }
            .withMessage("Can't move from STOPPED to PAUSED timer state")

        assertThatExceptionOfType(JvmTimerError::class.java).isThrownBy { timer.resume() }
            .withMessage("Can't move from STOPPED to RESUMED timer state")

        assertThatExceptionOfType(JvmTimerError::class.java).isThrownBy { timer.stop() }
            .withMessage("Can't move from STOPPED to STOPPED timer state")
    }

    @Test
    fun startAndResume_alreadyStartedTimer_shouldThrowError() {
        timer.start()

        assertThatExceptionOfType(JvmTimerError::class.java).isThrownBy { timer.start() }
            .withMessage("Can't move from STARTED to STARTED timer state")

        assertThatExceptionOfType(JvmTimerError::class.java).isThrownBy { timer.resume() }
            .withMessage("Can't move from STARTED to RESUMED timer state")
    }

    @Test
    fun startAndPause_alreadyPausedTimer_shouldThrowError() {
        timer.start()
        timer.pause()

        assertThatExceptionOfType(JvmTimerError::class.java).isThrownBy { timer.start() }
            .withMessage("Can't move from PAUSED to STARTED timer state")

        assertThatExceptionOfType(JvmTimerError::class.java).isThrownBy { timer.pause() }
            .withMessage("Can't move from PAUSED to PAUSED timer state")
    }

    @Test
    fun startAndResume_alreadyResumedTimer_shouldThrowError() {
        timer.start()
        timer.pause()
        timer.resume()

        assertThatExceptionOfType(JvmTimerError::class.java).isThrownBy { timer.start() }
            .withMessage("Can't move from RESUMED to STARTED timer state")

        assertThatExceptionOfType(JvmTimerError::class.java).isThrownBy { timer.resume() }
            .withMessage("Can't move from RESUMED to RESUMED timer state")

        timer.stop()
    }
}
