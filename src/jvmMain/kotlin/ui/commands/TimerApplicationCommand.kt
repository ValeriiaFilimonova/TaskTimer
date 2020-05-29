package ui.commands

import Timer
import TimerPropertiesBuilder
import picocli.CommandLine.Command
import kotlin.time.ExperimentalTime

@Command(
    name = "timer",
    description = [
        "Task timer command line application.",
        "Allows configuring tasks to perform at a specific time or period.",
        "Supports sound playing, speech synthesis.",
        "%nExamples:",
        "timer create --duration=5.m",
        "timer task --time=1.m --class=EVERY --action=SAY_TIME",
        "timer task --timer=10.s --class=BEFORE_LEFT --action=SAY_TEXT -m=\"10 seconds left\"",
        "timer start"
    ],
    // todo add clear command
    subcommands = [
        CreateTimerCommand::class, AddTaskTimerCommand::class,
        StartTimerCommand::class, StopTimerCommand::class, ResumeTimerCommand::class, PauseTimerCommand::class,
        DefaultTimerCommand::class, PrintHelpCommand::class, QuitAppCommand::class
    ],
    descriptionHeading = "%nDescription:%n",
    commandListHeading = "%nCommands:%n"
)
@ExperimentalTime
class TimerApplicationCommand : Runnable {
    var propertiesBuilder: TimerPropertiesBuilder? = null

    var timer: Timer? = null

    override fun run() {
    }
}
