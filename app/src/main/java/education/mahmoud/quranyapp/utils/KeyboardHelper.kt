package education.mahmoud.quranyapp.utils

import android.content.Context
import android.view.inputmethod.InputMethodManager


object KeyboardHelper {

    fun dissmiss(ctx: Context) {
        val imm by lazy { ctx.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager }
        val windowHeightMethod =
                InputMethodManager::class.java.getMethod("getInputMethodWindowVisibleHeight")
        val height = windowHeightMethod.invoke(imm) as Int
        if (height > 0) {
            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
        }
    }
}

