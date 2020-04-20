package tasks

import alerts.Alert

interface TaskWithAlert : Task {
    val alert: Alert
}
