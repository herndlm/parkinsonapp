package org.herndl.parkinsonapp.track;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.herndl.parkinsonapp.R;
import org.joda.time.Duration;
import org.joda.time.Interval;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphView.LegendAlign;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.GraphViewStyle;
import com.jjoe64.graphview.LineGraphView;

// tracker fragment which holds the GraphView object
public class TrackerFragment extends Fragment {

	// colors for graphs which are used in this order and start from the
	// beginning if used up
	private static int[] colorsDefault = { Color.BLUE, Color.RED, Color.GREEN,
			Color.CYAN, Color.MAGENTA };
	// long value which is used to normalize data by subtracting it from the
	// biggest occurred value
	private long x_value_biggest = 0;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View rootView = inflater.inflate(R.layout.fragment_tracker, container,
				false);

		LinearLayout layout = (LinearLayout) rootView
				.findViewById(R.id.tracker_layout);

		// get all tracker entities
		List<TrackerEntity> entities = TrackerEntity
				.listAll(TrackerEntity.class);

		// hide default shown instructions if enough data found
		if (entities.size() > 1) {
			TextView tracker_empty = (TextView) rootView
					.findViewById(R.id.tracker_empty);
			tracker_empty.setVisibility(View.GONE);
		} else
			return rootView;

		// prepare med data in maps of maps
		// like an array in the form <name - date - value>
		TreeMap<String, TreeMap<Long, Integer>> med_map = new TreeMap<String, TreeMap<Long, Integer>>();
		for (TrackerEntity entity : entities) {

			// get the day_map of the med or create new one if not existing
			TreeMap<Long, Integer> day_map;
			if (med_map.containsKey(entity.name))
				day_map = med_map.get(entity.name);
			// map is reversed because of later normalization
			else
				day_map = new TreeMap<Long, Integer>(Collections.reverseOrder());

			// create an interval with Joda-Time
			Interval interval = new Interval(entity.calendar.getTimeInMillis(),
					Calendar.getInstance().getTimeInMillis());
			// get the duration in days from today to med taken day
			// this duration generates big x values for the oldest (left) values
			// which calls for normalization
			Duration duration = interval.toDuration();
			Long date_formatted = duration.getStandardDays();
			if (day_map.containsKey(date_formatted))
				day_map.put(date_formatted, day_map.get(date_formatted)
						+ entity.intValue);
			else
				day_map.put(date_formatted, entity.intValue);

			med_map.put(entity.name, day_map);
		}

		// prepare graph data series
		List<GraphViewSeries> graphViewSeries = new ArrayList<GraphViewSeries>();
		int j = 0;
		for (Entry<String, TreeMap<Long, Integer>> med_map_entry : med_map
				.entrySet()) {
			TreeMap<Long, Integer> day_map = med_map_entry.getValue();

			// get the biggest day_map key for later x value normalization
			for (Entry<Long, Integer> day_map_entry : day_map.entrySet()) {
				long x_value = day_map_entry.getKey();
				if (x_value > x_value_biggest)
					x_value_biggest = x_value;

			}

			GraphViewData[] data = new GraphViewData[day_map.size()];
			int i = 0;
			// add graph data points
			for (Entry<Long, Integer> day_map_entry : day_map.entrySet()) {
				// normalize x values to begin with 0 by subtracting them from
				// the biggest occurred value, this transforms data like { 8, 5,
				// 3 } to { 0, 3, 5} to be plottable
				double x_value = x_value_biggest - day_map_entry.getKey(); // TODO
				double y_value = day_map_entry.getValue();
				data[i] = new GraphViewData(x_value, y_value);
				i++;
			}
			// add graph data with next color found in colorsDefault
			graphViewSeries.add(new GraphViewSeries(med_map_entry.getKey(),
					new GraphViewSeriesStyle(colorsDefault[j
							% colorsDefault.length], 3), data));
			j++;
		}

		// create GraphView object and add all series
		GraphView graphView = new LineGraphView(getActivity(),
				getString(R.string.graph_title)) {
			private List<Integer> value_y = new ArrayList<Integer>();
			private List<Integer> value_x = new ArrayList<Integer>();

			// use only integer labels and adapt the horizontal labels with nice
			// readable day strings like "today", "yesterday", "7 days before"
			@Override
			protected String formatLabel(double value, boolean isValueX) {
				int valueInt = (int) value;
				if (!isValueX && !value_y.contains(valueInt)) {
					value_y.add(valueInt);
					return "" + (valueInt);
				} else if (isValueX && !value_x.contains(valueInt)) {
					// adapt x labels to show how many days ago this was
					value_x.add(valueInt);
					int days_ago = (int) (x_value_biggest - valueInt);
					if (days_ago == 0)
						return getString(R.string.today);
					else if (days_ago == 1)
						return getString(R.string.yesterday);
					else
						return getResources().getQuantityString(
								R.plurals.days_ago, days_ago, days_ago);
				} else
					return "";
			}
		};
		// add all series to the GraphView object
		for (GraphViewSeries series : graphViewSeries) {
			graphView.addSeries(series);
		}

		// set default graph parameters
		graphView.setShowLegend(true);
		graphView.setLegendAlign(LegendAlign.BOTTOM);

		GraphViewStyle graphStyle = new GraphViewStyle();
		graphStyle.setLegendWidth(200);
		graphStyle.setVerticalLabelsColor(Color.BLACK);
		graphStyle.setHorizontalLabelsColor(Color.BLACK);
		graphView.setGraphViewStyle(graphStyle);

		// add the GraphView to the layout and return it
		layout.addView(graphView);
		return rootView;
	}
}
