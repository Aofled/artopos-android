package ru.createsmart.artopos.core.network.interceptor

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.junit.Assert.assertEquals
import org.junit.Test

class HarvardApiKeyInterceptorTest {

    @Test
    fun `interceptor appends apikey query parameter to url`() {
        val interceptor = HarvardApiKeyInterceptor("test_super_secret_key")

        val originalRequest = Request.Builder()
            .url("https://api.harvardartmuseums.org/object?size=50")
            .build()

        val mockResponse = Response.Builder()
            .request(originalRequest)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .build()

        val chain = mockk<Interceptor.Chain>()
        val requestSlot = slot<Request>()

        every { chain.request() } returns originalRequest
        every { chain.proceed(capture(requestSlot)) } returns mockResponse

        interceptor.intercept(chain)

        val modifiedUrl = requestSlot.captured.url

        // THEN
        assertEquals("test_super_secret_key", modifiedUrl.queryParameter("apikey"))
        assertEquals("50", modifiedUrl.queryParameter("size"))
        assertEquals(
            "https://api.harvardartmuseums.org/object?size=50&apikey=test_super_secret_key",
            modifiedUrl.toString(),
        )
    }
}
