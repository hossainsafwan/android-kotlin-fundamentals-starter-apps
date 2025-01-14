/*
 * Copyright 2019, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.trackmysleepquality.sleeptracker

import android.app.Application
import androidx.lifecycle.*
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import com.example.android.trackmysleepquality.database.SleepNight
import com.example.android.trackmysleepquality.formatNights
import kotlinx.coroutines.launch

/**
 * ViewModel for SleepTrackerFragment.
 */
class SleepTrackerViewModel(val database: SleepDatabaseDao, application: Application) : AndroidViewModel(application) {

        private var tonight = MutableLiveData<SleepNight?>()
        private val night = database.getAllNights()
        val nightsString = Transformations.map(night) { night->
                formatNights(night, application.resources)
        }

        private var _trackerToQuality = MutableLiveData<SleepNight>()

        val trackerToQuality: LiveData<SleepNight> get() = _trackerToQuality

        val startButtonVisible = Transformations.map(tonight) {
                it == null
        }
        val stopButtonVisible = Transformations.map(tonight) {
                it != null
        }
        val clearButtonVisible = Transformations.map(night) {
                it?.isNotEmpty()
        }

        private var _showSnackbar = MutableLiveData<Boolean>()
        val showSnackbar: LiveData<Boolean> get() = _showSnackbar

        fun snackbarshownComplete() {
                _showSnackbar.value = false
        }


        init {
            initializeTonight()
        }

        fun doneNavigating() {
                _trackerToQuality.value = null
        }

        private fun initializeTonight() {
                viewModelScope.launch {
                        tonight.value  = getTonightFromDatabase()
                }

        }

        private suspend fun getTonightFromDatabase(): SleepNight? {
                var night = database.getTonight()
                if (night?.endTimeMilli != night?.startTimeMilli) {
                        night = null
                }
                return night
        }

        fun onStartTracking() {
                viewModelScope.launch {
                        val newNight = SleepNight()
                        insert(newNight)
                        tonight.value = getTonightFromDatabase()
                }
        }
        fun onStopTracking() {
                viewModelScope.launch {
                        val oldNight = tonight.value ?: return@launch
                        oldNight.endTimeMilli = System.currentTimeMillis()
                        update(oldNight)
                        _trackerToQuality.value = oldNight
                }
        }
        fun onClear() {
                viewModelScope.launch {
                        clearDatabase()
                        tonight.value = null
                        _showSnackbar.value = true
                }
        }
        private suspend fun clearDatabase() {
                database.clear()
        }
        private suspend fun update(night: SleepNight) {
                database.update(night)
        }

        private suspend fun insert(night: SleepNight) {
                database.insert(night)
        }
}

