package de.grobox.transportr.data.searches;


import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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
