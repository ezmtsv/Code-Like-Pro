package ru.netology.nmedia.dto

import android.util.Log


data class Post(
    val id: Int,
    val author: String,
    val authorAvatar: String,
    val published: String,
    val content: String,
    val likeByMe: Boolean,
    val countLike: Int,
    val countRepost: Int,
    val countViews: Int
)