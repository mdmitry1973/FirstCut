package com.mdmitry1973.firstcut;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

public class PortManagerDialog extends Dialog implements OnClickListener, DialogInterface.OnDismissListener{
	
	/*
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
	    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	    // Get the layout inflater
	    LayoutInflater inflater = getActivity().getLayoutInflater();

	    // Inflate and set the layout for the dialog
	    // Pass null as the parent view because its going in the dialog layout
	    builder.setView(inflater.inflate(R.layout.device_manager, null));//
	    // Add action buttons
	       //    .setPositiveButton(R.string.signin, new DialogInterface.OnClickListener() {
	      //         @Override
	      //         public void onClick(DialogInterface dialog, int id) {
	     //              // sign in the user ...
	      //         }
	      //     })
	       //    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
	       //        public void onClick(DialogInterface dialog, int id) {
	        //           LoginDialogFragment.this.getDialog().cancel();
	       //        }
	       //    }); 
	    
	   
	    ((Button)getDialog().findViewById(R.id.buttonOK)).setOnClickListener(this);
	    ((Button)getDialog().findViewById(R.id.buttonCancel)).setOnClickListener(this);
	    ((Button)getDialog().findViewById(R.id.buttonManualAdd)).setOnClickListener(this);
	    
	    return builder.create();
	}
	*/
	

	public PortManagerDialog(Context context) {
		super(context);
		
		setContentView(R.layout.port_manager);
		setTitle(R.string.PortManager);
		setCanceledOnTouchOutside(false);
		
		((Button)findViewById(R.id.buttonOK)).setOnClickListener(this);
	    ((Button)findViewById(R.id.buttonCancel)).setOnClickListener(this);
	    ((Button)findViewById(R.id.buttonManualAdd)).setOnClickListener(this);
	    
	    setListDevices();
	}
	
	public void setListDevices()
	{
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		Spinner s = (Spinner) findViewById(R.id.spinnerCurrentDevice);
		
		SpinnerAdapter adp = s.getAdapter();
		String currentName = "";
		int sel = s.getSelectedItemPosition();
		
		if (adp != null && sel != -1)
		{
			currentName = (String)adp.getItem(sel);
		}
		else
		{
			currentName = sharedPrefs.getString("currentPort", "");
		}
		
	   	String strPorts = sharedPrefs.getString("Ports", "");
	   	List<String> arrLines = new ArrayList<String>();
	   	
	   	if (!strPorts.isEmpty())
	   	{
		   	String []arrPortLines = strPorts.split("\n");
	   		
	   		for (String strLine : arrPortLines) {
		   	    String []arr = strLine.split(",");
		   	    arrLines.add(arr[0]);
		   	    
		   	    if (currentName.length() > 0 && arr[0] == currentName)
		   	    {
		   	    	sel = arrLines.size() - 1;
		   	    }
		   	}
	   	}
        
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, arrLines);
        s.setAdapter(adapter);
        
        if (sel == -1)
        {
        	sel = 0;
        }
        
        s.setSelection(sel);
	}
	
	
	public void onClick(View v) {       
		  switch (v.getId()) {
		    case R.id.buttonOK:
		    	onOK(v);
		    break;  
		    case R.id.buttonCancel:
		    	onCancel(v);
		    break;  
		    case R.id.buttonManualAdd:
		    	onManualAdd(v);
		    break;  
		    default:                
		        break;
		   }
		}   
	
	public void onRemove(View v) {
	   	Log.v("DriverManagerDialog", "onRemove");
	   	 
    }
	
	public void onEdit(View v) {
	   	Log.v("DriverManagerDialog", "onEdit");
	   	 
    }
	
	public void onSearch(View v) {
	   	Log.v("DriverManagerDialog", "onSearch");
	   	 
    }
	
	@Override
	public void onDismiss(DialogInterface dialogInterface)
	{
		setListDevices();
	}
	
	public void onManualAdd(View v) {
	   	Log.v("DriverManagerDialog", "onManualAdd");
	   	
	   	//PortSettingsDialog newFragment = new PortSettingsDialog();
   	    //newFragment.show(getFragmentManager(), "PortSettingsDialog");
	   	
	   	PortSettingsDialog dialog = new PortSettingsDialog(getContext());
   		dialog.setOnDismissListener(this);
		dialog.show();
	   	 
    }
	
	public void onCancel(View v) {
	   	Log.v("DriverManagerDialog", "onCancel");
	   	 
	   	cancel();
    }
	
	public void onOK(View v) {
	   	Log.v("DriverManagerDialog", "onOK");
	   	
	   	Spinner s = (Spinner) findViewById(R.id.spinnerCurrentDevice);
		
		SpinnerAdapter adp = s.getAdapter();
		String currentName = "";
		int sel = s.getSelectedItemPosition();
		
		if (adp != null && sel != -1)
		{
			currentName = (String)adp.getItem(sel);
		}
		
		if (currentName.length() != 0)
		{
			SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
			
			SharedPreferences.Editor editor = sharedPrefs.edit();
	    	editor.putString("currentPort", currentName);
			editor.commit();
		}
	   	 
	   	dismiss();
    }
}
