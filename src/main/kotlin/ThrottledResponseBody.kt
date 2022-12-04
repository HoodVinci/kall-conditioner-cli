import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import okio.*
import java.io.InputStream

class ThrottledResponseBody(
    private val delegate: ResponseBody,
    private val throttler: Throttler,
) : ResponseBody() {
    override fun contentLength(): Long = delegate.contentLength()

    override fun contentType(): MediaType? = delegate.contentType()

    override fun source(): BufferedSource = throttler.source(delegate.source()).buffer()
}


