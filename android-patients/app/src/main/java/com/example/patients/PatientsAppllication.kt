package com.example.patients

import android.app.Application
import com.example.patients.di.AppContainer

class PatientsApplication : Application() {
    // Created once, shared by the whole app
    val container by lazy { AppContainer() }
}