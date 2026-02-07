package com.bearkingsoftware.loddboka.viewmodel

import androidx.lifecycle.ViewModel
import com.bearkingsoftware.loddboka.data.Loddbok
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class HovedskjermState(
    val loddboker: List<Loddbok> = emptyList(),
    val showResetDialog: Boolean = false
)

data class Vinner(
    val loddbok: Loddbok,
    val loddNummer: Int
)

data class TrekningsskjermState(
    val sisteVinner: Vinner? = null,
    val vinnere: List<Vinner> = emptyList(),
    val ingenLodd: Boolean = false,
    val loddTilTrekking: List<Vinner> = emptyList()
)

class LoddbokViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HovedskjermState())
    val uiState: StateFlow<HovedskjermState> = _uiState.asStateFlow()

    private val _trekningState = MutableStateFlow(TrekningsskjermState())
    val trekningState: StateFlow<TrekningsskjermState> = _trekningState.asStateFlow()

    fun addLoddbok(loddbok: Loddbok) {
        _uiState.update {
            val nyeLoddboker = it.loddboker + loddbok
            it.copy(loddboker = nyeLoddboker)
        }
    }
    
    fun deleteLoddbok(loddbok: Loddbok) {
        _uiState.update {
            val oppdatertListe = it.loddboker.filterNot { it.id == loddbok.id }
            it.copy(loddboker = oppdatertListe)
        }
    }

    fun updateLoddbok(loddbok: Loddbok) {
        _uiState.update {
            val oppdatertListe = it.loddboker.map {
                if (it.id == loddbok.id) loddbok else it
            }
            it.copy(loddboker = oppdatertListe)
        }
    }

    fun getLoddbok(id: Long): Loddbok? {
        return _uiState.value.loddboker.find { it.id == id }
    }

    fun showResetDialog() {
        _uiState.update { it.copy(showResetDialog = true) }
    }

    fun hideResetDialog() {
        _uiState.update { it.copy(showResetDialog = false) }
    }

    fun resetLoddbøker() {
        _uiState.value = HovedskjermState()
        _trekningState.value = TrekningsskjermState()
    }

    fun initialiserTrekning() {
        _trekningState.update { 
            it.copy(
                loddTilTrekking = alleLodd(), 
                vinnere = emptyList(), 
                sisteVinner = null, 
                ingenLodd = false
            ) 
        }
    }

    private fun alleLodd(): List<Vinner> {
        return _uiState.value.loddboker.flatMap { loddbok ->
            (loddbok.start..loddbok.end).map { loddNummer ->
                Vinner(loddbok, loddNummer)
            }
        }
    }

    fun trekkVinner() {
        val state = _trekningState.value
        val loddTilTrekking = state.loddTilTrekking

        if (loddTilTrekking.isEmpty()) {
            _trekningState.update { it.copy(sisteVinner = null, ingenLodd = true) }
            return
        }

        val nyVinner = loddTilTrekking.random()
        
        _trekningState.update { currentState ->
            currentState.copy(
                sisteVinner = nyVinner,
                vinnere = listOf(nyVinner) + currentState.vinnere,
                ingenLodd = false,
                loddTilTrekking = currentState.loddTilTrekking - nyVinner
            )
        }
    }
}