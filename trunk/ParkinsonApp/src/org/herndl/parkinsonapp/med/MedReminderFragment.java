package org.herndl.parkinsonapp.med;

import java.util.Collections;
import java.util.List;

import org.herndl.parkinsonapp.R;
import org.herndl.parkinsonapp.TaskHandler;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

// the fragment which holds the med reminder data
public class MedReminderFragment extends Fragment {

	private List<MedReminderEntity> listMeds;
	private ArrayAdapter<MedReminderEntity> adapterMeds;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View rootView = inflater.inflate(R.layout.fragment_med_reminder,
				container, false);

		ListView listMedReminder = (ListView) rootView
				.findViewById(R.id.med_reminder_list);

		// query all meds from DB
		listMeds = MedReminderEntity.listAll(MedReminderEntity.class);

		// show instructions if nothing found
		TextView med_reminder_empty = (TextView) rootView
				.findViewById(R.id.med_reminder_empty);
		listMedReminder.setEmptyView(med_reminder_empty);

		// sort after remind time with custom comparator
		Collections.sort(listMeds, new MedReminderEntity.medComparator());

		// set adapter which handles filling the list with data
		adapterMeds = new MedReminderAdapter(rootView.getContext(), listMeds);
		listMedReminder.setAdapter(adapterMeds);

		// onItemClick handler for med delete actions
		listMedReminder.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, final View view,
					final int position, long id) {
				final MedReminderEntity med = listMeds.get(position);

				AlertDialog.Builder builder = new AlertDialog.Builder(
						getActivity());
				// dialog buttons
				builder.setTitle(
						String.format(
								getResources().getString(
										R.string.med_reminder_delete), med.name))
						.setPositiveButton(android.R.string.ok,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										// remove med item from system
										removeMedItem(position);
									}
								})
						.setNegativeButton(android.R.string.cancel,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										// do nothing
									}
								});

				// creat the dialog
				AlertDialog dialog = builder.create();
				dialog.show();
			}
		});

		// show MedReminderAddDialog when clicking on add button
		Button addButton = (Button) rootView
				.findViewById(R.id.med_reminder_button_add);
		addButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				DialogFragment dialog = new MedReminderAddDialog();
				dialog.show(getFragmentManager(), "dialog_med_reminder_add");
			}
		});

		return rootView;
	}

	// helper methode for removing a med item
	public void removeMedItem(int position) {
		MedReminderEntity med = listMeds.get(position);
		// remove reminder via TaskHandler
		TaskHandler.cancelMedAlarm(med);
		// remove entity from DB
		med.delete();
		// remove from med list
		listMeds.remove(position);
		// update list view via adapter
		adapterMeds.notifyDataSetChanged();
	}

	// helper methode for adding a med item
	public void addMedItem(MedReminderEntity med) {
		// save entity in DB
		med.save();
		// add to med list
		listMeds.add(med);
		// sort after remind time with custom comparator
		Collections.sort(listMeds, new MedReminderEntity.medComparator());
		// set reminder via TaskHandler
		TaskHandler.setMedAlarm(med);
		// update list view via adapter
		adapterMeds.notifyDataSetChanged();
	}

	// custom data adapter for the list view
	public class MedReminderAdapter extends ArrayAdapter<MedReminderEntity> {
		private final Context context;
		private final List<MedReminderEntity> listMeds;

		public MedReminderAdapter(Context context,
				List<MedReminderEntity> listMeds) {
			super(context, R.layout.view_element_med_reminder, listMeds);
			this.context = context;
			this.listMeds = listMeds;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// reuse view
			if (convertView == null) {
				LayoutInflater inflater = (LayoutInflater) context
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(
						R.layout.view_element_med_reminder, parent, false);
			}

			TextView med_name = (TextView) convertView
					.findViewById(R.id.view_med_name);
			TextView med_dose = (TextView) convertView
					.findViewById(R.id.view_med_dose);
			TextView med_time = (TextView) convertView
					.findViewById(R.id.view_med_time);

			// set MedReminderEntity data in gui views
			MedReminderEntity med = listMeds.get(position);
			med_name.setText(med.name);
			med_dose.setText(getResources().getQuantityString(
					R.plurals.med_dose, med.dose, med.dose));
			med_time.setText(String.format("%02d:%02d", med.remind_hour,
					med.remind_minute));

			return convertView;
		}
	}

	// med reminder add dialog
	@SuppressLint("InflateParams")
	public class MedReminderAddDialog extends DialogFragment {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			super.onCreateDialog(savedInstanceState);
			setRetainInstance(true);

			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			LayoutInflater inflater = getActivity().getLayoutInflater();

			final View view = inflater.inflate(
					R.layout.dialog_med_reminder_add, null);
			TimePicker input_med_time = (TimePicker) view
					.findViewById(R.id.add_med_time);
			// adapt time picker to user locale
			input_med_time.setIs24HourView(DateFormat
					.is24HourFormat(getActivity()));
			builder.setView(view);

			builder.setTitle(R.string.med_reminder_add_title)
					// ok button handling
					.setPositiveButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									// get all input elements
									EditText input_med_name = (EditText) view
											.findViewById(R.id.add_med_name);
									EditText input_med_dose = (EditText) view
											.findViewById(R.id.add_med_dose);
									TimePicker input_med_time = (TimePicker) view
											.findViewById(R.id.add_med_time);

									// read user input
									String med_name = input_med_name.getText()
											.toString().trim();
									String med_dose = input_med_dose.getText()
											.toString().trim();

									// check input (.isEquals is API level 9)
									if (med_name.equals("")) {
										Toast.makeText(
												getActivity()
														.getApplicationContext(),
												R.string.med_reminder_add_name_required,
												Toast.LENGTH_SHORT).show();
										return;
									}
									if (med_dose.equals("")) {
										Toast.makeText(
												getActivity()
														.getApplicationContext(),
												R.string.med_reminder_add_dose_required,
												Toast.LENGTH_SHORT).show();
										return;
									}

									// parse dose from string
									int med_dose_int = 0;
									try {
										med_dose_int = Integer
												.parseInt(med_dose);
									} catch (NumberFormatException e) {
										Log.w("MedReminderAddDialog:PositiveButton",
												"can't parse int from string "
														+ med_dose);
									}

									// create new MedReminderEntity
									MedReminderEntity med = new MedReminderEntity(
											med_name, med_dose_int,
											input_med_time.getCurrentHour(),
											input_med_time.getCurrentMinute());

									// add to list and DB
									addMedItem(med);
								}
							})
					// cancel button handling
					.setNegativeButton(android.R.string.cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									// do nothing
								}
							});
			// create the AlertDialog object and return it
			return builder.create();
		}

		// handling of fragment recreation bug in support library
		// see https://code.google.com/p/android/issues/detail?id=17423
		// e.g. retains the dialog on rotations
		@Override
		public void onDestroyView() {
			if (getDialog() != null && getRetainInstance())
				getDialog().setDismissMessage(null);
			super.onDestroyView();
		}
	}
}
