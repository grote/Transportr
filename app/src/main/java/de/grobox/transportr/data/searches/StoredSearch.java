/*
 *    Transportr
 *
 *    Copyright (c) 2013 - 2021 Torsten Grote
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

package de.grobox.transportr.data.searches;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Date;

import de.grobox.transportr.data.locations.FavoriteLocation;
import de.schildbach.pte.NetworkId;

@Entity(
		tableName = "searches",
		foreignKeys = {
				@ForeignKey(entity = FavoriteLocation.class, parentColumns = "uid", childColumns = "from_id"),
				@ForeignKey(entity = FavoriteLocation.class, parentColumns = "uid", childColumns = "via_id"),
				@ForeignKey(entity = FavoriteLocation.class, parentColumns = "uid", childColumns = "to_id"),
		},
		indices = {
				@Index("networkId"),
				@Index("from_id"),
				@Index("via_id"),
				@Index("to_id"),
				@Index(value = {"from_id", "via_id", "to_id"}, unique = true),
		}
)
public class StoredSearch {

	@PrimaryKey(autoGenerate = true)
	protected long uid;
	protected final @Nullable NetworkId networkId;

	@ColumnInfo(name = "from_id")
	final long fromId;
	@ColumnInfo(name = "via_id")
	@Nullable Long viaId;
	@ColumnInfo(name = "to_id")
	final long toId;
	public int count;
	public Date lastUsed;
	public boolean favorite;

	StoredSearch(long uid, @Nullable NetworkId networkId, long fromId, @Nullable Long viaId, long toId, int count, Date lastUsed, boolean favorite) {
		this.uid = uid;
		this.networkId = networkId;
		this.fromId = fromId;
		this.viaId = viaId;
		this.toId = toId;
		this.count = count;
		this.lastUsed = lastUsed;
		this.favorite = favorite;
	}

	@Ignore
	protected StoredSearch() {
		this(0, null, 0, 0L, 0, 1, new Date(), false);
	}

	public StoredSearch(StoredSearch s) {
		this(s.uid, s.networkId, s.fromId, s.viaId, s.toId, s.count, s.lastUsed, s.favorite);
	}

	@Ignore
	StoredSearch(@NonNull NetworkId networkId, FavoriteLocation from, @Nullable FavoriteLocation via, FavoriteLocation to) {
		this.networkId = networkId;
		this.fromId = from.getUid();
		if (via != null) this.viaId = via.getUid();
		this.toId = to.getUid();
		this.count = 1;
		this.lastUsed = new Date();
		this.favorite = false;
	}

	public long getUid() {
		return uid;
	}

	@Nullable
	public NetworkId getNetworkId() {
		return networkId;
	}

	public boolean isFavorite() {
		return favorite;
	}

	public void setFavorite(boolean favorite) {
		this.favorite = favorite;
	}

}
