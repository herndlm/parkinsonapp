package org.herndl.parkinsonapp.track;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.herndl.parkinsonapp.R;
import org.joda.time.Duration;
import org.joda.time.Interval;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;

public class TrackerFragment extends Fragment {

	@SuppressLint("UseSparseArrays")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View rootView = inflater.inflate(R.layout.fragment_tracker, container,
				false);

		// get all tracker entities
		List<TrackerEntity> entities = TrackerEntity
				.listAll(TrackerEntity.class);

		// prepare med data in maps of maps
		// like an array in the form <name - date - value>
		HashMap<String, HashMap<Long, Integer>> med_map = new HashMap<String, HashMap<Long, Integer>>();
		for (TrackerEntity entity : entities) {
			HashMap<Long, Integer> day_map = new HashMap<Long, Integer>();

			/*
			 * DateFormat format = SimpleDateFormat.getDateInstance(); String
			 * date_formatted = format.format(entity.calendar.getTime());
			 */
			// create an interval in Joda
			Interval interval = new Interval(entity.calendar.getTimeInMillis(),
					Calendar.getInstance().getTimeInMillis());
			// than get the duration
			Duration duration = interval.toDuration();
			Long date_formatted = duration.getStandardDays();
			if (day_map.containsKey(date_formatted))
				day_map.put(date_formatted, day_map.get(date_formatted)
						+ entity.intValue);
			else
				day_map.put(date_formatted, entity.intValue);

			med_map.put(entity.name, day_map);
		}

		// prepare graph data
		List<GraphViewSeries> graphViewSeries = new ArrayList<GraphViewSeries>();
		for (Entry<String, HashMap<Long, Integer>> med_map_entry : med_map
				.entrySet()) {
			HashMap<Long, Integer> day_map = med_map_entry.getValue();

			GraphViewData[] data = new GraphViewData[day_map.size()];
			for (Entry<Long, Integer> day_map_entry : day_map.entrySet()) {
				long x = day_map_entry.getKey(); // TODO
				double y = day_map_entry.getValue();
				Log.v("x", "" + x);
				Log.v("y", "" + y);
				data[(int) x] = new GraphViewData(x, y);
			}

			graphViewSeries.add(new GraphViewSeries(data));
		}

		GraphView graphView = new LineGraphView(getActivity() // context
				, "MedGraphs" // heading
		);

		for (GraphViewSeries series : graphViewSeries) {
			graphView.addSeries(series);
		}

		graphView.setScalable(true);

		LinearLayout layout = (LinearLayout) rootView
				.findViewById(R.id.tracker_layout);
		layout.addView(graphView);

		return rootView;
	}
}
