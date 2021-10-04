package com.rsschool.cats.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.*
import android.net.NetworkCapabilities.*
import android.os.Build
import androidx.lifecycle.*
import com.rsschool.cats.data.Cat
import com.rsschool.cats.data.CatApiImpl
import kotlinx.coroutines.launch

class CatViewModel(application: Application) : AndroidViewModel(application) {

    private val _items = MutableLiveData<List<Cat>>()
    val items: LiveData<List<Cat>> get() = _items
    private var lastNumber = 1

    init {
        viewModelScope.launch {
            if (hasInternetConnection()) {
                val curList = CatApiImpl.getListOfCats(lastNumber).toMutableList()
                curList.add(Cat(-lastNumber, null, null))
                _items.value = curList
            } else {
                val curList = mutableListOf<Cat>()
                curList.add(Cat(0, null, null))
                _items.value = curList
            }
        }
    }

    fun addCats() = viewModelScope.launch {
        val curList = _items.value?.toMutableList()
        if (curList != null) {
            if (hasInternetConnection()) {
                val lastCat = curList.maxByOrNull { it.number }
                lastNumber = lastCat?.number ?: 1
                curList.removeAll { it.number <= 0 }
                curList.addAll(CatApiImpl.getListOfCats(++lastNumber))
                curList.add(Cat(-lastNumber, null, null))
                _items.value = curList
            } else {
                curList.removeAll { it.number <= 0 }
                curList.add(Cat(0, null, null))
                _items.value = curList
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun hasInternetConnection(): Boolean {
        val connectivityManager = getApplication<Application>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            return when {
                capabilities.hasTransport(TRANSPORT_WIFI) -> true
                capabilities.hasTransport(TRANSPORT_CELLULAR) -> true
                capabilities.hasTransport(TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            connectivityManager.activeNetworkInfo?.run {
                return when (type) {
                    TYPE_WIFI -> true
                    TYPE_MOBILE -> true
                    TYPE_ETHERNET -> true
                    else -> false
                }
            }
        }
        return false
    }
}
