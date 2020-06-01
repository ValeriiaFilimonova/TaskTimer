package ui.commands

import picocli.CommandLine
import picocli.CommandLine.*
import picocli.CommandLine.Model.CommandSpec
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.findAnnotation
import kotlin.time.ExperimentalTime


@Command(
    name = "help",
    description = ["Print usage"]
)
@ExperimentalTime
class PrintHelpCommand : Runnable {
    @Spec
    lateinit var spec: CommandSpec

    @ParentCommand
    lateinit var applicationCommand: TimerApplicationCommand

    @Parameters(
        defaultValue = "timer",
        description = [
            "Command name to print help for. Supported values: \${COMPLETION-CANDIDATES}."
        ]
    )
    lateinit var command: CommandName

    override fun run() {
        if (command == CommandName.timer) {
            printCommandUsage(applicationCommand)
        } else {
            val annotation = TimerApplicationCommand::class.findAnnotation<Command>() ?: return
            val subcommand = annotation.subcommands
                .first { kClass -> kClass.findAnnotation<Command>()?.name == command.toString() }

            printCommandUsage(subcommand.createInstance())
        }
    }

    private fun printCommandUsage(command: Any) {
        val commandLine = spec.commandLine()

        CommandLine(command).run {
            setUsageHelpWidth(commandLine.usageHelpWidth)
            setUsageHelpAutoWidth(commandLine.isUsageHelpAutoWidth)
            usage(commandLine.out, Help.Ansi.OFF)
        }
    }
}

enum class CommandName {
    timer,
    create,
    task,
    start,
    stop,
    resume,
    pause,
    default
}
