expect class Timer(properties: TimerProperties) {
    val properties: TimerProperties

    var elapsedTime: Long
        private set

    var onStart: (() -> Unit)?

    var onStop: (() -> Unit)?

    var onPause: (() -> Unit)?

    var onResume: (() -> Unit)?

    var onTick: (() -> Unit)?

    fun start()

    fun stop()

    fun pause()

    fun resume()
}
