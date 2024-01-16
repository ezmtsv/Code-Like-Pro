package ru.netology.nmedia.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.entity.PostEntity

@Dao
interface PostDao {
    @Query("SELECT * FROM PostEntity WHERE visibility = 1 ORDER BY id DESC")
    fun getAllVisible(): Flow<List<PostEntity>>

    @Query("SELECT * FROM PostEntity WHERE visibility = 0 ORDER BY id DESC")
    fun getAllInvisible(): Flow<List<PostEntity>>

    @Query("SELECT * FROM PostEntity ORDER BY id DESC")
    fun getAllPosts(): Flow<List<PostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: PostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(posts: List<PostEntity>)

    @Query("UPDATE PostEntity SET content = :content, published = :published WHERE id = :id")
    suspend fun updateContentById(id: Long, content: String, published: Long)

    @Query("SELECT * FROM PostEntity WHERE id = :id")
    fun getPostById(id: Long): Flow<PostEntity>

    suspend fun save(post: PostEntity) =
        if (post.id == 0L) insert(post) else updateContentById(
            post.id,
            post.content,
            post.published,
        )

    @Query(
        """
        UPDATE PostEntity SET
        likes = likes + CASE WHEN likedByMe THEN -1 ELSE 1 END,
        likedByMe = CASE WHEN likedByMe THEN 0 ELSE 1 END
        WHERE id = :id
        """
    )
    suspend fun likeById(id: Long)

    @Query("DELETE FROM PostEntity WHERE id = :id")
    suspend fun removeById(id: Long)

//    @Query(
//        """
//        UPDATE PostEntity SET
//        countRepost = countRepost + 1
//        WHERE id = :id
//        """
//    )
//    fun share(id: Int)
}