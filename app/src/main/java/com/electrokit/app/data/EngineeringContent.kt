package com.electrokit.app.data

import kotlinx.serialization.Serializable

/**
 * Structured offline engineering corpus. Construction only stores data; validation occurs on load.
 */
@Serializable
data class EngineeringContent(
    /** Approximate educational cutaway markers, never job mounting coordinates. */
    val markers: List<EngineeringMarker> = emptyList(),
    /** Navigable engineering sections, independent of the legacy registry. */
    val sections: List<EngineeringSection>,
    /** Searchable photographed and educational technical records. */
    val records: List<EngineeringRecord>,
    /** Exact supplied fault rows with separately labelled explanatory metadata. */
    val faults: List<FaultCode>,
    /** Provenance and uncertainty records, including all fourteen photograph transcriptions. */
    val sources: List<ReferenceSource>,
)

/** One engineering module; no equipment-specific electrical claims are implied by its title. */
@Serializable
data class EngineeringSection(
    /** Stable navigation key. */
    val id: String,
    /** User-facing section title. */
    val title: String,
    /** Original vector illustration family. */
    val icon: String,
)

/** Source-traceable technical entry; relations are topic navigation, never implied wiring. */
@Serializable
data class EngineeringRecord(
    /** Stable deep-link and bookmark key. */
    val id: String,
    /** Identifier and full descriptive title. */
    val title: String,
    /** Owning engineering module. */
    val section: String,
    /** Short explanation for scanning/search. */
    val summary: String,
    /** Full original educational text or recorded specification lines. */
    val body: List<String>,
    /** Source identifiers resolvable in the same asset. */
    val sources: List<String>,
    /** Explicit verification status. */
    val status: String,
    /** Bidirectional subject links, not approved electrical connections. */
    val related: List<String>,
    /** Optional original vector-renderer key. */
    val diagram: String,
)

/** Exact source fault fields are distinct from authored explanations and derived topic indexes. */
@Serializable
data class FaultCode(
    /** Original serial number; gaps 112–114 are deliberately preserved. */
    val serial: Int,
    /** Exact string code, including numeric-looking 164–168 and leading zeros. */
    val code: String,
    /** Unmodified supplied error message. */
    val message: String,
    /** Supplied Auto/Manual classification; not permission to restart. */
    val reset: String,
    /** Exact Error/Alarm/Warning/CBM Alarm group. */
    val group: String,
    /** Derived browsing category, not verified equipment location or trigger logic. */
    val system: String,
    /** Plain-language description separate from the original message. */
    val description: String,
    /** Qualified possibilities, not confirmed root causes. */
    val possibleCauses: String,
    /** Safe high-level inspection guidance without bypass/energisation procedures. */
    val checks: String,
    /** Topic links to component/sensor references; not certified wiring. */
    val related: List<String>,
    /** Dataset source keys. */
    val sources: List<String>,
    /** Authority and association limits shown in the fault detail. */
    val status: String,
)

/**
 * Provenance information remains available offline; external URLs are copied/shared, never fetched.
 */
@Serializable
data class ReferenceSource(
    /** Filename or stable publication key. */
    val id: String,
    /** Source description. */
    val title: String,
    /** Photograph, supplied text, manufacturer reference or educational explanation. */
    val type: String,
    /** Complete readable technical transcription or limited publication-scope note. */
    val text: String,
    /** Explicit uncertainty/approval scope. */
    val status: String,
    /** SHA-256 of original source bytes where available. */
    val sha256: String,
    /** Optional public reference URL, stored as text. */
    val url: String,
)

/**
 * New calculator definition; examples require an explicit user action rather than silent defaults.
 */
@Serializable
data class EngineeringTool(
    /** Stable calculator and persistence key. */
    val id: String,
    /** User-facing tool title. */
    val title: String,
    /** Mathematical model displayed with the inputs. */
    val formula: String,
    /** Explicit fields and declared units. */
    val fields: List<EngineeringField>,
    /** Model limitations and required physical assumptions. */
    val assumptions: String,
)

/** A unit-labelled scalar or explicitly selectable technical conversion pair. */
@Serializable
data class EngineeringField(
    /** Stable saved-input key. */
    val key: String,
    /** Visible field name. */
    val label: String,
    /** Expected physical unit, empty for a ratio or selection. */
    val unit: String,
    /** Demonstration value used only by Load example. */
    val sample: String,
    /** Nonempty options turn this field into a dropdown. */
    val options: List<String>,
)

/** Source-backed educational drawing marker; coordinates are not installation measurements. */
@Serializable
data class EngineeringMarker(
    /** Destination record ID. */
    val record: String,
    /** Horizontal coordinate in the original 1000×560 illustration. */
    val x: Float,
    /** Vertical coordinate in the original 1000×560 illustration. */
    val y: Float,
    /** Photograph supporting the approximate functional region. */
    val source: String,
)
