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

package de.grobox.transportr.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.grobox.transportr.R
import de.grobox.transportr.TransportrFragment
import de.grobox.transportr.about.ContributorAdapter.ContributorViewHolder
import de.grobox.transportr.about.ContributorGroupAdapter.ContributorGroupViewHolder

class ContributorFragment : TransportrFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_contributors, container, false)

        val list = v.findViewById<RecyclerView>(R.id.list)
        list.layoutManager = LinearLayoutManager(context)
        list.adapter = ContributorGroupAdapter(CONTRIBUTORS)

        return v
    }

}

class TranslatorsFragment : TransportrFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_translators, container, false)

        val list = v.findViewById<RecyclerView>(R.id.list)
        list.layoutManager = LinearLayoutManager(context)
        list.adapter = ContributorGroupAdapter(LANGUAGES)

        return v
    }

}


private class ContributorGroupAdapter(val groups: List<ContributorGroup>) : RecyclerView.Adapter<ContributorGroupViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContributorGroupViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.list_item_contributor_group, parent, false)
        return ContributorGroupViewHolder(v)
    }

    override fun onBindViewHolder(ui: ContributorGroupViewHolder, position: Int) {
        ui.bind(groups[position])
    }

    override fun getItemCount(): Int = groups.size

    class ContributorGroupViewHolder(v: View) : RecyclerView.ViewHolder(v) {

        val languageName: TextView = v.findViewById(R.id.languageName)
        val list: RecyclerView = v.findViewById(R.id.list)

        internal fun bind(contributorGroup: ContributorGroup) {
            languageName.setText(contributorGroup.name)
            list.layoutManager = LinearLayoutManager(list.context)
            list.adapter = ContributorAdapter(contributorGroup.contributors)
        }
    }

}


private class ContributorAdapter(val contributors: List<Contributor>) : RecyclerView.Adapter<ContributorViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContributorViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.list_item_contributor, parent, false)
        return ContributorViewHolder(v)
    }

    override fun onBindViewHolder(ui: ContributorViewHolder, position: Int) {
        ui.bind(contributors[position])
    }

    override fun getItemCount(): Int = contributors.size


    class ContributorViewHolder(v: View) : RecyclerView.ViewHolder(v) {

        val name: TextView = v.findViewById(R.id.name)

        internal fun bind(contributor: Contributor) {
            name.text = contributor.name
        }
    }

}
