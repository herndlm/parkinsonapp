package org.herndl.parkinsonapp.maps;

import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class CustomMapFragment extends SupportMapFragment implements
		CallbackMapMarkerGetterWCANLAGEOGD {

	private GoogleMap map = null;

	private static final int defaultMapZoomFactor = 15;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		map = getMap();

		// map inits
		if (map == null) {
			Log.w("CustomMapFragment:onActivityCreated", "map is null");
			return;
		}

		map.setMyLocationEnabled(true);
		centerMapOnMyLocation();

		// init map marker getter for toilets in vienna
		new MapMarkerGetterWCANLAGEOGD(getActivity().getApplicationContext(),
				this);
	}

	// callback which is adding the markers
	@Override
	public void callbackMapMarkerGetterWCANLAGEOGD(
			List<MarkerOptions> markerOptions) {
		Log.v("CustomMapFragment", "callbackMapMarkerGetterWCANLAGEOGD");

		for (MarkerOptions marker : markerOptions) {
			map.addMarker(marker);
		}
	}

	private void centerMapOnMyLocation() {
		Log.v("CustomMapFragment", "centerMapOnMyLocation");

		if (map == null) {
			Log.w("CustomMapFragment", "map is null");
			return;
		}

		LocationManager locationManager = (LocationManager) getActivity()
				.getSystemService(Context.LOCATION_SERVICE);

		// get most last recent valid location provider
		List<String> matchingProviders = locationManager.getAllProviders();
		long bestTime = 0;
		long currentTime = Calendar.getInstance().getTimeInMillis();
		Location bestLocation = null;
		for (String provider : matchingProviders) {
			Location location = locationManager.getLastKnownLocation(provider);
			if (location == null)
				continue;

			long time = location.getTime();
			if ((currentTime - time) > bestTime) {
				bestTime = time;
				bestLocation = location;
			}
		}

		if (bestLocation == null) {
			Log.w("CustomMapFragment", "location is null");
			return;
		}

		map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
				bestLocation.getLatitude(), bestLocation.getLongitude()),
				defaultMapZoomFactor));
	}
}
