# Task Timer
_Test application to test Kotlin Multi-platform Framework_

#### Features:

* Single and repeatable tasks to configure the timer
* Task to perform as a reminder after timer finished
* Speech synthesis to notify along with the sound notification 

### Modules

#### Common module
Set of classes and interfaces shared between implementations.
[Timer](https://github.com/ValeriiaFilimonova/TaskTimer/blob/master/src/commonMain/kotlin/Timer.kt) class is defined here as an interface with simple set of required methods (e.g. start, stop, pause etc.)

Timer requires a properties object to be instantiated. The properties are (at least for now):
* duration
* tick interval
* list of task prototypes from which executable tasks are created

[TimerPropertiesBuilder](https://github.com/ValeriiaFilimonova/TaskTimer/blob/master/src/commonMain/kotlin/TimerPropertiesBuilder.kt) is the easiest way to build properties object:

```$kotlin
TimerPropertiesBuilder(1.minutes)
    .tickInterval(2.seconds)
    .finalAlarm(Sound.PRE_DAWN_SLEEP)
    .afterPassed(30.seconds, AlertGenerators.getVoiceAlertGenerator("half the time passed"))
    .repeatEvery(5.seconds, 5.seconds, 56.seconds, AlertGenerators.getSoundAlertGenerator(Sound.TING_A_LING))
    .beforeLeft(2.seconds, AlertGenerators.getSayTimeAlertGenerator())
    .remindAfterFinishEvery(10.seconds, AlertGenerators.getVoiceAlertGenerator("stop the timer"))
    .build()
```
_**Tasks**_

Tasks are the units to define timer behaviour.
[Task](https://github.com/ValeriiaFilimonova/TaskTimer/blob/master/src/commonMain/kotlin/tasks/Task.kt) interface contains only time the task should be executed at and method `execute()`.

Common module provides a set of alert tasks.
Alert task has a factory method for creating alerts that are called when executing the task.

Predefined alert tasks:
* [AfterElapsedTimeTask](https://github.com/ValeriiaFilimonova/TaskTimer/blob/master/src/commonMain/kotlin/tasks/AfterElapsedTimeTask.kt) is executed when the given aount of time elapsed
* [BeforeTimeLeftTask](https://github.com/ValeriiaFilimonova/TaskTimer/blob/master/src/commonMain/kotlin/tasks/BeforeTimeLeftTask.kt) is executed before the given amount of time is left till the end of the timer
* [RepeatableTask](https://github.com/ValeriiaFilimonova/TaskTimer/blob/master/src/commonMain/kotlin/tasks/RepeatableTask.kt) is executed at a given rate

Predefined alerts, and their generators (to be used as the task alert factory method):
* [SoundAlert](https://github.com/ValeriiaFilimonova/TaskTimer/blob/master/src/commonMain/kotlin/alerts/sound/SoundAlert.kt) and [SoundAlertGenerator](https://github.com/ValeriiaFilimonova/TaskTimer/blob/master/src/commonMain/kotlin/alerts/sound/SoundAlertGenerator.kt) provide functionality of playing the specified sound within any alert task
* [VoiceAlert](https://github.com/ValeriiaFilimonova/TaskTimer/blob/master/src/commonMain/kotlin/alerts/voice/VoiceAlert.kt) and [VoiceAlertGenerator](https://github.com/ValeriiaFilimonova/TaskTimer/blob/master/src/commonMain/kotlin/alerts/voice/VoiceAlertGenerator.kt) provide functionality of saying the specified text within any alert task
* [VoiceAlert](https://github.com/ValeriiaFilimonova/TaskTimer/blob/master/src/commonMain/kotlin/alerts/voice/VoiceAlert.kt) and [SayTimeAlertGenerator](https://github.com/ValeriiaFilimonova/TaskTimer/blob/master/src/commonMain/kotlin/alerts/voice/SayTimeAlertGenerator.kt) provide functionality of saying the auto generated time message

Concrete implementation of audio playing and speech synthesis must be provided in each module.

#### Jvm module
Jvm implementation of the timer built upon ScheduledExecutorService class with terminal GUI
built using [Lanterna](https://github.com/mabe02/lanterna) and [Picocli](https://picocli.info/) libraries.

New alert types, and the corresponding generators are added:
* [PrintAlert](https://github.com/ValeriiaFilimonova/TaskTimer/blob/master/src/jvmMain/kotlin/alerts/print/PrintAlert.kt) and [PrintAlertGenerator](https://github.com/ValeriiaFilimonova/TaskTimer/blob/master/src/jvmMain/kotlin/alerts/print/PrintAlertGenerator.kt) provide functionality of printing the given message to terminal
* [PrintAlert](https://github.com/ValeriiaFilimonova/TaskTimer/blob/master/src/jvmMain/kotlin/alerts/print/PrintAlert.kt) and [PrintTimeAlertGenerator](https://github.com/ValeriiaFilimonova/TaskTimer/blob/master/src/jvmMain/kotlin/alerts/print/PrintTimeAlertGenerator.kt) provide functionality of printing auto generated time message to terminal

Embedded Swing terminal is opened when running application from IDE:

![Application start](https://github.com/ValeriiaFilimonova/TaskTimer/blob/master/src/jvmMain/resources/Swing%20terminal%20start.png)

![Timer example](https://github.com/ValeriiaFilimonova/TaskTimer/blob/master/src/jvmMain/resources/Swing%20terminal%20timer%20test.png)

Unix terminal is used when running JAR on Ubuntu machine:

![Help and usage](https://github.com/ValeriiaFilimonova/TaskTimer/blob/master/src/jvmMain/resources/Ubuntu%20terminal%20help.png)

### Future Plans

- [ ] Add Kotlin/Js native implementation
- [ ] Add Android implementation
