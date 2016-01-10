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

import android.util.Log;

import de.schildbach.pte.dto.Product;
import de.schildbach.pte.dto.Style;
import de.schildbach.pte.dto.Style.Shape;

public class SpainProvider extends AbstractNavitiaProvider
{
	private static String API_REGION = "es";

	public SpainProvider(final String api, final String authorization)
	{
		super(NetworkId.SPAIN, api, authorization);

		setTimeZone("Europe/Spain");
	}

	@Override
	public String region()
	{
		return API_REGION;
	}

	@Override
	protected Style getLineStyle(final Product product, final String code, final String color)
	{
		int bc = Style.RED;
		if(!color.equals("#")) {
			bc = Style.parseColor(color);
		}
		int fc = Style.WHITE;
		if(!color.equals("#")) {
			fc = computeForegroundColor(color);
		}

		switch (product)
		{
			case SUBURBAN_TRAIN:
			{
				return new Style(Shape.CIRCLE, bc, fc);
			}
			case SUBWAY:
			{
				return new Style(Shape.CIRCLE, bc, fc);
			}
			case TRAM:
			{
				return new Style(Shape.RECT, bc, fc);
			}
			case BUS:
			{
				return new Style(Shape.RECT, bc, fc);
			}
			default:
				Log.d("SpainProvider", "Unhandled Product: " + product.toString());
				return new Style(bc, fc);
		}
	}
}
