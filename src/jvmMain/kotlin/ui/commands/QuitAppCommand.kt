package ui.commands

import alerts.voice.JvmSpeechSynthesizer
import picocli.CommandLine.Command
import picocli.CommandLine.ParentCommand
import ui.screen.JvmTerminalScreen
import kotlin.system.exitProcess
import kotlin.time.ExperimentalTime

@Command(
    name = "quit",
    description = ["Quit application"]
)
@ExperimentalTime
class QuitAppCommand : Runnable {
    @ParentCommand
    lateinit var applicationCommand: TimerApplicationCommand

    override fun run() {
        JvmTerminalScreen.close()
        JvmSpeechSynthesizer.close()
        exitProcess(0)
    }
}
