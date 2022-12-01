import okhttp3.MediaType
import okhttp3.RequestBody
import okio.BufferedSink
import okio.Throttler
import okio.buffer

class ThrottledRequestBody(
    private val delegate: RequestBody,
    private val throttler: Throttler,
) : RequestBody() {

    override fun contentType(): MediaType? = delegate.contentType()

    override fun writeTo(sink: BufferedSink) {
        throttler.sink(sink).buffer().use {
            delegate.writeTo(it)
        }
    }
}