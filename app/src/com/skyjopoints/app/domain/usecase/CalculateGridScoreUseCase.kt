package com.skyjopoints.app.domain.usecase

class CalculateGridScoreUseCase {
    /**
     * Calculates the score for a 3x4 grid represented as a list of lists of nullable integers.
     * If all three cards in a column are identical, they are discarded (0 points).
     * Unentered card slots (null) are treated as 0 points.
     */
    operator fun invoke(grid: List<List<Int?>>): Int {
        var totalScore = 0
        
        for (col in 0..3) {
            val v0 = if (grid.size > 0 && col < grid[0].size) grid[0][col] else null
            val v1 = if (grid.size > 1 && col < grid[1].size) grid[1][col] else null
            val v2 = if (grid.size > 2 && col < grid[2].size) grid[2][col] else null
            
            // A column is discarded only if all three values are identical and not null.
            if (v0 != null && v1 != null && v2 != null && v0 == v1 && v1 == v2) {
                continue
            }
            
            totalScore += (v0 ?: 0) + (v1 ?: 0) + (v2 ?: 0)
        }
        
        return totalScore
    }
}
