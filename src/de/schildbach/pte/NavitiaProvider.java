/*
 * Copyright 2014 the original author or authors.
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

package de.schildbach.pte;

public class NavitiaProvider extends AbstractNavitiaProvider
{
	public static NetworkId NETWORK_ID;
	private static String API_REGION;

	public NavitiaProvider(final String authorization, final String apiRegion, final NetworkId networkId)
	{
		super(authorization);

		API_REGION = apiRegion;
		NETWORK_ID = networkId;
	}

	public NetworkId id()
	{
		return NETWORK_ID;
	}

	@Override
	public String region()
	{
		return API_REGION;
	}
}
