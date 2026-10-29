package com.tinhtx.baseads.core

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InterstitialFrequencyGateTest {

    private fun gate(threshold: Int = 8, dedupe: Long = 800, cap: Int = 4) =
        InterstitialFrequencyGate(threshold, dedupe, cap)

    @Test
    fun thresholdOfOneKeepsLegacyBehaviour() {
        val g = gate(threshold = 1)
        assertTrue(g.registerAction(0))
        assertTrue(g.registerAction(1))
    }

    @Test
    fun showsOnlyOnTheNthAction() {
        val g = gate(threshold = 3)
        assertFalse(g.registerAction(0))
        assertFalse(g.registerAction(1_000))
        assertTrue(g.registerAction(2_000))
    }

    @Test
    fun staysEligibleUntilAnAdIsShown() {
        val g = gate(threshold = 2)
        assertFalse(g.registerAction(0))
        assertTrue(g.registerAction(1_000))
        // blocked elsewhere (cooldown): the next action is still eligible
        assertTrue(g.registerAction(2_000))
    }

    @Test
    fun countResetsAfterAdShown() {
        val g = gate(threshold = 2)
        g.registerAction(0)
        assertTrue(g.registerAction(1_000))
        g.onAdShown()
        assertFalse(g.registerAction(2_000))
        assertTrue(g.registerAction(3_000))
    }

    @Test
    fun duplicateTriggerWithinWindowIsIgnored() {
        val g = gate(threshold = 2, dedupe = 800)
        assertFalse(g.registerAction(0))
        // same gesture firing a second ad attempt 50 ms later must not count
        assertFalse(g.registerAction(50))
        assertTrue(g.registerAction(2_000))
    }

    @Test
    fun duplicateDoesNotShowEvenWhenFirstWasEligible() {
        val g = gate(threshold = 1 + 1, dedupe = 800)
        g.registerAction(0)
        assertTrue(g.registerAction(1_000))
        assertFalse(g.registerAction(1_100))
    }

    @Test
    fun sessionCapIsReachedAfterMaxAds() {
        val g = gate(cap = 2)
        assertFalse(g.isSessionCapReached())
        g.onAdShown()
        assertFalse(g.isSessionCapReached())
        g.onAdShown()
        assertTrue(g.isSessionCapReached())
    }

    @Test
    fun splashCanBeClaimedOnlyOnce() {
        val g = gate()
        assertTrue(g.tryClaimSplash())
        assertFalse(g.tryClaimSplash())
    }
}
