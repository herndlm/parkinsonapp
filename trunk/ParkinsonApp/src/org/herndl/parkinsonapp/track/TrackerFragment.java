package org.herndl.parkinsonapp.track;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.herndl.parkinsonapp.R;
import org.herndl.parkinsonapp.med.MedReminderFragment;
import org.joda.time.Duration;
import org.joda.time.Interval;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphView.LegendAlign;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.GraphViewStyle;
import com.jjoe64.graphview.LineGraphView;
import com.orm.SugarRecord;

// tracker fragment which holds the GraphView object
public class TrackerFragment extends Fragment {

	// colors for graphs which are used in this order and start from the
	// beginning if used up
	private static int[] colorsDefault = { Color.BLUE, Color.RED, Color.GREEN,
			Color.CYAN, Color.MAGENTA };
	// max days to show in the viewport (then it gets scrollable)
	private static int viewPortMaxDays = 31;
	// long value which is used to normalize data by subtracting it from the
	// biggest occurred value
	private long x_value_biggest = 0;
	// all tracker entities
	private static List<TrackerEntity> entities;
	// rootView of the fragment
	private static View rootView;
	// graphView object
	private GraphView graphView = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		rootView = inflater
				.inflate(R.layout.fragment_tracker, container, false);

		// get all tracker entities
		entities = SugarRecord.listAll(TrackerEntity.class);

		// show TrackAddDialog when clicking on add button
		Button addButton = (Button) rootView
				.findViewById(R.id.button_tracker_add);
		addButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				DialogFragment dialog = new TrackAddDialog();
				dialog.show(getFragmentManager(), "dialog_track_add");
			}
		});

		initGraphView();

		return rootView;
	}

	private void initGraphView() {
		// hide default shown instructions if enough data found
		if (entities.size() > 1) {
			TextView tracker_empty = (TextView) rootView
					.findViewById(R.id.tracker_empty);
			tracker_empty.setVisibility(View.GONE);
		} else
			return;

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

			Calendar calendar_entry = entity.calendar;
			Calendar calendar_now = Calendar.getInstance();

			// ignore future entries
			if (calendar_entry.after(calendar_now))
				continue;

			// create an interval with Joda-Time
			Interval interval = new Interval(calendar_entry.getTimeInMillis(),
					calendar_now.getTimeInMillis());
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
		graphView = new LineGraphView(getActivity(),
				getString(R.string.graph_title)) {
			// use only integer labels and adapt the horizontal labels with nice
			// readable day strings like "today", "yesterday", "7 days before"
			@Override
			protected String formatLabel(double value, boolean isValueX) {
				int valueInt = (int) value;
				if (!isValueX) {
					Log.v("graph", "y " + valueInt);
					return "" + (valueInt);
				} else if (isValueX) {
					// adapt x labels to show how many days ago this was
					int days_ago = (int) (x_value_biggest - valueInt);
					if (days_ago == 0)
						return getString(R.string.today);
					else if (days_ago == 1)
						return getString(R.string.yesterday);
					else
						return getResources().getQuantityString(
								R.plurals.days_ago, days_ago, days_ago);
				} else {
					return "";
				}
			}
		};
		// add all series to the GraphView object
		for (GraphViewSeries series : graphViewSeries) {
			graphView.addSeries(series);
		}

		// limit viewport to 1 month
		if (x_value_biggest > TrackerFragment.viewPortMaxDays)
			graphView.setViewPort(x_value_biggest
					- TrackerFragment.viewPortMaxDays,
					TrackerFragment.viewPortMaxDays);
		// default style paramaters
		graphView.setScalable(true);
		graphView.setScrollable(true);
		graphView.setShowLegend(true);
		graphView.setLegendAlign(LegendAlign.BOTTOM);

		GraphViewStyle graphStyle = new GraphViewStyle();
		graphStyle.setLegendWidth(200);
		graphStyle.setVerticalLabelsColor(Color.BLACK);
		graphStyle.setHorizontalLabelsColor(Color.BLACK);
		graphView.setGraphViewStyle(graphStyle);

		// add the GraphView to its layout
		LinearLayout layout = (LinearLayout) rootView
				.findViewById(R.id.tracker_graph_holder);
		layout.addView(graphView);
	}

	private class TrackAddDialog extends DialogFragment {
		@SuppressLint("InflateParams")
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			super.onCreateDialog(savedInstanceState);
			setRetainInstance(true);

			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			LayoutInflater inflater = getActivity().getLayoutInflater();
			final View view = inflater.inflate(R.layout.dialog_track_add, null);

			builder.setView(view);

			builder.setTitle(R.string.tracker_add_title)
					// ok button handling
					.setPositiveButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int id) {
									// get all user inputs
									EditText input_track_name = (EditText) view
											.findViewById(R.id.track_name);
									EditText input_track_value = (EditText) view
											.findViewById(R.id.track_value);
									DatePicker input_track_date = (DatePicker) view
											.findViewById(R.id.track_date);

									// read user input
									String track_name = input_track_name
											.getText().toString().trim();
									String track_value = input_track_value
											.getText().toString().trim();

									// check inputs (.isEquals is API level 9,
									// we are targeting level 8 here)
									if (track_name.equals("")) {
										Toast.makeText(
												getActivity()
														.getApplicationContext(),
												R.string.med_reminder_add_name_required,
												Toast.LENGTH_SHORT).show();
										return;
									}
									if (track_value.equals("")) {
										Toast.makeText(
												getActivity()
														.getApplicationContext(),
												R.string.med_reminder_add_dose_required,
												Toast.LENGTH_SHORT).show();
										return;
									}

									// parse value from string
									int track_value_int = 0;
									try {
										track_value_int = Integer
												.parseInt(track_value);
									} catch (NumberFormatException e) {
										Log.w("TrackAddDialog:PositiveButton",
												"can't parse int from string "
														+ track_value);
									}

									// create new tracker element
									Calendar calendar = Calendar.getInstance();
									calendar.set(input_track_date.getYear(),
											input_track_date.getMonth(),
											input_track_date.getDayOfMonth());
									TrackerEntity trackerEntity = new TrackerEntity(
											"med", track_name, track_value_int,
											null, calendar);

									// add to list and save to DB
									entities.add(trackerEntity);
									trackerEntity.save();

									// refresh graph
									if (graphView != null) {
										((ViewGroup) graphView.getParent())
												.removeView(graphView);
										graphView = null;
									}
									initGraphView();
								}
							})
					// cancel button handling
					.setNegativeButton(android.R.string.cancel,
							new MedReminderFragment.DummyOnClickListener());
			// create the AlertDialog object and return it
			return builder.create();
		}
	}
}
