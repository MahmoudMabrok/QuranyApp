package education.mahmoud.quranyapp.utils

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding

fun String.log(TAG: String = "") {
    Log.e("TestApp", "$TAG $this")
}

fun Context.show(msg: String) {
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}

fun Int.getSuraNameFromIndex(): String {
    return Data.SURA_NAMES[this - 1].orEmpty()
}

inline fun <T : ViewBinding> AppCompatActivity.viewBinding(
    crossinline bindingInflater: (LayoutInflater) -> T
) =
    lazy(LazyThreadSafetyMode.NONE) {
        bindingInflater.invoke(layoutInflater)
    }
