package org.herndl.parkinsonapp.med;

import java.util.Collections;
import java.util.List;

import org.herndl.parkinsonapp.R;
import org.herndl.parkinsonapp.TaskHandler;
import org.herndl.parkinsonapp.med.MedReminderAddEditDialog.MedReminderAddEditDialogMode;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

// the fragment which holds the med reminder data
public class MedReminderFragment extends Fragment {

	public static List<MedReminderEntity> listMeds;
	public static ArrayAdapter<MedReminderEntity> adapterMeds;

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

		// OnItemClickListener for med edit actions
		listMedReminder.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				DialogFragment dialog = new MedReminderAddEditDialog(
						getString(R.string.med_reminder_edit_title),
						MedReminderAddEditDialogMode.EDIT, listMeds
								.get(position), position);
				dialog.show(getFragmentManager(), "dialog_med_reminder_add");
			}
		});

		// OnItemLongClickListener for med delete actions
		listMedReminder
				.setOnItemLongClickListener(new OnItemLongClickListener() {
					public boolean onItemLongClick(AdapterView<?> parent,
							View view, final int position, long id) {
						AlertDialog.Builder builder = new AlertDialog.Builder(
								getActivity());

						// dialog buttons
						builder.setTitle(
								String.format(
										getResources().getString(
												R.string.med_reminder_delete),
										listMeds.get(position).name))
								.setPositiveButton(android.R.string.ok,
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int id) {
												// remove med item from system
												removeMedItem(position);
											}
										})
								.setNegativeButton(
										android.R.string.cancel,
										new MedReminderFragment.DummyOnClickListener());

						// create the dialog
						AlertDialog dialog = builder.create();
						dialog.show();
						return true;
					}
				});

		// show MedReminderAddDialog when clicking on add button
		Button addButton = (Button) rootView
				.findViewById(R.id.med_reminder_button_add);
		addButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				DialogFragment dialog = new MedReminderAddEditDialog(
						getString(R.string.med_reminder_add_title),
						MedReminderAddEditDialogMode.ADD, null, 0);
				dialog.show(getFragmentManager(), "dialog_med_reminder_add");
			}
		});

		return rootView;
	}

	// helper onClickListener class which does nothing, this is useful for
	// cancel actions which should just close dialogs
	public static class DummyOnClickListener implements
			DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			// do nothing
		}
	}

	// helper methode for adding a med item
	public static void addMedItem(MedReminderEntity med) {
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

	// helper methode for removing a med item
	public static void removeMedItem(int position) {
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

	// helper methode for editing a med item
	public static void editMedItem(int position, MedReminderEntity med) {
		removeMedItem(position);
		addMedItem(med);
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
}
