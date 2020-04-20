import alerts.AlertFactory
import alerts.sound.Sound.FAIRY_DISAPPEARANCE

fun main() {
    val properties = TimerPropertiesBuilder((3).SECONDS)
        .repeatEvery(500.MILLISECONDS, AlertFactory.getSoundAlert())
        .finalAlarm(FAIRY_DISAPPEARANCE)
        .build()

    val timer = Timer(properties)
        .also {
            it.onStart = { println("${System.currentTimeMillis()}: started") }
            it.onStop = { println("${System.currentTimeMillis()}: stopped") }
        }

    timer.start()
    Thread.sleep(1.SECONDS)
    timer.pause()
    Thread.sleep(1.SECONDS)
    timer.resume()
    Thread.sleep(1.SECONDS)
    timer.pause()
    Thread.sleep(1.SECONDS)
    timer.resume()
    Thread.sleep(1.SECONDS + 700.MILLISECONDS)
    timer.stop()
}
