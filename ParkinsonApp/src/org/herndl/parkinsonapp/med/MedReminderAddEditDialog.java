package org.herndl.parkinsonapp.med;

import org.herndl.parkinsonapp.R;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

public class MedReminderAddEditDialog extends DialogFragment {
	private String dialogTitle;
	private int mode;
	private MedReminderEntity medOld;
	private int medOldListPos;

	private static View view;
	private static EditText input_med_name;
	private static EditText input_med_dose;
	private static TimePicker input_med_time;

	public static class MedReminderAddEditDialogMode {
		public static final int ADD = 1;
		public static final int EDIT = 2;
	}

	public MedReminderAddEditDialog(String dialogTitle, int mode,
			MedReminderEntity medOld, int medOldListPos) {
		super();
		this.dialogTitle = dialogTitle;
		this.mode = mode;
		this.medOld = medOld;
		this.medOldListPos = medOldListPos;
	}

	@SuppressLint("InflateParams")
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		super.onCreateDialog(savedInstanceState);
		setRetainInstance(true);

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		view = inflater.inflate(R.layout.dialog_med_reminder_add_edit, null);

		// get and set all input elements for later actions
		getAllInputElements();

		// adapt time picker to locale of the user
		input_med_time
				.setIs24HourView(DateFormat.is24HourFormat(getActivity()));

		// prefill input values
		prefillValues();

		builder.setView(view);

		builder.setTitle(dialogTitle)
				// ok button handling
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								// read user input
								String med_name = input_med_name.getText()
										.toString().trim();
								String med_dose = input_med_dose.getText()
										.toString().trim();

								// check inputs (.isEquals is API level 9,
								// we are targeting level 8 here)
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
									med_dose_int = Integer.parseInt(med_dose);
								} catch (NumberFormatException e) {
									Log.w("MedReminderAddEditDialog:PositiveButton",
											"can't parse int from string "
													+ med_dose);
								}

								// create new MedReminderEntity
								MedReminderEntity med = new MedReminderEntity(
										med_name, med_dose_int, input_med_time
												.getCurrentHour(),
										input_med_time.getCurrentMinute());

								// add to list and DB
								if (mode == MedReminderAddEditDialogMode.ADD)
									MedReminderFragment.addMedItem(med);
								else if (mode == MedReminderAddEditDialogMode.EDIT)
									MedReminderFragment.editMedItem(
											medOldListPos, med);
							}
						})
				// cancel button handling
				.setNegativeButton(android.R.string.cancel,
						new MedReminderFragment.DummyOnClickListener());
		// create the AlertDialog object and return it
		return builder.create();
	}

	private void getAllInputElements() {
		input_med_name = (EditText) view.findViewById(R.id.add_med_name);
		input_med_dose = (EditText) view.findViewById(R.id.add_med_dose);
		input_med_time = (TimePicker) view.findViewById(R.id.add_med_time);
	}

	// sets the GUI values to the one of the old med object
	private void prefillValues() {
		if (medOld == null)
			return;
		input_med_name.setText(medOld.name);
		input_med_dose.setText("" + medOld.dose);
		input_med_time.setCurrentHour(medOld.remind_hour);
		input_med_time.setCurrentMinute(medOld.remind_minute);
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
