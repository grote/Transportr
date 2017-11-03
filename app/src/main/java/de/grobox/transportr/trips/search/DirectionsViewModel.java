package de.grobox.transportr.trips.search;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.support.annotation.Nullable;

import java.util.Calendar;
import java.util.EnumSet;
import java.util.Set;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.inject.Inject;

import de.grobox.transportr.TransportrApplication;
import de.grobox.transportr.data.locations.FavoriteLocation.FavLocationType;
import de.grobox.transportr.data.locations.LocationRepository;
import de.grobox.transportr.data.searches.SearchesRepository;
import de.grobox.transportr.favorites.trips.SavedSearchesViewModel;
import de.grobox.transportr.locations.LocationLiveData;
import de.grobox.transportr.locations.LocationView.LocationViewListener;
import de.grobox.transportr.locations.WrapLocation;
import de.grobox.transportr.networks.TransportNetworkManager;
import de.grobox.transportr.settings.SettingsManager;
import de.grobox.transportr.trips.TripQuery;
import de.grobox.transportr.trips.search.TripsRepository.QueryMoreState;
import de.grobox.transportr.ui.TimeDateFragment.TimeDateListener;
import de.grobox.transportr.utils.SingleLiveEvent;
import de.schildbach.pte.dto.Product;
import de.schildbach.pte.dto.Trip;

import static de.grobox.transportr.data.locations.FavoriteLocation.FavLocationType.FROM;
import static de.grobox.transportr.data.locations.FavoriteLocation.FavLocationType.VIA;
import static de.grobox.transportr.utils.DateUtils.isNow;

@ParametersAreNonnullByDefault
public class DirectionsViewModel extends SavedSearchesViewModel implements TimeDateListener, LocationViewListener {

	private final TripsRepository tripsRepository;

	private final MutableLiveData<WrapLocation> fromLocation = new MutableLiveData<>();
	private final MutableLiveData<WrapLocation> viaLocation = new MutableLiveData<>();
	private final MutableLiveData<WrapLocation> toLocation = new MutableLiveData<>();

	final LocationLiveData locationLiveData;
	final MutableLiveData<FavLocationType> findGpsLocation = new MutableLiveData<>();

	private final MutableLiveData<Boolean> now = new MutableLiveData<>();
	private final LiveData<Calendar> calendar = Transformations.switchMap(now, this::getUpdatedCalendar);
	private final MutableLiveData<Calendar> updatedCalendar = new MutableLiveData<>();
	private final MutableLiveData<EnumSet<Product>> products = new MutableLiveData<>();
	private final MutableLiveData<Boolean> isDeparture = new MutableLiveData<>();
	private final MutableLiveData<Boolean> isExpanded = new MutableLiveData<>();
	final SingleLiveEvent<Void> showTrips = new SingleLiveEvent<>();
	final MutableLiveData<Boolean> topSwipeEnabled = new MutableLiveData<>();
	private long favTripUid;

	@Inject
	DirectionsViewModel(TransportrApplication application, TransportNetworkManager transportNetworkManager, SettingsManager settingsManager,
	                    LocationRepository locationRepository, SearchesRepository searchesRepository) {
		super(application, transportNetworkManager, locationRepository, searchesRepository);
		now.setValue(true);
		updatedCalendar.setValue(Calendar.getInstance());
		products.setValue(EnumSet.allOf(Product.class));
		isDeparture.setValue(true);
		isExpanded.setValue(false);
		topSwipeEnabled.setValue(false);
		locationLiveData = new LocationLiveData(application.getApplicationContext());

		if (getTransportNetwork().getValue() == null) throw new IllegalStateException();
		tripsRepository = new TripsRepository(application.getApplicationContext(), getTransportNetwork().getValue().getNetworkProvider(),
				settingsManager, searchesRepository);
	}

	LiveData<WrapLocation> getFromLocation() {
		return fromLocation;
	}

	LiveData<WrapLocation> getViaLocation() {
		return viaLocation;
	}

	LiveData<WrapLocation> getToLocation() {
		return toLocation;
	}

	void setFromLocation(@Nullable WrapLocation location) {
		fromLocation.setValue(location);
	}

	void setViaLocation(@Nullable WrapLocation location) {
		viaLocation.setValue(location);
	}

	void setToLocation(@Nullable WrapLocation location) {
		toLocation.setValue(location);
	}

	private LiveData<Calendar> getUpdatedCalendar(boolean now) {
		if (now) {
			updatedCalendar.setValue(Calendar.getInstance());
		}
		return updatedCalendar;
	}

	LiveData<Calendar> getCalendar() {
		return calendar;
	}

	@Override
	public void onTimeAndDateSet(Calendar calendar) {
		setCalendar(calendar);
		search();
	}

	void resetCalender() {
		now.setValue(true);
		search();
	}

	private void setCalendar(Calendar calendar) {
		updatedCalendar.setValue(calendar);
		if (isNow(calendar)) {
			now.setValue(true);
		} else {
			now.setValue(false);
		}
	}

	LiveData<EnumSet<Product>> getProducts() {
		return products;
	}

	void setProducts(EnumSet<Product> newProducts) {
		products.setValue(newProducts);
		search();
	}

	LiveData<Boolean> getIsDeparture() {
		return isDeparture;
	}

	void setIsDeparture(boolean departure) {
		isDeparture.setValue(departure);
		search();
	}

	LiveData<Boolean> getIsExpanded() {
		return isExpanded;
	}

	void setIsExpanded(boolean expanded) {
		isExpanded.setValue(expanded);
	}

	void setFavTripUid(long favTripUid) {
		this.favTripUid = favTripUid;
	}

	@Override
	public void onLocationItemClick(WrapLocation loc, FavLocationType type) {
		useLocation(loc, type);
		if (type == FROM) {
			setFromLocation(loc);
		} else if (type == VIA) {
			setViaLocation(loc);
		} else {
			setToLocation(loc);
		}
		search();
		// clear finding GPS location request
		if (findGpsLocation.getValue() == type) findGpsLocation.setValue(null);
	}

	@Override
	public void onLocationCleared(FavLocationType type) {
		if (type == FROM) {
			setFromLocation(null);
		} else if (type == VIA) {
			setViaLocation(null);
			search();
		} else {
			setToLocation(null);
		}
		// clear finding GPS location request
		if (findGpsLocation.getValue() == type) findGpsLocation.setValue(null);
	}

	/* Trip Queries */

	void search() {
		if (fromLocation.getValue() == null || toLocation.getValue() == null) return;

		Calendar calendar;
		if (now.getValue() != null && now.getValue()) {
			calendar = Calendar.getInstance();
		} else {
			calendar = getCalendar().getValue();
			if (calendar == null) return;
		}

		TripQuery tripQuery = new TripQuery(favTripUid, fromLocation.getValue(), viaLocation.getValue(), toLocation.getValue(),
				calendar.getTime(), isDeparture.getValue(), products.getValue());

		tripsRepository.search(tripQuery);
		showTrips.call();
	}

	void searchMore(boolean later) {
		tripsRepository.searchMore(later);
	}

	LiveData<QueryMoreState> getQueryMoreState() {
		return tripsRepository.getQueryMoreState();
	}

	LiveData<Set<Trip>> getTrips() {
		return tripsRepository.getTrips();
	}

	LiveData<String> getQueryError() {
		return tripsRepository.getQueryError();
	}

	LiveData<String> getQueryMoreError() {
		return tripsRepository.getQueryMoreError();
	}

}
