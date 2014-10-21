package org.herndl.parkinsonapp.maps;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Calendar;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

// AsyncTask which downloads an URL in the background, it uses caching for faster
// and offline access and uses the CallbackString interface for callbacks to its caller 
public class DownloadToStringTask extends AsyncTask<URL, Integer, String> {

	private CallbackString callback;
	private Integer cacheSeconds;
	private Context context;

	DownloadToStringTask(Context context, CallbackString callback,
			Integer cacheSeconds) {
		this.context = context;
		this.callback = callback;
		this.cacheSeconds = cacheSeconds;
	}

	@Override
	protected String doInBackground(URL... urls) {
		try {
			// try to read cached data
			String data = getFromCache(urls[0].toString());

			// no cached data found or data too old, download
			if (data == null) {
				InputStream inputStream = urls[0].openStream();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(inputStream,
								Charset.forName("UTF-8")));
				data = readAll(reader);
				inputStream.close();

				// save to cache
				saveToCache(urls[0].toString(), data);
			}
			return data;
		} catch (Exception e) {
			Log.e("DownloadToStringTask:doInBackground", e.toString());
			return null;
		}
	}

	// call callback when ready
	@Override
	protected void onPostExecute(String data) {
		callback.callbackString(data);
	}

	// saveToCache helper which uses a hashCode of the URL to store the data in
	// the app cache dir
	public void saveToCache(String url, String data) {
		Log.v("DownloadToStringTask", "saveToCache");
		try {
			String fileName = Integer.toString(url.hashCode());

			File file = new File(context.getCacheDir(), fileName);

			FileOutputStream outputStream = new FileOutputStream(file);
			outputStream.write(data.getBytes());
			outputStream.close();
		} catch (Exception e) {
			Log.e("DownloadToStringTask:saveToCache", e.toString());
		}
		return;
	}

	// getFromCache helper which returns the cached data only if it's existing
	// and not too old
	public String getFromCache(String url) {
		Log.v("DownloadToStringTask", "getFromCache");
		String data = null;
		try {
			String fileName = Integer.toString(url.hashCode());
			File file = new File(context.getCacheDir(), fileName);

			// abort if file does not exist
			if (!file.exists())
				return null;

			// check last modified date and compare with max cache seconds
			Calendar calendarFile = Calendar.getInstance();
			calendarFile.setTimeInMillis(file.lastModified());
			calendarFile.add(Calendar.SECOND, cacheSeconds);
			Calendar calendarNow = Calendar.getInstance();
			if (calendarFile.before(calendarNow))
				return null;

			FileInputStream inputStream = new FileInputStream(file);
			BufferedReader rd = new BufferedReader(new InputStreamReader(
					inputStream, Charset.forName("UTF-8")));
			data = readAll(rd);
		} catch (Exception e) {
			Log.e("DownloadToStringTask:getFromCache", e.toString());
		}
		return data;
	}

	// helper methode for streams to read all data at once
	private String readAll(Reader rd) throws IOException {
		StringBuilder stringBuilder = new StringBuilder();
		int charCode;
		while ((charCode = rd.read()) != -1) {
			stringBuilder.append((char) charCode);
		}
		return stringBuilder.toString();
	}

}
