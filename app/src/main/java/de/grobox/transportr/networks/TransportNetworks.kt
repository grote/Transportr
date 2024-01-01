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

import android.annotation.SuppressLint
import android.content.Context
import de.grobox.transportr.R
import de.grobox.transportr.networks.TransportNetwork.Status.ALPHA
import de.grobox.transportr.networks.TransportNetwork.Status.BETA
import de.schildbach.pte.*
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.util.*

@SuppressLint("ConstantLocale")
private val networks = arrayOf(
    Continent(
        R.string.np_continent_europe, R.drawable.continent_europe,
        listOf(
            Country(
                R.string.np_continent_europe, flag = "🇪🇺", sticky = true, networks = listOf(
                    TransportNetwork(
                        id = NetworkId.RT,
                        name = R.string.np_name_rt,
                        description = R.string.np_desc_rt,
                        agencies = R.string.np_desc_rt_networks,
                        logo = R.drawable.network_rt_logo,
                        factory = { RtProvider() }
                    ),
                    TransportNetwork(
                        id = NetworkId.DB,
                        name = R.string.np_name_db,
                        description = R.string.np_desc_db2,
                        logo = R.drawable.network_db_logo,
                        factory = { DbProvider("{\"type\":\"AID\",\"aid\":\"n91dB8Z77MLdoR0K\"}", "bdI8UVj40K5fvxwf".toByteArray(Charsets.UTF_8)) }
                    )
                )
            ),
            Country(
                R.string.np_region_germany, flag = "🇩🇪", networks = listOf(
                    TransportNetwork(
                        id = NetworkId.DB,
                        name = R.string.np_name_db,
                        description = R.string.np_desc_db,
                        logo = R.drawable.network_db_logo,
                        itemIdExtra = 1,
                        factory = { DbProvider("{\"type\":\"AID\",\"aid\":\"n91dB8Z77MLdoR0K\"}", "bdI8UVj40K5fvxwf".toByteArray(Charsets.UTF_8)) }
                    ),
                    TransportNetwork(
                        id = NetworkId.BVG,
                        description = R.string.np_desc_bvg,
                        logo = R.drawable.network_bvg_logo,
                        factory = { BvgProvider("{\"aid\":\"1Rxs112shyHLatUX4fofnmdxK\",\"type\":\"AID\"}") }
                    ),
                    TransportNetwork(
                        id = NetworkId.VBB,
                        description = R.string.np_desc_vbb,
                        logo = R.drawable.network_vbb_logo,
                        factory = { VbbProvider("{\"type\":\"AID\",\"aid\":\"hafas-vbb-apps\"}", "RCTJM2fFxFfxxQfI".toByteArray(Charsets.UTF_8)) }
                    ),
                    TransportNetwork(
                        id = NetworkId.BAYERN,
                        name = R.string.np_name_bayern,
                        description = R.string.np_desc_bayern,
                        logo = R.drawable.network_bayern_logo,
                        factory = { BayernProvider() }
                    ),
                    TransportNetwork(
                        id = NetworkId.AVV,
                        description = R.string.np_desc_avv,
                        logo = R.drawable.network_avv_logo,
                        status = BETA,
                        factory = { AvvProvider() }
                    ),
                    TransportNetwork(
                        id = NetworkId.MVV,
                        description = R.string.np_desc_mvv,
                        logo = R.drawable.network_mvv_logo,
                        factory = { MvvProvider() }
                    ),
                    TransportNetwork(
                        id = NetworkId.INVG,
                        description = R.string.np_desc_invg,
                        logo = R.drawable.network_invg_logo,
                        status = BETA,
                        factory = { InvgProvider("{\"type\":\"AID\",\"aid\":\"GITvwi3BGOmTQ2a5\"}", "ERxotxpwFT7uYRsI".toByteArray(Charsets.UTF_8)) }
                    ),
                    TransportNetwork (
                        id = NetworkId.VBN,
                        description = R.string.np_desc_vbn,
                        logo = R.drawable.network_vbn_logo,
                        factory = { VbnProvider("{\"type\":\"AID\",\"aid\":\"rnOHBWhesvc7gFkd\"}", "SP31mBufSyCLmNxp".toByteArray(Charsets.UTF_8)) }
                    ),
                    TransportNetwork (
                        id = NetworkId.SH,
                        name = R.string.np_name_sh,
                        description = R.string.np_desc_sh,
                        logo = R.drawable.network_sh_logo,
                        factory = { ShProvider("{\"type\":\"AID\",\"aid\":\"r0Ot9FLFNAFxijLW\"}") }
                    ),
                    TransportNetwork (
                        id = NetworkId.AVV_AACHEN,
                        name = R.string.np_name_avvaachen,
                        description = R.string.np_desc_avvaachen,
                        logo = R.drawable.network_avvaachen_logo,
                        factory = { AvvAachenProvider("{\"type\":\"AID\",\"aid\":\"4vV1AcH3N511icH\"}") }
                    ),
                    TransportNetwork(
                        id = NetworkId.VGN,
                        description = R.string.np_desc_vgn,
                        agencies = R.string.np_desc_vgn_networks,
                        logo = R.drawable.network_vgn_logo,
                        status = BETA,
                        factory = { VgnProvider() }
                    ),
                    TransportNetwork(
                        id = NetworkId.VVM,
                        description = R.string.np_desc_vvm,
                        logo = R.drawable.network_vvm_logo,
                        factory = { VvmProvider() }
                    ),
                    TransportNetwork(
                        id = NetworkId.VMV,
                        description = R.string.np_desc_vmv,
                        logo = R.drawable.network_vmv_logo,
                        factory = { VmvProvider() }
                    ),
                    TransportNetwork(
                        id = NetworkId.GVH,
                        description = R.string.np_desc_gvh,
                        logo = R.drawable.network_gvh_logo,
                        factory = { GvhProvider() }
                    ),
                    TransportNetwork(
                        id = NetworkId.BSVAG,
                        name = R.string.np_name_bsvag,
                        description = R.string.np_desc_bsvag,
                        logo = R.drawable.network_bsvag_logo,
                        factory = { BsvagProvider() }
                    ),
                    TransportNetwork(
                        id = NetworkId.VVO,
                        description = R.string.np_desc_vvo,
                        logo = R.drawable.network_vvo_logo,
                        factory = { VvoProvider("http://efaproxy.fahrinfo.uptrade.de/standard/".toHttpUrlOrNull()) }
                    ),
                    TransportNetwork(
                        id = NetworkId.NASA,
                        name = R.string.np_name_nasa,
                        description = R.string.np_desc_nasa,
                        logo = R.drawable.network_nasa_logo,
                        status = BETA,
                        factory = { NasaProvider("{\"aid\":\"nasa-apps\",\"type\":\"AID\"}") }
                    ),
                    TransportNetwork(
                        id = NetworkId.VRR,
                        description = R.string.np_desc_vrr,
                        logo = R.drawable.network_vrr_logo,
                        factory = { VrrProvider() }
                    ),
                    TransportNetwork(
                        id = NetworkId.MVG,
                        description = R.string.np_desc_mvg,
                        logo = R.drawable.network_mvg_logo,
                        factory = { MvgProvider() }
                    ),
                    TransportNetwork(
                        id = NetworkId.NVV,
                        name = R.string.np_name_nvv,
                        description = R.string.np_desc_nvv,
                        logo = R.drawable.network_nvv_logo,
                        factory = { NvvProvider("{\"type\":\"AID\",\"aid\":\"Kt8eNOH7qjVeSxNA\"}") }
                    ),
                    TransportNetwork(
                        id = NetworkId.VRN,
                        description = R.string.np_desc_vrn,
                        logo = R.drawable.network_vrn_logo,
                        factory = { VrnProvider() }
                    ),
                    TransportNetwork(
                        id = NetworkId.VVS,
                        description = R.string.np_desc_vvs,
                        logo = R.drawable.network_vvs_logo,
                        factory = { VvsProvider("http://www2.vvs.de/oeffi/".toHttpUrlOrNull()) }
                    ),
                    TransportNetwork(
                        id = NetworkId.DING,
                        description = R.string.np_desc_ding,
                        logo = R.drawable.network_ding_logo,
                        factory = { DingProvider() }
                    ),
                    TransportNetwork(
                        id = NetworkId.KVV,
                        description = R.string.np_desc_kvv,
                        logo = R.drawable.network_kvv_logo,
                        factory = { KvvProvider("https://projekte.kvv-efa.de/oeffi/".toHttpUrlOrNull()) }
                    ),
                    TransportNetwork(
                        id = NetworkId.NVBW,
                        description = R.string.np_desc_nvbw,
                        logo = R.drawable.network_nvbw_logo,
                        factory = { NvbwProvider() }
                    ),
                    TransportNetwork(
                        id = NetworkId.VVV,
                        description = R.string.np_desc_vvv,
                        logo = R.drawable.network_vvv_logo,
                        factory = { VvvProvider() }
                    ),
                    TransportNetwork(
                        id = NetworkId.VGS,
                        name = R.string.np_name_vgs,
                        description = R.string.np_desc_vgs,
                        logo = R.drawable.network_vgs_logo,
                        factory = { VgsProvider("{\"type\":\"AID\",\"aid\":\"51XfsVqgbdA6oXzHrx75jhlocRg6Xe\"}", "HJtlubisvxiJxss".toByteArray(Charsets.UTF_8)) }
                    ),
                    TransportNetwork(
                        id = NetworkId.VMT,
                        description = R.string.np_desc_vmt,
                        factory = { VmtProvider("{\"aid\":\"vj5d7i3g9m5d7e3\",\"type\":\"AID\"}") }
                    )
                )
            ),
            Country(
                R.string.np_region_austria, flag = "🇦🇹", networks = listOf(
                    TransportNetwork(
                        id = NetworkId.OEBB,
                        name = R.string.np_name_oebb,
                        description = R.string.np_desc_oebb,
                        logo = R.drawable.network_oebb_logo,
                        factory = { OebbProvider("{\"type\":\"AID\",\"aid\":\"OWDL4fE4ixNiPBBm\"}") }
                    ),
                    // see https://github.com/grote/Transportr/issues/817
                    /*TransportNetwork(
                        id = NetworkId.VOR,
                        description = R.string.np_desc_vor,
                        logo = R.drawable.network_vor_logo,
                        factory = { VorProvider(VAO) }
                    ),*/
                    TransportNetwork(
                        id = NetworkId.LINZ,
                        name = R.string.np_name_linz,
                        description = R.string.np_desc_linz,
                        logo = R.drawable.network_linz_logo,
                        factory = { LinzProvider() }
                    ),
                    TransportNetwork(
                        id = NetworkId.VVT,
                        description = R.string.np_desc_vvt,
                        logo = R.drawable.network_vvt_logo,
                        factory = { VvtProvider("{\"type\":\"AID\",\"aid\":\"wf7mcf9bv3nv8g5f\"}") }
                    ),
//                    TransportNetwork(
//                        id = NetworkId.IVB,
//                        description = R.string.np_desc_ivb,
//                        logo = R.drawable.network_ivb_logo,
//                        factory = { IvbProvider() }
//                    ),
                    // see https://github.com/grote/Transportr/issues/817
                    /*TransportNetwork(
                        id = NetworkId.STV,
                        name = R.string.np_name_stv,
                        description = R.string.np_desc_stv,
                        logo = R.drawable.network_stv_logo,
                        factory = { StvProvider() }
                    ),*/
                    TransportNetwork(
                        id = NetworkId.WIEN,
                        name = R.string.np_name_wien,
                        description = R.string.np_desc_wien,
                        logo = R.drawable.network_wien_logo,
                        factory = { WienProvider() }
                    ),
                    // see https://github.com/grote/Transportr/issues/817
                    /*TransportNetwork(
                        id = NetworkId.VMOBIL,
                        name = R.string.np_name_vmobil,
                        description = R.string.np_desc_vmobil,
                        logo = R.drawable.network_vmobil_logo,
                        factory = { VmobilProvider(VAO) }
                    )*/
                )
            ),
            Country(
                R.string.np_region_liechtenstein, flag = "🇱🇮", networks = listOf(
                    TransportNetwork(
                        id = NetworkId.VMOBIL,
                        name = R.string.np_name_vmobil,
                        description = R.string.np_desc_vmobil,
                        logo = R.drawable.network_vmobil_logo,
                        itemIdExtra = 1,
                        factory = { VmobilProvider(VAO) }
                    )
                )
            ),
            Country(
                R.string.np_region_switzerland, flag = "🇨🇭", networks = listOf(
                    TransportNetwork(
                        id = NetworkId.VBL,
                        description = R.string.np_desc_vbl,
                        logo = R.drawable.network_vbl_logo,
                        factory = { VblProvider() }
                    ),
                    TransportNetwork(
                        id = NetworkId.ZVV,
                        description = R.string.np_desc_zvv,
                        logo = R.drawable.network_zvv_logo,
                        factory = { ZvvProvider("{\"type\":\"AID\",\"aid\":\"hf7mcf9bv3nv8g5f\"}") }
                    )
                )
            ),
            // disabled until https://github.com/schildbach/public-transport-enabler/issues/502 is resolved
            /*Country(
                R.string.np_region_belgium, flag = "🇧🇪", networks = listOf(
                    TransportNetwork(
                        id = NetworkId.SNCB,
                        name = R.string.np_region_belgium,
                        description = R.string.np_desc_sncb,
                        agencies = R.string.np_desc_sncb_networks,
                        logo = R.drawable.network_sncb_logo,
                        factory = { SncbProvider("{\"type\":\"AID\",\"aid\":\"sncb-mobi\"}") }
                    )
                )
            ),*/
            Country(
                R.string.np_region_luxembourg, flag = "🇱🇺", networks = listOf(
                    TransportNetwork(
                        id = NetworkId.LU,
                        name = R.string.np_name_lu,
                        description = R.string.np_desc_lu,
                        agencies = R.string.np_desc_lu_networks,
                        factory = { LuProvider("{\"type\":\"AID\",\"aid\":\"Aqf9kNqJLjxFx6vv\"}") }
                    )
                )
            ),
            Country(
                R.string.np_region_netherlands, flag = "🇳🇱", networks = listOf(
                    TransportNetwork(
                        id = NetworkId.NS,
                        description = R.string.np_desc_ns,
                        logo = R.drawable.network_ns_logo,
                        status = BETA,
                        factory = { NsProvider() }
                    ),
                    // disabled until https://github.com/schildbach/public-transport-enabler/issues/438 is fixed
                    /*TransportNetwork(
                        id = NetworkId.NEGENTWEE,
                        name = R.string.np_name_negentwee,
                        description = R.string.np_desc_negentwee,
                        logo = R.drawable.network_negentwee_logo,
                        status = ALPHA,
                        factory = {
                            if (Locale.getDefault().language == "nl") {
                                NegentweeProvider(NegentweeProvider.Language.NL_NL)
                            } else {
                                NegentweeProvider(NegentweeProvider.Language.EN_GB)
                            }
                        }
                    )*/
                )
            ),
            Country(
                R.string.np_region_denmark, flag = "🇩🇰", networks = listOf(
                    TransportNetwork(
                        id = NetworkId.DSB,
                        description = R.string.np_desc_dsb,
                        logo = R.drawable.network_dsb_logo,
                        factory = { DsbProvider("{\"type\":\"AID\",\"aid\":\"irkmpm9mdznstenr-android\"}") }
                    )
                )
            ),
            Country(
                R.string.np_region_sweden, flag = "🇸🇪", networks = listOf(
                    TransportNetwork(
                        id = NetworkId.SE,
                        description = R.string.np_desc_se,
                        logo = R.drawable.network_se_logo,
                        factory = { SeProvider("{\"type\":\"AID\",\"aid\":\"h5o3n7f4t2m8l9x1\"}") }
                    )
                )
            ),
            Country(
                R.string.np_region_gb, flag = "🇬🇧", networks = listOf(
                    TransportNetwork(
                        id = NetworkId.TLEM,
                        description = R.string.np_desc_tlem,
                        factory = { TlemProvider() }
                    ),
                    TransportNetwork(
                        id = NetworkId.MERSEY,
                        name = R.string.np_name_mersey,
                        description = R.string.np_desc_mersey,
                        logo = R.drawable.network_mersey_logo,
                        factory = { MerseyProvider() }
                    )
                )
            ),
            Country(
                R.string.np_region_ireland, flag = "🇮🇪", networks = listOf(
                    TransportNetwork(
                        id = NetworkId.TFI,
                        description = R.string.np_desc_tfi,
                        logo = R.drawable.network_tfi_logo,
                        factory = { TfiProvider() }
                    )
                )
            ),
            Country(
                R.string.np_name_it, flag = "🇮🇹", networks = listOf(
                    TransportNetwork(
                        id = NetworkId.IT,
                        name = R.string.np_name_it,
                        description = R.string.np_desc_it,
                        agencies = R.string.np_desc_it_networks,
                        logo = R.drawable.network_it_logo,
                        status = BETA,
                        goodLineNames = true,
                        factory = { ItalyProvider(NAVITIA) }
                    )
                )
            ),
            Country(
                R.string.np_region_france, "🇫🇷", networks = listOf(
                    TransportNetwork(
                        id = NetworkId.PARIS,
                        name = R.string.np_name_paris,
                        description = R.string.np_desc_paris,
                        agencies = R.string.np_desc_paris_networks,
                        logo = R.drawable.network_paris_logo,
                        status = BETA,
                        factory = { ParisProvider(NAVITIA) }
                    ),
                    TransportNetwork(
                        id = NetworkId.FRANCESOUTHWEST,
                        name = R.string.np_name_frenchsouthwest,
                        description = R.string.np_desc_frenchsouthwest,
                        agencies = R.string.np_desc_frenchsouthwest_networks,
                        logo = R.drawable.network_francesouthwest_logo,
                        status = BETA,
                        goodLineNames = true,
                        factory = { FranceSouthWestProvider(NAVITIA) }
                    ),
                    TransportNetwork(
                        id = NetworkId.FRANCENORTHEAST,
                        name = R.string.np_name_francenortheast,
                        description = R.string.np_desc_francenortheast,
                        agencies = R.string.np_desc_francenortheast_networks,
                        logo = R.drawable.network_francenortheast_logo,
                        status = ALPHA,
                        goodLineNames = true,
                        factory = { FranceNorthEastProvider(NAVITIA) }
                    ),
                    TransportNetwork(
                        id = NetworkId.FRANCENORTHWEST,
                        name = R.string.np_name_francenorthwest,
                        description = R.string.np_desc_francenorthwest,
                        agencies = R.string.np_desc_francenorthwest_networks,
                        logo = R.drawable.network_francenorthwest_logo,
                        status = ALPHA,
                        factory = { FranceNorthWestProvider(NAVITIA) }
                    ),
                    TransportNetwork(
                        id = NetworkId.FRANCESOUTHEAST,
                        name = R.string.np_name_frenchsoutheast,
                        description = R.string.np_desc_frenchsoutheast,
                        agencies = R.string.np_desc_frenchsoutheast_networks,
                        logo = R.drawable.network_francesoutheast_logo,
                        status = BETA,
                        goodLineNames = true,
                        factory = { FranceSouthEastProvider(NAVITIA) }
                    )
                )
            ),
            Country(
                R.string.np_name_spain, flag = "🇪🇸", networks = listOf(
                    TransportNetwork(
                        id = NetworkId.SPAIN,
                        name = R.string.np_name_spain,
                        description = R.string.np_desc_spain,
                        agencies = R.string.np_desc_spain_networks,
                        logo = R.drawable.network_spain_logo,
                        status = BETA,
                        factory = { SpainProvider(NAVITIA) }
                    )
                )
            ),
        )
    ),
    Continent(
        R.string.np_continent_north_america, R.drawable.continent_north_america, countries = listOf(
            Country(
                R.string.np_region_usa, "🇺🇸", networks = listOf(
                    TransportNetwork(
                        id = NetworkId.RTACHICAGO,
                        name = R.string.np_name_rtachicago,
                        description = R.string.np_desc_rtachicago,
                        agencies = R.string.np_desc_rtachicago_networks,
                        logo = R.drawable.network_rtachicago_logo,
                        status = BETA,
                        factory = { RtaChicagoProvider() }
                    ),
                    TransportNetwork(
                        id = NetworkId.BART,
                        description = R.string.np_desc_bart,
                        logo = R.drawable.network_bart_logo,
                        status = BETA,
                        factory = { BartProvider("{\"type\":\"AID\",\"aid\":\"kEwHkFUCIL500dym\"}") }
                    ),
                    TransportNetwork(
                        id = NetworkId.CMTA,
                        name = R.string.np_name_cmta,
                        description = R.string.np_desc_cmta,
                        logo = R.drawable.network_cmta_logo,
                        status = BETA,
                        factory = { CmtaProvider("{\"type\":\"AID\",\"aid\":\"web9j2nak29uz41irb\"}") }
                    ),
                )
            ),
        )
    ),
    Continent(
        R.string.np_continent_asia, R.drawable.continent_asia, countries = listOf(
            Country(
                R.string.np_region_uae, "🇦🇪", networks = listOf(
                    TransportNetwork(
                        id = NetworkId.DUB,
                        name = R.string.np_name_dub,
                        description = R.string.np_desc_dub,
                        status = BETA,
                        factory = { DubProvider() }
                    )
                )
            )
        )
    ),
    Continent(
        R.string.np_continent_oceania, R.drawable.continent_oceania, countries = listOf(
            Country(
                R.string.np_region_australia, flag = "🇦🇺", networks = listOf(
                    TransportNetwork(
                        id = NetworkId.AUSTRALIA,
                        name = R.string.np_name_australia,
                        description = R.string.np_desc_australia,
                        agencies = R.string.np_desc_australia_networks,
                        logo = R.drawable.network_aus_logo,
                        status = BETA,
                        factory = { AustraliaProvider(NAVITIA) }
                    ),
                    TransportNetwork(
                        id = NetworkId.SYDNEY,
                        name = R.string.np_name_sydney,
                        description = R.string.np_desc_sydney,
                        logo = R.drawable.network_sydney_logo,
                        factory = { SydneyProvider() }
                    )
                )
            ),
        )
    )
)

private const val NAVITIA = "87a37b95-913a-4cb4-ba52-eb0bc0b304ca"
private const val VAO = "{\"aid\":\"hf7mcf9bv3nv8g5f\",\"pw\":\"87a6f8ZbnBih32\",\"type\":\"USER\",\"user\":\"mobile\"}"

internal fun getContinentItems(context: Context): List<ContinentItem> {
    return List(networks.size) { i ->
        networks[i].getItem(context)
    }.sortedBy { it.getName(context) }
}

internal fun getTransportNetwork(id: NetworkId): TransportNetwork? {
    for (continent in networks) {
        return continent.getTransportNetworks().find { it.id == id } ?: continue
    }
    return null
}

internal fun getTransportNetworkPositions(context: Context, network: TransportNetwork): Triple<Int, Int, Int> {
    val continents = networks.sortedBy { it.getName(context) }.withIndex()
    for ((continentIndex, continent) in continents) {
        val countries = continent.countries.sortedWith(Country.Comparator(context)).withIndex()
        for ((countryIndex, country) in countries) {
            val networkIndex = country.networks.indexOf(network)
            if (networkIndex > -1) {
                return Triple(continentIndex, countryIndex, networkIndex)
            }
        }
    }
    return Triple(-1, -1, -1)
}
