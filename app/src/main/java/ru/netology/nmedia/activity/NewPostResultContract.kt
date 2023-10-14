package ru.netology.nmedia.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract


class NewPostResultContract(val key: String) : ActivityResultContract<String, String?>() {

    override fun createIntent(context: Context, input: String): Intent {
        return if (key == "keyNewPost") {
            Intent(context, NewPostActivity::class.java)
        } else {
            Intent(context, EditPostActivity::class.java).apply {
                putExtra("keyEditPost", input) // Пихаем значение
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