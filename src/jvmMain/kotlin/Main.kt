import alerts.AlertGenerators
import alerts.sound.Sound.MORNING_DEW_LONG
import alerts.sound.Sound.TING_A_LING

fun main() {
    val properties = TimerPropertiesBuilder(15.SECONDS)
        .tickInterval(500.MILLISECONDS)
        .afterPassed(0.SECONDS, AlertGenerators.getVoiceAlertGenerator("timer started"))
        .beforeLeft(2.SECONDS, AlertGenerators.getSoundAlertGenerator(TING_A_LING))
        .repeatEvery(2.SECONDS, 2.SECONDS, 10.SECONDS, AlertGenerators.getSayTimeAlertGenerator())
        .remindAfterFinishEvery(5.SECONDS, AlertGenerators.getVoiceAlertGenerator("stop the timer"))
        .finalAlarm(MORNING_DEW_LONG)
        .build()

    val timer = Timer(properties)
        .also {
            it.onStart = { println("${System.currentTimeMillis()}: started") }
            it.onStop = { println("${System.currentTimeMillis()}: stopped") }
            it.onTick = { println("${it.elapsedTime / 1.SECONDS.toDouble() }sec") }
        }

    timer.start()
    Thread.sleep(1.MINUTES.toLong())
    timer.stop()
}
