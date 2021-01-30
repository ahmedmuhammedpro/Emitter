package com.ahmed.emitter.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.ahmed.emitter.network.User
import com.ahmed.emitter.network.UserNetwork
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.IOException

class EmitterViewModel(app: Application) : AndroidViewModel(app) {

    private val users = MutableLiveData<List<User>>()
    private val _eventErrorNetwork = MutableLiveData(false)
    val eventErrorNetwork: LiveData<Boolean>
        get() = _eventErrorNetwork
    private val _isNetworkErrorShown = MutableLiveData(false)
    val isNetworkErrorShown: LiveData<Boolean>
        get() = _isNetworkErrorShown

    init {
        loadUsers()
    }

    fun loadUsers() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val userList = UserNetwork.userNetwork.getUsers()
                withContext(Dispatchers.Main) {
                    users.value = userList
                    _isNetworkErrorShown.value = false
                    _eventErrorNetwork.value = false
                }
            } catch (ex: IOException) {
                Timber.e(ex)
                if (users.value.isNullOrEmpty()) {
                    withContext(Dispatchers.Main) {
                        _eventErrorNetwork.value = true
                    }
                }
            }
        }
    }

    fun convertUserToJson(user: User): String {
        return Gson().toJson(user, User::class.java)
    }

    fun onNetworkError() {
        _eventErrorNetwork.value = true
    }

    fun getUsers(): LiveData<List<User>> {
        return users
    }

    class Factory(private val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(EmitterViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return EmitterViewModel(app) as T
            }

            throw IllegalArgumentException("Unable to construct viewmodel")
        }

    }

}