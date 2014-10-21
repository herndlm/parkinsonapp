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

			// no cached data found or data too old
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

	@Override
	protected void onPostExecute(String data) {
		callback.callbackString(data);
	}

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

	// TODO
	private String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
	}

}
