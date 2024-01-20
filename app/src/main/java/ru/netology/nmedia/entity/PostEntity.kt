package ru.netology.nmedia.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.Post

@Entity
data class PostEntity(
    @PrimaryKey
    val id: Long,
    val author: String,
    val content: String,
    val published: Long,
    val likedByMe: Boolean,
    val likes: Int = 0,
    val authorAvatar: String = "",
    val visibility: Boolean = true,
    @Embedded
    val attachment: Attachment? = null,
//    val countRepost: Int = 0,
//    val countViews: Int = 0,
//    val linkVideo: String = ""
) {
    fun toDto() = Post(
        id,
        author,
        content,
        published,
        likedByMe,
        likes,
        authorAvatar,
        visibility,
        attachment,
//        countRepost,
//        countViews,
//        linkVideo
    )

    companion object {
        fun fromDto(dto: Post) =
            PostEntity(
                dto.id,
                dto.author,
                dto.content,
                dto.published,
                dto.likedByMe,
                dto.likes,
                dto.authorAvatar,
                dto.visibility,
                dto.attachment,
//                dto.countRepost,
//                dto.countViews,
//                dto.linkVideo

            )
    }

}

fun List<PostEntity>.toDto(): List<Post> = map(PostEntity::toDto)
fun List<Post>.toEntity(): List<PostEntity> = map(PostEntity::fromDto)