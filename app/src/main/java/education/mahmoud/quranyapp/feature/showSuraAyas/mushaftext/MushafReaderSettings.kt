package education.mahmoud.quranyapp.feature.showSuraAyas.mushaftext

import education.mahmoud.quranyapp.feature.showSuraAyas.mushaf.SuraHeaderStyle

/**
 * Printed look of the text mushaf.
 *
 * [ORNATE] keeps the stored surah-header style and page-frame switch.
 * The other looks are skins from mushaf-text 1.6.0: same Madinah pages and
 * font, with that edition's frame and banner. Unknown stored ids fall back
 * to [ORNATE] so an old or future value still opens the reader.
 */
enum class MushafLook(val id: Int) {
    ORNATE(0),
    CAIRO(1),
    DIYANET(2),
    INDOPAK(3);

    companion object {
        val DEFAULT = ORNATE

        fun fromId(id: Int): MushafLook = values().firstOrNull { it.id == id } ?: DEFAULT
    }
}

/**
 * How pages turn. Ids match what is already stored in `mushaf_page_turn`.
 * Curl is intentionally unavailable for now; an old stored Curl id falls
 * through to [DEFAULT] and is therefore opened with Slide.
 */
enum class MushafPageTurn(val id: Int) {
    SLIDE(0),
    FLIP(2),
    FADE(3),
    COVER(4);

    companion object {
        val DEFAULT = SLIDE

        fun fromId(id: Int): MushafPageTurn = values().firstOrNull { it.id == id } ?: DEFAULT
    }
}

/**
 * One page, or a two-page spread on wide screens.
 * [SINGLE] matches the reader from before mushaf-text 1.6.0.
 */
enum class MushafSpreadMode(val id: Int) {
    SINGLE(0),
    AUTO(1);

    companion object {
        val DEFAULT = SINGLE

        fun fromId(id: Int): MushafSpreadMode = values().firstOrNull { it.id == id } ?: DEFAULT
    }
}

/**
 * Text-mushaf preferences. Defaults match what the app drew before 1.6.0:
 * classic-or-stored header, page frame on, no division marks, slide, one page,
 * tajweed off. New fields use their own preference keys.
 */
data class MushafReaderSettings(
    val headerStyle: SuraHeaderStyle = SuraHeaderStyle.DEFAULT,
    val pageFrame: Boolean = true,
    val look: MushafLook = MushafLook.DEFAULT,
    val divisionMarks: Boolean = false,
    val pageTurn: MushafPageTurn = MushafPageTurn.DEFAULT,
    val spread: MushafSpreadMode = MushafSpreadMode.DEFAULT,
    val tajweed: Boolean = false,
    val naturalMadd: Boolean = false,
) {
    /** Printed skins own their frame. The page-frame switch applies to [MushafLook.ORNATE]. */
    val ornateControlsApply: Boolean
        get() = look == MushafLook.ORNATE

    /**
     * Margin medallions need the ornate frame. Printed skins include their own
     * marks, so the stored switch is left as the user set it and ignored here.
     */
    val drawDivisionMarks: Boolean
        get() = look == MushafLook.ORNATE && pageFrame && divisionMarks

    /** The library frame draws the page number. The app footer would duplicate it. */
    val libraryDrawsPageNumber: Boolean
        get() = look != MushafLook.ORNATE || pageFrame

    /** Natural madd only colours anything while tajweed colouring is on. */
    val colorNaturalMadd: Boolean
        get() = tajweed && naturalMadd
}
