package education.mahmoud.quranyapp.utils

import java.util.*


class NumberHelper {

    companion object {

        fun getArabicNumber(num: Int): String {
            val locale = Locale("ar")
            return String.format(locale, "%d", num)
        }
    }
}