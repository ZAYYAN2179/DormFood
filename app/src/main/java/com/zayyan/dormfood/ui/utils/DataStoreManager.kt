package com.zayyan.dormfood.ui.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


object DataStoreManager {

    val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "dormfood_prefs")
    val PEMASUKAN_VISIBLE = booleanPreferencesKey("pemasukan_visible")

    fun isPemasukanVisible(context: Context): Flow<Boolean> =
        context.dataStore.data.map { it[PEMASUKAN_VISIBLE] ?: false }

    suspend fun setPemasukanVisible(context: Context, visible: Boolean) {
        context.dataStore.edit { it[PEMASUKAN_VISIBLE] = visible }
    }

    val SHOW_GRAFIK = booleanPreferencesKey("show_grafik")

    fun isShowGrafik(context: Context): Flow<Boolean> =
        context.dataStore.data.map { it[SHOW_GRAFIK] ?: false }

    suspend fun setShowGrafik(context: Context, show: Boolean) {
        context.dataStore.edit { it[SHOW_GRAFIK] = show }
    }
}