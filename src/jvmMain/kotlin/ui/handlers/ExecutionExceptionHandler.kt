package ui.handlers

import picocli.CommandLine
import picocli.CommandLine.*
import java.lang.Exception

class ExecutionExceptionHandler : IExecutionExceptionHandler {
    override fun handleExecutionException(
        ex: Exception?,
        commandLine: CommandLine?,
        parseResult: ParseResult?
    ): Int {
        ex?.printStackTrace(System.err)
        return ExitCode.OK
    }
}
