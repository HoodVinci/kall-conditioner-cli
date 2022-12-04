import okio.Throttler

// https://www.blogdumoderateur.com/rapport-arcep-internet-france-2022/
val networkConditioners = mapOf(
    "Low" to createThrottler(27 * 1024),
    "Medium" to createThrottler(75 * 1024),
    "High" to createThrottler(100 * 1024),
)


fun createThrottler(bandwidthInKb: Long) = Throttler().apply {
    bytesPerSecond(bytesPerSecond = bandwidthInKb * 1024)
}

