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

package de.grobox.liberario;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;

import de.schildbach.pte.*;

/**
 * @author Andreas Schildbach
 */
public final class NetworkProviderFactory
{
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
	private static Reference<OebbProvider> oebbProviderRef;
	private static Reference<VorProvider> vorProviderRef;
	private static Reference<LinzProvider> linzProviderRef;
	private static Reference<SvvProvider> svvProviderRef;
	private static Reference<VvtProvider> vvtProviderRef;
	private static Reference<VaoProvider> vaoProviderRef;
	private static Reference<IvbProvider> ivbProviderRef;
	private static Reference<StvProvider> stvProviderRef;
	private static Reference<SbbProvider> sbbProviderRef;
	private static Reference<BvbProvider> bvbProviderRef;
	private static Reference<VblProvider> vblProviderRef;
	private static Reference<ZvvProvider> zvvProviderRef;
	private static Reference<SncbProvider> sncbProviderRef;
	private static Reference<LuProvider> luProviderRef;
	private static Reference<NsProvider> nsProviderRef;
	private static Reference<DsbProvider> dsbProviderRef;
	private static Reference<SeProvider> seProviderRef;
	private static Reference<StockholmProvider> stockholmProviderRef;
	private static Reference<NriProvider> nriProviderRef;
	private static Reference<HslProvider> hslProviderRef;
	private static Reference<TlemProvider> tlemProviderRef;
	private static Reference<TfiProvider> tfiProviderRef;
	private static Reference<PlProvider> plProviderRef;
	private static Reference<AtcProvider> atcProviderRef;
	private static Reference<DubProvider> dubProviderRef;
	private static Reference<SfProvider> sfProviderRef;
	private static Reference<SeptaProvider> septaProviderRef;
	private static Reference<SydneyProvider> sydneyProviderRef;
	private static Reference<MetProvider> metProviderRef;
	private static Reference<VgsProvider> vgsProviderRef;
	private static Reference<VsnProvider> vsnProviderRef;
	private static Reference<WienProvider> wienProviderRef;
	private static Reference<JetProvider> jetProviderRef;
	private static Reference<PacaProvider> pacaProviderRef;
	private static Reference<RsagProvider> rsagProviderRef;
	private static Reference<MerseyProvider> merseyProviderRef;
	private static Reference<ParisProvider> parisProviderRef;
	private static Reference<NzProvider> nzProviderRef;
	private static Reference<SpainProvider> spainProviderRef;
	private static Reference<BrProvider> brProviderRef;

	private static final String NAVITIA = "87a37b95-913a-4cb4-ba52-eb0bc0b304ca";

	public static synchronized NetworkProvider provider(final NetworkId networkId)
	{
		if (networkId.equals(NetworkId.RT))
		{
			if (rtProviderRef != null)
			{
				final RtProvider provider = rtProviderRef.get();
				if (provider != null)
					return provider;
			}

			final RtProvider provider = new RtProvider();
			rtProviderRef = new SoftReference<>(provider);
			return provider;
		}
		else if (networkId.equals(NetworkId.DB))
		{
			if (bahnProviderRef != null)
			{
				final BahnProvider provider = bahnProviderRef.get();
				if (provider != null)
					return provider;
			}

			final BahnProvider provider = new BahnProvider();
			bahnProviderRef = new SoftReference<>(provider);
			return provider;
		}
		else if (networkId.equals(NetworkId.BVG))
		{
			if (bvgProviderRef != null)
			{
				final BvgProvider provider = bvgProviderRef.get();
				if (provider != null)
					return provider;
			}

			final BvgProvider provider = new BvgProvider();
			bvgProviderRef = new SoftReference<>(provider);
			return provider;
		}
		else if (networkId.equals(NetworkId.VBB))
		{
			if (vbbProviderRef != null)
			{
				final VbbProvider provider = vbbProviderRef.get();
				if (provider != null)
					return provider;
			}

			final VbbProvider provider = new VbbProvider();
			vbbProviderRef = new SoftReference<>(provider);
			return provider;
		}
		else if (networkId.equals(NetworkId.NVV))
		{
			if (nvvProviderRef != null)
			{
				final NvvProvider provider = nvvProviderRef.get();
				if (provider != null)
					return provider;
			}

			final NvvProvider provider = new NvvProvider();
			nvvProviderRef = new SoftReference<>(provider);
			return provider;
		}
		else if (networkId.equals(NetworkId.BAYERN))
		{
			if (bayernProviderRef != null)
			{
				final BayernProvider provider = bayernProviderRef.get();
				if (provider != null)
					return provider;
			}

			final BayernProvider provider = new BayernProvider();
			bayernProviderRef = new SoftReference<>(provider);
			return provider;
		}
		else if (networkId.equals(NetworkId.MVV))
		{
			if (mvvProviderRef != null)
			{
				final MvvProvider provider = mvvProviderRef.get();
				if (provider != null)
					return provider;
			}

			final MvvProvider provider = new MvvProvider();
			mvvProviderRef = new SoftReference<>(provider);
			return provider;
		}
		else if (networkId.equals(NetworkId.INVG))
		{
			if (invgProviderRef != null)
			{
				final InvgProvider provider = invgProviderRef.get();
				if (provider != null)
					return provider;
			}

			final InvgProvider provider = new InvgProvider();
			invgProviderRef = new SoftReference<>(provider);
			return provider;
		}
		else if (networkId.equals(NetworkId.AVV))
		{
			if (avvProviderRef != null)
			{
				final AvvProvider provider = avvProviderRef.get();
				if (provider != null)
					return provider;
			}

			final AvvProvider provider = new AvvProvider();
			avvProviderRef = new SoftReference<>(provider);
			return provider;
		}
		else if (networkId.equals(NetworkId.VGN))
		{
			if (vgnProviderRef != null)
			{
				final VgnProvider provider = vgnProviderRef.get();
				if (provider != null)
					return provider;
			}

			final VgnProvider provider = new VgnProvider("no secret");
			vgnProviderRef = new SoftReference<>(provider);
			return provider;
		}
		else if (networkId.equals(NetworkId.VVM))
		{
			if (vvmProviderRef != null)
			{
				final VvmProvider provider = vvmProviderRef.get();
				if (provider != null)
					return provider;
			}

			final VvmProvider provider = new VvmProvider();
			vvmProviderRef = new SoftReference<>(provider);
			return provider;
		}
		else if (networkId.equals(NetworkId.VMV))
		{
			if (vmvProviderRef != null)
			{
				final VmvProvider provider = vmvProviderRef.get();
				if (provider != null)
					return provider;
			}

			final VmvProvider provider = new VmvProvider();
			vmvProviderRef = new SoftReference<>(provider);
			return provider;
		}
		else if (networkId.equals(NetworkId.SH))
		{
			if (shProviderRef != null)
			{
				final ShProvider provider = shProviderRef.get();
				if (provider != null)
					return provider;
			}

			final ShProvider provider = new ShProvider();
			shProviderRef = new SoftReference<>(provider);
			return provider;
		}
		else if (networkId.equals(NetworkId.GVH))
		{
			if (gvhProviderRef != null)
			{
				final GvhProvider provider = gvhProviderRef.get();
				if (provider != null)
					return provider;
			}

			final GvhProvider provider = new GvhProvider("C2C_User=ASB");
			gvhProviderRef = new SoftReference<>(provider);
			return provider;
		}
		else if (networkId.equals(NetworkId.BSVAG))
		{
			if (bsvagProviderRef != null)
			{
				final BsvagProvider provider = bsvagProviderRef.get();
				if (provider != null)
					return provider;
			}

			final BsvagProvider provider = new BsvagProvider();
			bsvagProviderRef = new SoftReference<>(provider);
			return provider;
		}
		else if (networkId.equals(NetworkId.VBN))
		{
			if (vbnProviderRef != null)
			{
				final VbnProvider provider = vbnProviderRef.get();
				if (provider != null)
					return provider;
			}

			final VbnProvider provider = new VbnProvider();
			vbnProviderRef = new SoftReference<>(provider);
			return provider;
		}
		else if (networkId.equals(NetworkId.NASA))
		{
			if (nasaProviderRef != null)
			{
				final NasaProvider provider = nasaProviderRef.get();
				if (provider != null)
					return provider;
			}

			final NasaProvider provider = new NasaProvider();
			nasaProviderRef = new SoftReference<>(provider);
			return provider;
		}
		else if (networkId.equals(NetworkId.VVO))
		{
			if (vvoProviderRef != null)
			{
				final VvoProvider provider = vvoProviderRef.get();
				if (provider != null)
					return provider;
			}

			final VvoProvider provider = new VvoProvider("http://efaproxy.fahrinfo.uptrade.de/standard/");
			vvoProviderRef = new SoftReference<>(provider);
			return provider;
		}
		else if (networkId.equals(NetworkId.VMS))
		{
			if (vmsProviderRef != null)
			{
				final VmsProvider provider = vmsProviderRef.get();
				if (provider != null)
					return provider;
			}

			final VmsProvider provider = new VmsProvider();
			vmsProviderRef = new SoftReference<>(provider);
			return provider;
		}
		else if (networkId.equals(NetworkId.VRR))
		{
			if (vrrProviderRef != null)
			{
				final VrrProvider provider = vrrProviderRef.get();
				if (provider != null)
					return provider;
			}

			final VrrProvider provider = new VrrProvider();
			vrrProviderRef = new SoftReference<>(provider);
			return provider;
		}
		else if (networkId.equals(NetworkId.VRS))
		{
			if (vrsProviderRef != null)
			{
				final VrsProvider provider = vrsProviderRef.get();
				if (provider != null)
					return provider;
			}

			final VrsProvider provider = new VrsProvider();
			vrsProviderRef = new SoftReference<>(provider);
			return provider;
		}
		else if (networkId.equals(NetworkId.MVG))
		{
			if (mvgProviderRef != null)
			{
				final MvgProvider provider = mvgProviderRef.get();
				if (provider != null)
					return provider;
			}

			final MvgProvider provider = new MvgProvider();
			mvgProviderRef = new SoftReference<>(provider);
			return provider;
		}
		else if (networkId.equals(NetworkId.VRN))
		{
			if (vrnProviderRef != null)
			{
				final VrnProvider provider = vrnProviderRef.get();
				if (provider != null)
					return provider;
			}

			final VrnProvider provider = new VrnProvider();
			vrnProviderRef = new SoftReference<>(provider);
			return provider;
		}
		else if (networkId.equals(NetworkId.VVS))
		{
			if (vvsProviderRef != null)
			{
				final VvsProvider provider = vvsProviderRef.get();
				if (provider != null)
					return provider;
			}

			final VvsProvider provider = new VvsProvider("http://www2.vvs.de/oeffi/");
			vvsProviderRef = new SoftReference<>(provider);
			return provider;
		}
		else if (networkId.equals(NetworkId.DING))
		{
			if (dingProviderRef != null)
			{
				final DingProvider provider = dingProviderRef.get();
				if (provider != null)
					return provider;
			}

			final DingProvider provider = new DingProvider();
			dingProviderRef = new SoftReference<>(provider);
			return provider;
		}
		else if (networkId.equals(NetworkId.KVV))
		{
			if (kvvProviderRef != null)
			{
				final KvvProvider provider = kvvProviderRef.get();
				if (provider != null)
					return provider;
			}

			final KvvProvider provider = new KvvProvider("http://213.144.24.66/oeffi/");
			kvvProviderRef = new SoftReference<>(provider);
			return provider;
		}
		else if (networkId.equals(NetworkId.VAGFR))
		{
			if (vagfrProviderRef != null)
			{
				final VagfrProvider provider = vagfrProviderRef.get();
				if (provider != null)
					return provider;
			}

			final VagfrProvider provider = new VagfrProvider();
			vagfrProviderRef = new SoftReference<>(provider);
			return provider;
		}
		else if (networkId.equals(NetworkId.NVBW))
		{
			if (nvbwProviderRef != null)
			{
				final NvbwProvider provider = nvbwProviderRef.get();
				if (provider != null)
					return provider;
			}

			final NvbwProvider provider = new NvbwProvider();
			nvbwProviderRef = new SoftReference<>(provider);
			return provider;
		}
		else if (networkId.equals(NetworkId.VVV))
		{
			if (vvvProviderRef != null)
			{
				final VvvProvider provider = vvvProviderRef.get();
				if (provider != null)
					return provider;
			}

			final VvvProvider provider = new VvvProvider();
			vvvProviderRef = new SoftReference<>(provider);
			return provider;
		}
		else if (networkId.equals(NetworkId.OEBB))
		{
			if (oebbProviderRef != null)
			{
				final OebbProvider provider = oebbProviderRef.get();
				if (provider != null)
					return provider;
			}

			final OebbProvider provider = new OebbProvider();
			oebbProviderRef = new SoftReference<>(provider);
			return provider;
		}
		else if (networkId.equals(NetworkId.VOR))
		{
			if (vorProviderRef != null)
			{
				final VorProvider provider = vorProviderRef.get();
				if (provider != null)
					return provider;
			}

			final VorProvider provider = new VorProvider();
			vorProviderRef = new SoftReference<>(provider);
			return provider;
		}
		else if (networkId.equals(NetworkId.LINZ))
		{
			if (linzProviderRef != null)
			{
				final LinzProvider provider = linzProviderRef.get();
				if (provider != null)
					return provider;
			}

			final LinzProvider provider = new LinzProvider();
			linzProviderRef = new SoftReference<>(provider);
			return provider;
		}
		else if (networkId.equals(NetworkId.SVV))
		{
			if (svvProviderRef != null)
			{
				final SvvProvider provider = svvProviderRef.get();
				if (provider != null)
					return provider;
			}

			final SvvProvider provider = new SvvProvider();
			svvProviderRef = new SoftReference<>(provider);
			return provider;
		}
		else if (networkId.equals(NetworkId.VVT))
		{
			if (vvtProviderRef != null)
			{
				final VvtProvider provider = vvtProviderRef.get();
				if (provider != null)
					return provider;
			}

			final VvtProvider provider = new VvtProvider();
			vvtProviderRef = new SoftReference<>(provider);
			return provider;
		}
		else if (networkId.equals(NetworkId.VAO))
		{
			if (vaoProviderRef != null)
			{
				final VaoProvider provider = vaoProviderRef.get();
				if (provider != null)
					return provider;
			}

			// TODO what is the secret?
			final VaoProvider provider = new VaoProvider("secret");
			vaoProviderRef = new SoftReference<>(provider);
			return provider;
		}
		else if (networkId.equals(NetworkId.IVB))
		{
			if (ivbProviderRef != null)
			{
				final IvbProvider provider = ivbProviderRef.get();
				if (provider != null)
					return provider;
			}

			final IvbProvider provider = new IvbProvider();
			ivbProviderRef = new SoftReference<>(provider);
			return provider;
		}
		else if (networkId.equals(NetworkId.STV))
		{
			if (stvProviderRef != null)
			{
				final StvProvider provider = stvProviderRef.get();
				if (provider != null)
					return provider;
			}

			final StvProvider provider = new StvProvider();
			stvProviderRef = new SoftReference<>(provider);
			return provider;
		}
		else if (networkId.equals(NetworkId.SBB))
		{
			if (sbbProviderRef != null)
			{
				final SbbProvider provider = sbbProviderRef.get();
				if (provider != null)
					return provider;
			}

			final SbbProvider provider = new SbbProvider();
			sbbProviderRef = new SoftReference<>(provider);
			return provider;
		}
		else if (networkId.equals(NetworkId.BVB))
		{
			if (bvbProviderRef != null)
			{
				final BvbProvider provider = bvbProviderRef.get();
				if (provider != null)
					return provider;
			}

			final BvbProvider provider = new BvbProvider();
			bvbProviderRef = new SoftReference<>(provider);
			return provider;
		}
		else if (networkId.equals(NetworkId.VBL))
		{
			if (vblProviderRef != null)
			{
				final VblProvider provider = vblProviderRef.get();
				if (provider != null)
					return provider;
			}

			final VblProvider provider = new VblProvider();
			vblProviderRef = new SoftReference<>(provider);
			return provider;
		}
		else if (networkId.equals(NetworkId.ZVV))
		{
			if (zvvProviderRef != null)
			{
				final ZvvProvider provider = zvvProviderRef.get();
				if (provider != null)
					return provider;
			}

			final ZvvProvider provider = new ZvvProvider();
			zvvProviderRef = new SoftReference<>(provider);
			return provider;
		}
		else if (networkId.equals(NetworkId.SNCB))
		{
			if (sncbProviderRef != null)
			{
				final SncbProvider provider = sncbProviderRef.get();
				if (provider != null)
					return provider;
			}

			final SncbProvider provider = new SncbProvider();
			sncbProviderRef = new SoftReference<>(provider);
			return provider;
		}
		else if (networkId.equals(NetworkId.LU))
		{
			if (luProviderRef != null)
			{
				final LuProvider provider = luProviderRef.get();
				if (provider != null)
					return provider;
			}

			final LuProvider provider = new LuProvider();
			luProviderRef = new SoftReference<>(provider);
			return provider;
		}
		else if (networkId.equals(NetworkId.NS))
		{
			if (nsProviderRef != null)
			{
				final NsProvider provider = nsProviderRef.get();
				if (provider != null)
					return provider;
			}

			final NsProvider provider = new NsProvider();
			nsProviderRef = new SoftReference<>(provider);
			return provider;
		}
		else if (networkId.equals(NetworkId.DSB))
		{
			if (dsbProviderRef != null)
			{
				final DsbProvider provider = dsbProviderRef.get();
				if (provider != null)
					return provider;
			}

			final DsbProvider provider = new DsbProvider();
			dsbProviderRef = new SoftReference<>(provider);
			return provider;
		}
		else if (networkId.equals(NetworkId.SE))
		{
			if (seProviderRef != null)
			{
				final SeProvider provider = seProviderRef.get();
				if (provider != null)
					return provider;
			}

			final SeProvider provider = new SeProvider();
			seProviderRef = new SoftReference<>(provider);
			return provider;
		}
		else if (networkId.equals(NetworkId.STOCKHOLM))
		{
			if (stockholmProviderRef != null)
			{
				final StockholmProvider provider = stockholmProviderRef.get();
				if (provider != null)
					return provider;
			}

			final StockholmProvider provider = new StockholmProvider();
			stockholmProviderRef = new SoftReference<>(provider);
			return provider;
		}
		else if (networkId.equals(NetworkId.NRI))
		{
			if (nriProviderRef != null)
			{
				final NriProvider provider = nriProviderRef.get();
				if (provider != null)
					return provider;
			}

			final NriProvider provider = new NriProvider();
			nriProviderRef = new SoftReference<>(provider);
			return provider;
		}
		else if (networkId.equals(NetworkId.HSL))
		{
			if (hslProviderRef != null)
			{
				final HslProvider provider = hslProviderRef.get();
				if (provider != null)
					return provider;
			}

			final HslProvider provider = new HslProvider("pte_hsl", "Eixaeb9tnohcah7A");
			hslProviderRef = new SoftReference<>(provider);
			return provider;
		}
		else if (networkId.equals(NetworkId.TLEM))
		{
			if (tlemProviderRef != null)
			{
				final TlemProvider provider = tlemProviderRef.get();
				if (provider != null)
					return provider;
			}

			final TlemProvider provider = new TlemProvider();
			tlemProviderRef = new SoftReference<>(provider);
			return provider;
		}
		else if (networkId.equals(NetworkId.TFI))
		{
			if (tfiProviderRef != null)
			{
				final TfiProvider provider = tfiProviderRef.get();
				if (provider != null)
					return provider;
			}

			final TfiProvider provider = new TfiProvider();
			tfiProviderRef = new SoftReference<>(provider);
			return provider;
		}
		else if (networkId.equals(NetworkId.PL))
		{
			if (plProviderRef != null)
			{
				final PlProvider provider = plProviderRef.get();
				if (provider != null)
					return provider;
			}

			final PlProvider provider = new PlProvider();
			plProviderRef = new SoftReference<>(provider);
			return provider;
		}
		else if (networkId.equals(NetworkId.ATC))
		{
			if (atcProviderRef != null)
			{
				final AtcProvider provider = atcProviderRef.get();
				if (provider != null)
					return provider;
			}

			final AtcProvider provider = new AtcProvider();
			atcProviderRef = new SoftReference<>(provider);
			return provider;
		}
		else if (networkId.equals(NetworkId.DUB))
		{
			if (dubProviderRef != null)
			{
				final DubProvider provider = dubProviderRef.get();
				if (provider != null)
					return provider;
			}

			final DubProvider provider = new DubProvider();
			dubProviderRef = new SoftReference<>(provider);
			return provider;
		}
		else if (networkId.equals(NetworkId.SF))
		{
			if (sfProviderRef != null)
			{
				final SfProvider provider = sfProviderRef.get();
				if (provider != null)
					return provider;
			}

			final SfProvider provider = new SfProvider();
			sfProviderRef = new SoftReference<>(provider);
			return provider;
		}
		else if (networkId.equals(NetworkId.SEPTA))
		{
			if (septaProviderRef != null)
			{
				final SeptaProvider provider = septaProviderRef.get();
				if (provider != null)
					return provider;
			}

			final SeptaProvider provider = new SeptaProvider();
			septaProviderRef = new SoftReference<>(provider);
			return provider;
		}
		else if (networkId.equals(NetworkId.SYDNEY))
		{
			if (sydneyProviderRef != null)
			{
				final SydneyProvider provider = sydneyProviderRef.get();
				if (provider != null)
					return provider;
			}

			final SydneyProvider provider = new SydneyProvider();
			sydneyProviderRef = new SoftReference<>(provider);
			return provider;
		}
		else if (networkId.equals(NetworkId.MET))
		{
			if (metProviderRef != null)
			{
				final MetProvider provider = metProviderRef.get();
				if (provider != null)
					return provider;
			}

			final MetProvider provider = new MetProvider();
			metProviderRef = new SoftReference<>(provider);
			return provider;
		}
		else if (networkId.equals(NetworkId.VGS))
		{
			if (vgsProviderRef != null)
			{
				final VgsProvider provider = vgsProviderRef.get();
				if (provider != null)
					return provider;
			}

			final VgsProvider provider = new VgsProvider();
			vgsProviderRef = new SoftReference<>(provider);
			return provider;
		}
		else if (networkId.equals(NetworkId.VSN))
		{
			if (vsnProviderRef != null)
			{
				final VsnProvider provider = vsnProviderRef.get();
				if (provider != null)
					return provider;
			}

			final VsnProvider provider = new VsnProvider();
			vsnProviderRef = new SoftReference<>(provider);
			return provider;
		}
		else if (networkId.equals(NetworkId.WIEN))
		{
			if (wienProviderRef != null)
			{
				final WienProvider provider = wienProviderRef.get();
				if (provider != null)
					return provider;
			}

			final WienProvider provider = new WienProvider();
			wienProviderRef = new SoftReference<>(provider);
			return provider;
		}
		else if (networkId.equals(NetworkId.JET))
		{
			if (jetProviderRef != null)
			{
				final JetProvider provider = jetProviderRef.get();
				if (provider != null)
					return provider;
			}

			final JetProvider provider = new JetProvider();
			jetProviderRef = new SoftReference<>(provider);
			return provider;
		}
		else if (networkId.equals(NetworkId.PACA))
		{
			if (pacaProviderRef != null)
			{
				final PacaProvider provider = pacaProviderRef.get();
				if (provider != null)
					return provider;
			}

			final PacaProvider provider = new PacaProvider();
			pacaProviderRef = new SoftReference<>(provider);
			return provider;
		}
		else if (networkId.equals(NetworkId.RSAG))
		{
			if (rsagProviderRef != null)
			{
				final RsagProvider provider = rsagProviderRef.get();
				if (provider != null)
					return provider;
			}

			final RsagProvider provider = new RsagProvider();
			rsagProviderRef = new SoftReference<>(provider);
			return provider;
		}
		else if (networkId.equals(NetworkId.MERSEY))
		{
			if (merseyProviderRef != null)
			{
				final MerseyProvider provider = merseyProviderRef.get();
				if (provider != null)
					return provider;
			}

			final MerseyProvider provider = new MerseyProvider();
			merseyProviderRef = new SoftReference<>(provider);
			return provider;
		}
		else if (networkId.equals(NetworkId.PARIS))
		{
			if (parisProviderRef != null)
			{
				final ParisProvider provider = parisProviderRef.get();
				if (provider != null)
					return provider;
			}

			final ParisProvider provider = new ParisProvider(NAVITIA);
			parisProviderRef = new SoftReference<>(provider);
			return provider;
		}
		else if (networkId.equals(NetworkId.NZ))
		{
			if (nzProviderRef != null)
			{
				final NzProvider provider = nzProviderRef.get();
				if (provider != null)
					return provider;
			}

			final NzProvider provider = new NzProvider(NAVITIA);
			nzProviderRef = new SoftReference<>(provider);
			return provider;
		}
		else if (networkId.equals(NetworkId.SPAIN))
		{
			if (spainProviderRef != null)
			{
				final SpainProvider provider = spainProviderRef.get();
				if (provider != null)
					return provider;
			}

			final SpainProvider provider = new SpainProvider(NAVITIA);
			spainProviderRef = new SoftReference<>(provider);
			return provider;
		}
		else if (networkId.equals(NetworkId.BR))
		{
			if (brProviderRef != null)
			{
				final BrProvider provider = brProviderRef.get();
				if (provider != null)
					return provider;
			}

			final BrProvider provider = new BrProvider(NAVITIA);
			brProviderRef = new SoftReference<>(provider);
			return provider;
		}
		else
		{
			throw new IllegalArgumentException(networkId.name());
		}
	}
}
