package watch.facesapp.core.util

object NumbersToWords {

    private val tensNames = arrayOf(
        " oh",
        " ten",
        " twenty",
        " thirty",
        " forty",
        " fifty",
        " sixty",
        " seventy",
        " eighty",
        " ninety"
    )

    private val numNames = arrayOf(
        " ",
        " one",
        " two",
        " three",
        " four",
        " five",
        " six",
        " seven",
        " eight",
        " nine",
        " ten",
        " eleven",
        " twelve",
        " thirteen",
        " fourteen",
        " fifteen",
        " sixteen",
        " seven-\nteen",
        " eighteen",
        " nineteen"
    )

    fun convert(number: Int): String {
        if (number == 0) return "zero"
        var num = number
        val soFar: String

        if (num % 100 < 20) {
            soFar = numNames[num % 100]
            num /= 100
        } else {
            soFar = numNames[num % 10]
            num /= 10

            val tens = tensNames[num % 10]
            num /= 10
            return (tens + soFar).trim().replace(Regex("\\s+"), " ")
        }
        if (num == 0) return soFar.trim().replace(Regex("\\s+"), " ")
        return (numNames[num] + " hundred" + soFar).trim().replace(Regex("\\s+"), " ")
    }

    /**
     * Converts hours and minutes into word pairs for Once Upon A Time watch face.
     * Returns a pair of: [hourText, minuteLines].
     */
    fun getHoursAndMinutesWords(hours: Int, minutes: Int): Pair<String, List<String>> {
        val h = if (hours == 0) 12 else hours
        var hourWord = convert(h).substringBefore('\n')

        val minLines = when {
            h == 9 && minutes == 50 -> {
                hourWord = "cowboy"
                listOf("time")
            }
            minutes == 0 -> listOf("on the", "dot")
            minutes < 10 -> {
                val mWord = convert(minutes).replace(" ", "\n")
                listOf("oh") + mWord.split("\n")
            }
            else -> {
                val mWord = convert(minutes).replace(" ", "\n")
                mWord.split("\n")
            }
        }

        return Pair(hourWord, minLines)
    }

    fun getMinutesCompact(minutes: Int): String {
        return when {
            minutes == 0 -> "on the dot"
            minutes < 10 -> "oh " + convert(minutes)
            else -> convert(minutes)
        }
    }
}
