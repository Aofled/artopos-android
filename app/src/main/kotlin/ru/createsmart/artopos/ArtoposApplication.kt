package ru.createsmart.artopos

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import dagger.hilt.android.HiltAndroidApp
import okhttp3.OkHttpClient
import ru.createsmart.artopos.core.imageloader.di.ImageClient
import ru.createsmart.artopos.core.network.BuildConfig
import ru.createsmart.artopos.extensions.createCoilImageLoader
import javax.inject.Inject

@HiltAndroidApp
class ArtoposApplication : Application(), ImageLoaderFactory {

    @Inject
    @ImageClient
    lateinit var imageOkHttpClient: OkHttpClient

    // Coil Configuration: Set Global ImageLoader with our custom Client
    override fun newImageLoader(): ImageLoader {
        return createCoilImageLoader(imageOkHttpClient, BuildConfig.DEBUG)
    }
}
