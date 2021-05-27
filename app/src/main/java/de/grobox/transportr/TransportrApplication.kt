/*
 *    Transportr
 *
 *    Copyright (c) 2013 - 2021 Torsten Grote
 *
 *    This program is Free Software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as
 *    published by the Free Software Foundation, either version 3 of the
 *    License, or (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.grobox.transportr

import android.app.Application
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.services.android.telemetry.MapboxTelemetry

open class TransportrApplication : Application() {
    lateinit var component: AppComponent
        private set

    override fun onCreate() {
        super.onCreate()

        Mapbox.getInstance(
            applicationContext,
            "pk.eyJ1IjoidG92b2s3IiwiYSI6ImNpeTA1OG82YjAwN3YycXA5cWJ6NThmcWIifQ.QpURhF9y7XBMLmWhELsOnw"
        )
        MapboxTelemetry.getInstance().isTelemetryEnabled = false

        component = createComponent()
    }

    protected open fun createComponent(): AppComponent {
        return DaggerAppComponent.builder()
            .appModule(AppModule(this))
            .build()
    }
}