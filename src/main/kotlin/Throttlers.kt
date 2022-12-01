import okio.Throttler

// https://www.blogdumoderateur.com/rapport-arcep-internet-france-2022/
val networkConditioners = mapOf(
    "Low" to createThrottler(25),
    "Medium" to createThrottler(75),
    "High" to createThrottler(100),
)


fun createThrottler(bandwidthInMb: Long) =
    Throttler().apply { bytesPerSecond(bandwidthInMb * 1024 * 1024) }