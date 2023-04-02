package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl
import ru.netology.nmedia.util.SingleLiveEvent

private val empty = Post(
    id = 0,
    content = "",
    author = "",
    authorAvatar = "",
    likedByMe = false,
    likes = 0,
    published = ""
)

class PostViewModel(application: Application) : AndroidViewModel(application) {
    // упрощённый вариант
    private val repository: PostRepository = PostRepositoryImpl()
    private val _data = MutableLiveData(FeedModel())
    val data: LiveData<FeedModel>
        get() = _data
    private val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    init {
        loadPosts()
    }

    fun loadPosts() {
        _data.value = FeedModel(loading = true)
        repository.getAllAsync(object : PostRepository.Callback<List<Post>> {
            override fun onSuccess(posts: List<Post>) {
                _data.value = FeedModel(posts = posts, empty = posts.isEmpty())
            }

            override fun onError(e: Exception) {
                _data.value = FeedModel(error = true)
            }
        })
    }

    fun save() {
        edited.value?.let {
            repository.save(it, object : PostRepository.Callback<Post> {
                override fun onSuccess(post: Post) {
                    _postCreated.postValue(Unit)
                    edited.postValue(empty)
                    val posts = _data.value?.posts.orEmpty()
                    _data.postValue(FeedModel(posts = posts, empty = posts.isEmpty()))
                }
                override fun onError(e: Exception) {
                    _data.postValue(FeedModel(error = true))
                }
            })
            _postCreated.value = Unit
        }
        edited.value = empty
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (edited.value?.content == text) {
            return
        }
        edited.value = edited.value?.copy(content = text)
    }

    fun likeById(id: Long) {
        val posts = _data.value?.posts.orEmpty()
        _data.value = FeedModel(posts = posts, loading = true)
        val oldPost =
            _data.value?.posts?.find { it.id == id } ?: error("Post not found")
        var newPosts: List<Post>
        if (!oldPost.likedByMe) {
            repository.likeById(id, object : PostRepository.Callback<Post> {
                override fun onSuccess(post: Post) {
                    newPosts = _data.value?.posts.orEmpty()
                        .map { if (it.id == id) post else it }
                    _data.postValue(FeedModel(posts = newPosts))
                }
                override fun onError(e: Exception) {
                    _data.postValue(FeedModel(error = true))
                }
            })
        } else {
            repository.dislikeById(id, object : PostRepository.Callback<Post> {
                override fun onSuccess(post: Post) {
                    newPosts = _data.value?.posts.orEmpty()
                        .map { if (it.id == id) post else it }
                    _data.postValue(FeedModel(posts = newPosts))
                }
                override fun onError(e: Exception) {
                    _data.postValue(FeedModel(error = true))
                }
            })
        }
        loadPosts()
        _data.value = FeedModel(loading = false)
    }

    fun removeById(id: Long) {
        val oldPosts = _data.value?.posts.orEmpty()
        repository.removeById(id, object : PostRepository.Callback<Unit>{
            override fun onSuccess(post: Unit) {
                val posts = oldPosts.filter { it.id != id }
                _data.value = FeedModel(posts = posts, empty = posts.isEmpty())
            }

            override fun onError(e: Exception) {
                _data.postValue(_data.value?.copy(posts = oldPosts))
                _data.postValue(FeedModel(error = true))
            }
        })
    }
}
