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

import androidx.annotation.StringRes
import de.grobox.transportr.R


internal data class Contributor(val name: String)
internal data class ContributorGroup(@StringRes val name: Int, val contributors: List<Contributor>)

internal val CONTRIBUTORS = listOf(
    ContributorGroup(
        R.string.maintainer, listOf(
            Contributor("Torsten Grote")
        )
    ),
    ContributorGroup(
        R.string.contributors, listOf(
            Contributor("ByteHamster"),
            Contributor("Chimo"),
            Contributor("Felix Delattre"),
            Contributor("Hartmut Goebel"),
            Contributor("Jochen Sprickerhof"),
            Contributor("Patrick Kanzler"),
            Contributor("kas70"),
            Contributor("Mats Sjöberg"),
            Contributor("Mikolai Gütschow"),
            Contributor("mray"),
            Contributor("Noxsense"),
            Contributor("Paspartout"),
            Contributor("Peter Serwylo"),
            Contributor("Rob Snelders"),
            Contributor("Robert Schütz"),
            Contributor("Sebastian Grote"),
            Contributor("Simó Albert i Beltran")
        )
    )
)

internal val LANGUAGES = listOf(
    ContributorGroup(
        R.string.basque, listOf(
            Contributor("Aitor Beriain"),
            Contributor("Osoitz")
        )
    ),
    ContributorGroup(
        R.string.catalan, listOf(
            Contributor("el_libre"),
            Contributor("jmontane")
        )
    ),
    ContributorGroup(
        R.string.taiwanese, listOf(
            Contributor("Gerrit Schultz")
        )
    ),
    ContributorGroup(
        R.string.czech, listOf(
            Contributor("Miloš Koliáš")
        )
    ),
    ContributorGroup(
        R.string.dutch, listOf(
            Contributor("TheLastProject"),
            Contributor("Midgard")
        )
    ),
    ContributorGroup(
        R.string.esperanto, listOf(
            Contributor("Verdulo")
        )
    ),
    ContributorGroup(
        R.string.farsi, listOf(
            Contributor("Reza Ghasemi")
        )
    ),
    ContributorGroup(
        R.string.french, listOf(
            Contributor("Cryptie"),
            Contributor("David Maulat"),
            Contributor("Paul Caranton")
        )
    ),
    ContributorGroup(
        R.string.german, listOf(
            Contributor("Martin Riesner"),
            Contributor("Benedikt Volkmer"),
            Contributor("Ettore Atalan"),
            Contributor("dktz"),
            Contributor("Daniel Jäger"),
            Contributor("Fabian Neumann"),
            Contributor("Patrick Kanzler"),
            Contributor("Vinzenz Vietzke")
        )
    ),
    ContributorGroup(
        R.string.greek, listOf(
            Contributor("Vangelis Skarmoutsos")
        )
    ),
    ContributorGroup(
        R.string.hungarian, listOf(
            Contributor("András Lengyel-Nagy")
        )
    ),
    ContributorGroup(
        R.string.italian, listOf(
            Contributor("Giuseppe Pignataro"),
            Contributor("Michael Moroni"),
            Contributor("Davide Neri"),
            Contributor("Rosario")
        )
    ),
    ContributorGroup(
        R.string.japanese, listOf(
            Contributor("Naofumi")
        )
    ),
    ContributorGroup(
        R.string.norwegian_bokmal, listOf(
            Contributor("Allan Nordhøy")
        )
    ),
    ContributorGroup(
        R.string.polish, listOf(
            Contributor("Daniel Koć"),
            Contributor("Verdulo"),
            Contributor("jimmy konfitura"),
            Contributor("kompowiec2")
        )
    ),
    ContributorGroup(
        R.string.portuguese_br, listOf(
            Contributor("Gislene Kucker Arantes"),
            Contributor("Cibele A. Cambuí"),
            Contributor("Thiago Costa"),
            Contributor("Paulo Martins"),
            Contributor("José Victor"),
            Contributor("Caio Volpato"),
            Contributor("Danilo Silva"),
            Contributor("Renan Gomes")
        )
    ),
    ContributorGroup(
        R.string.russian, listOf(
            Contributor("Dmitry Grechka"),
            Contributor("Илья Пономарев")
        )
    ),
    ContributorGroup(
        R.string.spanish, listOf(
            Contributor("Adolfo Jayme Barrientos"),
            Contributor("acuccaro"),
            Contributor("Víctor Moral"),
            Contributor("Aitor Beriain"),
            Contributor("Juls Ko")
        )
    ),
    ContributorGroup(
        R.string.swedish, listOf(
            Contributor("Jonatan Nyberg")
        )
    ),
    ContributorGroup(
        R.string.tamil, listOf(
            Contributor("Prasanna Venkadesh")
        )
    ),
    ContributorGroup(
        R.string.turkish, listOf(
            Contributor("Emre Deniz"),
            Contributor("Erdoğan Şahin"),
            Contributor("Emre JILTA")
        )
    ),
    ContributorGroup(
        R.string.ukrainian, listOf(
            Contributor("Paul S")
        )
    ),
    ContributorGroup(
        R.string.danish, listOf(
            Contributor("Daniel L.")
        )
    )
)
