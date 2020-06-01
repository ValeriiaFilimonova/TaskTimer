package ui.handlers

import picocli.CommandLine
import picocli.CommandLine.*

class ExecutionExceptionHandler : IExecutionExceptionHandler {
    override fun handleExecutionException(
        ex: Exception?, commandLine: CommandLine?, parseResult: ParseResult?
    ): Int {
        if (ex == null || commandLine?.out == null) {
            return ExitCode.SOFTWARE
        }

        ex.printStackTrace(commandLine.err)
        commandLine.err.flush()

        return ExitCode.OK
    }
}
