package watch.facesapp.core

import watch.facesapp.core.util.NumbersToWords
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
        assertEquals("forty three", NumbersToWords.getMinutesCompact(43))
    }

    @Test
    fun testSunriseSunsetWordConversion() {
        // 07:43 -> hour 7 ("seven"), minutes 43 ("forty three")
        val rawH = 7
        val rawM = 43
        val h12 = if (rawH % 12 == 0) 12 else rawH % 12
        val hourWord = NumbersToWords.convert(h12).substringBefore('\n')
        val minWords = NumbersToWords.getMinutesCompact(rawM).split("\n")
        assertEquals("seven", hourWord)
        assertEquals(listOf("forty three"), minWords)

        // 18:00 (sunset) -> hour 6 ("six"), minutes 0 ("on the dot")
        val rawH2 = 18
        val rawM2 = 0
        val h12_2 = if (rawH2 % 12 == 0) 12 else rawH2 % 12
        val hourWord2 = NumbersToWords.convert(h12_2).substringBefore('\n')
        val minWords2 = NumbersToWords.getMinutesCompact(rawM2).split("\n")
        assertEquals("six", hourWord2)
        assertEquals(listOf("on the dot"), minWords2)
    }
}
