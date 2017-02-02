package de.grobox.liberario.networks;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import java.util.Comparator;

import javax.annotation.ParametersAreNonnullByDefault;

import de.grobox.liberario.R;

@ParametersAreNonnullByDefault
enum Region {

	EUROPE(R.string.np_region_europe, "\uD83C\uDDEA\uD83C\uDDFA"),
	GERMANY(R.string.np_region_germany, "\uD83C\uDDE9\uD83C\uDDEA"),
	AUSTRIA(R.string.np_region_austria, "\uD83C\uDDE6\uD83C\uDDF9"),
	LIECHTENSTEIN(R.string.np_region_liechtenstein, "\uD83C\uDDF1\uD83C\uDDEE"),
	SWITZERLAND(R.string.np_region_switzerland, "\uD83C\uDDE8\uD83C\uDDED"),
	BELGIUM(R.string.np_region_belgium, "\uD83C\uDDE7\uD83C\uDDEA"),
	LUXEMBOURG(R.string.np_region_luxembourg, "\uD83C\uDDF1\uD83C\uDDFA"),
	NETHERLANDS(R.string.np_region_netherlands, "\uD83C\uDDF3\uD83C\uDDF1"),
	DENMARK(R.string.np_region_denmark, "\uD83C\uDDE9\uD83C\uDDF0"),
	SWEDEN(R.string.np_region_sweden, "\uD83C\uDDF8\uD83C\uDDEA"),
	NORWAY(R.string.np_region_norway, "\uD83C\uDDF3\uD83C\uDDF4"),
	FINLAND(R.string.np_region_finland, "\uD83C\uDDEB\uD83C\uDDEE"),
	GREAT_BRITAIN(R.string.np_region_gb, "\uD83C\uDDEC\uD83C\uDDE7"),
	IRELAND(R.string.np_region_ireland, "\uD83C\uDDEE\uD83C\uDDEA"),
	ITALY(R.string.np_region_italy, "\uD83C\uDDEE\uD83C\uDDF9"),
	POLAND(R.string.np_region_poland, "\uD83C\uDDF5\uD83C\uDDF1"),
	UAE(R.string.np_region_uae, "\uD83C\uDDE6\uD83C\uDDEA"),
	USA(R.string.np_region_usa, "\uD83C\uDDFA\uD83C\uDDF8"),
	AUSTRALIA(R.string.np_region_australia, "\uD83C\uDDE6\uD83C\uDDFA"),
	FRANCE(R.string.np_region_france, "\uD83C\uDDEB\uD83C\uDDF7"),
	NEW_ZEALAND(R.string.np_region_nz, "\uD83C\uDDF3\uD83C\uDDFF"),
	SPAIN(R.string.np_region_spain, "\uD83C\uDDEA\uD83C\uDDF8"),
	BRAZIL(R.string.np_region_br, "\uD83C\uDDE7\uD83C\uDDF7"),
	CANADA(R.string.np_region_canada, "\uD83C\uDDE8\uD83C\uDDE6");

	private final @StringRes int name;
	private final @Nullable String flag;

	Region(@StringRes int name) {
		this(name, null);
	}

	Region(@StringRes int name, @Nullable String flag) {
		this.name = name;
		this.flag = flag;
	}

	public int getName() {
		return name;
	}

	@Nullable
	public String getFlag() {
		return flag;
	}

	public String getString(Context context) {
		return flag == null ? context.getString(name) : context.getString(name) + " " + flag;
	}

	static class RegionComparator implements Comparator<Region> {

		private final Context context;

		public RegionComparator(Context context) {
			super();
			this.context = context;
		}

		@Override
		public int compare(Region r1, Region r2) {
			return r1.getString(context).compareTo(r2.getString(context));
		}

	}

}
