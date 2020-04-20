val Int.MILLISECONDS: Long
    get() = this.toLong()

val Int.SECONDS: Long
    get() = this.MILLISECONDS * 1000

val Int.MINUTES: Long
    get() = this.SECONDS * 60

val Int.HOURS: Long
    get() = this.MINUTES * 60

val Int.DAYS: Long
    get() = this.HOURS * 24
