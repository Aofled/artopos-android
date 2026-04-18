package ru.createsmart.artopos.core.network.interceptor

import io.mockk.every
import io.mockk.mockk
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.time.Duration.Companion.days

class CacheControlInterceptorTest {

    @Test
    fun `interceptor forces cache headers and removes Pragma`() {
        val interceptor = CacheControlInterceptor()

        val request = Request.Builder()
            .url("https://nrs.harvard.edu/image.jpg")
            .build()

        // Simulating a bad server response (disabling caching)
        val serverResponse = Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .header("Pragma", "no-cache")
            .header("Cache-Control", "no-store")
            .build()

        val chain = mockk<Interceptor.Chain>()

        every { chain.request() } returns request
        every { chain.proceed(request) } returns serverResponse

        // Let's run the test. The interceptor should intercept the server's response and rewrite the headers.
        val modifiedResponse = interceptor.intercept(chain)

        // THEN
        assertNull(modifiedResponse.header("Pragma"))

        val expectedMaxAge = 7.days.inWholeSeconds
        assertEquals("public, max-age=$expectedMaxAge", modifiedResponse.header("Cache-Control"))
    }
}
