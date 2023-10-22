package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.FeedFragment.Companion.postEditArg
import ru.netology.nmedia.databinding.FragmentEditPostBinding
import ru.netology.nmedia.util.AndroidUtils.focusAndShowKeyboard
import ru.netology.nmedia.viewmodel.PostViewModel

class EditPostFragment : Fragment() {
    private val viewModel by activityViewModels<PostViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentEditPostBinding.inflate(layoutInflater)

        val idPost = arguments?.postEditArg
        val textPost = viewModel.data.value?.find { it.id == idPost }?.content

        println("content $textPost")

        fun endEdit() {
            val content = binding.edit.text.toString()
            if (content.isBlank()) {
                Toast.makeText(
                    activity,
                    R.string.error_empty_content,
                    Toast.LENGTH_LONG
                ).show()
            } else {
                if (textPost != content) {
                    viewModel.savePost(content)
                }
            }
            findNavController().navigateUp()
        }

        binding.edit.setText(textPost)
        binding.edit.focusAndShowKeyboard()

        binding.ok.setOnClickListener {
            endEdit()
        }

        binding.icEdit.setOnClickListener {
            endEdit()
        }

        return binding.root
    }

}