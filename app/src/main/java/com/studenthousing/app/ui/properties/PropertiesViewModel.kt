package com.studenthousing.app.ui.properties

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studenthousing.app.data.local.PropertyEntity
import com.studenthousing.app.data.repo.ResultState
import com.studenthousing.app.data.repo.StudentHousingRepository
import com.studenthousing.app.util.LocationHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PropertiesViewModel(
    private val repository: StudentHousingRepository
) : ViewModel() {

    private val _state = MutableLiveData<ResultState<List<PropertyEntity>>>()
    val state: LiveData<ResultState<List<PropertyEntity>>> = _state

    private val _offline = MutableLiveData(false)
    val offline: LiveData<Boolean> = _offline

    private var lastItems: List<PropertyEntity> = emptyList()
    private var searchJob: Job? = null

    fun load(online: Boolean) {
        _offline.value = !online
        _state.value = ResultState.Loading
        viewModelScope.launch {
            val result = repository.getProperties(online)
            if (result is ResultState.Success) lastItems = result.data
            _state.value = result
        }
    }

    fun search(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(400) // debounce
            if (query.isBlank()) {
                _state.value = ResultState.Success(lastItems)
                return@launch
            }
            _state.value = ResultState.Loading
            val result = repository.searchProperties(search = query)
            _state.value = result
        }
    }

    fun applyFilters(
        type: String? = null,
        minRooms: Int? = null,
        maxPrice: Double? = null,
        campusLat: Double? = null,
        campusLng: Double? = null,
        maxDistanceKm: Double? = null
    ) {
        _state.value = ResultState.Loading
        viewModelScope.launch {
            val result = repository.searchProperties(
                type = type,
                minRooms = minRooms,
                maxPrice = maxPrice
            )

            // If campus coordinates and distance are provided, filter client-side
            if (result is ResultState.Success &&
                campusLat != null && campusLat != 0.0 &&
                campusLng != null && campusLng != 0.0 &&
                maxDistanceKm != null && maxDistanceKm > 0.0
            ) {
                val filtered = result.data.filter { property ->
                    val lat = property.latitude
                    val lng = property.longitude
                    if (lat == null || lng == null) false
                    else LocationHelper.distanceKm(lat, lng, campusLat, campusLng) <= maxDistanceKm
                }
                lastItems = filtered
                _state.value = ResultState.Success(filtered)
            } else {
                if (result is ResultState.Success) lastItems = result.data
                _state.value = result
            }
        }
    }

    // Sort all loaded properties by distance to a campus — used by the near campus switch
    fun sortByDistanceToCampus(
        enabled: Boolean,
        campusLat: Double = 0.0,
        campusLng: Double = 0.0
    ): List<PropertyEntity> {
        if (!enabled || campusLat == 0.0 || campusLng == 0.0) return lastItems
        return lastItems.sortedBy { item ->
            if (item.latitude == null || item.longitude == null) Double.MAX_VALUE
            else LocationHelper.distanceKm(item.latitude, item.longitude, campusLat, campusLng)
        }
    }
}
