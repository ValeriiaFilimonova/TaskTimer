import alerts.AlertGenerator
import io.mockk.*
import io.mockk.impl.annotations.RelaxedMockK
import org.junit.jupiter.api.*
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

@Tags(Tag("mocked"))
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class TimerMockedExecutorTest {
    lateinit var executor: ScheduledExecutorService

    @RelaxedMockK
    lateinit var generator: AlertGenerator

    @BeforeAll
    fun initMocks() {
        MockKAnnotations.init(this)
        mockkObject(DependenciesFactory)
    }

    @BeforeEach
    fun setupMocks() {
        executor = mockk(relaxed = true)
        every { DependenciesFactory.createExecutor() } returns executor
    }

    @AfterAll
    fun clearMocks() {
        unmockkAll()
    }

    @Test
    fun start_withoutCustomTasks_scheduleOnlyPredefinedTasks() {
        val properties = TimerPropertiesBuilder(1.seconds).tickInterval(200.milliseconds).build()

        Timer(properties).start()

        verifySequence {
            executor.scheduleAtFixedRate(any(), 0, 10, TimeUnit.MILLISECONDS)
            executor.schedule(any(), 1000, TimeUnit.MILLISECONDS)
        }
    }

    @Test
    fun start_withCustomTasks_scheduleAllTasksInProperSequence() {
        val properties = TimerPropertiesBuilder(1.minutes)
            .tickInterval(1.seconds)
            .afterPassed(20.seconds, generator)
            .beforeLeft(5.seconds, generator)
            .repeatEvery(15.seconds, 1.seconds, generator = generator)
            .remindAfterFinishEvery(6.seconds, generator)
            .build()

        Timer(properties).start()

        verify(exactly = 1) {
            executor.scheduleAtFixedRate(any(), 0, 10, TimeUnit.MILLISECONDS)
            executor.schedule(any(), 20000, TimeUnit.MILLISECONDS)
            executor.schedule(any(), 55000, TimeUnit.MILLISECONDS)
            executor.scheduleAtFixedRate(any(), 1000, 15000, TimeUnit.MILLISECONDS)
            executor.scheduleAtFixedRate(any(), 66000, 6000, TimeUnit.MILLISECONDS)
            executor.schedule(any(), 60000, TimeUnit.MILLISECONDS)
        }
    }
}
