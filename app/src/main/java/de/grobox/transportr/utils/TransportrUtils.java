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

package de.grobox.transportr.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;

import java.text.DecimalFormat;

import javax.annotation.ParametersAreNonnullByDefault;

import de.grobox.transportr.R;
import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.LocationType;
import de.schildbach.pte.dto.Product;

@ParametersAreNonnullByDefault
public class TransportrUtils {

	@DrawableRes
	public static int getDrawableForProduct(@Nullable Product p) {
		@DrawableRes
		int image_res = R.drawable.product_bus;
		if (p == null) return image_res;
		switch (p) {
			case HIGH_SPEED_TRAIN:
				image_res = R.drawable.product_high_speed_train;
				break;
			case REGIONAL_TRAIN:
				image_res = R.drawable.product_regional_train;
				break;
			case SUBURBAN_TRAIN:
				image_res = R.drawable.product_suburban_train;
				break;
			case SUBWAY:
				image_res = R.drawable.product_subway;
				break;
			case TRAM:
				image_res = R.drawable.product_tram;
				break;
			case BUS:
				image_res = R.drawable.product_bus;
				break;
			case FERRY:
				image_res = R.drawable.product_ferry;
				break;
			case CABLECAR:
				image_res = R.drawable.product_cablecar;
				break;
			case ON_DEMAND:
				image_res = R.drawable.product_on_demand;
				break;
		}
		return image_res;
	}

	@SuppressWarnings("ConstantConditions")
	public static void copyToClipboard(Context context, String text) {
		ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
		ClipData clip = ClipData.newPlainText("label", text);
		clipboard.setPrimaryClip(clip);
	}

	public static String getLocationName(Location l) {
		if (l.type.equals(LocationType.COORD)) {
			return getCoordsName(l);
		} else if (l.uniqueShortName() != null) {
			return l.uniqueShortName();
		} else {
			return "";
		}
	}

	public static String getCoordsName(Location location) {
		return getCoordsName(location.getLatAsDouble(), location.getLonAsDouble());
	}

	private static String getCoordsName(double lat, double lon) {
		DecimalFormat df = new DecimalFormat("#.###");
		return df.format(lat) + '/' + df.format(lon);
	}

	static public int getDragDistance(Context context) {
		final DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		return (int) (0.25 * metrics.heightPixels);
	}

	public static int dpToPx(Context context, int dp) {
		DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
		return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
	}

}
