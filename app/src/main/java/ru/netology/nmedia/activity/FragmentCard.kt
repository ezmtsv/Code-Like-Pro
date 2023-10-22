package ru.netology.nmedia.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.FeedFragment.Companion.postEditArg
import ru.netology.nmedia.adapter.OnIteractionListener
import ru.netology.nmedia.adapter.PostViewHolder
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.viewmodel.PostViewModel


class FragmentCard : Fragment() {
    private val viewModel by activityViewModels<PostViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = CardPostBinding.inflate(layoutInflater, container, false)
        val idPost = arguments?.postEditArg

        //val post = viewModel.data.value?.find { it.id == idPost }!!


        viewModel.data.observe(viewLifecycleOwner) { posts ->
            val post = posts.find { it.id == idPost }
            post?.let {
                PostViewHolder(binding, object : OnIteractionListener {
                    override fun onLike(post: Post) {
                        viewModel.like(post.id)
                    }

                    override fun onShare(post: Post) {
                        val intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, post.content)
                            type = "text/plain"
                        }

                        val shareIntent =
                            Intent.createChooser(intent, getString(R.string.chooser_share_post))
                        startActivity(shareIntent)
                        viewModel.share(post.id)
                    }

                    override fun onEdit(post: Post) {
                        viewModel.edit(post)
                        findNavController().navigate(
                            R.id.action_fragmentCard_to_editPostFragment,
                            Bundle().apply {
                                postEditArg = post.id
                            }
                        )
                    }

                    override fun onRemove(post: Post) {
                        viewModel.remove(post.id)
                        findNavController().navigateUp()
                    }

                    override fun openLinkVideo(post: Post) {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(post.linkVideo))
                        startActivity(intent)
                    }

                    override fun openCardPost(post: Post) {
                        findNavController().navigate(
                            R.id.action_feedFragment_to_fragmentCard,
                            Bundle().apply {
                                postEditArg = post.id
                            }
                        )
                    }
                }).bind(post)
            }
        }
        return binding.root
    }

}