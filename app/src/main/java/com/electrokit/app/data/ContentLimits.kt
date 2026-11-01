package com.electrokit.app.data

/** Storage limits for user-authored text; quantities and formula results are unchanged. */
object ContentLimits {
    /** Maximum UTF-16 characters accepted by one raw calculator field. */
    const val INPUT_CHARACTERS = 4096
    /** Maximum encoded bytes accepted by one explicit saved calculation. */
    const val SNAPSHOT_BYTES = 32768
    /** Maximum encoded bytes retained across explicit calculation history. */
    const val HISTORY_BYTES = 262144

    /**
     * Adds a complete snapshot, evicting oldest entries rather than truncating technical results.
     *
     * @param text New snapshot; oversized snapshots are rejected.
     * @param existing Most-recent-first saved history.
     * @return At most fifty complete records within the UTF-8 byte budget.
     */
    fun prepend(text: String, existing: List<String>): List<String> {
        val size = text.toByteArray(Charsets.UTF_8).size
        require(size <= SNAPSHOT_BYTES) { "Saved calculation is too large." }
        var remaining = HISTORY_BYTES
        return (listOf(text) + existing).take(50).takeWhile {
            remaining -= it.toByteArray(Charsets.UTF_8).size
            remaining >= 0
        }
    }
}
