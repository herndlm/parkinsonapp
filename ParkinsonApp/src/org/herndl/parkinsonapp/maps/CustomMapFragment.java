package org.herndl.parkinsonapp.maps;

import java.util.List;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class CustomMapFragment extends SupportMapFragment implements
		CallbackMapMarkerGetterWCANLAGEOGD,
		CallbackMapMarkerGetterBEHINDERTENPARKPLATZOGD {

	private GoogleMap map = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		map = getMap();

		// map inits
		if (map != null) {

			centerMapOnMyLocation();

			// init map marker getter for toilets in vienna
			new MapMarkerGetterWCANLAGEOGD(this);

			// init map marker getter for disabled parking in vienna
			// new MapMarkerGetterBEHINDERTENPARKPLATZOGD(this);

		} else {
			Log.w("CustomMapFragment:onActivityCreated",
					"getMap() returned null");
		}
	}

	private void centerMapOnMyLocation() {
		Log.v("CustomMapFragment", "centerMapOnMyLocation");

		map.setMyLocationEnabled(true);

		LocationManager locationManager = (LocationManager) getActivity()
				.getSystemService(Context.LOCATION_SERVICE);
		Criteria criteria = new Criteria();

		Location location = locationManager
				.getLastKnownLocation(locationManager.getBestProvider(criteria,
						false));
		if (location != null) {
			Log.v("CustomMapFragment:centerMapOnMyLocation",
					"location not null");
			map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
					location.getLatitude(), location.getLongitude()), 13));

			CameraPosition cameraPosition = new CameraPosition.Builder()
					.target(new LatLng(location.getLatitude(), location
							.getLongitude())) // Sets the center of the map to
												// location user
					.zoom(17) // Sets the zoom
					.bearing(90) // Sets the orientation of the camera to east
					.tilt(40) // Sets the tilt of the camera to 30 degrees
					.build(); // Creates a CameraPosition from the builder
			map.animateCamera(CameraUpdateFactory
					.newCameraPosition(cameraPosition));

		}
	}

	@Override
	public void callbackMapMarkerGetterWCANLAGEOGD(
			List<MarkerOptions> markerOptions) {
		Log.v("CustomMapFragment", "callbackMapMarkerGetterWCANLAGEOGD");

		for (MarkerOptions marker : markerOptions) {
			map.addMarker(marker);
		}

	}

	@Override
	public void callbackMapMarkerGetterBEHINDERTENPARKPLATZOGD(
			List<MarkerOptions> markerOptions) {
		Log.v("CustomMapFragment",
				"callbackMapMarkerGetterBEHINDERTENPARKPLATZOGD");

		for (MarkerOptions marker : markerOptions) {
			map.addMarker(marker);
		}
	}
}
