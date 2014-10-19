package org.herndl.parkinsonapp.maps;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;

import android.os.AsyncTask;
import android.util.Log;

public class DownloadToStringTask extends AsyncTask<URL, Integer, String> {

	private CallbackString callback;

	DownloadToStringTask(CallbackString callback) {
		this.callback = callback;
	}

	@Override
	protected String doInBackground(URL... urls) {
		try {
			InputStream is = urls[0].openStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is,
					Charset.forName("UTF-8")));
			String data = readAll(rd);
			is.close();
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
