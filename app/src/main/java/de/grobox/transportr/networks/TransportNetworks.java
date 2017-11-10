/*
 *    Transportr
 *
 *    Copyright (c) 2013 - 2017 Torsten Grote
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

package de.grobox.transportr.networks;

import de.grobox.transportr.R;
import de.schildbach.pte.NetworkId;

import static de.grobox.transportr.networks.TransportNetwork.Status.ALPHA;
import static de.grobox.transportr.networks.TransportNetwork.Status.BETA;

public interface TransportNetworks {

	TransportNetwork[] networks = {

			// Europe

			new TransportNetworkBuilder()
					.setId(NetworkId.RT)
					.setRegion(Region.EUROPE)
					.setName(R.string.np_name_rt)
					.setDescription(R.string.np_desc_rt)
					.setAgencies(R.string.np_desc_rt_networks)
					.setLogo(R.drawable.network_rt_logo)
					.build(),

			// Germany

			new TransportNetworkBuilder()
					.setId(NetworkId.DB)
					.setName(R.string.np_name_db)
					.setDescription(R.string.np_desc_db)
					.setLogo(R.drawable.network_db_logo)
					.setRegion(Region.GERMANY)
					.build(),
			new TransportNetworkBuilder()
					.setId(NetworkId.BVG)
					.setDescription(R.string.np_desc_bvg)
					.setLogo(R.drawable.network_bvg_logo)
					.setRegion(Region.GERMANY)
					.build(),
			new TransportNetworkBuilder()
					.setId(NetworkId.VBB)
					.setDescription(R.string.np_desc_vbb)
					.setLogo(R.drawable.network_vbb_logo)
					.setRegion(Region.GERMANY)
					.build(),
			new TransportNetworkBuilder()
					.setId(NetworkId.BAYERN)
					.setName(R.string.np_name_bayern)
					.setDescription(R.string.np_desc_bayern)
					.setLogo(R.drawable.network_bayern_logo)
					.setRegion(Region.GERMANY)
					.build(),
			new TransportNetworkBuilder()
					.setId(NetworkId.AVV)
					.setDescription(R.string.np_desc_avv)
					.setLogo(R.drawable.network_avv_logo)
					.setRegion(Region.GERMANY)
					.setStatus(BETA)
					.build(),
			new TransportNetworkBuilder()
					.setId(NetworkId.MVV)
					.setDescription(R.string.np_desc_mvv)
					.setLogo(R.drawable.network_mvv_logo)
					.setRegion(Region.GERMANY)
					.build(),
			new TransportNetworkBuilder()
					.setId(NetworkId.INVG)
					.setDescription(R.string.np_desc_invg)
					.setLogo(R.drawable.network_invg_logo)
					.setRegion(Region.GERMANY)
					.setStatus(BETA)
					.build(),
			new TransportNetworkBuilder()
					.setId(NetworkId.VGN)
					.setDescription(R.string.np_desc_vgn)
					.setAgencies(R.string.np_desc_vgn_networks)
					.setLogo(R.drawable.network_vgn_logo)
					.setRegion(Region.GERMANY)
					.setStatus(BETA)
					.build(),
			new TransportNetworkBuilder()
					.setId(NetworkId.VVM)
					.setDescription(R.string.np_desc_vvm)
					.setLogo(R.drawable.network_vvm_logo)
					.setRegion(Region.GERMANY)
					.build(),
			new TransportNetworkBuilder()
					.setId(NetworkId.VMV)
					.setDescription(R.string.np_desc_vmv)
					.setLogo(R.drawable.network_vmv_logo)
					.setRegion(Region.GERMANY)
					.build(),
			new TransportNetworkBuilder()
					.setId(NetworkId.GVH)
					.setDescription(R.string.np_desc_gvh)
					.setLogo(R.drawable.network_gvh_logo)
					.setRegion(Region.GERMANY)
					.build(),
			new TransportNetworkBuilder()
					.setId(NetworkId.BSVAG)
					.setName(R.string.np_name_bsvag)
					.setDescription(R.string.np_desc_bsvag)
					.setLogo(R.drawable.network_bsvag_logo)
					.setRegion(Region.GERMANY)
					.build(),
			new TransportNetworkBuilder()
					.setId(NetworkId.VVO)
					.setDescription(R.string.np_desc_vvo)
					.setLogo(R.drawable.network_vvo_logo)
					.setRegion(Region.GERMANY)
					.build(),
			new TransportNetworkBuilder()
					.setId(NetworkId.VMS)
					.setDescription(R.string.np_desc_vms)
					.setLogo(R.drawable.network_vms_logo)
					.setRegion(Region.GERMANY)
					.setStatus(BETA)
					.build(),
			new TransportNetworkBuilder()
					.setId(NetworkId.NASA)
					.setName(R.string.np_name_nasa)
					.setDescription(R.string.np_desc_nasa)
					.setLogo(R.drawable.network_nasa_logo)
					.setRegion(Region.GERMANY)
					.setStatus(BETA)
					.build(),
			new TransportNetworkBuilder()
					.setId(NetworkId.VRR)
					.setDescription(R.string.np_desc_vrr)
					.setLogo(R.drawable.network_vrr_logo)
					.setRegion(Region.GERMANY)
					.build(),
			new TransportNetworkBuilder()
					.setId(NetworkId.MVG)
					.setDescription(R.string.np_desc_mvg)
					.setLogo(R.drawable.network_mvg_logo)
					.setRegion(Region.GERMANY)
					.build(),
			new TransportNetworkBuilder()
					.setId(NetworkId.NVV)
					.setName(R.string.np_name_nvv)
					.setDescription(R.string.np_desc_nvv)
					.setLogo(R.drawable.network_nvv_logo)
					.setRegion(Region.GERMANY)
					.build(),
			new TransportNetworkBuilder()
					.setId(NetworkId.VRN)
					.setDescription(R.string.np_desc_vrn)
					.setLogo(R.drawable.network_vrn_logo)
					.setRegion(Region.GERMANY)
					.build(),
			new TransportNetworkBuilder()
					.setId(NetworkId.VVS)
					.setDescription(R.string.np_desc_vvs)
					.setLogo(R.drawable.network_vvs_logo)
					.setRegion(Region.GERMANY)
					.build(),
			new TransportNetworkBuilder()
					.setId(NetworkId.DING)
					.setDescription(R.string.np_desc_ding)
					.setLogo(R.drawable.network_ding_logo)
					.setRegion(Region.GERMANY)
					.build(),
			new TransportNetworkBuilder()
					.setId(NetworkId.KVV)
					.setDescription(R.string.np_desc_kvv)
					.setLogo(R.drawable.network_kvv_logo)
					.setRegion(Region.GERMANY)
					.build(),
			new TransportNetworkBuilder()
					.setId(NetworkId.VAGFR)
					.setName(R.string.np_name_vagfr)
					.setDescription(R.string.np_desc_vagfr)
					.setLogo(R.drawable.network_vagfr_logo)
					.setRegion(Region.GERMANY)
					.build(),
			new TransportNetworkBuilder()
					.setId(NetworkId.NVBW)
					.setDescription(R.string.np_desc_nvbw)
					.setLogo(R.drawable.network_nvbw_logo)
					.setRegion(Region.GERMANY)
					.build(),
			new TransportNetworkBuilder()
					.setId(NetworkId.VVV)
					.setDescription(R.string.np_desc_vvv)
					.setLogo(R.drawable.network_vvv_logo)
					.setRegion(Region.GERMANY)
					.build(),
			new TransportNetworkBuilder()
					.setId(NetworkId.VGS)
					.setDescription(R.string.np_desc_vgs)
					.setLogo(R.drawable.network_vgs_logo)
					.setRegion(Region.GERMANY)
					.build(),
			new TransportNetworkBuilder()
					.setId(NetworkId.VRS)
					.setDescription(R.string.np_desc_vrs)
					.setLogo(R.drawable.network_vrs_logo)
					.setRegion(Region.GERMANY)
					.build(),
			new TransportNetworkBuilder()
					.setId(NetworkId.VMT)
					.setDescription(R.string.np_desc_vmt)
					.setRegion(Region.GERMANY)
					.build(),

			// Austria

			new TransportNetworkBuilder()
					.setId(NetworkId.OEBB)
					.setName(R.string.np_name_oebb)
					.setDescription(R.string.np_desc_oebb)
					.setLogo(R.drawable.network_oebb_logo)
					.setRegion(Region.AUSTRIA)
					.build(),
			new TransportNetworkBuilder()
					.setId(NetworkId.VOR)
					.setDescription(R.string.np_desc_vor)
					.setLogo(R.drawable.network_vor_logo)
					.setRegion(Region.AUSTRIA)
					.build(),
			new TransportNetworkBuilder()
					.setId(NetworkId.LINZ)
					.setName(R.string.np_name_linz)
					.setDescription(R.string.np_desc_linz)
					.setLogo(R.drawable.network_linz_logo)
					.setRegion(Region.AUSTRIA)
					.build(),
			new TransportNetworkBuilder()
					.setId(NetworkId.VVT)
					.setDescription(R.string.np_desc_vvt)
					.setLogo(R.drawable.network_vvt_logo)
					.setRegion(Region.AUSTRIA)
					.build(),
			new TransportNetworkBuilder()
					.setId(NetworkId.IVB)
					.setDescription(R.string.np_desc_ivb)
					.setLogo(R.drawable.network_ivb_logo)
					.setRegion(Region.AUSTRIA)
					.build(),
			new TransportNetworkBuilder()
					.setId(NetworkId.STV)
					.setName(R.string.np_name_stv)
					.setDescription(R.string.np_desc_stv)
					.setLogo(R.drawable.network_stv_logo)
					.setRegion(Region.AUSTRIA)
					.build(),
			new TransportNetworkBuilder()
					.setId(NetworkId.WIEN)
					.setName(R.string.np_name_wien)
					.setDescription(R.string.np_desc_wien)
					.setLogo(R.drawable.network_wien_logo)
					.setRegion(Region.AUSTRIA)
					.build(),
			new TransportNetworkBuilder()
					.setId(NetworkId.VMOBIL)
					.setName(R.string.np_name_vmobil)
					.setDescription(R.string.np_desc_vmobil)
					.setLogo(R.drawable.network_vmobil_logo)
					.setRegion(Region.AUSTRIA)
					.build(),

			// Liechtenstein

			new TransportNetworkBuilder()
					.setId(NetworkId.VMOBIL)
					.setName(R.string.np_name_vmobil)
					.setDescription(R.string.np_desc_vmobil)
					.setLogo(R.drawable.network_vmobil_logo)
					.setRegion(Region.LIECHTENSTEIN)
					.build(),

			// Switzerland

			new TransportNetworkBuilder()
					.setId(NetworkId.SBB)
					.setName(R.string.np_name_sbb)
					.setDescription(R.string.np_desc_sbb)
					.setLogo(R.drawable.network_sbb_logo)
					.setRegion(Region.SWITZERLAND)
					.build(),
			new TransportNetworkBuilder()
					.setId(NetworkId.VBL)
					.setDescription(R.string.np_desc_vbl)
					.setLogo(R.drawable.network_vbl_logo)
					.setRegion(Region.SWITZERLAND)
					.build(),
			new TransportNetworkBuilder()
					.setId(NetworkId.ZVV)
					.setDescription(R.string.np_desc_zvv)
					.setLogo(R.drawable.network_zvv_logo)
					.setRegion(Region.SWITZERLAND)
					.build(),

			// Belgium

			new TransportNetworkBuilder()
					.setId(NetworkId.SNCB)
					.setName(R.string.np_region_belgium)
					.setDescription(R.string.np_desc_sncb)
					.setAgencies(R.string.np_desc_sncb_networks)
					.setLogo(R.drawable.network_sncb_logo)
					.setRegion(Region.BELGIUM)
					.build(),

			// Luxembourg

			new TransportNetworkBuilder()
					.setId(NetworkId.LU)
					.setName(R.string.np_name_lu)
					.setDescription(R.string.np_desc_lu)
					.setAgencies(R.string.np_desc_lu_networks)
					.setRegion(Region.LUXEMBOURG)
					.build(),

			// Netherlands

			new TransportNetworkBuilder()
					.setId(NetworkId.NS)
					.setDescription(R.string.np_desc_ns)
					.setLogo(R.drawable.network_ns_logo)
					.setRegion(Region.NETHERLANDS)
					.setStatus(BETA)
					.build(),

			new TransportNetworkBuilder()
					.setId(NetworkId.NEGENTWEE)
					.setName(R.string.np_name_negentwee)
					.setDescription(R.string.np_desc_negentwee)
					.setLogo(R.drawable.network_negentwee_logo)
					.setRegion(Region.NETHERLANDS)
					.setStatus(ALPHA)
					.build(),

			// Denmark

			new TransportNetworkBuilder()
					.setId(NetworkId.DSB)
					.setDescription(R.string.np_desc_dsb)
					.setLogo(R.drawable.network_dsb_logo)
					.setRegion(Region.DENMARK)
					.build(),

			// Sweden

			new TransportNetworkBuilder()
					.setId(NetworkId.SE)
					.setDescription(R.string.np_desc_se)
					.setLogo(R.drawable.network_se_logo)
					.setRegion(Region.SWEDEN)
					.build(),

			// Norway

			new TransportNetworkBuilder()
					.setId(NetworkId.NRI)
					.setDescription(R.string.np_desc_nri)
					.setLogo(R.drawable.network_nri_logo)
					.setRegion(Region.NORWAY)
					.build(),

			// Finland

			new TransportNetworkBuilder()
					.setId(NetworkId.HSL)
					.setDescription(R.string.np_desc_hsl)
					.setRegion(Region.FINLAND)
					.setLogo(R.drawable.network_hsl_logo)
					.setStatus(BETA)
					.build(),

			// Great Britain

			new TransportNetworkBuilder()
					.setId(NetworkId.TLEM)
					.setDescription(R.string.np_desc_tlem)
					.setRegion(Region.GREAT_BRITAIN)
					.build(),
			new TransportNetworkBuilder()
					.setId(NetworkId.MERSEY)
					.setName(R.string.np_name_mersey)
					.setDescription(R.string.np_desc_mersey)
					.setRegion(Region.GREAT_BRITAIN)
					.setLogo(R.drawable.network_mersey_logo)
					.build(),

			// Ireland

			new TransportNetworkBuilder()
					.setId(NetworkId.TFI)
					.setDescription(R.string.np_desc_tfi)
					.setRegion(Region.IRELAND)
					.setLogo(R.drawable.network_tfi_logo)
					.build(),

			// Italy

			new TransportNetworkBuilder()
					.setId(NetworkId.IT)
					.setName(R.string.np_name_it)
					.setDescription(R.string.np_desc_it)
					.setAgencies(R.string.np_desc_it_networks)
					.setRegion(Region.ITALY)
					.setLogo(R.drawable.network_it_logo)
					.setStatus(BETA)
					.setGoodLineNames(true)
					.build(),

			// Poland

			new TransportNetworkBuilder()
					.setId(NetworkId.PL)
					.setName(R.string.np_name_pl)
					.setDescription(R.string.np_desc_pl)
					.setRegion(Region.POLAND)
					.setLogo(R.drawable.network_pl_logo)
					.build(),

			// United Arabian Emirates

			new TransportNetworkBuilder()
					.setId(NetworkId.DUB)
					.setName(R.string.np_name_dub)
					.setDescription(R.string.np_desc_dub)
					.setRegion(Region.UAE)
					.setStatus(BETA)
					.build(),

			// United States of America

			new TransportNetworkBuilder()
					.setId(NetworkId.RTACHICAGO)
					.setName(R.string.np_name_rtachicago)
					.setDescription(R.string.np_desc_rtachicago)
					.setAgencies(R.string.np_desc_rtachicago_networks)
					.setRegion(Region.USA)
					.setLogo(R.drawable.network_rtachicago_logo)
					.setStatus(BETA)
					.build(),
			new TransportNetworkBuilder()
					.setId(NetworkId.OREGON)
					.setName(R.string.np_name_oregon)
					.setDescription(R.string.np_desc_oregon)
					.setRegion(Region.USA)
					.setLogo(R.drawable.network_oregon_logo)
					.setStatus(ALPHA)
					.build(),

			// Australia

			new TransportNetworkBuilder()
					.setId(NetworkId.AUSTRALIA)
					.setName(R.string.np_name_australia)
					.setDescription(R.string.np_desc_australia)
					.setAgencies(R.string.np_desc_australia_networks)
					.setRegion(Region.AUSTRALIA)
					.setLogo(R.drawable.network_aus_logo)
					.setStatus(BETA)
					.build(),

			new TransportNetworkBuilder()
					.setId(NetworkId.SYDNEY)
					.setName(R.string.np_name_sydney)
					.setDescription(R.string.np_desc_sydney)
					.setRegion(Region.AUSTRALIA)
					.setLogo(R.drawable.network_sydney_logo)
					.build(),

			// France

			new TransportNetworkBuilder()
					.setId(NetworkId.PARIS)
					.setName(R.string.np_name_paris)
					.setDescription(R.string.np_desc_paris)
					.setAgencies(R.string.np_desc_paris_networks)
					.setRegion(Region.FRANCE)
					.setLogo(R.drawable.network_paris_logo)
					.setStatus(BETA)
					.build(),
			new TransportNetworkBuilder()
					.setId(NetworkId.FRANCESOUTHWEST)
					.setName(R.string.np_name_frenchsouthwest)
					.setDescription(R.string.np_desc_frenchsouthwest)
					.setAgencies(R.string.np_desc_frenchsouthwest_networks)
					.setRegion(Region.FRANCE)
					.setLogo(R.drawable.network_francesouthwest_logo)
					.setStatus(BETA)
					.setGoodLineNames(true)
					.build(),
			new TransportNetworkBuilder()
					.setId(NetworkId.FRANCENORTHEAST)
					.setName(R.string.np_name_francenortheast)
					.setDescription(R.string.np_desc_francenortheast)
					.setAgencies(R.string.np_desc_francenortheast_networks)
					.setRegion(Region.FRANCE)
					.setLogo(R.drawable.network_francenortheast_logo)
					.setStatus(ALPHA)
					.setGoodLineNames(true)
					.build(),
			new TransportNetworkBuilder()
					.setId(NetworkId.FRANCENORTHWEST)
					.setName(R.string.np_name_francenorthwest)
					.setDescription(R.string.np_desc_francenorthwest)
					.setAgencies(R.string.np_desc_francenorthwest_networks)
					.setRegion(Region.FRANCE)
					.setLogo(R.drawable.network_francenorthwest_logo)
					.setStatus(ALPHA)
					.build(),
			new TransportNetworkBuilder()
					.setId(NetworkId.FRANCESOUTHEAST)
					.setName(R.string.np_name_frenchsoutheast)
					.setDescription(R.string.np_desc_frenchsoutheast)
					.setAgencies(R.string.np_desc_frenchsoutheast_networks)
					.setRegion(Region.FRANCE)
					.setLogo(R.drawable.network_francesoutheast_logo)
					.setStatus(BETA)
					.setGoodLineNames(true)
					.build(),

			// New Zealand

			new TransportNetworkBuilder()
					.setId(NetworkId.NZ)
					.setName(R.string.np_name_nz)
					.setDescription(R.string.np_desc_nz)
					.setAgencies(R.string.np_desc_nz_networks)
					.setRegion(Region.NEW_ZEALAND)
					.setLogo(R.drawable.network_nz_logo)
					.setStatus(BETA)
					.build(),

			// Spain

			new TransportNetworkBuilder()
					.setId(NetworkId.SPAIN)
					.setName(R.string.np_name_spain)
					.setDescription(R.string.np_desc_spain)
					.setAgencies(R.string.np_desc_spain_networks)
					.setRegion(Region.SPAIN)
					.setLogo(R.drawable.network_spain_logo)
					.setStatus(BETA)
					.build(),

			// Brazil

			new TransportNetworkBuilder()
					.setId(NetworkId.BR)
					.setName(R.string.np_name_br)
					.setDescription(R.string.np_desc_br)
					.setAgencies(R.string.np_desc_br_networks)
					.setRegion(Region.BRAZIL)
					.setLogo(R.drawable.network_br_logo)
					.setStatus(ALPHA)
					.setGoodLineNames(true)
					.build(),
			new TransportNetworkBuilder()
					.setId(NetworkId.BRFLORIPA)
					.setName(R.string.np_name_br_floripa)
					.setDescription(R.string.np_desc_br_floripa)
					.setAgencies(R.string.np_desc_br_floripa_networks)
					.setRegion(Region.BRAZIL)
					.setLogo(R.drawable.network_brfloripa_logo)
					.setStatus(ALPHA)
					.setGoodLineNames(true)
					.build(),

			// Canada

			new TransportNetworkBuilder()
					.setId(NetworkId.ONTARIO)
					.setName(R.string.np_name_ontario)
					.setDescription(R.string.np_desc_ontario)
					.setAgencies(R.string.np_desc_ontario_networks)
					.setRegion(Region.CANADA)
					.setLogo(R.drawable.network_ontario_logo)
					.setStatus(BETA)
					.setGoodLineNames(true)
					.build(),
			new TransportNetworkBuilder()
					.setId(NetworkId.QUEBEC)
					.setName(R.string.np_name_quebec)
					.setDescription(R.string.np_desc_quebec)
					.setAgencies(R.string.np_desc_quebec_networks)
					.setRegion(Region.CANADA)
					.setLogo(R.drawable.network_quebec_logo)
					.setStatus(ALPHA)
					.setGoodLineNames(true)
					.build(),
	};

}
