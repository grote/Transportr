/*
 * Copyright 2013 the original author or authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.grobox.transportr.networks;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.Locale;

import de.schildbach.pte.*;
import okhttp3.HttpUrl;

/**
 * @author Andreas Schildbach
 */
public final class NetworkProviderFactory {
	private static Reference<RtProvider> rtProviderRef;
	private static Reference<BahnProvider> bahnProviderRef;
	private static Reference<BvgProvider> bvgProviderRef;
	private static Reference<VbbProvider> vbbProviderRef;
	private static Reference<NvvProvider> nvvProviderRef;
	private static Reference<BayernProvider> bayernProviderRef;
	private static Reference<MvvProvider> mvvProviderRef;
	private static Reference<InvgProvider> invgProviderRef;
	private static Reference<AvvProvider> avvProviderRef;
	private static Reference<VgnProvider> vgnProviderRef;
	private static Reference<VvmProvider> vvmProviderRef;
	private static Reference<VmvProvider> vmvProviderRef;
	private static Reference<ShProvider> shProviderRef;
	private static Reference<GvhProvider> gvhProviderRef;
	private static Reference<BsvagProvider> bsvagProviderRef;
	private static Reference<VbnProvider> vbnProviderRef;
	private static Reference<NasaProvider> nasaProviderRef;
	private static Reference<VvoProvider> vvoProviderRef;
	private static Reference<VmsProvider> vmsProviderRef;
	private static Reference<VrrProvider> vrrProviderRef;
	private static Reference<VrsProvider> vrsProviderRef;
	private static Reference<MvgProvider> mvgProviderRef;
	private static Reference<VrnProvider> vrnProviderRef;
	private static Reference<VvsProvider> vvsProviderRef;
	private static Reference<DingProvider> dingProviderRef;
	private static Reference<KvvProvider> kvvProviderRef;
	private static Reference<VagfrProvider> vagfrProviderRef;
	private static Reference<NvbwProvider> nvbwProviderRef;
	private static Reference<VvvProvider> vvvProviderRef;
	private static Reference<VmtProvider> vmtProviderRef;
	private static Reference<OebbProvider> oebbProviderRef;
	private static Reference<VorProvider> vorProviderRef;
	private static Reference<LinzProvider> linzProviderRef;
	private static Reference<VvtProvider> vvtProviderRef;
	private static Reference<VaoProvider> vaoProviderRef;
	private static Reference<VmobilProvider> vmobilProviderRef;
	private static Reference<IvbProvider> ivbProviderRef;
	private static Reference<StvProvider> stvProviderRef;
	private static Reference<SbbProvider> sbbProviderRef;
	private static Reference<VblProvider> vblProviderRef;
	private static Reference<ZvvProvider> zvvProviderRef;
	private static Reference<SncbProvider> sncbProviderRef;
	private static Reference<LuProvider> luProviderRef;
	private static Reference<NsProvider> nsProviderRef;
	private static Reference<NegentweeProvider> negentweeProviderRef;
	private static Reference<DsbProvider> dsbProviderRef;
	private static Reference<SeProvider> seProviderRef;
	private static Reference<NriProvider> nriProviderRef;
	private static Reference<HslProvider> hslProviderRef;
	private static Reference<TlemProvider> tlemProviderRef;
	private static Reference<TfiProvider> tfiProviderRef;
	private static Reference<PlProvider> plProviderRef;
	private static Reference<DubProvider> dubProviderRef;
	private static Reference<SydneyProvider> sydneyProviderRef;
	private static Reference<VgsProvider> vgsProviderRef;
	private static Reference<WienProvider> wienProviderRef;
	private static Reference<MerseyProvider> merseyProviderRef;
	private static Reference<ParisProvider> parisProviderRef;
	private static Reference<NzProvider> nzProviderRef;
	private static Reference<SpainProvider> spainProviderRef;
	private static Reference<BrProvider> brProviderRef;
	private static Reference<BrFloripaProvider> brFloripaProviderRef;
	private static Reference<ItalyProvider> italyProviderRef;
	private static Reference<FranceSouthWestProvider> franceSouthWestProviderRef;
	private static Reference<FranceNorthEastProvider> franceNorthEastProviderRef;
	private static Reference<FranceNorthWestProvider> franceNorthWestProviderRef;
	private static Reference<FranceSouthEastProvider> franceSouthEastProviderRef;
	private static Reference<OntarioProvider> ontarioProviderRef;
	private static Reference<QuebecProvider> quebecProviderRef;
	private static Reference<RtaChicagoProvider> rtaChicagoProviderRef;
	private static Reference<CaliforniaProvider> californiaProviderRef;
	private static Reference<OregonProvider> oregonProviderRef;
	private static Reference<AustraliaProvider> australiaProviderRef;
	private static Reference<CostaRicaProvider> costaRicaProviderRef;
	private static Reference<GhanaProvider> ghanaProviderRef;
	private static Reference<NewyorkProvider> newYorkProviderRef;
	private static Reference<NicaraguaProvider> nicaraguaProviderRef;

	private static final String NAVITIA = "87a37b95-913a-4cb4-ba52-eb0bc0b304ca";
	private static final String VAO = "{\"aid\":\"hf7mcf9bv3nv8g5f\",\"pw\":\"87a6f8ZbnBih32\",\"type\":\"USER\",\"user\":\"mobile\"}";

	public static synchronized NetworkProvider provider(final NetworkId networkId) {
		if (networkId.equals(NetworkId.RT)) {
			if (rtProviderRef != null) {
				final RtProvider provider = rtProviderRef.get();
				if (provider != null)
					return provider;
			}

			final RtProvider provider = new RtProvider();
			rtProviderRef = new SoftReference<>(provider);
			return provider;
		} else if (networkId.equals(NetworkId.DB)) {
			if (bahnProviderRef != null) {
				final BahnProvider provider = bahnProviderRef.get();
				if (provider != null)
					return provider;
			}

			final BahnProvider provider = new BahnProvider();
			bahnProviderRef = new SoftReference<>(provider);
			return provider;
		} else if (networkId.equals(NetworkId.BVG)) {
			if (bvgProviderRef != null) {
				final BvgProvider provider = bvgProviderRef.get();
				if (provider != null)
					return provider;
			}

			final BvgProvider provider = new BvgProvider("{\"aid\":\"1Rxs112shyHLatUX4fofnmdxK\",\"type\":\"AID\"}");
			bvgProviderRef = new SoftReference<>(provider);
			return provider;
		} else if (networkId.equals(NetworkId.VBB)) {
			if (vbbProviderRef != null) {
				final VbbProvider provider = vbbProviderRef.get();
				if (provider != null)
					return provider;
			}

			final VbbProvider provider = new VbbProvider();
			vbbProviderRef = new SoftReference<>(provider);
			return provider;
		} else if (networkId.equals(NetworkId.NVV)) {
			if (nvvProviderRef != null) {
				final NvvProvider provider = nvvProviderRef.get();
				if (provider != null)
					return provider;
			}

			final NvvProvider provider = new NvvProvider();
			nvvProviderRef = new SoftReference<>(provider);
			return provider;
		} else if (networkId.equals(NetworkId.BAYERN)) {
			if (bayernProviderRef != null) {
				final BayernProvider provider = bayernProviderRef.get();
				if (provider != null)
					return provider;
			}

			final BayernProvider provider = new BayernProvider();
			bayernProviderRef = new SoftReference<>(provider);
			return provider;
		} else if (networkId.equals(NetworkId.MVV)) {
			if (mvvProviderRef != null) {
				final MvvProvider provider = mvvProviderRef.get();
				if (provider != null)
					return provider;
			}

			final MvvProvider provider = new MvvProvider();
			mvvProviderRef = new SoftReference<>(provider);
			return provider;
		} else if (networkId.equals(NetworkId.INVG)) {
			if (invgProviderRef != null) {
				final InvgProvider provider = invgProviderRef.get();
				if (provider != null)
					return provider;
			}

			final InvgProvider provider = new InvgProvider();
			invgProviderRef = new SoftReference<>(provider);
			return provider;
		} else if (networkId.equals(NetworkId.AVV)) {
			if (avvProviderRef != null) {
				final AvvProvider provider = avvProviderRef.get();
				if (provider != null)
					return provider;
			}

			final AvvProvider provider = new AvvProvider();
			avvProviderRef = new SoftReference<>(provider);
			return provider;
		} else if (networkId.equals(NetworkId.VGN)) {
			if (vgnProviderRef != null) {
				final VgnProvider provider = vgnProviderRef.get();
				if (provider != null)
					return provider;
			}

			final VgnProvider provider = new VgnProvider();
			vgnProviderRef = new SoftReference<>(provider);
			return provider;
		} else if (networkId.equals(NetworkId.VVM)) {
			if (vvmProviderRef != null) {
				final VvmProvider provider = vvmProviderRef.get();
				if (provider != null)
					return provider;
			}

			final VvmProvider provider = new VvmProvider();
			vvmProviderRef = new SoftReference<>(provider);
			return provider;
		} else if (networkId.equals(NetworkId.VMV)) {
			if (vmvProviderRef != null) {
				final VmvProvider provider = vmvProviderRef.get();
				if (provider != null)
					return provider;
			}

			final VmvProvider provider = new VmvProvider();
			vmvProviderRef = new SoftReference<>(provider);
			return provider;
		} else if (networkId.equals(NetworkId.SH)) {
			if (shProviderRef != null) {
				final ShProvider provider = shProviderRef.get();
				if (provider != null)
					return provider;
			}

			// TODO get API authorization
			final ShProvider provider = new ShProvider("");
			shProviderRef = new SoftReference<>(provider);
			return provider;
		} else if (networkId.equals(NetworkId.GVH)) {
			if (gvhProviderRef != null) {
				final GvhProvider provider = gvhProviderRef.get();
				if (provider != null)
					return provider;
			}

			final GvhProvider provider = new GvhProvider();
			gvhProviderRef = new SoftReference<>(provider);
			return provider;
		} else if (networkId.equals(NetworkId.BSVAG)) {
			if (bsvagProviderRef != null) {
				final BsvagProvider provider = bsvagProviderRef.get();
				if (provider != null)
					return provider;
			}

			final BsvagProvider provider = new BsvagProvider();
			bsvagProviderRef = new SoftReference<>(provider);
			return provider;
		} else if (networkId.equals(NetworkId.VBN)) {
			if (vbnProviderRef != null) {
				final VbnProvider provider = vbnProviderRef.get();
				if (provider != null)
					return provider;
			}

			// TODO get API authorization
			final VbnProvider provider = new VbnProvider("");
			vbnProviderRef = new SoftReference<>(provider);
			return provider;
		} else if (networkId.equals(NetworkId.NASA)) {
			if (nasaProviderRef != null) {
				final NasaProvider provider = nasaProviderRef.get();
				if (provider != null)
					return provider;
			}

			final NasaProvider provider = new NasaProvider();
			nasaProviderRef = new SoftReference<>(provider);
			return provider;
		} else if (networkId.equals(NetworkId.VVO)) {
			if (vvoProviderRef != null) {
				final VvoProvider provider = vvoProviderRef.get();
				if (provider != null)
					return provider;
			}

			final VvoProvider provider = new VvoProvider(HttpUrl.parse("http://efaproxy.fahrinfo.uptrade.de/standard/"));
			vvoProviderRef = new SoftReference<>(provider);
			return provider;
		} else if (networkId.equals(NetworkId.VMS)) {
			if (vmsProviderRef != null) {
				final VmsProvider provider = vmsProviderRef.get();
				if (provider != null)
					return provider;
			}

			final VmsProvider provider = new VmsProvider();
			vmsProviderRef = new SoftReference<>(provider);
			return provider;
		} else if (networkId.equals(NetworkId.VRR)) {
			if (vrrProviderRef != null) {
				final VrrProvider provider = vrrProviderRef.get();
				if (provider != null)
					return provider;
			}

			final VrrProvider provider = new VrrProvider();
			vrrProviderRef = new SoftReference<>(provider);
			return provider;
		} else if (networkId.equals(NetworkId.VRS)) {
			if (vrsProviderRef != null) {
				final VrsProvider provider = vrsProviderRef.get();
				if (provider != null)
					return provider;
			}

			final VrsProvider provider = new VrsProvider();
			vrsProviderRef = new SoftReference<>(provider);
			return provider;
		} else if (networkId.equals(NetworkId.MVG)) {
			if (mvgProviderRef != null) {
				final MvgProvider provider = mvgProviderRef.get();
				if (provider != null)
					return provider;
			}

			final MvgProvider provider = new MvgProvider();
			mvgProviderRef = new SoftReference<>(provider);
			return provider;
		} else if (networkId.equals(NetworkId.VRN)) {
			if (vrnProviderRef != null) {
				final VrnProvider provider = vrnProviderRef.get();
				if (provider != null)
					return provider;
			}

			final VrnProvider provider = new VrnProvider();
			vrnProviderRef = new SoftReference<>(provider);
			return provider;
		} else if (networkId.equals(NetworkId.VVS)) {
			if (vvsProviderRef != null) {
				final VvsProvider provider = vvsProviderRef.get();
				if (provider != null)
					return provider;
			}

			final VvsProvider provider = new VvsProvider(HttpUrl.parse("http://www2.vvs.de/oeffi/"));
			vvsProviderRef = new SoftReference<>(provider);
			return provider;
		} else if (networkId.equals(NetworkId.DING)) {
			if (dingProviderRef != null) {
				final DingProvider provider = dingProviderRef.get();
				if (provider != null)
					return provider;
			}

			final DingProvider provider = new DingProvider();
			dingProviderRef = new SoftReference<>(provider);
			return provider;
		} else if (networkId.equals(NetworkId.KVV)) {
			if (kvvProviderRef != null) {
				final KvvProvider provider = kvvProviderRef.get();
				if (provider != null)
					return provider;
			}

			final KvvProvider provider = new KvvProvider(HttpUrl.parse("http://213.144.24.66/oeffi/"));
			kvvProviderRef = new SoftReference<>(provider);
			return provider;
		} else if (networkId.equals(NetworkId.VAGFR)) {
			if (vagfrProviderRef != null) {
				final VagfrProvider provider = vagfrProviderRef.get();
				if (provider != null)
					return provider;
			}

			final VagfrProvider provider = new VagfrProvider();
			vagfrProviderRef = new SoftReference<>(provider);
			return provider;
		} else if (networkId.equals(NetworkId.NVBW)) {
			if (nvbwProviderRef != null) {
				final NvbwProvider provider = nvbwProviderRef.get();
				if (provider != null)
					return provider;
			}

			final NvbwProvider provider = new NvbwProvider();
			nvbwProviderRef = new SoftReference<>(provider);
			return provider;
		} else if (networkId.equals(NetworkId.VVV)) {
			if (vvvProviderRef != null) {
				final VvvProvider provider = vvvProviderRef.get();
				if (provider != null)
					return provider;
			}

			final VvvProvider provider = new VvvProvider();
			vvvProviderRef = new SoftReference<>(provider);
			return provider;
		} else if (networkId.equals(NetworkId.VMT)) {
			if (vmtProviderRef != null) {
				final VmtProvider provider = vmtProviderRef.get();
				if (provider != null)
					return provider;
			}

			final VmtProvider provider = new VmtProvider("{\"aid\":\"vj5d7i3g9m5d7e3\",\"type\":\"AID\"}");
			vmtProviderRef = new SoftReference<>(provider);
			return provider;
		} else if (networkId.equals(NetworkId.OEBB)) {
			if (oebbProviderRef != null) {
				final OebbProvider provider = oebbProviderRef.get();
				if (provider != null)
					return provider;
			}

			final OebbProvider provider = new OebbProvider();
			oebbProviderRef = new SoftReference<>(provider);
			return provider;
		} else if (networkId.equals(NetworkId.VOR)) {
			if (vorProviderRef != null) {
				final VorProvider provider = vorProviderRef.get();
				if (provider != null)
					return provider;
			}

			final VorProvider provider = new VorProvider(VAO);
			vorProviderRef = new SoftReference<>(provider);
			return provider;
		} else if (networkId.equals(NetworkId.LINZ)) {
			if (linzProviderRef != null) {
				final LinzProvider provider = linzProviderRef.get();
				if (provider != null)
					return provider;
			}

			final LinzProvider provider = new LinzProvider();
			linzProviderRef = new SoftReference<>(provider);
			return provider;
		} else if (networkId.equals(NetworkId.VVT)) {
			if (vvtProviderRef != null) {
				final VvtProvider provider = vvtProviderRef.get();
				if (provider != null)
					return provider;
			}

			final VvtProvider provider = new VvtProvider(VAO);
			vvtProviderRef = new SoftReference<>(provider);
			return provider;
		} else if (networkId.equals(NetworkId.VAO)) {
			if (vaoProviderRef != null) {
				final VaoProvider provider = vaoProviderRef.get();
				if (provider != null)
					return provider;
			}

			final VaoProvider provider = new VaoProvider(VAO);
			vaoProviderRef = new SoftReference<>(provider);
			return provider;
		} else if (networkId.equals(NetworkId.VMOBIL)) {
			if (vmobilProviderRef != null) {
				final VmobilProvider provider = vmobilProviderRef.get();
				if (provider != null)
					return provider;
			}

			final VmobilProvider provider = new VmobilProvider(VAO);
			vmobilProviderRef = new SoftReference<>(provider);
			return provider;
		} else if (networkId.equals(NetworkId.IVB)) {
			if (ivbProviderRef != null) {
				final IvbProvider provider = ivbProviderRef.get();
				if (provider != null)
					return provider;
			}

			final IvbProvider provider = new IvbProvider();
			ivbProviderRef = new SoftReference<>(provider);
			return provider;
		} else if (networkId.equals(NetworkId.STV)) {
			if (stvProviderRef != null) {
				final StvProvider provider = stvProviderRef.get();
				if (provider != null)
					return provider;
			}

			final StvProvider provider = new StvProvider();
			stvProviderRef = new SoftReference<>(provider);
			return provider;
		} else if (networkId.equals(NetworkId.SBB)) {
			if (sbbProviderRef != null) {
				final SbbProvider provider = sbbProviderRef.get();
				if (provider != null)
					return provider;
			}

			final SbbProvider provider = new SbbProvider();
			sbbProviderRef = new SoftReference<>(provider);
			return provider;
		} else if (networkId.equals(NetworkId.VBL)) {
			if (vblProviderRef != null) {
				final VblProvider provider = vblProviderRef.get();
				if (provider != null)
					return provider;
			}

			final VblProvider provider = new VblProvider();
			vblProviderRef = new SoftReference<>(provider);
			return provider;
		} else if (networkId.equals(NetworkId.ZVV)) {
			if (zvvProviderRef != null) {
				final ZvvProvider provider = zvvProviderRef.get();
				if (provider != null)
					return provider;
			}

			final ZvvProvider provider = new ZvvProvider();
			zvvProviderRef = new SoftReference<>(provider);
			return provider;
		} else if (networkId.equals(NetworkId.SNCB)) {
			if (sncbProviderRef != null) {
				final SncbProvider provider = sncbProviderRef.get();
				if (provider != null)
					return provider;
			}

			final SncbProvider provider = new SncbProvider();
			sncbProviderRef = new SoftReference<>(provider);
			return provider;
		} else if (networkId.equals(NetworkId.LU)) {
			if (luProviderRef != null) {
				final LuProvider provider = luProviderRef.get();
				if (provider != null)
					return provider;
			}

			final LuProvider provider = new LuProvider();
			luProviderRef = new SoftReference<>(provider);
			return provider;
		} else if (networkId.equals(NetworkId.NS)) {
			if (nsProviderRef != null) {
				final NsProvider provider = nsProviderRef.get();
				if (provider != null)
					return provider;
			}

			final NsProvider provider = new NsProvider();
			nsProviderRef = new SoftReference<>(provider);
			return provider;
		} else if (networkId.equals(NetworkId.NEGENTWEE)) {
			if (negentweeProviderRef != null) {
				final NegentweeProvider provider = negentweeProviderRef.get();
				if (provider != null)
					return provider;
			}

			NegentweeProvider.Language lang;
			if (Locale.getDefault().getLanguage().equals("nl")) {
				lang = NegentweeProvider.Language.NL_NL;
			} else {
				lang = NegentweeProvider.Language.EN_GB;
			}

			final NegentweeProvider provider = new NegentweeProvider(lang);
			negentweeProviderRef = new SoftReference<>(provider);
			return provider;
		} else if (networkId.equals(NetworkId.DSB)) {
			if (dsbProviderRef != null) {
				final DsbProvider provider = dsbProviderRef.get();
				if (provider != null)
					return provider;
			}

			final DsbProvider provider = new DsbProvider();
			dsbProviderRef = new SoftReference<>(provider);
			return provider;
		} else if (networkId.equals(NetworkId.SE)) {
			if (seProviderRef != null) {
				final SeProvider provider = seProviderRef.get();
				if (provider != null)
					return provider;
			}

			final SeProvider provider = new SeProvider();
			seProviderRef = new SoftReference<>(provider);
			return provider;
		} else if (networkId.equals(NetworkId.NRI)) {
			if (nriProviderRef != null) {
				final NriProvider provider = nriProviderRef.get();
				if (provider != null)
					return provider;
			}

			final NriProvider provider = new NriProvider();
			nriProviderRef = new SoftReference<>(provider);
			return provider;
		} else if (networkId.equals(NetworkId.HSL)) {
			if (hslProviderRef != null) {
				final HslProvider provider = hslProviderRef.get();
				if (provider != null)
					return provider;
			}

			final HslProvider provider = new HslProvider("pte_hsl", "Eixaeb9tnohcah7A");
			hslProviderRef = new SoftReference<>(provider);
			return provider;
		} else if (networkId.equals(NetworkId.TLEM)) {
			if (tlemProviderRef != null) {
				final TlemProvider provider = tlemProviderRef.get();
				if (provider != null)
					return provider;
			}

			final TlemProvider provider = new TlemProvider();
			tlemProviderRef = new SoftReference<>(provider);
			return provider;
		} else if (networkId.equals(NetworkId.TFI)) {
			if (tfiProviderRef != null) {
				final TfiProvider provider = tfiProviderRef.get();
				if (provider != null)
					return provider;
			}

			final TfiProvider provider = new TfiProvider();
			tfiProviderRef = new SoftReference<>(provider);
			return provider;
		} else if (networkId.equals(NetworkId.PL)) {
			if (plProviderRef != null) {
				final PlProvider provider = plProviderRef.get();
				if (provider != null)
					return provider;
			}

			final PlProvider provider = new PlProvider();
			plProviderRef = new SoftReference<>(provider);
			return provider;
		} else if (networkId.equals(NetworkId.DUB)) {
			if (dubProviderRef != null) {
				final DubProvider provider = dubProviderRef.get();
				if (provider != null)
					return provider;
			}

			final DubProvider provider = new DubProvider();
			dubProviderRef = new SoftReference<>(provider);
			return provider;
		} else if (networkId.equals(NetworkId.SYDNEY)) {
			if (sydneyProviderRef != null) {
				final SydneyProvider provider = sydneyProviderRef.get();
				if (provider != null)
					return provider;
			}

			final SydneyProvider provider = new SydneyProvider();
			sydneyProviderRef = new SoftReference<>(provider);
			return provider;
		} else if (networkId.equals(NetworkId.VGS)) {
			if (vgsProviderRef != null) {
				final VgsProvider provider = vgsProviderRef.get();
				if (provider != null)
					return provider;
			}

			final VgsProvider provider = new VgsProvider();
			vgsProviderRef = new SoftReference<>(provider);
			return provider;
		} else if (networkId.equals(NetworkId.WIEN)) {
			if (wienProviderRef != null) {
				final WienProvider provider = wienProviderRef.get();
				if (provider != null)
					return provider;
			}

			final WienProvider provider = new WienProvider();
			wienProviderRef = new SoftReference<>(provider);
			return provider;
		} else if (networkId.equals(NetworkId.MERSEY)) {
			if (merseyProviderRef != null) {
				final MerseyProvider provider = merseyProviderRef.get();
				if (provider != null)
					return provider;
			}

			final MerseyProvider provider = new MerseyProvider();
			merseyProviderRef = new SoftReference<>(provider);
			return provider;
		} else if (networkId.equals(NetworkId.PARIS)) {
			if (parisProviderRef != null) {
				final ParisProvider provider = parisProviderRef.get();
				if (provider != null)
					return provider;
			}

			final ParisProvider provider = new ParisProvider(NAVITIA);
			parisProviderRef = new SoftReference<>(provider);
			return provider;
		} else if (networkId.equals(NetworkId.NZ)) {
			if (nzProviderRef != null) {
				final NzProvider provider = nzProviderRef.get();
				if (provider != null)
					return provider;
			}

			final NzProvider provider = new NzProvider(NAVITIA);
			nzProviderRef = new SoftReference<>(provider);
			return provider;
		} else if (networkId.equals(NetworkId.SPAIN)) {
			if (spainProviderRef != null) {
				final SpainProvider provider = spainProviderRef.get();
				if (provider != null)
					return provider;
			}

			final SpainProvider provider = new SpainProvider(NAVITIA);
			spainProviderRef = new SoftReference<>(provider);
			return provider;
		} else if (networkId.equals(NetworkId.BR)) {
			if (brProviderRef != null) {
				final BrProvider provider = brProviderRef.get();
				if (provider != null)
					return provider;
			}

			final BrProvider provider = new BrProvider(NAVITIA);
			brProviderRef = new SoftReference<>(provider);
			return provider;
		} else if (networkId.equals(NetworkId.BRFLORIPA)) {
			if (brFloripaProviderRef != null) {
				final BrFloripaProvider provider = brFloripaProviderRef.get();
				if (provider != null)
					return provider;
			}

			final BrFloripaProvider provider = new BrFloripaProvider(HttpUrl.parse("https://transportr.grobox.de/api/v1/"), null);
			brFloripaProviderRef = new SoftReference<>(provider);
			return provider;
		} else if (networkId.equals(NetworkId.IT)) {
			if (italyProviderRef != null) {
				final ItalyProvider provider = italyProviderRef.get();
				if (provider != null)
					return provider;
			}

			final ItalyProvider provider = new ItalyProvider(NAVITIA);
			italyProviderRef = new SoftReference<>(provider);
			return provider;
		} else if (networkId.equals(NetworkId.FRANCESOUTHWEST)) {
			if (franceSouthWestProviderRef != null) {
				final FranceSouthWestProvider provider = franceSouthWestProviderRef.get();
				if (provider != null)
					return provider;
			}

			final FranceSouthWestProvider provider = new FranceSouthWestProvider(NAVITIA);
			franceSouthWestProviderRef = new SoftReference<>(provider);
			return provider;
		} else if (networkId.equals(NetworkId.FRANCENORTHEAST)) {
			if (franceNorthEastProviderRef != null) {
				final FranceNorthEastProvider provider = franceNorthEastProviderRef.get();
				if (provider != null)
					return provider;
			}

			final FranceNorthEastProvider provider = new FranceNorthEastProvider(NAVITIA);
			franceNorthEastProviderRef = new SoftReference<>(provider);
			return provider;
		} else if (networkId.equals(NetworkId.FRANCENORTHWEST)) {
			if (franceNorthWestProviderRef != null) {
				final FranceNorthWestProvider provider = franceNorthWestProviderRef.get();
				if (provider != null)
					return provider;
			}

			final FranceNorthWestProvider provider = new FranceNorthWestProvider(NAVITIA);
			franceNorthWestProviderRef = new SoftReference<>(provider);
			return provider;
		} else if (networkId.equals(NetworkId.FRANCESOUTHEAST)) {
			if (franceSouthEastProviderRef != null) {
				final FranceSouthEastProvider provider = franceSouthEastProviderRef.get();
				if (provider != null)
					return provider;
			}

			final FranceSouthEastProvider provider = new FranceSouthEastProvider(NAVITIA);
			franceSouthEastProviderRef = new SoftReference<>(provider);
			return provider;
		} else if (networkId.equals(NetworkId.ONTARIO)) {
			if (ontarioProviderRef != null) {
				final OntarioProvider provider = ontarioProviderRef.get();
				if (provider != null)
					return provider;
			}

			final OntarioProvider provider = new OntarioProvider(NAVITIA);
			ontarioProviderRef = new SoftReference<>(provider);
			return provider;
		} else if (networkId.equals(NetworkId.QUEBEC)) {
			if (quebecProviderRef != null) {
				final QuebecProvider provider = quebecProviderRef.get();
				if (provider != null)
					return provider;
			}

			final QuebecProvider provider = new QuebecProvider(NAVITIA);
			quebecProviderRef = new SoftReference<>(provider);
			return provider;
		} else if (networkId.equals(NetworkId.RTACHICAGO)) {
			if (rtaChicagoProviderRef != null) {
				final RtaChicagoProvider provider = rtaChicagoProviderRef.get();
				if (provider != null)
					return provider;
			}

			final RtaChicagoProvider provider = new RtaChicagoProvider();
			rtaChicagoProviderRef = new SoftReference<>(provider);
			return provider;
		} else if (networkId.equals(NetworkId.CALIFORNIA)) {
			if (californiaProviderRef != null) {
				final CaliforniaProvider provider = californiaProviderRef.get();
				if (provider != null)
					return provider;
			}

			final CaliforniaProvider provider = new CaliforniaProvider(NAVITIA);
			californiaProviderRef = new SoftReference<>(provider);
			return provider;
		} else if (networkId.equals(NetworkId.OREGON)) {
			if (oregonProviderRef != null) {
				final OregonProvider provider = oregonProviderRef.get();
				if (provider != null)
					return provider;
			}

			final OregonProvider provider = new OregonProvider(NAVITIA);
			oregonProviderRef = new SoftReference<>(provider);
			return provider;
		} else if (networkId.equals(NetworkId.AUSTRALIA)) {
			if (australiaProviderRef != null) {
				final AustraliaProvider provider = australiaProviderRef.get();
				if (provider != null)
					return provider;
			}

			final AustraliaProvider provider = new AustraliaProvider(NAVITIA);
			australiaProviderRef = new SoftReference<>(provider);
			return provider;
		} else if (networkId.equals(NetworkId.CR)) {
			if (costaRicaProviderRef != null) {
				final CostaRicaProvider provider = costaRicaProviderRef.get();
				if (provider != null)
					return provider;
			}

			final CostaRicaProvider provider = new CostaRicaProvider(null);
			costaRicaProviderRef = new SoftReference<>(provider);
			return provider;
		} else if (networkId.equals(NetworkId.GHANA)) {
			if (ghanaProviderRef != null) {
				final GhanaProvider provider = ghanaProviderRef.get();
				if (provider != null)
					return provider;
			}

			final GhanaProvider provider = new GhanaProvider(NAVITIA);
			ghanaProviderRef = new SoftReference<>(provider);
			return provider;
		} else if (networkId.equals(NetworkId.NEWYORK)) {
			if (newYorkProviderRef != null) {
				final NewyorkProvider provider = newYorkProviderRef.get();
				if (provider != null)
					return provider;
			}

			final NewyorkProvider provider = new NewyorkProvider(NAVITIA);
			newYorkProviderRef = new SoftReference<>(provider);
			return provider;
		} else if (networkId.equals(NetworkId.NICARAGUA)) {
			if (nicaraguaProviderRef != null) {
				final NicaraguaProvider provider = nicaraguaProviderRef.get();
				if (provider != null)
					return provider;
			}

			final NicaraguaProvider provider = new NicaraguaProvider(NAVITIA);
			nicaraguaProviderRef = new SoftReference<>(provider);
			return provider;
		} else {
			throw new IllegalArgumentException(networkId.name());
		}
	}

}
