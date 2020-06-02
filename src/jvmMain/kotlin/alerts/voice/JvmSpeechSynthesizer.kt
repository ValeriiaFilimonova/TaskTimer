package alerts.voice

import alerts.Terminatable
import java.io.Closeable
import java.util.*
import javax.speech.Central
import javax.speech.EngineStateError
import javax.speech.synthesis.Synthesizer
import javax.speech.synthesis.SynthesizerModeDesc

object JvmSpeechSynthesizer : Speaker, Closeable, Terminatable {
    private const val tts = "com.sun.speech.freetts"

    private val synthesizer: Synthesizer

    init {
        System.setProperty("freetts.voices", "${tts}.en.us.cmu_us_kal.KevinVoiceDirectory,${tts}.en.us.cmu_time_awb.AlanVoiceDirectory")

        Central.registerEngineCentral("${tts}.jsapi.FreeTTSEngineCentral")

        synthesizer = Central.createSynthesizer(SynthesizerModeDesc(Locale.US))
    }

    override fun say(text: String) {
        terminate()

        if (synthesizer.engineState == Synthesizer.DEALLOCATED) {
            synthesizer.allocate()
        }

        if (synthesizer.engineState == Synthesizer.PAUSED) {
            synthesizer.resume()
        }

        synthesizer.speakPlainText(text, null)
        synthesizer.waitEngineState(Synthesizer.QUEUE_EMPTY)
    }

    override fun close() {
        synthesizer.deallocate()
    }

    override fun terminate() {
        try {
            synthesizer.cancelAll()
        } catch (e: EngineStateError) {
        }
    }
}