package org.herndl.parkinsonapp.maps;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.herndl.parkinsonapp.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapMarkerGetterWCANLAGEOGD implements CallbackString,
		CallbackMapMarkerGetterWCANLAGEOGD {

	private static final String dataURL = "http://data.wien.gv.at/daten/geo?service=WFS&request=GetFeature&version=1.1.0&typeName=ogdwien:WCANLAGEOGD&srsName=EPSG:4326&outputFormat=json";

	private CallbackMapMarkerGetterWCANLAGEOGD callbackMapMarkerGetterWCANLAGEOGD;

	MapMarkerGetterWCANLAGEOGD(
			CallbackMapMarkerGetterWCANLAGEOGD callbackMapMarkerGetterWCANLAGEOGD) {
		this.callbackMapMarkerGetterWCANLAGEOGD = callbackMapMarkerGetterWCANLAGEOGD;

		Log.v("MapMarkerGetterWCANLAGEOGD", "construct");

		// start download task, which calls "callbackString" if ready
		try {
			new DownloadToStringTask(this).execute(new URL(dataURL));
		} catch (MalformedURLException e) {
			Log.e("MapMarkerGetterWCANLAGEOGD", e.toString());
			return;
		}
	}

	// callback from download task, parses json, builds the list
	// of marker options and calls the methode which calls the origin callback
	// methode
	@Override
	public void callbackString(String data) {
		Log.v("MapMarkerGetterWCANLAGEOGD", "callbackString");
		List<MarkerOptions> markerOptions = new ArrayList<MarkerOptions>();

		try {
			JSONArray features = (JSONArray) new JSONObject(data)
					.get("features");
			for (int i = 0; i < features.length(); i++) {
				JSONObject row = features.getJSONObject(i);
				String type = row.getString("type");
				// String id = row.getString("id");
				String geometry_type = row.getJSONObject("geometry").getString(
						"type");
				JSONArray coordinates = row.getJSONObject("geometry")
						.getJSONArray("coordinates");
				double lng = coordinates.getDouble(0);
				double lat = coordinates.getDouble(1);

				JSONObject properties = row.getJSONObject("properties");
				String kategorie = properties.getString("KATEGORIE");

				// properties, more info
				BitmapDescriptor icon = null;
				if (kategorie.indexOf("Euro-Key") != -1)
					icon = BitmapDescriptorFactory
							.fromResource(R.drawable.wceurokeyogd);
				else if (kategorie.indexOf("Behindertenkabine") != -1)
					icon = BitmapDescriptorFactory
							.fromResource(R.drawable.oeffwcbehindertenkabine);
				else if (kategorie.indexOf("Pissoir") != -1)
					icon = BitmapDescriptorFactory
							.fromResource(R.drawable.oeffwcpissoir);
				else
					icon = BitmapDescriptorFactory
							.fromResource(R.drawable.oeffwcwartepersonal);

				if (type.equals("Feature") && geometry_type.equals("Point")) {
					markerOptions.add(new MarkerOptions()
							.position(new LatLng(lat, lng)).icon(icon)
							.draggable(false));
				} else
					Log.w("CallbackMapMarkerGetterWCANLAGEOGD",
							"invalid feature type " + type + " geometry_type "
									+ geometry_type + " found");
			}
			callbackMapMarkerGetterWCANLAGEOGD(markerOptions);

		} catch (JSONException e) {
			Log.e("MapMarkerGetterWCANLAGEOGD:callbackString", e.toString());
		}
	}

	@Override
	public void callbackMapMarkerGetterWCANLAGEOGD(
			List<MarkerOptions> markerOptions) {
		callbackMapMarkerGetterWCANLAGEOGD
				.callbackMapMarkerGetterWCANLAGEOGD(markerOptions);
	}

}
