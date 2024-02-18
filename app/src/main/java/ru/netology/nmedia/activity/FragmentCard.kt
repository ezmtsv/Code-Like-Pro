package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.FeedFragment.Companion.postArg
import ru.netology.nmedia.activity.FeedFragment.Companion.uriArg
import ru.netology.nmedia.adapter.OnIteractionListener
import ru.netology.nmedia.adapter.PostViewHolder
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.dialogs.DialogAuth
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.viewmodel.AuthViewModel
import ru.netology.nmedia.viewmodel.PostViewModel

@OptIn(ExperimentalCoroutinesApi::class)
@AndroidEntryPoint
class FragmentCard : Fragment() {

    private val viewModel: PostViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = CardPostBinding.inflate(layoutInflater, container, false)
        //val idPost = arguments?.longArg
        val post = arguments?.postArg

        post?.let {
            PostViewHolder(binding, object : OnIteractionListener {
                override fun onLike(post: Post) {
                    if (AuthViewModel.userAuth) viewModel.like(post)
                    else DialogAuth.newInstance(AuthViewModel.DIALOG_IN)
                        .show(childFragmentManager, "TAG")
                }

                override fun onEdit(post: Post) {
                    viewModel.edit(post)
                    findNavController().navigate(
                        R.id.action_fragmentCard_to_editPostFragment,
                        Bundle().apply {
                            //longArg = post.id
                            postArg = post
                        }
                    )
                }

                override fun onRemove(post: Post) {
                    viewModel.remove(post)
                    findNavController().navigateUp()
                }

//                    override fun openLinkVideo(post: Post) {
//                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(post.linkVideo))
//                        startActivity(intent)
//                    }

                override fun openCardPost(post: Post) {

                }

                override fun openSpacePhoto(post: Post) {
                    findNavController().navigate(
                        R.id.action_fragmentCard_to_spacePhoto,
                        Bundle().apply {
                            uriArg = post.attachment?.url
                        }
                    )
                }
            }).bind(post)
        }


//        viewLifecycleOwner.lifecycleScope.launch {
//            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
//                viewModel.data.asLiveData().observe(viewLifecycleOwner) {
//
//                }
//            }
//        }

        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            binding.progressCard.isVisible = state.loading
        }
        return binding.root
    }
}