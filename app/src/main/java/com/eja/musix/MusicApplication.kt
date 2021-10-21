package com.eja.musix

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

// inisialisasi inject root untuk dagger hilt
@HiltAndroidApp
class MusicApplication : Application()