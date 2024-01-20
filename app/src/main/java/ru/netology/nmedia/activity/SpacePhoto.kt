package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import ru.netology.nmedia.R

import ru.netology.nmedia.activity.FeedFragment.Companion.uriArg
import ru.netology.nmedia.databinding.FragmentSpacePhotoBinding


class SpacePhoto : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentSpacePhotoBinding.inflate(inflater, container, false)
        val uri = arguments?.uriArg

        Glide.with(binding.spacePhoto)
            .load("http://10.0.2.2:9999/media/$uri")
            .error(R.drawable.err_load)
            .into(binding.spacePhoto)
        return binding.root
    }

}