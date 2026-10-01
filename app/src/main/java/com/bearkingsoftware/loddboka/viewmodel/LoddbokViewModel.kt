package com.bearkingsoftware.loddboka.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bearkingsoftware.loddboka.data.Loddbok
import com.bearkingsoftware.loddboka.data.LoddbokRepository
import com.bearkingsoftware.loddboka.data.TicketPool
import kotlin.random.Random
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicLong

data class HovedskjermState(
    val loddboker: List<Loddbok> = emptyList(),
    val showResetDialog: Boolean = false,
    val isLoaded: Boolean = false
)

data class Vinner(
    val loddbok: Loddbok,
    val loddNummer: Int
)

data class TrekningsskjermState(
    val sisteVinner: Vinner? = null,
    /** Full winner history (kept across "new draw"). */
    val vinnere: List<Vinner> = emptyList(),
    val ingenLodd: Boolean = false,
    /** Remaining ticket ranges per book id — avoids materializing every ticket. */
    val remainingByBookId: Map<Long, List<IntRange>> = emptyMap(),
    /** Winners drawn in the current pool session (subtracted from remaining). Cleared on new draw. */
    val sessionDrawn: List<Vinner> = emptyList()
)

private data class MutableBookPool(
    val loddbok: Loddbok,
    val ranges: MutableList<IntRange>
) {
    fun remainingCount(): Long = ranges.sumOf { TicketPool.rangeSize(it) }
}



class LoddbokViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = LoddbokRepository(application)
    private val writeMutex = Mutex()

    private val _uiState = MutableStateFlow(HovedskjermState())
    val uiState: StateFlow<HovedskjermState> = _uiState.asStateFlow()

    private val _trekningState = MutableStateFlow(TrekningsskjermState())
    val trekningState: StateFlow<TrekningsskjermState> = _trekningState.asStateFlow()

    private val nextId = AtomicLong(System.currentTimeMillis())

    init {
        viewModelScope.launch {
            // Load once — avoid DataStore collect clobbering in-flight writes.
            val books = repository.loddboker.first()
            if (books.isNotEmpty()) {
                nextId.updateAndGet { current -> maxOf(current, books.maxOf { it.id } + 1) }
            }
            _uiState.update { it.copy(loddboker = books, isLoaded = true) }
        }
    }

    fun addLoddbok(loddbok: Loddbok) {
        viewModelScope.launch {
            writeMutex.withLock {
                val withId = if (loddbok.id == 0L) loddbok.copy(id = nextId.getAndIncrement()) else loddbok
                commitBooks(_uiState.value.loddboker + withId)
            }
        }
    }

    fun deleteLoddbok(loddbok: Loddbok) {
        viewModelScope.launch {
            writeMutex.withLock {
                commitBooks(_uiState.value.loddboker.filterNot { it.id == loddbok.id })
            }
        }
    }

    fun updateLoddbok(loddbok: Loddbok) {
        viewModelScope.launch {
            writeMutex.withLock {
                commitBooks(_uiState.value.loddboker.map { if (it.id == loddbok.id) loddbok else it })
            }
        }
    }

    fun getLoddbok(id: Long): Loddbok? = _uiState.value.loddboker.find { it.id == id }

    fun showResetDialog() {
        _uiState.update { it.copy(showResetDialog = true) }
    }

    fun hideResetDialog() {
        _uiState.update { it.copy(showResetDialog = false) }
    }

    fun resetLoddbøker() {
        viewModelScope.launch {
            writeMutex.withLock {
                repository.clear()
                _uiState.update { it.copy(loddboker = emptyList(), showResetDialog = false) }
                _trekningState.value = TrekningsskjermState()
            }
        }
    }

    /** Full reset of the draw session: clears history and rebuilds the ticket pool. */
    fun initialiserTrekning() {
        _trekningState.value = buildTrekningState(
            books = _uiState.value.loddboker,
            sessionDrawn = emptyList(),
            history = emptyList(),
            sisteVinner = null
        )
    }

    /**
     * Start a new draw: tickets become drawable again, books and winner history are kept.
     */
    fun startNewDraw() {
        val history = _trekningState.value.vinnere
        _trekningState.value = buildTrekningState(
            books = _uiState.value.loddboker,
            sessionDrawn = emptyList(),
            history = history,
            sisteVinner = null
        )
    }

    fun trekkVinner() {
        val state = _trekningState.value
        val booksById = _uiState.value.loddboker.associateBy { it.id }
        val mutablePool = state.remainingByBookId.mapNotNull { (id, ranges) ->
            val book = booksById[id] ?: return@mapNotNull null
            MutableBookPool(book, ranges.toMutableList())
        }.toMutableList()

        val total = mutablePool.sumOf { it.remainingCount() }
        if (total <= 0L) {
            _trekningState.update {
                it.copy(sisteVinner = null, ingenLodd = true, remainingByBookId = emptyMap())
            }
            return
        }

        var ticket = Random.nextLong(total)
        var chosen: Vinner? = null

        outer@ for (bookPool in mutablePool) {
            val rangeIterator = bookPool.ranges.listIterator()
            while (rangeIterator.hasNext()) {
                val range = rangeIterator.next()
                val size = TicketPool.rangeSize(range)
                if (ticket < size) {
                    // Long offset: ticket may exceed Int.MAX_VALUE for huge ranges; toInt() truncates.
                    val number = (range.first.toLong() + ticket).toInt()
                    chosen = Vinner(bookPool.loddbok, number)
                    when {
                        range.first == range.last -> rangeIterator.remove()
                        number == range.first -> rangeIterator.set((number + 1)..range.last)
                        number == range.last -> rangeIterator.set(range.first until number)
                        else -> {
                            rangeIterator.set(range.first until number)
                            rangeIterator.add((number + 1)..range.last)
                        }
                    }
                    break@outer
                }
                ticket -= size
            }
        }

        val remainingMap = mutablePool.associate { it.loddbok.id to it.ranges.toList() }
            .filterValues { it.isNotEmpty() }

        _trekningState.update { current ->
            current.copy(
                sisteVinner = chosen,
                vinnere = if (chosen != null) listOf(chosen) + current.vinnere else current.vinnere,
                sessionDrawn = if (chosen != null) listOf(chosen) + current.sessionDrawn else current.sessionDrawn,
                ingenLodd = chosen == null || remainingMap.isEmpty(),
                remainingByBookId = remainingMap
            )
        }
    }

    private suspend fun commitBooks(books: List<Loddbok>) {
        repository.save(books)
        _uiState.update { it.copy(loddboker = books) }
        syncTrekningPoolWithBooks(books)
    }

    /**
     * Keep an active draw session consistent when books change:
     * rebuild remaining ranges from current books, minus tickets already drawn this session.
     */
    private fun syncTrekningPoolWithBooks(books: List<Loddbok>) {
        val trekning = _trekningState.value
        val sessionActive = trekning.remainingByBookId.isNotEmpty() ||
            trekning.sessionDrawn.isNotEmpty() ||
            trekning.vinnere.isNotEmpty() ||
            trekning.sisteVinner != null ||
            trekning.ingenLodd
        if (!sessionActive) return

        val sessionDrawn = trekning.sessionDrawn.filter { winner ->
            books.any { it.id == winner.loddbok.id }
        }
        val history = trekning.vinnere.filter { winner ->
            books.any { it.id == winner.loddbok.id }
        }
        val siste = trekning.sisteVinner?.takeIf { winner ->
            books.any { it.id == winner.loddbok.id }
        }
        _trekningState.value = buildTrekningState(books, sessionDrawn, history, siste)
    }

    private fun buildTrekningState(
        books: List<Loddbok>,
        sessionDrawn: List<Vinner>,
        history: List<Vinner>,
        sisteVinner: Vinner?
    ): TrekningsskjermState {
        if (books.isEmpty()) {
            return TrekningsskjermState(
                sisteVinner = sisteVinner,
                vinnere = history,
                sessionDrawn = sessionDrawn,
                ingenLodd = true,
                remainingByBookId = emptyMap()
            )
        }
        val drawnByBook = sessionDrawn.groupBy({ it.loddbok.id }, { it.loddNummer })
        val remaining = books.associate { book ->
            val ranges = mutableListOf(book.start..book.end)
            drawnByBook[book.id].orEmpty().forEach { num -> TicketPool.removeNumber(ranges, num) }
            book.id to ranges.toList()
        }.filterValues { ranges -> ranges.any { TicketPool.rangeSize(it) > 0L } }

        return TrekningsskjermState(
            sisteVinner = sisteVinner,
            vinnere = history,
            sessionDrawn = sessionDrawn,
            ingenLodd = remaining.isEmpty(),
            remainingByBookId = remaining
        )
    }
}
