package com.electrokit.app.domain

/**
 * Keeps numeric filtering separate from text search and component prose. Construction holds
 * metadata/helpers; physical validation is performed by the named operations, not by field
 * assignment.
 */
object NumericFilter {
    /**
     * Separates invalid numeric bounds from a legitimate empty result set.
     *
     * @param minimum Optional lower numeric bound.
     * @param maximum Optional upper numeric bound.
     * @return Null for valid bounds, otherwise actionable validation prose. Invalid input follows
     *   the nullable/typed-validation contract above; this operation performs no I/O.
     */
    fun validation(minimum: String, maximum: String): String? {
        val min = Engineering.parse(minimum)
        val max = Engineering.parse(maximum)
        return when {
            minimum.isNotBlank() && min == null -> Messages.ENTER_A_VALID_MINIMUM_VALUE
            maximum.isNotBlank() && max == null -> Messages.ENTER_A_VALID_MAXIMUM_VALUE
            min != null && max != null && min > max -> Messages.MINIMUM_MUST_NOT_EXCEED_MAXIMUM
            else -> null
        }
    }

    /**
     * Applies explicit numeric bounds only to parseable metadata; invalid filters never match
     * accidentally.
     *
     * @param value Input scalar in the operation’s documented units.
     * @param minimum Optional lower numeric bound.
     * @param maximum Optional upper numeric bound.
     * @return True only when the scalar meets both valid optional bounds. Invalid input follows the
     *   nullable/typed-validation contract above; this operation performs no I/O.
     */
    fun matches(value: String?, minimum: String, maximum: String): Boolean {
        if (validation(minimum, maximum) != null) return false
        val number = Engineering.parse(value.orEmpty()) ?: return false
        return (minimum.isBlank() || number >= Engineering.parse(minimum)!!) &&
            (maximum.isBlank() || number <= Engineering.parse(maximum)!!)
    }
}
