package com.electrokit.app.update

/** A rate limit is distinct from loss of connectivity; never embed an API credential. */
class ReleaseRateLimitException : java.io.IOException("GitHub rate limit")

/** Bounded plain-text notes, normalized once at the metadata boundary. */
object ReleaseNotes {
    /**
     * @param markdown Published release body. @param limit Maximum visible Unicode code points
     *   before ellipsis.
     * @return Plain text truncated at a nearby word boundary without splitting surrogate pairs.
     */
    fun plainText(markdown: String, limit: Int = 650): String {
        require(limit > 0)
        val text =
            markdown
                .replace(Regex("!\\[([^]]*)]\\([^)]*\\)"), "$1")
                .replace(Regex("\\[([^]]+)]\\([^)]*\\)"), "$1")
                .replace(Regex("(?m)^\\s{0,3}#{1,6}\\s+"), "")
                .replace(Regex("(?m)^\\s*[-*+]\\s+"), "• ")
                .replace("**", "")
                .replace("__", "")
                .replace("`", "")
                .replace(Regex("[ \\t]+"), " ")
                .trim()
        if (text.isBlank()) return "See the GitHub release notes."
        if (text.codePointCount(0, text.length) <= limit) return text
        val end = text.offsetByCodePoints(0, limit)
        val prefix = text.substring(0, end)
        val boundary = prefix.indexOfLast { it.isWhitespace() }
        return (if (boundary >= prefix.length / 2) prefix.substring(0, boundary) else prefix)
            .trimEnd() + "…"
    }
}
