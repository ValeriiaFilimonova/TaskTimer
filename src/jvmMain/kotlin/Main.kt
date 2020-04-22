import alerts.AlertFactory
import alerts.sound.Sound.*

fun main() {
    val properties = TimerPropertiesBuilder(10.SECONDS)
        .tickInterval(500.MILLISECONDS)
        .repeatEvery(2.SECONDS, AlertFactory.getSoundAlert(), 1.SECONDS, finishTimeInMillis = 8.SECONDS)
        .remindAfterFinishEvery(5.SECONDS, AlertFactory.getSoundAlert(FAIRY_DISAPPEARANCE))
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
