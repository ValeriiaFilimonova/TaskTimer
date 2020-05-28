package alerts.sound

enum class Sound(
    val resourceName: String,
    val duration: Long,
    val prettyName: String
) {
    NIGHT_IS_COMING_LONG("1_cycle.wav", 6000, "Night is coming (long)"),
    NIGHT_IS_COMING_SHORT("1_with_tale.wav", 3000, "Night is coming (short)"),
    TING_A_LING("2_short.wav", 2000, "Ting a ling"),
    MORNING_DEW_LONG("3_long.wav", 7500, "Morning dew (long)"),
    MORNING_DEW_SHORT("3_long.wav", 3500, "Morning dew (short)"),
    TEA_CEREMONY("4_cycle.wav", 6000, "Tea ceremony"),
    PRE_DAWN_SLEEP("5_long_cycle.wav", 9500, "Predawn sleep"),
    STORY_END_LONG("6_long.wav", 16500, "The end of a story (long)"),
    STORY_END_SHORT("6_long.wav", 1000, "The end of a story (short)"),
    INTRIGUE_LONG("8_long.wav", 8000, "Intrigue (long)"),
    INTRIGUE_SHORT("8_long.wav", 1900, "Intrigue (short)"),
    PLAYFUL_MOOD("9_mid.wav", 5500, "Playful mood"),
    FAIRY_DISAPPEARANCE("10_short.wav", 2000, "Fairy disappearance"),
    WISE_MASTER("11_mid.wav", 4500, "Wise master");
}
