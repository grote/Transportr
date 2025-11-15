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
import android.util.Base64
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
                        factory = { DbProvider() }
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
                        factory = { DbProvider() }
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
                        id = NetworkId.AVV_AUGSBURG,
                        name = R.string.np_name_avv,
                        description = R.string.np_desc_avv,
                        logo = R.drawable.network_avv_logo,
                        factory = { AvvAugsburgProvider("{\"type\":\"AID\",\"aid\":\"jK91AVVZU77xY5oH\"}") }
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
                        factory = { VvoProvider() }
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
                        factory = { VvsProvider() }
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
                        id = NetworkId.VRS,
                        description = R.string.np_desc_vrs,
                        logo = R.drawable.network_vrs_logo,
                        factory = { VrsProvider(VRS) }
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
                        id = NetworkId.SEARCHCH,
                        name = R.string.np_name_sbb,
                        description = R.string.np_desc_sbb,
                        logo = R.drawable.network_sbb_logo,
                        factory = { CHSearchProvider() }
                    ),
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
                        logo = R.drawable.network_lu_logo,
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
                R.string.np_region_poland, flag = "🇵🇱", networks = listOf(
                    TransportNetwork(
                        id = NetworkId.PL,
                        description = R.string.np_desc_pl,
                        logo = R.drawable.network_pl_logo,
                        factory = { PlProvider("{\"type\":\"AID\",\"aid\":\"DrxJYtYZQpEBCtcb\"}"); }
                    ),
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
    )
)

private const val VAO = "{\"aid\":\"hf7mcf9bv3nv8g5f\",\"pw\":\"87a6f8ZbnBih32\",\"type\":\"USER\",\"user\":\"mobile\"}"
private val VRS: ByteArray = Base64.decode(
    "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",
    Base64.DEFAULT
)
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
