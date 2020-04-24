package ui.commands

import picocli.CommandLine
import picocli.CommandLine.*
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField
import kotlin.time.ExperimentalTime

@Command(
    name = "help",
    description = ["Print usage"]
)
@ExperimentalTime
class PrintHelpCommand : Runnable {
    @ParentCommand
    lateinit var applicationCommand: TimerApplicationCommand

    override fun run() {
        val annotation = TimerApplicationCommand::class.findAnnotation<Command>() ?: return

        printCommandUsage(applicationCommand)
        printSubCommandsUsages(annotation.subcommands)
    }

    private fun printSubCommandsUsages(commandClasses: Array<KClass<*>>) {
        for (kClass in commandClasses) {
            val hasOptions = kClass.memberProperties.any {
                it.javaField?.isAnnotationPresent(Option::class.java) ?: false
            }

            if (hasOptions) {
                printCommandUsage(kClass.createInstance())
            }
        }
    }

    private fun printCommandUsage(command: Any) {
        CommandLine(command).usage(System.out)
    }
}
