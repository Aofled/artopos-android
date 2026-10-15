package ru.createsmart.artopos

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import dagger.hilt.android.HiltAndroidApp
import okhttp3.OkHttpClient
import ru.createsmart.artopos.core.imageloader.di.ImageClient
import ru.createsmart.artopos.extensions.createCoilImageLoader
import javax.inject.Inject

@HiltAndroidApp
class ArtoposApplication : Application(), SingletonImageLoader.Factory {

    @Inject
    @ImageClient
    lateinit var imageOkHttpClient: OkHttpClient

    // Coil 3 Configuration: Set Global ImageLoader with our custom Client
    override fun newImageLoader(context: PlatformContext): ImageLoader {
        val isDebug = (applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
        return context.createCoilImageLoader(imageOkHttpClient, isDebug)
    }
}
