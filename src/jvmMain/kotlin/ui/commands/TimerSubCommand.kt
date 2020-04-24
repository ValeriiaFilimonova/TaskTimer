package ui.commands

import Timer
import TimerPropertiesBuilder
import ui.ApplicationUsageError
import kotlin.time.ExperimentalTime

@ExperimentalTime
abstract class TimerSubCommand : Runnable {
    abstract var applicationCommand: TimerApplicationCommand

    protected var properties: TimerPropertiesBuilder?
        get() {
            if (applicationCommand.propertiesBuilder == null) {
                throw ApplicationUsageError("Create timer first")
            }
            return applicationCommand.propertiesBuilder
        }
        set(builder) {
            applicationCommand.propertiesBuilder = builder
        }

    protected var timer: Timer?
        get() = applicationCommand.timer
        set(timer) {
            applicationCommand.timer = timer
        }
}
