package ui.commands

import MILLISECONDS
import MillisecondsTimeUnit
import TimerPropertiesBuilder
import alerts.sound.Sound
import picocli.CommandLine.*
import kotlin.time.ExperimentalTime

@Command(
    name = "create",
    description = ["Create new timer. Stops and removes current timer if such exists."],
    headerHeading = "%n########################################################################%n",
    synopsisHeading = "%n",
    descriptionHeading = "%nDescription: ",
    optionListHeading = "%nOptions:%n",
    footerHeading = "%nExamples:%n",
    footer = [
        "create -d=4.m -a=TEA_CEREMONY",
        "create --duration=2.d --alarm=NONE --interval=12.h"
    ]
)
@ExperimentalTime
class CreateTimerCommand : TimerSubCommand() {
    @ParentCommand
    override lateinit var applicationCommand: TimerApplicationCommand

    @Option(
        names = ["-d", "--duration"],
        required = true,
        description = [
            "Set timer duration. Format [NUMBER].[UNIT].",
            "Supported units: ms, s, m, h, d."
        ]
    )
    private var duration: MillisecondsTimeUnit = 0.MILLISECONDS

    @Option(
        names = ["-a", "--alarm"],
        defaultValue = "WISE_MASTER",
        description = [
            "Set final alarm sound.",
            "If set to NONE final alarm is skipped.",
            "Supported values: \${COMPLETION-CANDIDATES}.",
            "Defaults to \${DEFAULT - VALUE}."
        ]
    )
    private var alarm: Sound? = null

    @Option(
        names = ["-i", "--interval"],
        defaultValue = "500.ms",
        description = ["Set frequency of timer screen update.", "Defaults to \${DEFAULT-VALUE}."]
    )
    private var interval: MillisecondsTimeUnit? = null

    override fun run() {
        timer?.stop()

        properties = TimerPropertiesBuilder(duration)
            .tickInterval(interval!!)
            .finalAlarm(alarm)
    }
}
