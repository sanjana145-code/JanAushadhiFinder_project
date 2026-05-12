package com.example.janaushadhifinder

object FuzzySearch {

    fun matches(text: String, query: String): Boolean {
        val t = text.lowercase()
        val q = query.lowercase()

        // Direct contains check
        if (t.contains(q)) return true

        // Levenshtein distance for typo tolerance
        if (q.length >= 4) {
            for (i in 0..maxOf(0, t.length - q.length + 1)) {
                val end = minOf(i + q.length + 1, t.length)
                val chunk = t.substring(i, end)
                if (levenshtein(chunk, q) <= 1) return true
            }
        }
        return false
    }

    private fun levenshtein(a: String, b: String): Int {
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