package com.pengurur.jarakradius

import android.app.Application
import com.pengurur.jarakradius.data.local.RadiusDatabase
import com.pengurur.jarakradius.data.repository.MarkerRepositoryImpl
import com.pengurur.jarakradius.domain.repository.MarkerRepository

class RadiusApp : Application() {

    val markerRepository: MarkerRepository by lazy {
        MarkerRepositoryImpl(RadiusDatabase.getInstance(this).markerDao())
    }
}
