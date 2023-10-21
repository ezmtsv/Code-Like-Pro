package ru.netology.nmedia.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.FeedFragment.Companion.postEditArg
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.util.AndroidUtils
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
        viewModel.cancelEdit()
        with(binding) {
            viewModel.data.observe(viewLifecycleOwner) { posts ->
                val post = posts.find { it.id == idPost }
                post?.let {
                    author.text = post.author
                    published.text = post.published
                    content.text = post.content
                    icLike.isChecked = post.likeByMe
                    icLike.text = AndroidUtils.getCountClick(post.countLike)
                    icShare.text = AndroidUtils.getCountClick(post.countRepost)
                    icView.text = AndroidUtils.getCountClick(4500)
                    if (post.linkVideo != "") groupVideo.visibility = View.VISIBLE
                    else groupVideo.visibility = View.GONE
                    icLike.setOnClickListener {
                        viewModel.like(post.id)
                    }
                    idVideo.setOnClickListener {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(post.linkVideo))
                        startActivity(intent)
                    }
                    play.setOnClickListener {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(post.linkVideo))
                        startActivity(intent)
                    }
                    icShare.setOnClickListener {
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
                    menu.setOnClickListener {
                        PopupMenu(it.context, it).apply {
                            inflate(R.menu.options_post)
                            setOnMenuItemClickListener { menuItem ->
                                when (menuItem.itemId) {
                                    R.id.edit -> {
                                        viewModel.edit(post)
                                        findNavController().navigate(
                                            R.id.action_fragmentCard_to_editPostFragment
                                        )
                                        true
                                    }

                                    R.id.remove -> {
                                        viewModel.remove(post.id)
                                        findNavController().navigateUp()
                                        true
                                    }

                                    else -> false
                                }
                            }
                        }.show()
                    }
                }
            }


        }
        return binding.root
    }

}