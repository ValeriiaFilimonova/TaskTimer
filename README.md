# Task Timer
Test application to test Kotlin Multi-platform Framework

### Modules

#### Common
Set of classes and interfaces shared between implementations.
Timer class is defined here as an interface with simple set of required methods (e.g. start, stop, pause etc.)

#### Jvm
Jvm implementation of the timer built upon ScheduledExecutorService class with terminal GUI
built using [Lanterna](https://github.com/mabe02/lanterna) and [Picocli](https://picocli.info/) libraries.

Embedded Swing terminal is opened when running application from IDE:

![Application start](https://github.com/ValeriiaFilimonova/TaskTimer/tree/master/src/jvmMain/resources/Swing%20terminal%20start.png)

![Timer example](https://github.com/ValeriiaFilimonova/TaskTimer/tree/master/src/jvmMain/resources/Swing%20terminal%20timer%20test.png)

Unix terminal is used when running JAR:

![Help and usage](https://github.com/ValeriiaFilimonova/TaskTimer/tree/master/src/jvmMain/resources/Ubuntu%20terminal%20help.png)
