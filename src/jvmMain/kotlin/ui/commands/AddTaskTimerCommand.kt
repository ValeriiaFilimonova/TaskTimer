package ui.commands

import MillisecondsTimeUnit
import alerts.AlertGenerators
import alerts.print.PrintAlertGenerator
import alerts.print.PrintTimeAlertGenerator
import alerts.sound.Sound
import picocli.CommandLine.*
import ui.ApplicationUsageError
import kotlin.time.ExperimentalTime

@Command(
    name = "task",
    description = ["Add task for the current timer."],
    synopsisHeading = "%n%n",
    descriptionHeading = "%nDescription:%n",
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
        defaultValue = "AT",
        description = [
            "Set task class. Supported values:",
            "AT - execute task at the given time from the timer start",
            "BEFORE_LEFT - execute the task before given time left till the timer end",
            "EVERY - execute the task with the given period",
            "AFTER_FINISH_EVERY - execute that with the given period after timer is finished but not stopped",
            "Defaults to \${DEFAULT-VALUE}."
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
        description = ["Text to say on SAY_TEXT or to print on PRINT_TEXT action"]
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
            Action.PRINT_TEXT -> {
                if (text == null) {
                    throw ApplicationUsageError("Message is needed for PRINT_TEXT action")
                }
                PrintAlertGenerator(text!!.trim('"'))
            }
            Action.PRINT_TIME -> {
                PrintTimeAlertGenerator()
            }
        }

        when (type) {
            TaskClass.AT -> {
                properties?.afterPassed(time, alertGenerator)
            }
            TaskClass.BEFORE_LEFT -> {
                properties?.beforeLeft(time, alertGenerator)
            }
            TaskClass.EVERY -> {
                properties?.repeatEvery(time, alertGenerator, delay, finish)
            }
            TaskClass.AFTER_FINISH_EVERY -> {
                properties?.remindAfterFinishEvery(time, alertGenerator)
            }
        }
    }
}

enum class TaskClass {
    AT,
    BEFORE_LEFT,
    EVERY,
    AFTER_FINISH_EVERY
}

enum class Action {
    PLAY_SOUND,
    SAY_TEXT,
    SAY_TIME,
    PRINT_TEXT,
    PRINT_TIME
}
