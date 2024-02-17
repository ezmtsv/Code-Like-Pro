package ru.netology.nmedia.activity

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentNewPostBinding
import ru.netology.nmedia.util.AndroidUtils.focusAndShowKeyboard
import ru.netology.nmedia.util.StringArg
import ru.netology.nmedia.viewmodel.PostViewModel
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.fragment.app.activityViewModels
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
@AndroidEntryPoint
class NewPostFragment : Fragment() {

    companion object {
        var Bundle.textArg: String? by StringArg
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val viewModel: PostViewModel by activityViewModels()
        val binding = FragmentNewPostBinding.inflate(layoutInflater, container, false)
        var phtoEmpty = true

        viewModel.postCreated.observe(viewLifecycleOwner) {
            viewModel.loadPosts()
            findNavController().navigateUp()
        }

        arguments?.textArg?.let(binding.edit::setText)

        val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            when (it.resultCode) {
                ImagePicker.RESULT_ERROR -> {
                    Snackbar.make(
                        binding.root,
                        ImagePicker.getError(it.data),
                        Snackbar.LENGTH_LONG
                    ).show()
                }

                Activity.RESULT_OK -> {
                    val uri: Uri? = it.data?.data
                    viewModel.changePhoto(uri, uri?.toFile())
                }
            }
        }

        binding.pickPhoto.setOnClickListener {
            ImagePicker.with(this)
                .galleryOnly()
                .crop()
                .maxResultSize(2048, 2048)
                .createIntent(launcher::launch)
        }

        binding.takePhoto.setOnClickListener {
            ImagePicker.with(this)
                .cameraOnly()
                .crop()
                .maxResultSize(2048, 2048)
                .createIntent(launcher::launch)

        }

        binding.removePhoto.setOnClickListener {
            viewModel.clearPhoto()
        }

        viewModel.photo.observe(viewLifecycleOwner) {
            if (it == viewModel.noPhoto) {
                binding.photoContainer.visibility = View.GONE
                binding.edit.focusAndShowKeyboard()
                phtoEmpty = true
                return@observe
            }
            phtoEmpty = false
            binding.edit.clearFocus()
            binding.photoContainer.visibility = View.VISIBLE
            binding.photo.setImageURI(it.uri)

        }

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.new_post_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                when (menuItem.itemId) {
                    R.id.save -> {
                        if (!binding.edit.text.isNullOrBlank() || !phtoEmpty) {
                            val content = binding.edit.text.toString()
                            viewModel.savePost(content)
                        } else {
                            context?.toast("Для создания поста нужен контент!")
                        }
                        true
                    }

                    androidx.appcompat.R.id.home -> {
                        println("home")
                        true
                    }

                    else -> false
                }

        }, viewLifecycleOwner)


        return binding.root
    }

    private fun Context.toast(message: CharSequence) =
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()

}