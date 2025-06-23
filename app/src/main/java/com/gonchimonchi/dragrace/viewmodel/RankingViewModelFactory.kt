package com.gonchimonchi.dragrace.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class RankingViewModelFactory(private val seasonName: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RankingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RankingViewModel(seasonName) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}