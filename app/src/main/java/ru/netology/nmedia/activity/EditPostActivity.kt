package ru.netology.nmedia.activity

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.ActivityEditPostBinding
import ru.netology.nmedia.util.AndroidUtils.focusAndShowKeyboard

class EditPostActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityEditPostBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val textPost = intent?.getStringExtra(getString(R.string.KEY_EDIT_POST))

        Log.d("TAG", "edit Activity $textPost")

        binding.edit.setText(textPost)
        binding.edit.focusAndShowKeyboard()
        fun endEdit() {

            val intent = Intent()
            if (binding.edit.text.isNullOrBlank()) {
                setResult(Activity.RESULT_CANCELED, intent)
            } else {
                val content = binding.edit.text.toString()
                intent.putExtra(Intent.EXTRA_TEXT, content)
                setResult(Activity.RESULT_OK, intent)
            }
            finish()
        }

        binding.ok.setOnClickListener {
            endEdit()
        }

        binding.icEdit.setOnClickListener {
            endEdit()
        }

    }
}