package com.example.janaushadhifinder

object FuzzySearch {

    fun matches(text: String, query: String): Boolean {
        return score(text, query) <= threshold(query)
    }

    fun matchesAny(query: String, vararg fields: String): Boolean {
        if (query.isBlank()) return true
        return fields.any { matches(it, query) }
    }

    fun medicineScore(query: String, vararg fields: String): Int {
        if (query.isBlank()) return 0
        return fields.minOfOrNull { score(it, query) } ?: Int.MAX_VALUE
    }

    fun score(text: String, query: String): Int {
        val t = normalize(text)
        val q = normalize(query)

        if (q.isBlank()) return 0

        if (t.contains(q)) return 0
        if (q.contains(t)) return 1

        val compactText = t.replace(" ", "")
        val compactQuery = q.replace(" ", "")
        if (compactText.contains(compactQuery)) return 0

        val words = t.split(" ").filter { it.isNotBlank() }
        val wordScore = words.minOfOrNull { word ->
            bestWindowDistance(word, q)
        } ?: Int.MAX_VALUE
        val fullScore = bestWindowDistance(t, q)
        return minOf(wordScore, fullScore)
    }

    fun suggestions(
        query: String,
        candidates: List<String>,
        limit: Int = 5
    ): List<String> {
        if (query.isBlank()) return emptyList()
        return candidates
            .distinct()
            .map { it to score(it, query) }
            .filter { it.second <= threshold(query) + 1 }
            .sortedWith(compareBy<Pair<String, Int>> { it.second }.thenBy { it.first })
            .take(limit)
            .map { it.first }
    }

    fun closest(
        query: String,
        candidates: List<String>
    ): String? {
        if (query.isBlank()) return null
        return candidates
            .distinct()
            .map { it to score(it, query) }
            .filter { it.second <= threshold(query) + 2 }
            .minWithOrNull(compareBy<Pair<String, Int>> { it.second }.thenBy { it.first })
            ?.first
    }

    private fun bestWindowDistance(text: String, query: String): Int {
        if (text.isBlank()) return Int.MAX_VALUE
        if (text.length <= query.length) return levenshtein(text, query)

        var best = levenshtein(text, query)
        val minWindow = (query.length - 1).coerceAtLeast(1)
        val maxWindow = (query.length + 2).coerceAtMost(text.length)
        for (window in minWindow..maxWindow) {
            for (start in 0..(text.length - window)) {
                val chunk = text.substring(start, start + window)
                best = minOf(best, levenshtein(chunk, query))
            }
        }
        return best
    }

    private fun threshold(query: String): Int {
        val length = normalize(query).length
        return when {
            length <= 2 -> 0
            length <= 4 -> 1
            length <= 7 -> 2
            length <= 10 -> 3
            else -> 3
        }
    }

    private fun normalize(value: String): String {
        return value.lowercase()
            .replace(Regex("[^a-z0-9]+"), " ")
            .trim()
    }

    fun levenshtein(a: String, b: String): Int {
        val dp = Array(a.length + 1) { IntArray(b.length + 1) }
        for (i in 0..a.length) dp[i][0] = i
        for (j in 0..b.length) dp[0][j] = j
        for (i in 1..a.length) {
            for (j in 1..b.length) {
                dp[i][j] = if (a[i-1] == b[j-1]) dp[i-1][j-1]
                else 1 + minOf(dp[i-1][j-1], dp[i-1][j], dp[i][j-1])
            }
        }
        return dp[a.length][b.length]
    }
}
