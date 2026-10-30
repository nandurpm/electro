package com.electrokit.app.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Defines a registry route whose stable identifiers decouple navigation and favourites from visible
 * titles.
 *
 * @param id Stable calculator or registry identifier; independent of the visible title.
 * @param title Visible calculator title used to select the intended test route.
 * @param icon Unique original vector motif key, independent of the visible title.
 * @param tab Stable catalogue group used for filtering, ordering and feature counts.
 * @param screenType Renderer discriminator selecting calculator, reference or module content.
 * @param contentId Shared content target; aliases avoid duplicated reference datasets.
 * @param description Optional original module summary, or icon accessibility text at the drawing
 *   entry point. Construction holds metadata/helpers; physical validation is performed by the named
 *   operations, not by field assignment.
 */
@Serializable
data class Entry(
    /** Stable route/preference key survives title changes. */
    val id: String,
    /** Visible catalogue/reference title authored in the bundled dataset. */
    val title: String,
    /** Original vector motif key; no downloaded artwork required. */
    val icon: String,
    /** Catalogue grouping used for ordering and feature counts. */
    val tab: String,
    /** Renderer discriminator: calculator, reference or module. */
    val screenType: String,
    /** Shared content target; aliases avoid duplicate reference data. */
    val contentId: String,
    /** Original database summary shown before opening its records. */
    val description: String = "",
)

/**
 * Locates a labelled contact in normalised diagram coordinates without requiring a bitmap.
 *
 * @param label Visible field/cell caption used for accessible presentation.
 * @param x Coordinate in this renderer’s documented canvas space.
 * @param y Coordinate in this renderer’s documented canvas space. Construction holds
 *   metadata/helpers; physical validation is performed by the named operations, not by field
 *   assignment.
 */
@Serializable
data class Pin(
    /** Visible contact identifier, not a certified PCB footprint number. */
    val label: String,
    /** Horizontal coordinate normalised to diagram width. */
    val x: Float,
    /** Vertical coordinate normalised to diagram height. */
    val y: Float,
)

/**
 * Describes an original vector connector or symbol independently of display density.
 *
 * @param caption Connector variant/view explanation shown beside its original vector drawing.
 * @param shape Vector renderer key; it does not establish certified mechanical geometry.
 * @param pins Ordered contact labels and normalised coordinates used by the diagram renderer.
 *   Construction holds metadata/helpers; physical validation is performed by the named operations,
 *   not by field assignment.
 */
@Serializable
data class Diagram(
    /** Connector variant/view explanation rendered with the drawing. */
    val caption: String,
    /** Vector renderer key interpreted by DiagramView. */
    val shape: String = "rect",
    /** Contact identifiers and normalised coordinates in dataset order. */
    val pins: List<Pin> = emptyList(),
)

/**
 * Groups a searchable reference table, its explanatory notes and identification drawings.
 *
 * @param title Visible calculator title used to select the intended test route.
 * @param headers Ordered column captions whose count must match each data row.
 * @param rows Offline reference cells validated against the header width.
 * @param notes Scope and assumptions retained with the displayed reference.
 * @param diagrams Original vector identification drawings belonging to this reference section.
 *   Construction holds metadata/helpers; physical validation is performed by the named operations,
 *   not by field assignment.
 */
@Serializable
data class Section(
    /** Visible catalogue/reference title authored in the bundled dataset. */
    val title: String,
    /** Ordered table columns; each row must have matching width. */
    val headers: List<String>,
    /** Offline text cells validated for structural width. */
    val rows: List<List<String>>,
    /** Scope and assumptions shown alongside the reference. */
    val notes: String = "",
    /** Original identification drawings for this reference section. */
    val diagrams: List<Diagram> = emptyList(),
)

/**
 * Holds offline reference groups and optional source URLs without performing network lookups.
 *
 * @param title Visible calculator title used to select the intended test route.
 * @param sections Ordered searchable groups of tables, diagrams and explanatory text.
 * @param notes Scope and assumptions retained with the displayed reference.
 * @param sources Reference URLs shown as text; the app never fetches them. Construction holds
 *   metadata/helpers; physical validation is performed by the named operations, not by field
 *   assignment.
 */
@Serializable
data class Page(
    /** Visible catalogue/reference title authored in the bundled dataset. */
    val title: String,
    /** Ordered searchable diagram/table groups. */
    val sections: List<Section>,
    /** Scope and assumptions shown alongside the reference. */
    val notes: String = "",
    /** Source URLs shown as text, never fetched by the app. */
    val sources: List<String> = emptyList(),
    /** Records dataset origin and verification limits separately from citation URLs. */
    val provenance: String = "",
)

/**
 * Keeps numeric filter keys explicit so descriptions containing numbers are never inferred to be
 * scalar specifications.
 *
 * @param name Asset filename or preview output name, as stated by the operation.
 * @param category Local browsing category, separate from prose specification fields.
 * @param specs Named specification strings that preserve units and qualifications.
 * @param source Product/family reference behind the component summary.
 * @param numericKeys Explicit scalar keys eligible for numeric filters; descriptions are excluded.
 *   Construction holds metadata/helpers; physical validation is performed by the named operations,
 *   not by field assignment.
 */
@Serializable
data class Part(
    /** Part identifier used for local search and display. */
    val name: String,
    /** Browsing category retained separately from specification prose. */
    val category: String,
    /** Named specification strings preserving units and qualifications. */
    val specs: Map<String, String>,
    /** Specific product/family reference behind the component summary. */
    val source: String,
    /** Explicit scalar keys eligible for numeric filters; prose is excluded. */
    val numericKeys: Set<String> = emptySet(),
)

/**
 * Represents one atomic persisted record with defaults compatible with older snapshots.
 *
 * @param light True selects the light palette.
 * @param alphabetical True selects A–Z order within the favourite and ordinary groups.
 * @param favourites Stable registry IDs pinned before ordinary catalogue entries.
 * @param modules Legacy optional-module selection retained only for saved-record compatibility; all
 *   databases are now available.
 * @param values Caller-owned field/component snapshot; not mutated by this operation.
 * @param modes Ordered selectable calculation modes. Construction holds metadata/helpers; physical
 *   validation is performed by the named operations, not by field assignment.
 */
@Serializable
data class PreferencesState(
    /** Persisted palette choice; first run defaults to light. */
    val light: Boolean = true,
    /** True selects A–Z within favourite and ordinary groups. */
    val alphabetical: Boolean = false,
    /** Stable registry IDs pinned before other entries. */
    val favourites: Set<String> = emptySet(),
    /**
     * Legacy optional-module selection retained for saved-record compatibility; database
     * availability ignores it.
     */
    val modules: Set<String> = emptySet(),
    /** Per-calculator raw snapshots, including selected unit scales. */
    val values: Map<String, Map<String, String>> = emptyMap(),
    /** Per-calculator mode indices, clamped by the engine when read. */
    val modes: Map<String, Int> = emptyMap(),
    /** Empty mode migrates existing light/dark preferences without changing their meaning. */
    val themeMode: String = "",
    /** Most-recent-first public registry routes; protected engineering details are excluded. */
    val recent: List<String> = emptyList(),
    /** Bounded, explicitly saved legacy calculator results, stored in the existing DataStore. */
    val calculations: List<String> = emptyList(),
)

/** One application-scoped DataStore delegate avoids concurrent stores for the same file. */
private val Context.store by preferencesDataStore("electrokit")

/**
 * Provides a storage boundary so tests can simulate failures without corrupting real user
 * preferences. Construction holds metadata/helpers; physical validation is performed by the named
 * operations, not by field assignment.
 */
interface AppRepository {
    /** Ordered routes published only after every asset decodes. */
    val registry: List<Entry>
    /** Shared reference content keyed by stable content ID. */
    val pages: Map<String, Page>
    /** Bundled component datasets keyed by module ID. */
    val parts: Map<String, List<Part>>
    /** Preference observation owned by storage and exposed read-only by the ViewModel. */
    val preferences: Flow<PreferencesState>

    /**
     * Stages an input synchronously before accepting its UI edit.
     *
     * @param id Stable calculator key.
     * @param values Raw input snapshot.
     * @return Unit after durable staging; IOException propagates on storage failure.
     */
    fun stageInputs(id: String, values: Map<String, String>)

    /**
     * Reads inputs not yet acknowledged by the main preference record.
     *
     * @return Recoverable field maps; IOException reports corrupt storage.
     */
    fun stagedInputs(): Map<String, Map<String, String>>

    /**
     * Acknowledges only records that reached DataStore.
     *
     * @param committed Successfully written snapshot.
     * @return Unit; IOException propagates if the backup cannot be cleared.
     */
    fun clearStagedInputs(committed: Map<String, Map<String, String>>)

    /**
     * Decodes every bundled dataset before publishing the complete catalogue.
     *
     * @return Unit after all datasets are published together. I/O and serialization failures
     *   propagate; cancellation is not converted into success.
     */
    suspend fun loadAssets()

    /**
     * Transforms the latest preference record inside an atomic DataStore edit.
     *
     * @param transform Transformation applied to the latest stored snapshot, not a stale UI copy.
     * @return Unit after the transformed record commits. I/O and serialization failures propagate;
     *   cancellation is not converted into success.
     */
    suspend fun update(transform: (PreferencesState) -> PreferencesState)

    /**
     * Removes the saved record so the next observation uses documented defaults.
     *
     * @return Unit after the removal commits. I/O and serialization failures propagate;
     *   cancellation is not converted into success.
     */
    suspend fun resetPreferences()
}

/**
 * Loads bundled JSON and updates local DataStore atomically; construction does not parse the
 * assets.
 *
 * @param context Android context used only for local assets/storage or the user-directed share
 *   chooser. Construction retains application context; asset I/O is deferred until loadAssets.
 */
class Repository(context: Context) : AppRepository {
    /** Application context avoids retaining an Activity through storage. */
    private val app = context.applicationContext
    /** Private write-through recovery file; no cloud or external storage is used. */
    private val inputJournal = InputJournal(java.io.File(context.filesDir, "input-journal.json"))

    /**
     * Stages a complete input snapshot; the caller must execute this blocking operation on IO.
     *
     * @param id Calculator key.
     * @param values Raw fields.
     * @return Unit after durable staging; IOException propagates.
     */
    override fun stageInputs(id: String, values: Map<String, String>) = inputJournal.put(id, values)

    /**
     * Loads staged values so process recreation cannot revert to an older main record.
     *
     * @return Pending snapshots; IOException reports damaged storage.
     */
    override fun stagedInputs() = inputJournal.read()

    /**
     * Retains newer edits when an older main-record write completes.
     *
     * @param committed Snapshot accepted by DataStore.
     * @return Unit; IOException is retryable by the owner.
     */
    override fun clearStagedInputs(committed: Map<String, Map<String, String>>) =
        inputJournal.clear(committed)

    /** Compatibility codec accepts additive fields and writes explicit defaults. */
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    /**
     * Decodes a bundled asset without swallowing I/O/schema failures needed by the loading error
     * handler.
     *
     * @param name Asset filename or preview output name, as stated by the operation.
     * @return The requested decoded asset model. I/O and serialization failures propagate to the
     *   owner; cancellation remains cooperative.
     */
    private inline fun <reified T> asset(name: String): T =
        json.decodeFromString(app.assets.open(name).bufferedReader().use { it.readText() })

    /** Ordered routes published only after every asset decodes. */
    override var registry: List<Entry> = emptyList()
        private set

    /** Shared reference content keyed by stable content ID. */
    override var pages: Map<String, Page> = emptyMap()
        private set

    /** Bundled component datasets keyed by module ID. */
    override var parts: Map<String, List<Part>> = emptyMap()
        private set

    /**
     * Decodes every bundled dataset before publishing the complete catalogue.
     *
     * @return Unit after all datasets are published together. I/O and serialization failures
     *   propagate; cancellation is not converted into success.
     */
    override suspend fun loadAssets() =
        withContext(Dispatchers.IO) {
            val loadedRegistry: List<Entry> = asset("registry.json")
            val loadedPages: Map<String, Page> = asset("pages.json")
            val loadedParts: Map<String, List<Part>> = asset("modules.json")
            registry = loadedRegistry
            pages = loadedPages
            parts = loadedParts
        }

    /** Single DataStore record enables atomic transforms of independent preference fields. */
    private val key = stringPreferencesKey("state")

    /** Preference observation owned by storage and exposed read-only by the ViewModel. */
    override val preferences: Flow<PreferencesState> =
        app.store.data.map { PreferenceCodec.decode(it[key]) }

    /**
     * Transforms the latest preference record inside an atomic DataStore edit.
     *
     * @param transform Transformation applied to the latest stored snapshot, not a stale UI copy.
     * @return Unit after the transformed record commits. I/O and serialization failures propagate;
     *   cancellation is not converted into success.
     */
    override suspend fun update(transform: (PreferencesState) -> PreferencesState) {
        app.store.edit { prefs ->
            prefs[key] =
                json.encodeToString(
                    PreferencesState.serializer(),
                    transform(PreferenceCodec.decode(prefs[key])),
                )
        }
    }

    /**
     * Removes the saved record so the next observation uses documented defaults.
     *
     * @return Unit after the removal commits. I/O and serialization failures propagate;
     *   cancellation is not converted into success.
     */
    override suspend fun resetPreferences() {
        inputJournal.reset()
        app.store.edit { it.remove(key) }
    }
}
