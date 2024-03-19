package ru.netology.nmedia.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
//import androidx.lifecycle.asLiveData
//import kotlinx.coroutines.flow.catch
//import kotlinx.coroutines.flow.stateIn
//import ru.netology.nmedia.model.FeedModel
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dto.Ad
import ru.netology.nmedia.dto.FeedItem
import ru.netology.nmedia.dto.MediaUpload
import ru.netology.nmedia.dto.PhotoModel
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.util.SingleLiveEvent
import java.io.File
import javax.inject.Inject
import kotlin.random.Random

private val empty = Post(
    id = 0,
    authorId = 0,
    author = "Me",
    content = "",
    likedByMe = false,
//    countRepost = 0,
//    countViews = 0,
    published = 0,
)

@HiltViewModel
@ExperimentalCoroutinesApi
class PostViewModel @Inject constructor(
    private val repository: PostRepository,
    appAuth: AppAuth
) : ViewModel() {
//    private val repository: PostRepository =
//        PostRepositoryImpl(AppDb.getInstance(application).postDao())

    private val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val noPhoto = PhotoModel()

    val postCreated: LiveData<Unit>
        get() = _postCreated

    private val cached = repository.data.cachedIn(viewModelScope)

    val data: Flow<PagingData<FeedItem>> = appAuth
        .authState
        .flatMapLatest { (myId, _) ->
            cached.map { pagingData ->
                pagingData.map { item ->
                    if (item !is Post) item else item.copy(ownedByMe = item.authorId == myId)
                }
            }
        }
        .flowOn(Dispatchers.Default)


    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    private val _photo = MutableLiveData(noPhoto)
    val photo: LiveData<PhotoModel>
        get() = _photo

    init {
        loadPosts()
        //getPosts()
    }

//    private fun getPosts() {
//        viewModelScope.launch {
//            repository.getPosts()
//        }
//    }

    fun loadPosts() {
        _dataState.value = FeedModelState(loading = true)
        viewModelScope.launch {
            try {
                //repository.getAllAsync()
                _dataState.value = FeedModelState()
            } catch (e: Exception) {
                if (e.javaClass.name == "ru.netology.nmedia.error.AuthorisationError") {
                    _dataState.value = FeedModelState(error403 = true)
                } else _dataState.value = FeedModelState(error = true)
            }
        }
    }

    fun savePost(content: String) {
        _postCreated.postValue(Unit)
        edited.value?.let { post ->
            viewModelScope.launch {
                try {
                    suspend fun save(updateContent: Boolean) {
                        when (_photo.value) {
                            noPhoto -> {
                                if (updateContent) repository.updateContentAsync(post, content)
                                else repository.saveAsync(post.copy(content = content))
                            }

                            else -> _photo.value?.file?.let { file ->
                                repository.saveWithAttachment(
                                    post.copy(content = content),
                                    MediaUpload(file)
                                )
                            }
                        }
                    }

                    _dataState.value = FeedModelState(loading = true)
                    if (post.id == 0L) save(false)
                    else save(true)

                    _dataState.value = FeedModelState()
                } catch (e: Exception) {
                    _dataState.value = FeedModelState(error = true)
                }
            }
        }
        edited.value = empty
        _photo.value = noPhoto
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun like(post: Post) {
        viewModelScope.launch {
            try {
                repository.likeAsync(post)
                _dataState.value = FeedModelState()
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }

    fun remove(post: Post) {
        viewModelScope.launch {
            try {
                _dataState.value = FeedModelState(loading = true)
                repository.removeByIdAsync(post)
                _dataState.value = FeedModelState()
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }


    fun cancelEdit() {
        edited.value = empty
    }

    fun clearPhoto() {
        _photo.value = noPhoto
    }

    fun changePhoto(uri: Uri?, file: File?) {
        _photo.value = PhotoModel(uri, file)
    }

}