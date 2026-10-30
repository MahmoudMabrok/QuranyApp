package education.mahmoud.quranyapp.feature.listen

/** Modes for a multi-reciter listening session. */
enum class SessionMode {
    /** Each reciter reads a fixed number of ayahs, then the next reciter, until the range ends. */
    CHUNK,

    /** Each reciter reads the entire selected range, then the next reciter. */
    FULL,

    /** For each ayah, all selected reciters read it in order, then move to the next ayah. */
    PER_AYAH,
}
