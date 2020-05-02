package education.mahmoud.quranyapp.utils

import android.content.Context
import android.util.Log
import android.widget.Toast

fun String.log(TAG: String = "TestApp") {
    Log.e(TAG, this)
}

fun Context.show(msg: String) {
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
