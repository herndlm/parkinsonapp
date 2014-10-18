package org.herndl.parkinsonapp;

import java.util.List;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

public class MedReminderFragment extends Fragment {
	
	private List<MedReminderEntity> listMeds;
	private ArrayAdapter<MedReminderEntity> adapterMeds;
		
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_med_reminder, container, false);
        
        // get all saved med reminders and fill list
        ListView listMedReminder = (ListView) rootView.findViewById(R.id.med_reminder_list);
        
        // query all meds from DB
        listMeds = MedReminderEntity.listAll(MedReminderEntity.class);

        // set adapter which handles filling the list with data
        adapterMeds = new MedReminderAdapter(rootView.getContext(), listMeds);
        listMedReminder.setAdapter(adapterMeds);
        
        // onItemClick handler for med delete actions
        listMedReminder.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, final View view, final int position, long id) {
            	final MedReminderEntity med = listMeds.get(position);
            	
            	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            	// Add the buttons
            	builder.setTitle(String.format("Soll die Erinnerung an %s wirklich gelöscht werden?", med.name))
                       .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            	           public void onClick(DialogInterface dialog, int id) {
            	        	   // remove med from list and DB
            	        	   removeMedItem(position);
            	           }
            	       })
            	       .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            	           public void onClick(DialogInterface dialog, int id) {
            	        	   // do nothing
            	           }
            	       });

            	// Create the AlertDialog
            	AlertDialog dialog = builder.create();
            	dialog.show();
            }
        });
        
        // start MedReminderAddDialog when clicking on add button
        Button addButton = (Button) rootView.findViewById(R.id.med_reminder_button_add);
        addButton.setOnClickListener(new View.OnClickListener() {	
			@Override
			public void onClick(View v) {
				DialogFragment dialog = new MedReminderAddDialog();
				dialog.show(getFragmentManager(), "dialog_med_reminder_add");
			}
		});

        return rootView;
    }
    
    public void removeMedItem(int position) {
    	// remove entity from DB
    	listMeds.get(position).delete();
    	// remove from list
    	listMeds.remove(position);
    	// update view via adapter
    	adapterMeds.notifyDataSetChanged();
    }
    
    public void addMedItem(MedReminderEntity item) {
    	// save entity in DB
    	item.save();
    	// add to list
    	listMeds.add(item);
    	// update view via adapter
    	adapterMeds.notifyDataSetChanged();
    }
    
    public class MedReminderAdapter extends ArrayAdapter<MedReminderEntity> {
    	  private final Context context;
    	  private final List<MedReminderEntity> listMeds;

    	  public MedReminderAdapter(Context context, List<MedReminderEntity> listMeds) {
    	    super(context, R.layout.view_element_med_reminder, listMeds);
    	    this.context = context;
    	    this.listMeds = listMeds;
    	  }

    	  @Override
    	  public View getView(int position, View convertView, ViewGroup parent) {
    		  // reuse view
    		  if (convertView == null) {
    			  LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    			  convertView = inflater.inflate(R.layout.view_element_med_reminder, parent, false);
    		  }
    		  
    		  // TODO mark already taken meds?
			  TextView med_name = (TextView) convertView.findViewById(R.id.view_med_name);
			  TextView med_dose = (TextView) convertView.findViewById(R.id.view_med_dose);
			  TextView med_time = (TextView) convertView.findViewById(R.id.view_med_time);

			  // set MedReminderEntity data in gui views
			  MedReminderEntity med = listMeds.get(position);
			  med_name.setText(med.name);
			  med_dose.setText(String.format("%d", med.dose));
			  med_time.setText(String.format("%02d:%02d", med.remind_hour, med.remind_minute));
			  
    		  return convertView;
    	  }
    }
    
    @SuppressLint("InflateParams")
	public class MedReminderAddDialog extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
        	super.onCreateDialog(savedInstanceState);
        	setRetainInstance(true);
        	  
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            // Get the layout inflater
            LayoutInflater inflater = getActivity().getLayoutInflater();
            
            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            final View view = inflater.inflate(R.layout.dialog_med_reminder_add, null);
            builder.setView(view);
            
            builder.setTitle("Reminder Add")
                   .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface dialog, int id) {
                    	   // create new med entity with user input
                    	   EditText med_name = (EditText) view.findViewById(R.id.add_med_name);
                    	   EditText med_dose = (EditText) view.findViewById(R.id.add_med_dose);
                    	   TimePicker med_time = (TimePicker) view.findViewById(R.id.add_med_time);
                    	   MedReminderEntity med = new MedReminderEntity(med_name.getText().toString(), Integer.parseInt(med_dose.getText().toString()), med_time.getCurrentHour(), med_time.getCurrentMinute());
                    	   // add to list and DB
                    	   addMedItem(med);
                       }
                   })
                   .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface dialog, int id) {
                           // do nothing
                       }
                   });
            // Create the AlertDialog object and return it
            return builder.create();
        }
        
        // unclean handle of fragment recreation bug in support library
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
