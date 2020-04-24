package ui.handlers

import picocli.CommandLine.*

class ParameterExceptionHandler: IParameterExceptionHandler {
    override fun handleParseException(ex: ParameterException?, args: Array<out String>?): Int {
        if (ex != null) {
            println(ex.message!!)
            ex.commandLine.usage(System.out)
        }
        return ExitCode.OK
    }
}
