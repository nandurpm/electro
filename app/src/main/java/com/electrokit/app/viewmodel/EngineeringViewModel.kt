package com.electrokit.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.electrokit.app.data.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json

/**
 * Loads the immutable engineering assets off the UI thread while preserving legacy storage.
 *
 * @param application Application-owned asset access; no Activity is retained. Asset failures become
 *   a retryable error state; cancellation is never swallowed.
 */
class EngineeringViewModel(application: Application) : AndroidViewModel(application) {
    /** Atomic complete corpus; null until both datasets are validated. */
    private val mutableContent =
        MutableStateFlow<Pair<EngineeringContent, List<EngineeringTool>>?>(null)
    /** Read-only complete content, preventing partially loaded cross-reference links. */
    val content = mutableContent.asStateFlow()
    /** True when asset parsing or structural validation failed. */
    private val mutableFailed = MutableStateFlow(false)
    /** UI uses a resource message rather than exposing parser exception details. */
    val failed = mutableFailed.asStateFlow()
    /** A single load job prevents competing retries publishing inconsistent snapshots. */
    private var job: Job? = null

    init {
        reload()
    }

    /**
     * Retries reading and validating the local engineering datasets.
     *
     * @return Unit; I/O/serialization failures set failed, cancellation propagates.
     */
    fun reload() {
        job?.cancel()
        mutableFailed.value = false
        job =
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val assets = getApplication<Application>().assets
                    val corpus =
                        assets.open("engineering.json").bufferedReader().use {
                            Json.decodeFromString<EngineeringContent>(it.readText())
                        }
                    val tools =
                        assets.open("engineering_tools.json").bufferedReader().use {
                            Json.decodeFromString<List<EngineeringTool>>(it.readText())
                        }
                    require(
                        corpus.faults.isNotEmpty() &&
                            corpus.faults.map { it.code }.distinct().size == corpus.faults.size
                    )
                    val ids = corpus.records.map { it.id }.toSet()
                    val sources = corpus.sources.map { it.id }.toSet()
                    require(
                        ids.size == corpus.records.size &&
                            tools.map { it.id }.distinct().size == tools.size
                    )
                    require(
                        corpus.records.all {
                            sources.containsAll(it.sources) && ids.containsAll(it.related)
                        }
                    )
                    require(
                        corpus.faults.all {
                            sources.containsAll(it.sources) && ids.containsAll(it.related)
                        }
                    )
                    mutableContent.value = corpus to tools
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    mutableFailed.value = true
                }
            }
    }
}
