package com.example.multicomposetask2

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

sealed class UiState {
    object Loading : UiState()
    data class Success(val data: String) : UiState()
    data class Error(val message: String) : UiState()
}

class MainViewModel : ViewModel() {

    // MutableStateFlow to hold UI state
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> get() = _uiState

    // Function to fetch data from API
    fun fetchData() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading // Show loading state
            try {
                val api = RetrofitInstance.api
                val posts = api.getPosts()
                if (posts.isNotEmpty()) {
                    _uiState.value = UiState.Success(posts.first().title)
                } else {
                    _uiState.value = UiState.Error("No data found")
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }
}

// Retrofit instance to create API service
object RetrofitInstance {
    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://jsonplaceholder.typicode.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}

// API service definition
interface ApiService {
    @GET("posts")
    suspend fun getPosts(): List<Post>
}

// Data class representing the post model
data class Post(
    val userId: Int,
    val id: Int,
    val title: String,
    val body: String
)
