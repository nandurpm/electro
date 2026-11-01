package com.electrokit.app.data

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Family-level reference facts, explicitly separate from device-specific ratings.
 *
 * @property category Library filter category.
 * @property aliases Related terminology and marking-search hints, not identification guarantees.
 * @property principle Operating explanation.
 * @property specifications Parameters to check in the exact datasheet, not assigned ratings.
 * @property testing Ordered safe test/limitation guidance.
 * @property identification Marking conventions and limitations.
 * @property application Educational application, assumptions and formula.
 * @property source Original reference URL.
 */
@Serializable
data class ComponentGuide(
    val category: String,
    val aliases: List<String>,
    val principle: String,
    val specifications: List<String>,
    val testing: List<String>,
    val identification: String,
    val application: String,
    val source: String,
)

/**
 * Reads the explicit family key; never infers a type or terminal order from a filename.
 *
 * @param asset Existing stable component record.
 * @return Family key or null when only the original reference is available.
 */
fun componentFamily(asset: ComponentAsset): String? = asset.family.takeIf(String::isNotBlank)

/**
 * Reads the bundled guide dataset once; UI callers perform this on IO.
 *
 * @param context Application assets context.
 * @return Validated immutable family records, with failures propagated rather than replaced by fake
 *   facts.
 */
fun loadComponentGuides(context: Context): Map<String, ComponentGuide> =
    context.assets.open("component_guides.json").bufferedReader().use {
        Json.decodeFromString(it.readText())
    }
