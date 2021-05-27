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

package de.grobox.transportr.trips.search

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.MenuItem
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import de.grobox.transportr.R
import de.grobox.transportr.TransportrActivity
import de.grobox.transportr.data.locations.FavoriteLocation.FavLocationType
import de.grobox.transportr.locations.WrapLocation
import de.grobox.transportr.locations.WrapLocation.WrapType.GPS
import de.grobox.transportr.trips.search.SavedSearchesFragment.HomePickerFragment
import de.grobox.transportr.trips.search.SavedSearchesFragment.WorkPickerFragment
import de.grobox.transportr.utils.Constants.*
import kotlinx.android.synthetic.main.activity_directions.*
import javax.inject.Inject

class DirectionsActivity : TransportrActivity(), OnOffsetChangedListener {

    companion object {
        const val ACTION_PRE_FILL = "preFill"
        const val ACTION_SEARCH = "search"

        const val INTENT_URI_HOME = "transportr://home"
        const val INTENT_URI_WORK = "transportr://work"
        const val INTENT_URI_FAVORITE = "transportr://favorite"
    }

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: DirectionsViewModel

    private val isShowingTrips: Boolean
        get() = fragmentIsVisible(TripsFragment.TAG)

    private val timeUpdater: CountDownTimer = object : CountDownTimer(Long.MAX_VALUE, 1000 * 30) {
        override fun onTick(millisUntilFinished: Long) {
            viewModel.timeUpdate.trigger()
        }
        override fun onFinish() {}
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        component.inject(this)
        setContentView(R.layout.activity_directions)

        // get view model and observe data
        viewModel = ViewModelProvider(this, viewModelFactory).get(DirectionsViewModel::class.java)
        viewModel.showTrips.observe(this, Observer { showTrips() })

        appBarLayout.addOnOffsetChangedListener(this)

        if (savedInstanceState == null) {
            showFavorites()
            processIntent(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        timeUpdater.start()
    }

    override fun onStop() {
        super.onStop()
        timeUpdater.cancel()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home && isShowingTrips) {
            showFavorites()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
        if (verticalOffset == 0) {
            viewModel.topSwipeEnabled.value = true
        } else {
            val enabled = viewModel.topSwipeEnabled.value
            if (enabled != null && enabled) {
                viewModel.topSwipeEnabled.value = false
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        processIntent(intent)
    }

    private fun showFavorites() {
        viewModel.isFavTrip.value = null
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragmentContainer, SavedSearchesFragment(), SavedSearchesFragment::class.java.simpleName)
            .commit()
    }

    private fun showTrips() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, TripsFragment(), TripsFragment.TAG)
            .commit()
        fragmentContainer.requestFocus()
    }

    private fun processIntent(intent: Intent?) {
        if (intent == null || intent.action == null) return

        // remove the intent (and clear its action) since it will be processed now
        // and should not be processed again
        setIntent(null)

        if (intent.action == ACTION_PRE_FILL) {
            val from = intent.getSerializableExtra(FROM) as WrapLocation?
            val via = intent.getSerializableExtra(VIA) as WrapLocation?
            val to = intent.getSerializableExtra(TO) as WrapLocation?
            if (from != null) viewModel.setFromLocation(from)
            if (via != null) viewModel.setViaLocation(via)
            if (to != null) viewModel.setToLocation(to)
        } else if (intent.action == ACTION_SEARCH) {
            val via = intent.getSerializableExtra(VIA) as WrapLocation?

            val data = intent.dataString
            if (data == null) {
                val from = intent.getSerializableExtra(FROM) as WrapLocation?
                val to = intent.getSerializableExtra(TO) as WrapLocation?
                searchFromTo(from, via, to)
            } else if (data == INTENT_URI_HOME) {
                val liveData = viewModel.home
                liveData.observe(this, Observer { home ->
                    if (home == null) {
                        HomePickerFragment().show(supportFragmentManager, HomePickerFragment::class.java.simpleName)
                    } else {
                        searchFromTo(WrapLocation(GPS), via, home)
                    }
                    liveData.removeObservers(this)
                })
            } else if (data == INTENT_URI_WORK) {
                val liveData = viewModel.work
                liveData.observe(this, Observer { work ->
                    if (work == null) {
                        WorkPickerFragment().show(supportFragmentManager, WorkPickerFragment::class.java.simpleName)
                    } else {
                        searchFromTo(WrapLocation(GPS), via, work)
                    }
                    liveData.removeObservers(this)
                })
            } else if (data == INTENT_URI_FAVORITE) {
                val uid = intent.getLongExtra(FAV_TRIP_UID, 0)
                val liveData = viewModel.favoriteTrips
                liveData.observe(this, Observer { trips ->
                    if (trips != null) {
                        for (trip in trips) {
                            if (trip.uid == uid) {
                                searchFromTo(trip.from, trip.via, trip.to)
                                break
                            }
                        }
                    }
                    liveData.removeObservers(this)
                })
            } else {
                throw IllegalArgumentException()
            }
        }
    }

    private fun searchFromTo(from: WrapLocation?, via: WrapLocation?, to: WrapLocation?) {
        if (from == null || from.wrapType == GPS) {
            viewModel.setFromLocation(null)
            viewModel.findGpsLocation.setValue(FavLocationType.FROM)
        } else {
            viewModel.findGpsLocation.value = null
            viewModel.setFromLocation(from)
        }
        viewModel.setViaLocation(via)
        viewModel.setToLocation(to)
        viewModel.search()
    }

}
