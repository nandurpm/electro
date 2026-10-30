package com.electrokit.app.update

/** Strict version. @property major Breaking version. @property feature Feature milestone. @property patch Maintenance patch. */
data class ElectroVersion(val major: Int, val feature: Int, val patch: Int) : Comparable<ElectroVersion> {
    /** @param other Version to compare. @return Numeric ordering. */
    override fun compareTo(other: ElectroVersion): Int =
        compareValuesBy(this, other, ElectroVersion::major, ElectroVersion::feature, ElectroVersion::patch)

    companion object {
        /** @param value Published version tag. @return Parsed version or null for invalid tags. */
        fun parse(value: String): ElectroVersion? {
            val match = Regex("^v?(\\d{1,5})\\.(\\d{1,5})\\.(\\d{1,5})$").matchEntire(value.trim()) ?: return null
            val parts = match.groupValues.drop(1).map { it.toIntOrNull() ?: return null }
            return ElectroVersion(parts[0], parts[1], parts[2])
        }
    }
}

/** Update policy categories for release availability. */
enum class UpdatePriority { NONE, OPTIONAL_PATCH, REQUIRED_FEATURE }

/** e.g. 4.3.1 is optional over 4.3.0, but 4.4.0 is a required feature release. */
object UpdatePolicy {
    /** @param installed Installed version. @param published Published version. @return Optional or required priority. */
    fun priority(installed: String, published: String): UpdatePriority {
        val current = ElectroVersion.parse(installed) ?: return UpdatePriority.NONE
        val latest = ElectroVersion.parse(published) ?: return UpdatePriority.NONE
        if (latest <= current) return UpdatePriority.NONE
        return if (latest.major > current.major || latest.feature > current.feature)
            UpdatePriority.REQUIRED_FEATURE else UpdatePriority.OPTIONAL_PATCH
    }
}
