package ru.netology.nmedia.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nmedia.dto.Post

@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val author: String,
    val published: String,
    val content: String,
    val likeByMe: Boolean,
    val countLike: Int = 0,
    val countRepost: Int = 0,
    val countViews: Int = 0,
    val linkVideo: String = ""
) {
    fun toDto() = Post(
        id,
        author,
        published,
        content,
        likeByMe,
        countLike,
        countRepost,
        countViews,
        linkVideo
    )

    companion object {
        fun fromDto(dto: Post) =
            PostEntity(
                dto.id,
                dto.author,
                dto.published,
                dto.content,
                dto.likeByMe,
                dto.countLike,
                dto.countRepost,
                dto.countViews,
                dto.linkVideo
            )
    }

}