package ru.netology.nmedia.viewmodel

import android.annotation.SuppressLint
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
//import androidx.lifecycle.asLiveData
//import kotlinx.coroutines.flow.catch
//import kotlinx.coroutines.flow.stateIn
//import ru.netology.nmedia.model.FeedModel
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dto.FeedItem
import ru.netology.nmedia.dto.MediaUpload
import ru.netology.nmedia.dto.PhotoModel
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.util.SingleLiveEvent
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import javax.inject.Inject

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
                pagingData.map {post ->
                    if (post is Post) {
                        post.copy(ownedByMe = post.authorId == myId)
                    } else {post}
                }
            }
        }
        .flowOn(Dispatchers.Default)



//    val dataInvisible: LiveData<FeedModel> = repository.dataInvisible
//        .map(::FeedModel)
//        .catch { it.printStackTrace() }
//        .asLiveData(Dispatchers.Default)

//    val newerCount: LiveData<Int> = data.switchMap {
//        repository.getNewer(it.posts.firstOrNull()?.id ?: 0L)
//            .catch { e -> e.printStackTrace() }
//            .asLiveData(Dispatchers.Default)
//    }

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

//    fun refreshPosts() {
//        _dataState.value = FeedModelState(refreshing = true)
//        viewModelScope.launch {
//            try {
//                //repository.getAllAsync()
//                _dataState.value = FeedModelState()
//            } catch (e: Exception) {
//                if (e.javaClass.name == "ru.netology.nmedia.error.AuthorisationError") {
//                    _dataState.value = FeedModelState(error403 = true)
//                } else _dataState.value = FeedModelState(error = true)
//            }
//        }
//    }

//    fun showPosts() {
//        viewModelScope.launch {
//            val posts = dataInvisible.value?.posts?.map {
//                it.copy(visibility = true)
//            }
//            posts?.let {
//                repository.savePosts(posts)
//            }
//        }
//    }

    fun clearPhoto() {
        _photo.value = noPhoto
    }

    fun changePhoto(uri: Uri?, file: File?) {
        _photo.value = PhotoModel(uri, file)
    }

    @SuppressLint("SimpleDateFormat")
    private fun getTime(): String =
        SimpleDateFormat("dd MMMM yyyy, HH:mm").format(Calendar.getInstance().time).toString()
}