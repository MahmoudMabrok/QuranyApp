package education.mahmoud.quranyapp.feature.showSuraAyas.mushaf

/** Reader page look chosen in Settings. */
data class MushafOptions(
    val headerStyle: SuraHeaderStyle = SuraHeaderStyle.DEFAULT,
    val pageFrame: Boolean = true,
    val nightMode: Boolean = false,
)
