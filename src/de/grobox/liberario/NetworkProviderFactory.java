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
	private static Reference<MvgProvider> mvgProviderRef;
	private static Reference<VrnProvider> vrnProviderRef;
	private static Reference<VvsProvider> vvsProviderRef;
	private static Reference<NaldoProvider> naldoProviderRef;
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
	private static Reference<VmobilProvider> vmobilProviderRef;
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
	private static Reference<TlemProvider> tlemProviderRef;
	private static Reference<TlwmProvider> tlwmProviderRef;
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
	private static Reference<VrtProvider> vrtProviderRef;
	private static Reference<JetProvider> jetProviderRef;
	private static Reference<PacaProvider> pacaProviderRef;
	private static Reference<RsagProvider> rsagProviderRef;
	private static Reference<ParisProvider> parisProviderRef;

	public static synchronized NetworkProvider provider(final NetworkId networkId)
	{
		if (networkId.equals(RtProvider.NETWORK_ID))
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
		else if (networkId.equals(BahnProvider.NETWORK_ID))
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
		else if (networkId.equals(BvgProvider.NETWORK_ID))
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
		else if (networkId.equals(VbbProvider.NETWORK_ID))
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
		else if (networkId.equals(NvvProvider.NETWORK_ID))
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
		else if (networkId.equals(BayernProvider.NETWORK_ID))
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
		else if (networkId.equals(MvvProvider.NETWORK_ID))
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
		else if (networkId.equals(InvgProvider.NETWORK_ID))
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
		else if (networkId.equals(AvvProvider.NETWORK_ID))
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
		else if (networkId.equals(VgnProvider.NETWORK_ID))
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
		else if (networkId.equals(VvmProvider.NETWORK_ID))
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
		else if (networkId.equals(VmvProvider.NETWORK_ID))
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
		else if (networkId.equals(ShProvider.NETWORK_ID))
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
		else if (networkId.equals(GvhProvider.NETWORK_ID))
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
		else if (networkId.equals(BsvagProvider.NETWORK_ID))
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
		else if (networkId.equals(VbnProvider.NETWORK_ID))
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
		else if (networkId.equals(NasaProvider.NETWORK_ID))
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
		else if (networkId.equals(VvoProvider.NETWORK_ID))
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
		else if (networkId.equals(VmsProvider.NETWORK_ID))
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
		else if (networkId.equals(VrrProvider.NETWORK_ID))
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
		else if (networkId.equals(MvgProvider.NETWORK_ID))
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
		else if (networkId.equals(VrnProvider.NETWORK_ID))
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
		else if (networkId.equals(VvsProvider.NETWORK_ID))
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
		else if (networkId.equals(NaldoProvider.NETWORK_ID))
		{
			if (naldoProviderRef != null)
			{
				final NaldoProvider provider = naldoProviderRef.get();
				if (provider != null)
					return provider;
			}

			final NaldoProvider provider = new NaldoProvider();
			naldoProviderRef = new SoftReference<>(provider);
			return provider;
		}
		else if (networkId.equals(DingProvider.NETWORK_ID))
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
		else if (networkId.equals(KvvProvider.NETWORK_ID))
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
		else if (networkId.equals(VagfrProvider.NETWORK_ID))
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
		else if (networkId.equals(NvbwProvider.NETWORK_ID))
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
		else if (networkId.equals(VvvProvider.NETWORK_ID))
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
		else if (networkId.equals(OebbProvider.NETWORK_ID))
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
		else if (networkId.equals(VorProvider.NETWORK_ID))
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
		else if (networkId.equals(LinzProvider.NETWORK_ID))
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
		else if (networkId.equals(SvvProvider.NETWORK_ID))
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
		else if (networkId.equals(VvtProvider.NETWORK_ID))
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
		else if (networkId.equals(VmobilProvider.NETWORK_ID))
		{
			if (vmobilProviderRef != null)
			{
				final VmobilProvider provider = vmobilProviderRef.get();
				if (provider != null)
					return provider;
			}

			final VmobilProvider provider = new VmobilProvider();
			vmobilProviderRef = new SoftReference<>(provider);
			return provider;
		}
		else if (networkId.equals(IvbProvider.NETWORK_ID))
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
		else if (networkId.equals(StvProvider.NETWORK_ID))
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
		else if (networkId.equals(SbbProvider.NETWORK_ID))
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
		else if (networkId.equals(BvbProvider.NETWORK_ID))
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
		else if (networkId.equals(VblProvider.NETWORK_ID))
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
		else if (networkId.equals(ZvvProvider.NETWORK_ID))
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
		else if (networkId.equals(SncbProvider.NETWORK_ID))
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
		else if (networkId.equals(LuProvider.NETWORK_ID))
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
		else if (networkId.equals(NsProvider.NETWORK_ID))
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
		else if (networkId.equals(DsbProvider.NETWORK_ID))
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
		else if (networkId.equals(SeProvider.NETWORK_ID))
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
		else if (networkId.equals(StockholmProvider.NETWORK_ID))
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
		else if (networkId.equals(NriProvider.NETWORK_ID))
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
		else if (networkId.equals(TlemProvider.NETWORK_ID))
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
		else if (networkId.equals(TlwmProvider.NETWORK_ID))
		{
			if (tlwmProviderRef != null)
			{
				final TlwmProvider provider = tlwmProviderRef.get();
				if (provider != null)
					return provider;
			}

			final TlwmProvider provider = new TlwmProvider();
			tlwmProviderRef = new SoftReference<>(provider);
			return provider;
		}
		else if (networkId.equals(TfiProvider.NETWORK_ID))
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
		else if (networkId.equals(PlProvider.NETWORK_ID))
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
		else if (networkId.equals(AtcProvider.NETWORK_ID))
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
		else if (networkId.equals(DubProvider.NETWORK_ID))
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
		else if (networkId.equals(SfProvider.NETWORK_ID))
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
		else if (networkId.equals(SeptaProvider.NETWORK_ID))
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
		else if (networkId.equals(SydneyProvider.NETWORK_ID))
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
		else if (networkId.equals(MetProvider.NETWORK_ID))
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
		else if (networkId.equals(NetworkId.VRT))
		{
			if (vrtProviderRef != null)
			{
				final VrtProvider provider = vrtProviderRef.get();
				if (provider != null)
					return provider;
			}

			final VrtProvider provider = new VrtProvider();
			vrtProviderRef = new SoftReference<>(provider);
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
		else if (networkId.equals(NetworkId.PARIS))
		{
			if (parisProviderRef != null)
			{
				final ParisProvider provider = parisProviderRef.get();
				if (provider != null)
					return provider;
			}

			final ParisProvider provider = new ParisProvider("87a37b95-913a-4cb4-ba52-eb0bc0b304ca");
			parisProviderRef = new SoftReference<>(provider);
			return provider;
		}
		else
		{
			throw new IllegalArgumentException(networkId.name());
		}
	}
}
