import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.Throttler
import kotlin.time.Duration.Companion.milliseconds

fun main(args: Array<String>) {
    val parser = ArgParser("kaller")
    val nbOfCalls by parser.option(
        type = ArgType.Int,
        fullName = "nbCalls",
        shortName = "n",
        description = "number of times request will be made"
    )
        .default(10)

    val url by parser.argument(
        type = ArgType.String,
        fullName = "resource url to request"
    )

    val serializedHeaders by parser.option(
        type = ArgType.String,
        fullName = "header",
        shortName = "H",
        description = "header for the request expected format key1:value1 "
    ).default("")

    parser.parse(args)

    networkConditioners.forEach { (profile, conditioner) ->
        printLineSep()
        println("$nbOfCalls on $profile conditions  ")
        batchRequest(conditioner, nbOfCalls, url, serializedHeaders.parseHeaders())
    }
}

private fun String.parseHeaders(): Map<String, String> =

        associate {
            val pair = split(":")
            pair[0] to pair[1]
        }

private fun batchRequest(
    throttler: Throttler,
    nbOfCalls: Int,
    url: String,
    headers: Map<String, String>
) {
    val client: OkHttpClient = createClient(throttler)
    val allMetadata = (0 until nbOfCalls).map { client.makeRequest(url, headers).toMetadata() }
        .filter { it.httpCode == 200 }

    val size = allMetadata.map { it.size }.average().formatToKb()
    val duration = allMetadata.map { it.duration }.average().milliseconds

    println("Average size : $size time : $duration")

}

private fun createClient(throttler: Throttler) = OkHttpClient().newBuilder()
    .addInterceptor { chain ->
        val request = chain.request()
        val originalRequestBody = request.body
        val newRequest = if (originalRequestBody != null) {
            val wrappedRequestBody = ThrottledRequestBody(originalRequestBody, throttler)
            request.newBuilder()
                .method(request.method, wrappedRequestBody)
                .build()
        } else {
            request
        }
        chain.proceed(newRequest)
    }
    .build()


private fun OkHttpClient.makeRequest(url: String, headers: Map<String, String>): Response =
    newCall(buildRequest(url, headers).build()).execute()

private fun buildRequest(
    url: String,
    headers: Map<String, String>
): Request.Builder {
    val requestBuilder = Request.Builder()
        .cacheControl(CacheControl.FORCE_NETWORK)
        .url(url)
        .addHeader("Accept-Encoding", "gzip")

    headers.forEach { (key, value) -> requestBuilder.addHeader(key, value) }
    return requestBuilder
}

private fun Response.toMetadata() = ResponseMetadata(
    duration = (receivedResponseAtMillis - sentRequestAtMillis),
    size = body?.contentLength() ?: -1,
    httpCode = code
)

private fun printLineSep() = println("-".repeat(80))

private fun Double.formatToKb(): String = "${(this / 1024)} kb"

private data class ResponseMetadata(
    val duration: Long,
    val size: Long,
    val httpCode: Int
)
