/*    Transportr
 *    Copyright (C) 2013 - 2016 Torsten Grote
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

package de.grobox.liberario;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.schildbach.pte.NetworkId;

import static de.grobox.liberario.TransportNetwork.Status.ALPHA;
import static de.grobox.liberario.TransportNetwork.Status.BETA;

public class TransportNetworks {

	private List<TransportNetwork> networks;
	private HashMap<String, TransportNetwork> networks_by_id;
	private HashMap<String, List<TransportNetwork>> networks_by_region;
	private Context context;

	TransportNetworks(Context context) {
		this.context = context;
		this.networks = populateNetworks();
	}

	private List<TransportNetwork> populateNetworks() {
		List<TransportNetwork> list = new ArrayList<>();
		String region;

		// Europe

		list.add(new TransportNetwork(context, NetworkId.RT)
				         .setName(getString(R.string.np_name_rt))
				         .setDescription(description(getString(R.string.np_desc_rt), getString(R.string.np_desc_rt_networks)))
				         .setRegion(region(getString(R.string.np_region_europe), "\uD83C\uDDEA\uD83C\uDDFA"))
		);

		// Germany
		region = region(getString(R.string.np_region_germany), "\uD83C\uDDE9\uD83C\uDDEA");

		list.add(new TransportNetwork(context, NetworkId.DB)
				         .setName(getString(R.string.np_name_db))
				         .setDescription(getString(R.string.np_desc_db))
				         .setRegion(region)
		);

		list.add(new TransportNetwork(context, NetworkId.BVG)
				         .setDescription(getString(R.string.np_desc_bvg))
				         .setRegion(region)
		);

		list.add(new TransportNetwork(context, NetworkId.VBB)
				         .setDescription(getString(R.string.np_desc_vbb))
				         .setRegion(region)
		);

		list.add(new TransportNetwork(context, NetworkId.BAYERN)
				         .setName(getString(R.string.np_name_bayern))
				         .setDescription(getString(R.string.np_desc_bayern))
				         .setRegion(region)
		);

		list.add(new TransportNetwork(context, NetworkId.AVV)
				         .setDescription(getString(R.string.np_desc_avv))
				         .setRegion(region)
						 .setStatus(BETA)
		);

		list.add(new TransportNetwork(context, NetworkId.MVV)
				         .setDescription(getString(R.string.np_desc_mvv))
				         .setRegion(region)
		);

		list.add(new TransportNetwork(context, NetworkId.INVG)
				         .setDescription(getString(R.string.np_desc_invg))
				         .setRegion(region)
						 .setStatus(BETA)
		);

		list.add(new TransportNetwork(context, NetworkId.VGN)
				         .setDescription(description(getString(R.string.np_desc_vgn), getString(R.string.np_desc_vgn_networks)))
				         .setRegion(region)
				         .setStatus(BETA)
		);

		list.add(new TransportNetwork(context, NetworkId.VVM)
				         .setName(getString(R.string.np_name_vvm))
				         .setDescription(getString(R.string.np_desc_vvm))
				         .setRegion(region)
		);

		list.add(new TransportNetwork(context, NetworkId.VMV)
				         .setDescription(getString(R.string.np_desc_vmv))
				         .setRegion(region)
		);

/*		list.add(new TransportNetwork(context, NetworkId.HVV)
				         .setDescription(getString(R.string.np_desc_hvv))
				         .setRegion(region)
		);

		list.add(new TransportNetwork(context, NetworkId.SH)
				         .setDescription(getString(R.string.np_desc_sh))
				         .setRegion(region)
		);
*/
		list.add(new TransportNetwork(context, NetworkId.GVH)
				         .setDescription(getString(R.string.np_desc_gvh))
				         .setRegion(region)
		);

		list.add(new TransportNetwork(context, NetworkId.BSVAG)
				         .setName(getString(R.string.np_name_bsvag))
				         .setDescription(getString(R.string.np_desc_bsvag))
				         .setRegion(region)
		);

		// No secret
//		list.add(new TransportNetwork(context, NetworkId.VBN)
//				         .setDescription(getString(R.string.np_desc_vbn))
//				         .setRegion(region)
//		);

		list.add(new TransportNetwork(context, NetworkId.VVO)
				         .setDescription(getString(R.string.np_desc_vvo))
				         .setRegion(region)
		);

		list.add(new TransportNetwork(context, NetworkId.VMS)
				         .setDescription(getString(R.string.np_desc_vms))
				         .setRegion(region)
				         .setStatus(BETA)
		);

		list.add(new TransportNetwork(context, NetworkId.NASA)
				         .setName(getString(R.string.np_name_nasa))
				         .setDescription(getString(R.string.np_desc_nasa))
				         .setRegion(region)
				         .setStatus(BETA)
		);

		list.add(new TransportNetwork(context, NetworkId.VRR)
				         .setDescription(getString(R.string.np_desc_vrr))
				         .setRegion(region)
		);

		list.add(new TransportNetwork(context, NetworkId.MVG)
				         .setDescription(getString(R.string.np_desc_mvg))
				         .setRegion(region)
		);

		list.add(new TransportNetwork(context, NetworkId.NVV)
				         .setName(getString(R.string.np_name_nvv))
				         .setDescription(getString(R.string.np_desc_nvv))
				         .setRegion(region)
		);

		list.add(new TransportNetwork(context, NetworkId.VRN)
				         .setDescription(getString(R.string.np_desc_vrn))
				         .setRegion(region)
		);

		list.add(new TransportNetwork(context, NetworkId.VVS)
				         .setDescription(getString(R.string.np_desc_vvs))
				         .setRegion(region)
		);

		list.add(new TransportNetwork(context, NetworkId.DING)
				         .setDescription(getString(R.string.np_desc_ding))
				         .setRegion(region)
		);

		list.add(new TransportNetwork(context, NetworkId.KVV)
				         .setDescription(getString(R.string.np_desc_kvv))
				         .setRegion(region)
		);

		list.add(new TransportNetwork(context, NetworkId.VAGFR)
				         .setName(getString(R.string.np_name_vagfr))
				         .setDescription(getString(R.string.np_desc_vagfr))
				         .setRegion(region)
		);

		list.add(new TransportNetwork(context, NetworkId.NVBW)
				         .setDescription(getString(R.string.np_desc_nvbw))
				         .setRegion(region)
		);

		list.add(new TransportNetwork(context, NetworkId.VVV)
				         .setDescription(getString(R.string.np_desc_vvv))
				         .setRegion(region)
		);

		list.add(new TransportNetwork(context, NetworkId.VGS)
				         .setDescription(getString(R.string.np_desc_vgs))
				         .setRegion(region)
		);

		list.add(new TransportNetwork(context, NetworkId.VRS)
				         .setDescription(getString(R.string.np_desc_vrs))
				         .setRegion(region)
		);

		// Austria
		region = region(getString(R.string.np_region_austria), "\uD83C\uDDE6\uD83C\uDDF9");

		list.add(new TransportNetwork(context, NetworkId.OEBB)
				         .setName(getString(R.string.np_name_oebb))
				         .setDescription(getString(R.string.np_desc_oebb))
				         .setRegion(region)
		);

		list.add(new TransportNetwork(context, NetworkId.VOR)
				         .setDescription(getString(R.string.np_desc_vor))
				         .setRegion(region)
		);

		list.add(new TransportNetwork(context, NetworkId.LINZ)
				         .setName(getString(R.string.np_name_linz))
				         .setDescription(getString(R.string.np_desc_linz))
				         .setRegion(region)
		);

		list.add(new TransportNetwork(context, NetworkId.VVT)
				         .setDescription(getString(R.string.np_desc_vvt))
				         .setRegion(region)
		);

		list.add(new TransportNetwork(context, NetworkId.IVB)
				         .setDescription(getString(R.string.np_desc_ivb))
				         .setRegion(region)
		);

		list.add(new TransportNetwork(context, NetworkId.STV)
				         .setName(getString(R.string.np_name_stv))
				         .setDescription(getString(R.string.np_desc_stv))
				         .setRegion(region)
		);

		list.add(new TransportNetwork(context, NetworkId.WIEN)
				         .setName(getString(R.string.np_name_wien))
				         .setDescription(getString(R.string.np_desc_wien))
				         .setRegion(region)
		);

		// Liechtenstein
		region = region(getString(R.string.np_region_liechtenstein), "\uD83C\uDDF1\uD83C\uDDEE");

		list.add(new TransportNetwork(context, NetworkId.VAO)
				         .setDescription(getString(R.string.np_desc_vmobil))
				         .setRegion(region)
		);

		// Switzerland
		region = region(getString(R.string.np_region_switzerland), "\uD83C\uDDE8\uD83C\uDDED");

		list.add(new TransportNetwork(context, NetworkId.SBB)
				         .setName(getString(R.string.np_name_sbb))
				         .setDescription(getString(R.string.np_desc_sbb))
				         .setRegion(region)
		);

/*		list.add(new TransportNetwork(context, NetworkId.BVB)
				         .setRegion(region)
		);
*/
		list.add(new TransportNetwork(context, NetworkId.VBL)
				         .setDescription(getString(R.string.np_desc_vbl))
				         .setRegion(region)
		);

		list.add(new TransportNetwork(context, NetworkId.ZVV)
				         .setDescription(getString(R.string.np_desc_zvv))
				         .setRegion(region)
		);

		// Belgium
		region = region(getString(R.string.np_region_belgium), "\uD83C\uDDE7\uD83C\uDDEA");

		list.add(new TransportNetwork(context, NetworkId.SNCB)
				         .setDescription(getString(R.string.np_desc_sncb))
				         .setRegion(region)
		);

		// Luxembourg
		region = region(getString(R.string.np_region_luxembourg), "\uD83C\uDDF1\uD83C\uDDFA");

		list.add(new TransportNetwork(context, NetworkId.LU)
				         .setName(getString(R.string.np_name_lu))
				         .setDescription(description(getString(R.string.np_desc_lu), getString(R.string.np_desc_lu_networks)))
				         .setRegion(region)
		);

		// Netherlands
		region = region(getString(R.string.np_region_netherlands), "\uD83C\uDDF3\uD83C\uDDF1");

		list.add(new TransportNetwork(context, NetworkId.NS)
				         .setDescription(getString(R.string.np_desc_ns))
				         .setRegion(region)
				         .setStatus(BETA)
		);

		// Denmark
		region = region(getString(R.string.np_region_denmark), "\uD83C\uDDE9\uD83C\uDDF0");

		list.add(new TransportNetwork(context, NetworkId.DSB)
				         .setDescription(getString(R.string.np_desc_dsb))
				         .setRegion(region)
		);

		// Sweden
		region = region(getString(R.string.np_region_sweden), "\uD83C\uDDF8\uD83C\uDDEA");

		list.add(new TransportNetwork(context, NetworkId.SE)
				         .setDescription(getString(R.string.np_desc_se))
				         .setRegion(region)
		);

		// See https://github.com/grote/Transportr/issues/175
/*		list.add(new TransportNetwork(context, NetworkId.STOCKHOLM)
				         .setName(getString(R.string.np_name_stockholm))
				         .setDescription(getString(R.string.np_desc_stockholm))
				         .setRegion(region)
				         .setStatus(TransportNetwork.Status.BETA)
		);
*/
		// Norway
		region = region(getString(R.string.np_region_norway), "\uD83C\uDDF3\uD83C\uDDF4");

		list.add(new TransportNetwork(context, NetworkId.NRI)
				         .setDescription(getString(R.string.np_desc_nri))
				         .setRegion(region)
		);

		// Finland
		region = region(getString(R.string.np_region_finland), "\uD83C\uDDEB\uD83C\uDDEE");

		list.add(new TransportNetwork(context, NetworkId.HSL)
				         .setDescription(getString(R.string.np_desc_hsl))
				         .setRegion(region)
				         .setStatus(BETA)
		);

		// Great Britain
		region = region(getString(R.string.np_region_gb), "\uD83C\uDDEC\uD83C\uDDE7");

		list.add(new TransportNetwork(context, NetworkId.TLEM)
				         .setDescription(getString(R.string.np_desc_tlem))
				         .setRegion(region)
		);

		list.add(new TransportNetwork(context, NetworkId.MERSEY)
				         .setDescription(getString(R.string.np_desc_mersey))
				         .setRegion(region)
		);

		// Ireland
		region = region(getString(R.string.np_region_ireland), "\uD83C\uDDEE\uD83C\uDDEA");

		list.add(new TransportNetwork(context, NetworkId.TFI)
				         .setDescription(getString(R.string.np_desc_tfi))
				         .setRegion(region)
		);

/*		list.add(new TransportNetwork(context, NetworkId.EIREANN)
				         .setDescription("Bus Éireann")
				         .setRegion(region)
		);
*/
		// Italy
		region = region(getString(R.string.np_region_italy), "\uD83C\uDDEE\uD83C\uDDF9");

		list.add(new TransportNetwork(context, NetworkId.IT)
				.setName(getString(R.string.np_name_it))
				.setDescription(description(getString(R.string.np_desc_it), getString(R.string.np_desc_it_networks)))
				.setRegion(region)
				.setStatus(BETA)
				.setGoodLineNames(true)
		);

		list.add(new TransportNetwork(context, NetworkId.ATC)
				         .setDescription(getString(R.string.np_desc_atc))
				         .setRegion(region)
				         .setStatus(BETA)
		);

		// Poland
		region = region(getString(R.string.np_region_poland), "\uD83C\uDDF5\uD83C\uDDF1");

		list.add(new TransportNetwork(context, NetworkId.PL)
				         .setName(getString(R.string.np_name_pl))
				         .setDescription(getString(R.string.np_desc_pl))
				         .setRegion(region)
		);

		// United Arabian Emirates
		region = region(getString(R.string.np_region_uae), "\uD83C\uDDE6\uD83C\uDDEA");

		list.add(new TransportNetwork(context, NetworkId.DUB)
				         .setName(getString(R.string.np_name_dub))
				         .setDescription(getString(R.string.np_desc_dub))
				         .setRegion(region)
				         .setStatus(BETA)
		);

		// United States of America
		region = region(getString(R.string.np_region_usa), "\uD83C\uDDFA\uD83C\uDDF8");

		list.add(new TransportNetwork(context, NetworkId.SF)
				         .setName(getString(R.string.np_name_sf))
				         .setDescription(getString(R.string.np_desc_sf))
				         .setRegion(region)
		);

		list.add(new TransportNetwork(context, NetworkId.SEPTA)
				         .setDescription(getString(R.string.np_desc_septa))
				         .setRegion(region)
				         .setStatus(BETA)
		);

		list.add(new TransportNetwork(context, NetworkId.RTACHICAGO)
				.setName(getString(R.string.np_name_rtachicago))
				.setDescription(description(getString(R.string.np_desc_rtachicago), getString(R.string.np_desc_rtachicago_networks)))
				.setRegion(region)
				.setStatus(BETA)
		);

/*		list.add(new TransportNetwork(context, NetworkId.USNY)
				         .setName(getString(R.string.np_name_usny))
				         .setDescription(getString(R.string.np_desc_usny))
				         .setRegion(region)
				         .setStatus(TransportNetwork.Status.BETA)
		);
*/
		// Australia
		region = region(getString(R.string.np_region_australia), "\uD83C\uDDE6\uD83C\uDDFA");

		list.add(new TransportNetwork(context, NetworkId.SYDNEY)
				         .setName(getString(R.string.np_name_sydney))
				         .setDescription(getString(R.string.np_desc_sydney))
				         .setRegion(region)
		);

		list.add(new TransportNetwork(context, NetworkId.MET)
				         .setName(getString(R.string.np_name_met))
				         .setDescription(getString(R.string.np_desc_met))
				         .setRegion(region)
		);

/*		// Israel
		region = region(getString(R.string.np_region_israel), "\uD83C\uDDEE\uD83C\uDDF1");

		list.add(new TransportNetwork(context, NetworkId.JET)
				         .setDescription(getString(R.string.np_desc_jet))
				         .setRegion(region)
		);
*/
		// France
		region = region(getString(R.string.np_region_france), "\uD83C\uDDEB\uD83C\uDDF7");

		list.add(new TransportNetwork(context, NetworkId.PARIS)
				         .setName(getString(R.string.np_name_paris))
				         .setDescription(description(getString(R.string.np_desc_paris), getString(R.string.np_desc_paris_networks)))
				         .setRegion(region)
				         .setStatus(BETA)
		);

		list.add(new TransportNetwork(context, NetworkId.PACA)
				         .setDescription(getString(R.string.np_desc_paca))
				         .setRegion(region)
				         .setStatus(BETA)
		);

		list.add(new TransportNetwork(context, NetworkId.FRANCESOUTHWEST)
				.setName(getString(R.string.np_name_frenchsouthwest))
				.setDescription(description(getString(R.string.np_desc_frenchsouthwest), getString(R.string.np_desc_frenchsouthwest_networks)))
				.setRegion(region)
				.setStatus(BETA)
				.setGoodLineNames(true)
		);

		list.add(new TransportNetwork(context, NetworkId.FRANCENORTHEAST)
				.setName(getString(R.string.np_name_francenortheast))
				.setDescription(description(getString(R.string.np_desc_francenortheast), getString(R.string.np_desc_francenortheast_networks)))
				.setRegion(region)
				.setStatus(ALPHA)
				.setGoodLineNames(true)
		);

		list.add(new TransportNetwork(context, NetworkId.FRANCENORTHWEST)
				.setName(getString(R.string.np_name_francenorthwest))
				.setDescription(description(getString(R.string.np_desc_francenorthwest), getString(R.string.np_desc_francenorthwest_networks)))
				.setRegion(region)
				.setStatus(ALPHA)
		);

		// New Zealand
		region = region(getString(R.string.np_region_nz), "\uD83C\uDDF3\uD83C\uDDFF");

		list.add(new TransportNetwork(context, NetworkId.NZ)
				         .setName(getString(R.string.np_name_nz))
				         .setDescription(description(getString(R.string.np_desc_nz), getString(R.string.np_desc_nz_networks)))
				         .setRegion(region)
				         .setStatus(BETA)
		);

		// Spain
		region = region(getString(R.string.np_region_spain), "\uD83C\uDDEA\uD83C\uDDF8");

		list.add(new TransportNetwork(context, NetworkId.SPAIN)
				         .setName(getString(R.string.np_name_spain))
				         .setDescription(description(getString(R.string.np_desc_spain), getString(R.string.np_desc_spain_networks)))
				         .setRegion(region)
				         .setStatus(BETA)
		);

		// Brazil
		region = region(getString(R.string.np_region_br), "\uD83C\uDDE7\uD83C\uDDF7");

		list.add(new TransportNetwork(context, NetworkId.BR)
				.setName(getString(R.string.np_name_br))
				.setDescription(description(getString(R.string.np_desc_br), getString(R.string.np_desc_br_networks)))
				.setRegion(region)
				.setStatus(TransportNetwork.Status.ALPHA)
				.setGoodLineNames(true)
		);
		list.add(new TransportNetwork(context, NetworkId.BRFLORIPA)
				.setName(getString(R.string.np_name_br_floripa))
				.setDescription(getString(R.string.np_desc_br_floripa))
				.setRegion(region)
				.setStatus(TransportNetwork.Status.ALPHA)
				.setGoodLineNames(true)
		);

		// Canada
		region = region(getString(R.string.np_region_canada), "\uD83C\uDDE8\uD83C\uDDE6");

		list.add(new TransportNetwork(context, NetworkId.ONTARIO)
				.setName(getString(R.string.np_name_ontario))
				.setDescription(description(getString(R.string.np_desc_ontario), getString(R.string.np_desc_ontario_networks)))
				.setRegion(region)
				.setStatus(BETA)
				.setGoodLineNames(true)
		);
		list.add(new TransportNetwork(context, NetworkId.QUEBEC)
				.setName(getString(R.string.np_name_quebec))
				.setDescription(description(getString(R.string.np_desc_quebec), getString(R.string.np_desc_quebec_networks)))
				.setRegion(region)
				.setStatus(TransportNetwork.Status.ALPHA)
				.setGoodLineNames(true)
		);

		return list;
	}

	public List<TransportNetwork> getList() {
		return networks;
	}

	private HashMap<String, TransportNetwork> getHashMapByStringId() {
		if(networks_by_id == null) {
			networks_by_id = new HashMap<>();

			for(final TransportNetwork network : networks) {
				networks_by_id.put(network.getIdString(), network);
			}
		}
		return networks_by_id;
	}

	public HashMap<String, List<TransportNetwork>> getHashMapByRegion() {
		if(networks_by_region == null) {
			networks_by_region = new HashMap<>();

			for(final TransportNetwork network : networks) {
				if(networks_by_region.containsKey(network.getRegion())) {
					networks_by_region.get(network.getRegion()).add(network);
				} else {
					List<TransportNetwork> list = new ArrayList<>(1);
					list.add(network);

					networks_by_region.put(network.getRegion(), list);
				}
			}
		}
		return networks_by_region;
	}

	TransportNetwork getTransportNetwork(String idString) {
		return getHashMapByStringId().get(idString);
	}

	private String getString(int res) {
		return context.getString(res);
	}

	private String region(String name, String flag) {
		return flag + " " + name;
	}

	private String description(String desc, String networks) {
		return desc + "\n(" + networks + ")";
	}
}
