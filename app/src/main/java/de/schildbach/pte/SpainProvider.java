/*
 * Copyright 2014-2015 the original author or authors.
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

import okhttp3.HttpUrl;

public class SpainProvider extends AbstractNavitiaProvider
{
	private static String API_REGION = "es";

	public SpainProvider(final HttpUrl api, final String authorization)
	{
		super(NetworkId.SPAIN, api, authorization);

		setTimeZone("Europe/Spain");
	}

	@Override
	public String region()
	{
		return API_REGION;
	}

}
