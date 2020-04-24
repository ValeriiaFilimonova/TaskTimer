package ui.commands

import MillisecondsTimeUnit
import alerts.AlertGenerators
import alerts.sound.Sound
import picocli.CommandLine.*
import kotlin.time.ExperimentalTime

@Command(
    name = "task",
    description = ["Add task for the current timer."],
    headerHeading = "%n########################################################################%n",
    synopsisHeading = "%n",
    descriptionHeading = "%nDescription: ",
    optionListHeading = "%nOptions:%n",
    footerHeading = "%nExamples:%n",
    footer = [
        "task -t=2.m -a=PLAY_SOUND -s=FAIRY_DISAPPEARANCE",
        "task --time=4.h --class=EVERY --delay=1.h --finish=3.h -a=SAY_TIME"
    ]
)
@ExperimentalTime
class AddTaskTimerCommand : TimerSubCommand() {
    @ParentCommand
    override lateinit var applicationCommand: TimerApplicationCommand

    @Option(
        names = ["-t", "--time"],
        required = true,
        description = ["Set time when task is performed."]
    )
    lateinit var time: MillisecondsTimeUnit

    @Option(
        names = ["-a", "--action"],
        required = true,
        description = [
            "Set action to perform at the given time or with the given period.",
            "Supported values: \${COMPLETION-CANDIDATES}."
        ]
    )
    lateinit var action: Action

    @Option(
        names = ["-c", "--class"],
        defaultValue = "AFTER",
        description = [
            "Set task class.",
            "Supported values: \${COMPLETION-CANDIDATES}.",
            "Defaults to \${DEFAULT - VALUE}."
        ]
    )
    lateinit var type: TaskClass

    @Option(
        names = ["-s", "--sound"],
        description = [
            "Sound to play on PLAY_SOUND action",
            "Supported values: \${COMPLETION-CANDIDATES}."
        ]
    )
    var sound: Sound? = null

    @Option(
        names = ["-m", "--message"],
        description = ["Text to say on SAY_TEXT action"]
    )
    var text: String? = null

    @Option(
        names = ["-d", "--delay"],
        description = [
            "Set delay for repeatable task.",
            "Task starts after the given time elapsed.",
            "Defaults to task time."
        ]
    )
    var delay: MillisecondsTimeUnit? = null

    @Option(
        names = ["-f", "--finish"],
        description = ["Set repeatable task finish time.", "Defaults to the timer duration."]
    )
    var finish: MillisecondsTimeUnit? = null

    override fun run() {
        val alertGenerator = when (action) {
            Action.PLAY_SOUND -> {
                if (sound == null) {
                    throw ApplicationUsageError("Sound is needed for PLAY_SOUND action")
                }
                AlertGenerators.getSoundAlertGenerator(sound!!)
            }
            Action.SAY_TEXT -> {
                if (text == null) {
                    throw ApplicationUsageError("Message is needed for SAY_TEXT action")
                }
                AlertGenerators.getVoiceAlertGenerator(text!!)
            }
            Action.SAY_TIME -> {
                AlertGenerators.getSayTimeAlertGenerator()
            }
        }

        when (type) {
            TaskClass.AFTER -> {
                properties?.afterPassed(time, alertGenerator)
            }
            TaskClass.BEFORE -> {
                properties?.beforeLeft(time, alertGenerator)
            }
            TaskClass.EVERY -> {
                properties?.repeatEvery(time, alertGenerator, delay, finish)
            }
        }
    }
}

enum class TaskClass {
    AFTER,
    BEFORE,
    EVERY
}

enum class Action {
    PLAY_SOUND,
    SAY_TEXT,
    SAY_TIME
}
