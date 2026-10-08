package com.bearkingsoftware.loddboka.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.loddbokDataStore: DataStore<Preferences> by preferencesDataStore(name = "loddboker")

class LoddbokRepository(private val context: Context) {

    private val key = stringPreferencesKey("loddboker_json")

    val loddboker: Flow<List<Loddbok>> = context.loddbokDataStore.data.map { prefs ->
        decode(prefs[key].orEmpty())
    }

    suspend fun save(loddboker: List<Loddbok>) {
        context.loddbokDataStore.edit { prefs ->
            prefs[key] = encode(loddboker)
        }
    }

    suspend fun clear() {
        save(emptyList())
    }

    private fun encode(items: List<Loddbok>): String {
        val arr = JSONArray()
        items.forEach { item ->
            arr.put(
                JSONObject()
                    .put("id", item.id)
                    .put("farge", item.farge.name)
                    .put("bokstav", item.bokstav.toString())
                    .put("start", item.start)
                    .put("end", item.end)
            )
        }
        return arr.toString()
    }

    private fun decode(raw: String): List<Loddbok> {
        if (raw.isBlank()) return emptyList()
        return try {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val fargeName = obj.getString("farge")
                    val farge = runCatching { Farge.valueOf(fargeName) }.getOrNull() ?: continue
                    val bokstav = obj.getString("bokstav").firstOrNull() ?: continue
                    add(
                        Loddbok(
                            id = obj.getLong("id"),
                            farge = farge,
                            bokstav = bokstav,
                            start = obj.getInt("start"),
                            end = obj.getInt("end")
                        )
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }
}
