package com.electrokit.app.domain

import com.electrokit.app.data.FaultCode

/** Pure filter logic is shared with tests; browsing never rewrites source fault fields. */
object EngineeringSearch {
    /**
     * Combines all selected local fault filters without changing dataset order.
     *
     * @param all Complete source list.
     * @param query Code/message query, trimmed and matched case-insensitively.
     * @param group Exact group or blank for All.
     * @param reset Exact supplied reset type or blank for All.
     * @param system Derived browsing index or blank for All; not a certified location.
     * @param bookmarks Null disables this filter; otherwise only these exact code strings match.
     * @return Stable-order matching rows; empty lists are valid and no errors are thrown.
     */
    fun faults(
        all: List<FaultCode>,
        query: String,
        group: String = "",
        reset: String = "",
        system: String = "",
        bookmarks: Set<String>? = null,
    ): List<FaultCode> =
        all.filter {
            (query.isBlank() ||
                it.code.contains(query.trim(), true) ||
                it.message.contains(query.trim(), true)) &&
                (group.isBlank() || it.group == group) &&
                (reset.isBlank() || it.reset == reset) &&
                (system.isBlank() || it.system == system) &&
                (bookmarks == null || it.code in bookmarks)
        }
}
