package education.mahmoud.quranyapp.feature.listen

import education.mahmoud.quranyapp.datalayer.local.room.AyahItem
import education.mahmoud.quranyapp.feature.gotoscreen.GoToInput

/**
 * Pure validation / playlist helpers for the Listen screen (no Android types, unit tested).
 */
object ListenInput {
    const val MAX_REPEAT = 20
    const val DEFAULT_CHUNK_SIZE = 5

    /** Raw form values as typed by the user. Empty ayah fields mean "first" / "last" ayah. */
    data class Form(
        val startSura: Int = 1,
        val startAyah: String = "",
        val endSura: Int = 1,
        val endAyah: String = "",
        val repeatEach: String = "",
        val repeatAll: String = "",
        val sessionMode: SessionMode = SessionMode.CHUNK,
        val chunkSize: String = DEFAULT_CHUNK_SIZE.toString(),
        val selectedReciters: Set<Reciter> = setOf(Reciter.ALAFASY),
    )

    data class Range(
        val startSura: Int,
        val startAyah: Int,
        val endSura: Int,
        val endAyah: Int,
        val repeatEach: Int,
        val repeatAll: Int,
        val sessionMode: SessionMode,
        val chunkSize: Int,
        val selectedReciters: List<Reciter>,
    )

    data class Errors(
        val startAyah: Boolean = false,
        val endAyah: Boolean = false,
        val order: Boolean = false,
        val repeatEach: Boolean = false,
        val repeatAll: Boolean = false,
        val chunkSize: Boolean = false,
        val noReciters: Boolean = false,
    ) {
        val any: Boolean get() = startAyah || endAyah || order || repeatEach || repeatAll || chunkSize || noReciters
    }

    /**
     * @param ayahCount number of ayahs of a sura (1-based index)
     * @return the resolved range (null when invalid) and the per-field errors
     */
    fun validate(form: Form, ayahCount: (Int) -> Int): Pair<Range?, Errors> {
        val startMax = ayahCount(form.startSura)
        val endMax = ayahCount(form.endSura)
        val startAyah = valueOrDefault(GoToInput.parse(form.startAyah, 1, startMax), 1)
        val endAyah = valueOrDefault(GoToInput.parse(form.endAyah, 1, endMax), endMax)
        val repeatEach = valueOrDefault(GoToInput.parse(form.repeatEach, 1, MAX_REPEAT), 1)
        val repeatAll = valueOrDefault(GoToInput.parse(form.repeatAll, 1, MAX_REPEAT), 1)
        val chunkSize = valueOrDefault(GoToInput.parse(form.chunkSize, 1, 50), DEFAULT_CHUNK_SIZE)
        val orderOk = startAyah == null || endAyah == null ||
            form.startSura < form.endSura || (form.startSura == form.endSura && startAyah <= endAyah)
        val errors = Errors(
            startAyah = startAyah == null,
            endAyah = endAyah == null,
            order = !orderOk,
            repeatEach = repeatEach == null,
            repeatAll = repeatAll == null,
            chunkSize = chunkSize == null,
            noReciters = form.selectedReciters.isEmpty(),
        )
        if (errors.any) return null to errors
        return Range(
            form.startSura,
            requireNotNull(startAyah),
            form.endSura,
            requireNotNull(endAyah),
            requireNotNull(repeatEach),
            requireNotNull(repeatAll),
            form.sessionMode,
            requireNotNull(chunkSize),
            form.selectedReciters.toList(),
        ) to errors
    }

    /** Global (1..6236) ayah number from a sura index and the ayah number inside it. */
    fun globalIndex(ayahCount: (Int) -> Int, sura: Int, ayah: Int): Int =
        (1 until sura).sumOf(ayahCount) + ayah

    /** Each item repeated [repeatEach] times, then the whole list repeated [repeatAll] times. */
    fun <T> playlist(items: List<T>, repeatEach: Int, repeatAll: Int): List<T> {
        val once = items.flatMap { item -> List(repeatEach) { item } }
        return List(repeatAll) { once }.flatten()
    }

    /**
     * Builds a playlist of (ayah, reciter) pairs according to the session mode.
     */
    fun sessionPlaylist(
        ayahs: List<AyahItem>,
        reciters: List<Reciter>,
        mode: SessionMode,
        chunkSize: Int,
    ): List<Pair<AyahItem, Reciter>> {
        if (ayahs.isEmpty() || reciters.isEmpty()) return emptyList()
        return when (mode) {
            SessionMode.FULL -> {
                reciters.flatMap { reciter -> ayahs.map { it to reciter } }
            }
            SessionMode.CHUNK -> {
                val result = mutableListOf<Pair<AyahItem, Reciter>>()
                var reciterIndex = 0
                ayahs.chunked(chunkSize).forEach { chunk ->
                    val reciter = reciters[reciterIndex % reciters.size]
                    chunk.forEach { ayah -> result.add(ayah to reciter) }
                    reciterIndex++
                }
                result
            }
            SessionMode.PER_AYAH -> {
                ayahs.flatMap { ayah -> reciters.map { ayah to it } }
            }
        }
    }

    private fun valueOrDefault(result: GoToInput.Result, default: Int): Int? = when (result) {
        GoToInput.Result.Empty -> default
        is GoToInput.Result.Valid -> result.value
        is GoToInput.Result.OutOfRange -> null
    }
}
