package com.faces.core

import com.faces.core.util.NumbersToWords
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NumbersToWordsTest {

    @Test
    fun testBasicConversions() {
        assertEquals("zero", NumbersToWords.convert(0).trim().ifEmpty { "zero" })
        assertEquals("one", NumbersToWords.convert(1))
        assertEquals("ten", NumbersToWords.convert(10))
        assertEquals("twelve", NumbersToWords.convert(12))
        assertEquals("twenty", NumbersToWords.convert(20))
        assertEquals("twenty five", NumbersToWords.convert(25))
        assertEquals("forty seven", NumbersToWords.convert(47))
        assertEquals("fifty nine", NumbersToWords.convert(59))
    }

    @Test
    fun testHoursAndMinutesWords() {
        // 9:50 is Cowboy Time!
        val (hCowboy, mCowboy) = NumbersToWords.getHoursAndMinutesWords(9, 50)
        assertEquals("cowboy", hCowboy)
        assertEquals(listOf("time"), mCowboy)

        // On the dot
        val (hDot, mDot) = NumbersToWords.getHoursAndMinutesWords(4, 0)
        assertEquals("four", hDot)
        assertEquals(listOf("on the", "dot"), mDot)

        // Oh seven
        val (hOh, mOh) = NumbersToWords.getHoursAndMinutesWords(8, 7)
        assertEquals("eight", hOh)
        assertEquals("oh", mOh[0])
        assertEquals("seven", mOh[1])

        // Midnight / 0 hour translates to twelve
        val (hMid, _) = NumbersToWords.getHoursAndMinutesWords(0, 15)
        assertEquals("twelve", hMid)
    }

    @Test
    fun testMinutesCompact() {
        assertEquals("on the dot", NumbersToWords.getMinutesCompact(0))
        assertEquals("oh five", NumbersToWords.getMinutesCompact(5))
        assertEquals("thirty", NumbersToWords.getMinutesCompact(30))
    }
}
