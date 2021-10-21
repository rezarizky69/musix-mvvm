package com.eja.musix.di

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.eja.musix.R
import com.eja.musix.adapters.SwipeSongAdapter
import com.eja.musix.exoplayer.MusicServiceConnection
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

// class module untuk dependency injection
// yg dijalankan dalam lingkup keseluruhan aplikasi
@Module
@InstallIn(ApplicationComponent::class)
object AppModule {

    // function untuk membuat inject dari swipe song adapter
    @Singleton
    @Provides
    fun provideSwipeSongAdapter() = SwipeSongAdapter()

    // function untuk membuat inject dari musicServiceConnection
    @Singleton
    @Provides
    fun provideMusicServiceConnection(
        @ApplicationContext context: Context
    ) = MusicServiceConnection(context)

    // function untuk membuat inject dari glide
    @Singleton
    @Provides
    fun provideGlideInstance(
        @ApplicationContext context: Context
    ) = Glide.with(context).setDefaultRequestOptions(
        RequestOptions()
            .placeholder(R.drawable.ic_image)
            .error(R.drawable.ic_image)
            .diskCacheStrategy(DiskCacheStrategy.DATA)
    )
}