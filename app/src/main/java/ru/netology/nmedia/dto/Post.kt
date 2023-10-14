package ru.netology.nmedia.dto

import android.util.Log


data class Post(
    val id: Int,
    val author: String,
    val authorAvatar: String = "",
    val published: String,
    val content: String,
    val likeByMe: Boolean,
    val countLike: Int = 0,
    val countRepost: Int = 0,
    val countViews: Int = 0,
    val linkVideo: String = ""
)