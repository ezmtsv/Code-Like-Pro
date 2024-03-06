package ru.netology.nmedia.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.insertSeparators
import androidx.paging.map
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.flow.flowOn
//import kotlinx.coroutines.flow.update
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody

import ru.netology.nmedia.api.PostsApiService
import ru.netology.nmedia.auth.AuthState
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dao.PostRemoteKeyDao
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.Ad
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.FeedItem
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.MediaUpload
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dto.TextSeparator
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.toDto
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.enumeration.AttachmentType
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.ApiError403
import ru.netology.nmedia.error.AppError
import ru.netology.nmedia.error.AuthorisationError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import java.io.IOException
import java.time.Duration
import java.time.OffsetDateTime
import java.util.Random
import javax.inject.Inject


class PostRepositoryImpl @Inject constructor(
    private val dao: PostDao,
    private val apiService: PostsApiService,
    postRemoteKeyDao: PostRemoteKeyDao,
    appDb: AppDb,
) : PostRepository {
    private val postsFlow = MutableStateFlow(emptyList<PostEntity>())

    @OptIn(ExperimentalPagingApi::class)
    override val data: Flow<PagingData<FeedItem>> = Pager(
        config = PagingConfig(
            pageSize = 5,
        ),
        pagingSourceFactory = { dao.getPagingSource() },
        remoteMediator = PostRemoteMediator(
            service = apiService,
            postDao = dao,
            postRemoteKeyDao = postRemoteKeyDao,
            db = appDb,

            )
    ).flow
        .map {
            var lastText = ""
            it.map(PostEntity::toDto)
                .insertSeparators { previos: Post?, next: Post? ->
                    val currentTime = OffsetDateTime.now()
                    val yesterday = currentTime.minus(Duration.ofDays(1))
                    val twoDay = currentTime.minus(Duration.ofDays(2))
                    val text = when {
                        next?.published!! < twoDay.toEpochSecond() -> "На прошлой неделе"
                        next.published < yesterday.toEpochSecond() -> "Вчера"
                        else -> "Сегодня"
                    }
                    if (previos?.id?.rem(5) == 0L) {
                        if (lastText == text) {
                            Ad(Random().nextLong(), 0, "https://netology.ru", "figma.jpg", "")
                        } else {
                            lastText = text
                            Ad(Random().nextLong(), 0, "https://netology.ru", "figma.jpg", text)
                        }

                    } else {
                        if (lastText == text) {
                            null
                        } else {
                            lastText = text
                            TextSeparator(0, next.published, text)
                        }

                    }
                }

        }

//    override val dataInvisible = dao.getAllInvisible()
//        .map(List<PostEntity>::toDto)

    override suspend fun getAllAsync() {
        try {
            val response = apiService.getAll()

            if (response.code() == 403) {
                throw ApiError403(response.code().toString())
            }

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val posts = response.body() ?: throw ApiError(response.code(), response.message())

            val postsUpdata = posts.map { post ->
                if (postsFlow.value.toDto()
                        .find { it.id == post.id && it.visibility } != null
                ) post.copy(visibility = true)
                else post
            }

            dao.insert(
                postsUpdata.map {
                    PostEntity.fromDto(it)
                })

        } catch (e: IOException) {
            throw NetworkError
        } catch (e: ApiError403) {
            throw AuthorisationError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override fun getNewer(id: Long): Flow<Int> = flow {
        while (true) {
            delay(10_000L)
            val response = apiService.getNewer(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(body.toEntity())
            emit(body.size)
        }
    }
        .catch { e -> throw AppError.from(e) }


    override suspend fun saveAsync(post: Post) {
        try {
            val response = apiService.save(post)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(PostEntity.fromDto(body.copy(visibility = true)))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun removeByIdAsync(post: Post) {
        try {
            dao.removeById(post.id)
            val response = apiService.removeById(post.id)
            if (!response.isSuccessful) {
                dao.insert(PostEntity.fromDto(post))
                throw ApiError(response.code(), response.message())
            }
        } catch (e: IOException) {
            dao.insert(PostEntity.fromDto(post))
            throw NetworkError
        } catch (e: Exception) {
            dao.insert(PostEntity.fromDto(post))
            throw UnknownError
        }

    }

    override suspend fun likeAsync(post: Post) {
        try {
            dao.likeById(post.copy(likedByMe = !post.likedByMe).id)
            if (!post.likedByMe) {
                val response = apiService.likeById(post.id)
                if (!response.isSuccessful) {
                    dao.likeById(post.id)
                    throw ApiError(response.code(), response.message())
                }
            } else {
                val response = apiService.dislikeById(post.id)
                if (!response.isSuccessful) {
                    dao.likeById(post.id)
                    throw ApiError(response.code(), response.message())
                }
            }

        } catch (e: IOException) {
            dao.likeById(post.id)
            throw NetworkError
        } catch (e: Exception) {
            dao.likeById(post.id)
            throw UnknownError
        }
    }

    override suspend fun updateContentAsync(post: Post, content: String) {
        dao.insert(PostEntity.fromDto(post.copy(content = content)))
        try {
            val response = apiService.save(post.copy(content = content))
            if (!response.isSuccessful) {
                dao.insert(PostEntity.fromDto(post))
                throw ApiError(response.code(), response.message())
            }
            response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: IOException) {
            dao.insert(PostEntity.fromDto(post))
            throw NetworkError
        } catch (e: Exception) {
            dao.insert(PostEntity.fromDto(post))
            throw UnknownError
        }
    }


    override suspend fun savePosts(posts: List<Post>) {
        dao.insert(posts.toEntity())
    }

    override suspend fun saveWithAttachment(post: Post, upload: MediaUpload) {
        try {
            val media = upload(upload)
            val postWithAttachment =
                post.copy(attachment = Attachment(media.id, AttachmentType.IMAGE))
            saveAsync(postWithAttachment)
        } catch (e: AppError) {
            throw e
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun upload(upload: MediaUpload): Media {
        try {
            val media = MultipartBody.Part.createFormData(
                "file", upload.file.name, upload.file.asRequestBody()
            )

            val response = apiService.upload(media)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            return response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

//    override suspend fun getPosts() {
//        dao.getAllPosts().flowOn(Dispatchers.IO).collect { posts ->
//            postsFlow.update { posts }
//        }
//    }

    override suspend fun userAuth(login: String, pass: String): AuthState {
        try {
            val response = apiService.updateUser(login, pass)
            if (!response.isSuccessful) {
                println("Error Auth")
                throw ApiError(response.code(), response.message())
            }
            return response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

}