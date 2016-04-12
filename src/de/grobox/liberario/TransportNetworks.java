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

public class TransportNetworks {

	private List<TransportNetwork> networks;
	private HashMap<String, TransportNetwork> networks_by_id;
	private HashMap<String, List<TransportNetwork>> networks_by_region;
	private Context context;

	public TransportNetworks(Context context) {
		this.context = context;
		this.networks = populateNetworks();
	}

	private List<TransportNetwork> populateNetworks() {
		List<TransportNetwork> list = new ArrayList<>();
		String region;

		// Europe

		list.add(new TransportNetwork(context, NetworkId.RT)
				         .setName(getString(R.string.np_name_rt))
				         .setDescription(getString(R.string.np_desc_rt))
				         .setRegion(getString(R.string.np_region_europe))
		);

		// Germany
		region = getString(R.string.np_region_germany);

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
						 .setStatus(TransportNetwork.Status.BETA)
		);

		list.add(new TransportNetwork(context, NetworkId.MVV)
				         .setDescription(getString(R.string.np_desc_mvv))
				         .setRegion(region)
		);

		list.add(new TransportNetwork(context, NetworkId.INVG)
				         .setDescription(getString(R.string.np_desc_invg))
				         .setRegion(region)
						 .setStatus(TransportNetwork.Status.BETA)
		);

		list.add(new TransportNetwork(context, NetworkId.VGN)
				         .setDescription(getString(R.string.np_desc_vgn))
				         .setRegion(region)
						 .setStatus(TransportNetwork.Status.BETA)
		);

		list.add(new TransportNetwork(context, NetworkId.VVM)
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
				         .setStatus(TransportNetwork.Status.BETA)
		);

		list.add(new TransportNetwork(context, NetworkId.NASA)
				         .setDescription(getString(R.string.np_desc_nasa))
				         .setRegion(region)
				         .setStatus(TransportNetwork.Status.BETA)
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
				         .setName("NVV/RMV")
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
		region = getString(R.string.np_region_austria);

		list.add(new TransportNetwork(context, NetworkId.OEBB)
				         .setDescription(getString(R.string.np_desc_oebb))
				         .setRegion(region)
		);

		list.add(new TransportNetwork(context, NetworkId.VOR)
				         .setDescription(getString(R.string.np_desc_vor))
				         .setRegion(region)
		);

		list.add(new TransportNetwork(context, NetworkId.LINZ)
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
				         .setDescription(getString(R.string.np_desc_stv))
				         .setRegion(region)
		);

		list.add(new TransportNetwork(context, NetworkId.WIEN)
				         .setName(getString(R.string.np_name_wien))
				         .setDescription(getString(R.string.np_desc_wien))
				         .setRegion(region)
		);

		// Liechtenstein
		list.add(new TransportNetwork(context, NetworkId.VAO)
				         .setDescription(getString(R.string.np_desc_vmobil))
				         .setRegion(getString(R.string.np_region_liechtenstein))
		);

		// Switzerland
		region = getString(R.string.np_region_switzerland);

		list.add(new TransportNetwork(context, NetworkId.SBB)
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

		list.add(new TransportNetwork(context, NetworkId.SNCB)
				         .setRegion(getString(R.string.np_region_belgium))
		);

		// Luxembourg

		list.add(new TransportNetwork(context, NetworkId.LU)
				         .setRegion(getString(R.string.np_region_luxembourg))
		);

		// Netherlands

		list.add(new TransportNetwork(context, NetworkId.NS)
				         .setDescription(getString(R.string.np_desc_ns))
				         .setRegion(getString(R.string.np_region_netherlands))
				         .setStatus(TransportNetwork.Status.BETA)
		);

		// Denmark

		list.add(new TransportNetwork(context, NetworkId.DSB)
				         .setDescription(getString(R.string.np_desc_dsb))
				         .setRegion(getString(R.string.np_region_denmark))
		);

		// Sweden
		region = getString(R.string.np_region_sweden);

		list.add(new TransportNetwork(context, NetworkId.SE)
				         .setDescription(getString(R.string.np_desc_se))
				         .setRegion(region)
		);

		list.add(new TransportNetwork(context, NetworkId.STOCKHOLM)
				         .setName(getString(R.string.np_name_stockholm))
				         .setDescription(getString(R.string.np_desc_stockholm))
				         .setRegion(region)
				         .setStatus(TransportNetwork.Status.BETA)
		);

		// Norway

		list.add(new TransportNetwork(context, NetworkId.NRI)
				         .setDescription(getString(R.string.np_desc_nri))
				         .setRegion(getString(R.string.np_region_norway))
		);

		// Finland

		list.add(new TransportNetwork(context, NetworkId.HSL)
				         .setDescription(getString(R.string.np_desc_hsl))
				         .setRegion(getString(R.string.np_region_finland))
				         .setStatus(TransportNetwork.Status.BETA)
		);

		// Great Britain
		region = getString(R.string.np_region_gb);

		list.add(new TransportNetwork(context, NetworkId.TLEM)
				         .setDescription(getString(R.string.np_desc_tlem))
				         .setRegion(region)
		);

		list.add(new TransportNetwork(context, NetworkId.MERSEY)
				         .setDescription(getString(R.string.np_desc_mersey))
				         .setRegion(region)
		);

		// Ireland
		region = getString(R.string.np_region_ireland);

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
		region = getString(R.string.np_region_italy);

		list.add(new TransportNetwork(context, NetworkId.IT)
						.setDescription(getString(R.string.np_desc_it) + "\n(" + getString(R.string.np_desc_it_networks) + ")")
						.setRegion(region)
						.setStatus(TransportNetwork.Status.BETA)
		);

		list.add(new TransportNetwork(context, NetworkId.ATC)
				         .setDescription(getString(R.string.np_desc_atc))
				         .setRegion(region)
				         .setStatus(TransportNetwork.Status.BETA)
		);

		// Poland

		list.add(new TransportNetwork(context, NetworkId.PL)
				         .setDescription(getString(R.string.np_desc_pl))
				         .setRegion(getString(R.string.np_region_poland))
		);

		// United Arabian Emirates

		list.add(new TransportNetwork(context, NetworkId.DUB)
				         .setDescription(getString(R.string.np_desc_dub))
				         .setRegion(getString(R.string.np_region_uae))
				         .setStatus(TransportNetwork.Status.BETA)
		);

		// United States of America
		region = getString(R.string.np_region_usa);

		list.add(new TransportNetwork(context, NetworkId.SF)
				         .setDescription(getString(R.string.np_desc_sf))
				         .setRegion(region)
		);

		list.add(new TransportNetwork(context, NetworkId.SEPTA)
				         .setDescription(getString(R.string.np_desc_septa))
				         .setRegion(region)
				         .setStatus(TransportNetwork.Status.BETA)
		);

/*		list.add(new TransportNetwork(context, NetworkId.USNY)
				         .setName(getString(R.string.np_name_usny))
				         .setDescription(getString(R.string.np_desc_usny))
				         .setRegion(region)
				         .setStatus(TransportNetwork.Status.BETA)
		);
*/
		// Australia
		region = getString(R.string.np_region_australia);

		list.add(new TransportNetwork(context, NetworkId.SYDNEY)
				         .setName(getString(R.string.np_name_sydney))
				         .setDescription(getString(R.string.np_desc_sydney))
				         .setRegion(region)
		);

		list.add(new TransportNetwork(context, NetworkId.MET)
				         .setDescription(getString(R.string.np_desc_met))
				         .setRegion(region)
		);

		// Israel

		list.add(new TransportNetwork(context, NetworkId.JET)
				         .setDescription(getString(R.string.np_desc_jet))
				         .setRegion(getString(R.string.np_region_israel))
		);

		// France
		region = getString(R.string.np_region_france);

		list.add(new TransportNetwork(context, NetworkId.PARIS)
				         .setName(getString(R.string.np_name_paris))
				         .setDescription(getString(R.string.np_desc_paris))
				         .setRegion(region)
				         .setStatus(TransportNetwork.Status.BETA)
		);

		list.add(new TransportNetwork(context, NetworkId.PACA)
				         .setDescription(getString(R.string.np_desc_paca))
				         .setRegion(region)
				         .setStatus(TransportNetwork.Status.BETA)
		);

		list.add(new TransportNetwork(context, NetworkId.FRENCHSOUTHWEST)
						.setName(getString(R.string.np_name_frenchsouthwest))
						.setDescription(getString(R.string.np_desc_frenchsouthwest) + "\n(" + getString(R.string.np_desc_frenchsouthwest_networks) + ")")
						.setRegion(region)
						.setStatus(TransportNetwork.Status.BETA)
		);

		// New Zealand

		list.add(new TransportNetwork(context, NetworkId.NZ)
				         .setDescription(getString(R.string.np_desc_nz))
				         .setRegion(getString(R.string.np_region_nz))
				         .setStatus(TransportNetwork.Status.BETA)
		);

		// Spain

		list.add(new TransportNetwork(context, NetworkId.SPAIN)
				         .setName(getString(R.string.np_name_spain))
				         .setDescription(getString(R.string.np_desc_spain))
				         .setRegion(getString(R.string.np_region_spain))
				         .setStatus(TransportNetwork.Status.BETA)
		);

		// Brazil
		region = getString(R.string.np_region_br);

		list.add(new TransportNetwork(context, NetworkId.BR)
				         .setName(getString(R.string.np_name_br))
				         .setDescription(getString(R.string.np_desc_br))
				         .setRegion(region)
				         .setStatus(TransportNetwork.Status.APLHA)
		);
		list.add(new TransportNetwork(context, NetworkId.BRFLORIPA)
						.setName(context.getString(R.string.np_name_br_floripa))
						.setDescription(context.getString(R.string.np_desc_br_floripa))
						.setRegion(region)
						.setStatus(TransportNetwork.Status.APLHA)
		);

		// Canada
		region = getString(R.string.np_region_canada);

		list.add(new TransportNetwork(context, NetworkId.ONTARIO)
				         .setName(context.getString(R.string.np_name_ontario))
				         .setDescription(context.getString(R.string.np_desc_ontario))
				         .setRegion(region)
				         .setStatus(TransportNetwork.Status.BETA)
		);
		list.add(new TransportNetwork(context, NetworkId.QUEBEC)
				         .setName(context.getString(R.string.np_name_quebec))
				         .setDescription(context.getString(R.string.np_desc_quebec))
				         .setRegion(region)
				         .setStatus(TransportNetwork.Status.APLHA)
		);

		return list;
	}

	public List<TransportNetwork> getList() {
		return networks;
	}

	public HashMap<String, TransportNetwork> getHashMapByStringId() {
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

	public TransportNetwork getTransportNetwork(String idString) {
		return getHashMapByStringId().get(idString);
	}

	private String getString(int res) {
		return context.getString(res);
	}
}
