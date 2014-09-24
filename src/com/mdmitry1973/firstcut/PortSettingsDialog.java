package com.mdmitry1973.firstcut;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class PortSettingsDialog extends Dialog implements OnClickListener{//Fragment implements OnClickListener{
	
	private EditText editName;
	private EditText editPortNumber;
	private EditText editTextIP;
	
	/*
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
	    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	    // Get the layout inflater
	    LayoutInflater inflater = getActivity().getLayoutInflater();

	    // Inflate and set the layout for the dialog
	    // Pass null as the parent view because its going in the dialog layout
	    builder.setView(inflater.inflate(R.layout.port_settings, null));//
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
	    
	    return builder.create();
	}
	*/
	
	public PortSettingsDialog(Context context) {
		super(context);
		
		setContentView(R.layout.port_settings);
		setTitle(R.string.PortSettings);
		setCanceledOnTouchOutside(false);
		
		editName = ((EditText)findViewById(R.id.editTextName));
	   	editPortNumber = ((EditText)findViewById(R.id.editTextPortNumber));
	   	editTextIP = ((EditText)findViewById(R.id.editTextIP));
	   	
	   	editPortNumber.setText("9100");
		
		((Button)findViewById(R.id.buttonOk)).setOnClickListener(this);
		((Button)findViewById(R.id.buttonCancel)).setOnClickListener(this);
	}
	
	public void setDefaultSettings(String name, String portNumber, String ip)
	{
		editName.setText(name);
		editTextIP.setText(ip);
	   	editPortNumber.setText(portNumber);
	}
	
	public void onClick(View v) {       
		  switch (v.getId()) {
		    case R.id.buttonOk:
		    	onOK(v);
		    break;  
		    case R.id.buttonCancel:
		    	onCancel(v);
		    break;  
		    default:                
		        break;
		   }
		}   

	public void onCancel(View v) {
	   	Log.v("DriverManagerDialog", "onCancel");
	   	 
	   	cancel();
    }
	
	public void onOK(View v) {
	   	Log.v("DriverManagerDialog", "onOK");
	   	
	   	editName = ((EditText)findViewById(R.id.editTextName));
	    editPortNumber = ((EditText)findViewById(R.id.editTextPortNumber));
	    editTextIP = ((EditText)findViewById(R.id.editTextIP));
	   	
	   	String portSettings = "";
	   	
	   	portSettings += editName.getText();
	   	portSettings += ",";
	   	portSettings += editPortNumber.getText();
	   	portSettings += ",";
	   	portSettings += editTextIP.getText();
	   	
	   	SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
	   	
	   	String strPorts = sharedPrefs.getString("Ports", "");
	   	ArrayList<String> arrLines = new ArrayList<String>();
	   	
	   	if (strPorts.length() != 0)
	   	{
	   		String []arrPortLines = strPorts.split("\n");
	   		
	   		for (String s : arrPortLines) {
		   	    String []arr = s.split(",");
		   	    
		   	    if (arr.length > 2)
		   	    {
		   	    	if (arr[0].contains(editName.getText()) == false)
		   	    	{
		   	    		arrLines.add(s);
		   	    	}
		   	    }
		   	}
	   	}
	   	
	   	arrLines.add(portSettings);
	   	
	   	strPorts = "";
	   	
	   	for(int i = 0; i < arrLines.size(); i++)
	   	{
	   		strPorts += (arrLines.get(i) + "\n");
	   	}
	   	
	   	SharedPreferences.Editor editor = sharedPrefs.edit();
    	editor.putString("Ports", strPorts);
		editor.commit();
	   	 
		dismiss();
    }
}
