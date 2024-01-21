package com.example.midterm_project.presentation.screen.list

import android.util.Log.d
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.midterm_project.data.common.Resource
import com.example.midterm_project.domain.repository.list.GenresRepository
import com.example.midterm_project.domain.repository.list.MovieFilterRepository
import com.example.midterm_project.presentation.event.list.ListEvent
import com.example.midterm_project.presentation.mapper.list.toPresenter
import com.example.midterm_project.presentation.state.list.ListState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListViewModel @Inject constructor(
    private val genresRepository: GenresRepository,
    private val movieFilterRepository: MovieFilterRepository
): ViewModel() {
    private val _listState = MutableStateFlow(ListState())
    val listState: SharedFlow<ListState> = _listState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<ListUIEvent>()
    val uiEvent: SharedFlow<ListUIEvent> get() = _uiEvent

    fun onEvent(event: ListEvent) {
        when(event) {
            is ListEvent.FetchGenres -> fetchGenres()
            is ListEvent.FetchMovies -> fetchMovies(id = event.id)
        }
    }

    private fun fetchGenres() {
        viewModelScope.launch {
            genresRepository.getGenres().collect{
                when(it) {
                    is Resource.Success -> {

                        _listState.update { currentState ->
                            currentState.copy(genres = it.data.toPresenter().genres)
                        }
                    }

                    is Resource.Error -> {
                        _listState.update { currentState->
                            currentState.copy(errorMessage = it.errorMessage)
                        }
                    }
                    is Resource.Loading -> {
                        _listState.update { currentState->
                            currentState.copy(isLoading = it.loading)
                        }
                    }
                }
            }
        }
    }

    private fun fetchMovies(id: Int?) {
        viewModelScope.launch {
            movieFilterRepository.getMovies(id = id).collect{
                when(it) {
                    is Resource.Success -> {
                        _listState.update {currentState ->
                            currentState.copy(movies = it.data.toPresenter().results)
                        }
                    }

                    is Resource.Error -> {
                        _listState.update {currentState ->
                            currentState.copy(errorMessage = it.errorMessage)
                        }
                    }

                    is Resource.Loading -> {
                        _listState.update {currentState ->
                            currentState.copy(isLoading = it.loading)
                        }
                    }
                }
            }
        }
    }

    sealed interface ListUIEvent {
        object NavigateToDetailed: ListUIEvent
    }
}