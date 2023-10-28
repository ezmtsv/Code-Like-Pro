package ru.netology.nmedia.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ru.netology.nmedia.entity.PostEntity

@Dao
interface PostDao {
    @Query("SELECT * FROM PostEntity ORDER BY id DESC")
    fun getAll(): LiveData<List<PostEntity>>

    @Insert
    fun insert(post: PostEntity)

    @Query("UPDATE PostEntity SET content = :content, published = :published WHERE id = :id")
    fun updateContentById(id: Int, content: String, published: String)

    fun save(post: PostEntity) =
        if (post.id == 0) insert(post) else updateContentById(post.id, post.content, post.published)

    @Query(
        """
        UPDATE PostEntity SET
        countLike = countLike + CASE WHEN likeByMe THEN -1 ELSE 1 END,
        likeByMe = CASE WHEN likeByMe THEN 0 ELSE 1 END
        WHERE id = :id
        """
    )
    fun likeById(id: Int)

    @Query("DELETE FROM PostEntity WHERE id = :id")
    fun removeById(id: Int)

    @Query(
        """
        UPDATE PostEntity SET
        countRepost = countRepost + 1
        WHERE id = :id
        """
    )
    fun share(id: Int)
}