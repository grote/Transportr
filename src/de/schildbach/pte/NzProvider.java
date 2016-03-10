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

import java.util.HashMap;
import java.util.Map;

import de.schildbach.pte.dto.Product;
import de.schildbach.pte.dto.Style;
import de.schildbach.pte.dto.Style.Shape;

/**
 * @author Torsten Grote
 */
public class NzProvider extends AbstractNavitiaProvider
{
	private static String API_REGION = "nz";

	public NzProvider(final String api, final String authorization)
	{
		super(NetworkId.NZ, api, authorization);

		setTimeZone("Pacific/Auckland");
		setStyles(STYLES);
	}

	@Override
	public String region()
	{
		return API_REGION;
	}

	@Override
	protected Style getLineStyle(final Product product, final String code, final String color)
	{
		if(color != null) {
			return lineStyle(network.name(), product, code);
		}
		else {
			return super.getLineStyle(product, code, null);
		}
	}

	private static final Map<String, Style> STYLES = new HashMap<>();
	static
	{
		// Wellington buses
		STYLES.put("B1", new Style(Shape.ROUNDED, Style.parseColor("#A10082"), Style.WHITE));
		STYLES.put("B2", new Style(Shape.ROUNDED, Style.parseColor("#cf142b"), Style.WHITE));
		STYLES.put("B3", new Style(Shape.ROUNDED, Style.parseColor("#61bf1a"), Style.WHITE));
		STYLES.put("B4", new Style(Shape.ROUNDED, Style.parseColor("#52006b"), Style.WHITE));
		STYLES.put("B5", new Style(Shape.ROUNDED, Style.parseColor("#f54029"), Style.WHITE));
		STYLES.put("B6", new Style(Shape.ROUNDED, Style.parseColor("#009970"), Style.WHITE));
		STYLES.put("B7", new Style(Shape.ROUNDED, Style.parseColor("#b35408"), Style.WHITE));
		STYLES.put("B8", new Style(Shape.ROUNDED, Style.parseColor("#703824"), Style.WHITE));
		STYLES.put("B9", new Style(Shape.ROUNDED, Style.parseColor("#d42e12"), Style.WHITE));
		STYLES.put("B10", new Style(Shape.ROUNDED, Style.parseColor("#703824"), Style.WHITE));
		STYLES.put("B11", new Style(Shape.ROUNDED, Style.parseColor("#b35408"), Style.WHITE));
		STYLES.put("B13", new Style(Shape.ROUNDED, Style.parseColor("#974e18"), Style.WHITE));
		STYLES.put("B14", new Style(Shape.ROUNDED, Style.parseColor("#8599a8"), Style.WHITE));
		STYLES.put("B17", new Style(Shape.ROUNDED, Style.parseColor("#61bf1a"), Style.WHITE));
		STYLES.put("B18", new Style(Shape.ROUNDED, Style.parseColor("#00b8e0"), Style.WHITE));
		STYLES.put("B20", new Style(Shape.ROUNDED, Style.parseColor("#f266b5"), Style.WHITE));
		STYLES.put("B21", new Style(Shape.ROUNDED, Style.parseColor("#70752b"), Style.WHITE));
		STYLES.put("B22", new Style(Shape.ROUNDED, Style.parseColor("#d97300"), Style.WHITE));
		STYLES.put("B23", new Style(Shape.ROUNDED, Style.parseColor("#e6b012"), Style.WHITE));
		STYLES.put("B24", new Style(Shape.ROUNDED, Style.parseColor("#214230"), Style.WHITE));
		STYLES.put("B25", new Style(Shape.ROUNDED, Style.parseColor("#002a42"), Style.WHITE));
		STYLES.put("B28", new Style(Shape.ROUNDED, Style.parseColor("#bd9065"), Style.WHITE));
		STYLES.put("B29", new Style(Shape.ROUNDED, Style.parseColor("#006973"), Style.WHITE));
		STYLES.put("B30", new Style(Shape.ROUNDED, Style.parseColor("#703824"), Style.WHITE));
		STYLES.put("B31", new Style(Shape.ROUNDED, Style.parseColor("#cf142b"), Style.WHITE));
		STYLES.put("B32", new Style(Shape.ROUNDED, Style.parseColor("#a10082"), Style.WHITE));
		STYLES.put("B43", new Style(Shape.ROUNDED, Style.parseColor("#8599a8"), Style.WHITE));
		STYLES.put("B44", new Style(Shape.ROUNDED, Style.parseColor("#57b5e0"), Style.WHITE));
		STYLES.put("B45", new Style(Shape.ROUNDED, Style.parseColor("#00b0c7"), Style.WHITE));
		STYLES.put("B46", new Style(Shape.ROUNDED, Style.parseColor("#005ec4"), Style.WHITE));
		STYLES.put("B47", new Style(Shape.ROUNDED, Style.parseColor("#9F6614"), Style.WHITE));
		STYLES.put("B50", new Style(Shape.ROUNDED, Style.parseColor("#008542"), Style.WHITE));
		STYLES.put("B52", new Style(Shape.ROUNDED, Style.parseColor("#4f8c0d"), Style.WHITE));
		STYLES.put("B53", new Style(Shape.ROUNDED, Style.parseColor("#c25e03"), Style.WHITE));
		STYLES.put("B54", new Style(Shape.ROUNDED, Style.parseColor("#a8034f"), Style.WHITE));
		STYLES.put("B55", new Style(Shape.ROUNDED, Style.parseColor("#703824"), Style.WHITE));
		STYLES.put("B56", new Style(Shape.ROUNDED, Style.parseColor("#f58025"), Style.WHITE));
		STYLES.put("B57", new Style(Shape.ROUNDED, Style.parseColor("#eba96b"), Style.WHITE));
		STYLES.put("B58", new Style(Shape.ROUNDED, Style.parseColor("#a8034f"), Style.WHITE));
		STYLES.put("B80", new Style(Shape.ROUNDED, Style.parseColor("#002b45"), Style.WHITE));
		STYLES.put("B81", new Style(Shape.ROUNDED, Style.parseColor("#5c2624"), Style.WHITE));
		STYLES.put("B83", new Style(Shape.ROUNDED, Style.parseColor("#ad2624"), Style.WHITE));
		STYLES.put("B84", new Style(Shape.ROUNDED, Style.parseColor("#f27d00"), Style.WHITE));
		STYLES.put("B85", new Style(Shape.ROUNDED, Style.parseColor("#781496"), Style.WHITE));
		STYLES.put("B90", new Style(Shape.ROUNDED, Style.parseColor("#9278d1"), Style.WHITE));
		STYLES.put("B91", new Style(Shape.ROUNDED, Style.parseColor("#f27d00"), Style.WHITE));
		STYLES.put("B92", new Style(Shape.ROUNDED, Style.parseColor("#6e0a78"), Style.WHITE));
		STYLES.put("B93", new Style(Shape.ROUNDED, Style.parseColor("#a87cb6"), Style.WHITE));
		STYLES.put("B110", new Style(Shape.ROUNDED, Style.parseColor("#9c1a87"), Style.WHITE));
		STYLES.put("B111", new Style(Shape.ROUNDED, Style.parseColor("#00599c"), Style.WHITE));
		STYLES.put("B112", new Style(Shape.ROUNDED, Style.parseColor("#85c7e3"), Style.WHITE));
		STYLES.put("B114", new Style(Shape.ROUNDED, Style.parseColor("#9eab05"), Style.WHITE));
		STYLES.put("B115", new Style(Shape.ROUNDED, Style.parseColor("#006647"), Style.WHITE));
		STYLES.put("B120", new Style(Shape.ROUNDED, Style.parseColor("#0db02b"), Style.WHITE));
		STYLES.put("B121", new Style(Shape.ROUNDED, Style.parseColor("#00599c"), Style.WHITE));
		STYLES.put("B130", new Style(Shape.ROUNDED, Style.parseColor("#00a6d6"), Style.WHITE));
		STYLES.put("B145", new Style(Shape.ROUNDED, Style.parseColor("#2ec7d6"), Style.WHITE));
		STYLES.put("B150", new Style(Shape.ROUNDED, Style.parseColor("#8f2140"), Style.WHITE));
		STYLES.put("B154", new Style(Shape.ROUNDED, Style.parseColor("#e0457a"), Style.WHITE));
		STYLES.put("B160", new Style(Shape.ROUNDED, Style.parseColor("#cf142b"), Style.WHITE));
		STYLES.put("B170", new Style(Shape.ROUNDED, Style.parseColor("#7d7805"), Style.WHITE));
		STYLES.put("B200", new Style(Shape.ROUNDED, Style.parseColor("#e0457a"), Style.WHITE));
		STYLES.put("B201", new Style(Shape.ROUNDED, Style.parseColor("#007073"), Style.WHITE));
		STYLES.put("B202", new Style(Shape.ROUNDED, Style.parseColor("#e6b012"), Style.WHITE));
		STYLES.put("B203", new Style(Shape.ROUNDED, Style.parseColor("#9c70cc"), Style.WHITE));
		STYLES.put("B204", new Style(Shape.ROUNDED, Style.parseColor("#b3c98c"), Style.WHITE));
		STYLES.put("B205", new Style(Shape.ROUNDED, Style.parseColor("#85c7e3"), Style.WHITE));
		STYLES.put("B206", new Style(Shape.ROUNDED, Style.parseColor("#DD0A61"), Style.WHITE));
		STYLES.put("B210", new Style(Shape.ROUNDED, Style.parseColor("#7A1600"), Style.WHITE));
		STYLES.put("B211", new Style(Shape.ROUNDED, Style.parseColor("#F47836"), Style.WHITE));
		STYLES.put("B220", new Style(Shape.ROUNDED, Style.parseColor("#008952"), Style.WHITE));
		STYLES.put("B226", new Style(Shape.ROUNDED, Style.parseColor("#007FB1"), Style.WHITE));
		STYLES.put("B230", new Style(Shape.ROUNDED, Style.parseColor("#D31145"), Style.WHITE));
		STYLES.put("B236", new Style(Shape.ROUNDED, Style.parseColor("#862175"), Style.WHITE));
		STYLES.put("B250", new Style(Shape.ROUNDED, Style.parseColor("#00679D"), Style.WHITE));
		STYLES.put("B260", new Style(Shape.ROUNDED, Style.parseColor("#570861"), Style.WHITE));
		STYLES.put("B261", new Style(Shape.ROUNDED, Style.parseColor("#ED1C24"), Style.WHITE));
		STYLES.put("B262", new Style(Shape.ROUNDED, Style.parseColor("#5D9732"), Style.WHITE));
		STYLES.put("B270", new Style(Shape.ROUNDED, Style.parseColor("#d95121"), Style.WHITE));
		STYLES.put("B280", new Style(Shape.ROUNDED, Style.parseColor("#2526a9"), Style.WHITE));
		STYLES.put("B289", new Style(Shape.ROUNDED, Style.parseColor("#2526a9"), Style.WHITE));
		STYLES.put("B290", new Style(Shape.ROUNDED, Style.parseColor("#00a6de"), Style.WHITE));

		// Wellington suburban trains
		STYLES.put("SHVL", new Style(Shape.ROUNDED, Style.parseColor("#de7008"), Style.WHITE));
		STYLES.put("SWRL", new Style(Shape.ROUNDED, Style.parseColor("#003f8d"), Style.WHITE));
		STYLES.put("SJVL", new Style(Shape.ROUNDED, Style.parseColor("#29c2ce"), Style.WHITE));
		STYLES.put("SKPL", new Style(Shape.ROUNDED, Style.parseColor("#d4d152"), Style.WHITE));
		STYLES.put("SMEL", new Style(Shape.ROUNDED, Style.parseColor("#00885f"), Style.WHITE));
		STYLES.put("SNEX", new Style(Shape.ROUNDED, Style.parseColor("#00354d"), Style.WHITE));
		STYLES.put("SPNL", new Style(Shape.ROUNDED, Style.parseColor("#00354d"), Style.WHITE));

		// Wellington other
		STYLES.put("CCCL", new Style(Shape.ROUNDED, Style.parseColor("#CC0000"), Style.WHITE));
		STYLES.put("FWHF", new Style(Shape.ROUNDED, Style.parseColor("#004b38"), Style.WHITE));
	}
}
