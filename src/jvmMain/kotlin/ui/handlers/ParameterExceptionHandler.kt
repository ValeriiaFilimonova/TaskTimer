package ui.handlers

import picocli.CommandLine.*

class ParameterExceptionHandler: IParameterExceptionHandler {
    override fun handleParseException(ex: ParameterException?, args: Array<out String>?): Int {
        if (ex == null) {
            return ExitCode.SOFTWARE
        }

        val commandLine = ex.commandLine

        commandLine.out.println(ex.message)

        if (!UnmatchedArgumentException.printSuggestions(ex, commandLine.out)) {
            commandLine.usage(commandLine.out, Help.Ansi.OFF)
        }

        commandLine.out.flush()

        return ExitCode.OK
    }
}
