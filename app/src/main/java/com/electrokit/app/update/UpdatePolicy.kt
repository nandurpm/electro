package com.electrokit.app.update

/** Release version consists of a major, feature, and patch number. */
data class ElectroVersion(val major: Int, val feature: Int, val patch: Int) : Comparable<ElectroVersion> {
    override fun compareTo(other: ElectroVersion): Int =
        compareValuesBy(this, other, ElectroVersion::major, ElectroVersion::feature, ElectroVersion::patch)

    companion object {
        fun parse(value: String): ElectroVersion? {
            val match = Regex("^v?(\\d{1,5})\\.(\\d{1,5})\\.(\\d{1,5})$").matchEntire(value.trim()) ?: return null
            val parts = match.groupValues.drop(1).map { it.toIntOrNull() ?: return null }
            return ElectroVersion(parts[0], parts[1], parts[2])
        }
    }
}

enum class UpdatePriority { NONE, OPTIONAL_PATCH, REQUIRED_FEATURE }

/** e.g. 4.3.1 is optional over 4.3.0, but 4.4.0 is a required feature release. */
object UpdatePolicy {
    fun priority(installed: String, published: String): UpdatePriority {
        val current = ElectroVersion.parse(installed) ?: return UpdatePriority.NONE
        val latest = ElectroVersion.parse(published) ?: return UpdatePriority.NONE
        if (latest <= current) return UpdatePriority.NONE
        return if (latest.major > current.major || latest.feature > current.feature)
            UpdatePriority.REQUIRED_FEATURE else UpdatePriority.OPTIONAL_PATCH
    }
}
