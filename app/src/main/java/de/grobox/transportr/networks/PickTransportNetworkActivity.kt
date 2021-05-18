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

package de.grobox.transportr.networks

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.fastadapter.IItem
import com.mikepenz.fastadapter.ISelectionListener
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter
import com.mikepenz.fastadapter.expandable.ExpandableExtension
import de.grobox.transportr.R
import de.grobox.transportr.TransportrActivity
import de.grobox.transportr.map.MapActivity

class PickTransportNetworkActivity : TransportrActivity(), ISelectionListener<IItem<*, *>> {

    private lateinit var adapter: FastItemAdapter<IItem<*, *>>
    private lateinit var expandableExtension: ExpandableExtension<IItem<*, *>>
    private lateinit var list: RecyclerView

    private var firstStart: Boolean = false
    private var selectAllowed = false

    companion object {
        const val FORCE_NETWORK_SELECTION = "ForceNetworkSelection"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pick_transport_network)
        setUpCustomToolbar(false)

        if (intent.getBooleanExtra(FORCE_NETWORK_SELECTION, false)) {
            firstStart = true
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
            findViewById<View>(R.id.firstRunTextView).visibility = VISIBLE
        } else {
            firstStart = false
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            findViewById<View>(R.id.firstRunTextView).visibility = GONE
        }
        setResult(RESULT_CANCELED)

        adapter = FastItemAdapter()
        adapter.withSelectable(true)
        adapter.withSelectionListener(this)
        expandableExtension = ExpandableExtension()
        expandableExtension.withOnlyOneExpandedItem(false)
        adapter.addExtension(expandableExtension)
        list = findViewById(R.id.list)
        list.layoutManager = LinearLayoutManager(this)
        list.adapter = adapter

        for (c in getContinentItems(this)) {
            adapter.add(c)
        }

        if (savedInstanceState != null) adapter.withSavedInstanceState(savedInstanceState)

        selectItem()
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        val newState = adapter.saveInstanceState(outState)
        super.onSaveInstanceState(newState, outPersistentState)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            onBackPressed()
            true
        } else {
            false
        }
    }

    override fun onSelectionChanged(item: IItem<*, *>?, selected: Boolean) {
        if (item == null || !selectAllowed || !selected) return
        selectAllowed = false
        val networkItem = item as TransportNetworkItem
        manager.setTransportNetwork(networkItem.transportNetwork)
        setResult(RESULT_OK)
        if (firstStart) { // MapActivity was closed
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
        }
        supportFinishAfterTransition()
    }

    private fun selectItem() {
        val network = manager.transportNetwork.value
        if (network == null) {
            selectAllowed = true
            return
        }

        val (continentPos, countryPos, networkPos) = getTransportNetworkPositions(this, network)

        if (continentPos != -1) {
            expandableExtension.expand(continentPos)
            if (countryPos != -1) {
                expandableExtension.expand(continentPos + countryPos + 1)
                if (networkPos != -1) {
                    val selectPos = continentPos + countryPos + networkPos + 2
                    adapter.select(selectPos, false)
                    list.scrollToPosition(selectPos)
                    list.smoothScrollBy(0, -75)
                    selectAllowed = true
                }
            }
        }

    }

}
