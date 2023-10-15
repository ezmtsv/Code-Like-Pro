package ru.netology.nmedia.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract


class NewPostResultContract(val key: String) : ActivityResultContract<String, String?>() {

    override fun createIntent(context: Context, input: String): Intent {
        return if (key == KEY_NEW_POST) {
            Intent(context, NewPostActivity::class.java)
        } else {
            Intent(context, EditPostActivity::class.java).apply {
                putExtra(KEY_EDIT_POST, input) // Пихаем значение
            }
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): String? =
        if (resultCode == Activity.RESULT_OK) {
            intent?.getStringExtra(Intent.EXTRA_TEXT)
        } else {
            null
        }
}