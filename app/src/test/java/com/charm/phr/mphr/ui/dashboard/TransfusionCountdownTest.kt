package com.charm.phr.mphr.ui.dashboard

import com.charm.phr.mphr.ui.dashboard.compose.TransfusionCountdown
import com.charm.phr.mphr.ui.dashboard.compose.transfusionCountdown
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Use Arrange, Act, Assert comments while writing tests
 **/
class TransfusionCountdownTest {

    @Test
    fun `transfusionCountdown returns DaysToGo for days greater than 1`() {
        // Arrange
        val daysRemaining = 23

        // Act
        val result = transfusionCountdown(daysRemaining)

        // Assert
        assertThat(result).isEqualTo(TransfusionCountdown.DaysToGo(23))
    }

    @Test
    fun `transfusionCountdown returns Tomorrow for exactly 1 day`() {
        // Arrange
        val daysRemaining = 1

        // Act
        val result = transfusionCountdown(daysRemaining)

        // Assert
        assertThat(result).isEqualTo(TransfusionCountdown.Tomorrow)
    }

    @Test
    fun `transfusionCountdown returns Today for 0 days`() {
        // Arrange
        val daysRemaining = 0

        // Act
        val result = transfusionCountdown(daysRemaining)

        // Assert
        assertThat(result).isEqualTo(TransfusionCountdown.Today)
    }

    @Test
    fun `transfusionCountdown returns DaysAgo for 1 day overdue`() {
        // Arrange
        val daysRemaining = -1

        // Act
        val result = transfusionCountdown(daysRemaining)

        // Assert
        assertThat(result).isEqualTo(TransfusionCountdown.DaysAgo(1))
    }

    @Test
    fun `transfusionCountdown returns DaysAgo with correct count for multiple days overdue`() {
        // Arrange
        val daysRemaining = -5

        // Act
        val result = transfusionCountdown(daysRemaining)

        // Assert
        assertThat(result).isEqualTo(TransfusionCountdown.DaysAgo(5))
    }
}
