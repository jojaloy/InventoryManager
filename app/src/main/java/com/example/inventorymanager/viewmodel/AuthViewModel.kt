package com.inventory.manager.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventory.manager.model.ApiResult
import com.inventory.manager.model.User
import com.inventory.manager.repository.InventoryRepository
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val repository = InventoryRepository()

    private val _loginResult = MutableLiveData<ApiResult<User?>>()
    val loginResult: LiveData<ApiResult<User?>> = _loginResult

    private val _registerResult = MutableLiveData<ApiResult<User>>()
    val registerResult: LiveData<ApiResult<User>> = _registerResult

    fun login(username: String) {
        _loginResult.value = ApiResult.Loading
        viewModelScope.launch {
            _loginResult.value = repository.login(username)
        }
    }

    fun register(user: User) {
        _registerResult.value = ApiResult.Loading
        viewModelScope.launch {
            _registerResult.value = repository.register(user)
        }
    }
}