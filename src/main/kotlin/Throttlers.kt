import okio.Throttler

// https://www.blogdumoderateur.com/rapport-arcep-internet-france-2022/
val networkConditioners = mapOf(
    "Low" to createThrottler(25),
    "Medium" to createThrottler(74),
    "High" to createThrottler(95),
    )


fun createThrottler(sizeInMb: Long) =
    Throttler().apply { bytesPerSecond(sizeInMb * 1024 * 1024) }