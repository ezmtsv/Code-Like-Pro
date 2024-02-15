package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.FeedFragment.Companion.postEditArg
import ru.netology.nmedia.databinding.FragmentEditPostBinding
import ru.netology.nmedia.di.DependencyContainer
import ru.netology.nmedia.util.AndroidUtils.focusAndShowKeyboard
import ru.netology.nmedia.viewmodel.PostViewModel
import ru.netology.nmedia.viewmodel.ViewModelFactory

@OptIn(ExperimentalCoroutinesApi::class)
class EditPostFragment : Fragment() {
    private val dependencyContainer = DependencyContainer.getInstance()

    private val viewModel: PostViewModel by viewModels(
        ownerProducer =:: requireParentFragment,
        factoryProducer = {
            ViewModelFactory(
                dependencyContainer.repository,
                dependencyContainer.appAuth,
                dependencyContainer.apiService
            )
        }
    )
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentEditPostBinding.inflate(layoutInflater)

        val idPost = arguments?.postEditArg
        val textPost = viewModel.data.value?.posts?.find { it.id == idPost }?.content

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
                } else findNavController().navigateUp()
            }

        }

        binding.edit.setText(textPost)
        binding.edit.focusAndShowKeyboard()

        binding.ok.setOnClickListener {
            endEdit()
        }

        viewModel.postCreated.observe(viewLifecycleOwner) {
            findNavController().navigateUp()
        }

        binding.icEdit.setOnClickListener {
            endEdit()
        }

        return binding.root
    }

}