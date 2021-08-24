package com.listocalixto.dailycosmos

import android.app.Application
import dagger.hilt.DefineComponent
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.internal.managers.ApplicationComponentManager

@HiltAndroidApp
class BaseApplication: Application()