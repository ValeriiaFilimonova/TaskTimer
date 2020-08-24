actual class Timer actual constructor(properties: TimerProperties) {
    actual val properties: TimerProperties
        get() = TODO("Not yet implemented")
    actual var elapsedTime: MillisecondsTimeUnit
        get() = TODO("Not yet implemented")
        set(value) {}
    actual var onStart: (() -> Unit)?
        get() = TODO("Not yet implemented")
        set(value) {}
    actual var onStop: (() -> Unit)?
        get() = TODO("Not yet implemented")
        set(value) {}
    actual var onPause: (() -> Unit)?
        get() = TODO("Not yet implemented")
        set(value) {}
    actual var onResume: (() -> Unit)?
        get() = TODO("Not yet implemented")
        set(value) {}
    actual var onTick: (() -> Unit)?
        get() = TODO("Not yet implemented")
        set(value) {}

    actual fun start() {
    }

    actual fun stop() {
    }

    actual fun pause() {
    }

    actual fun resume() {
    }

}