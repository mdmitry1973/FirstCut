package com.mdmitry1973.firstcut;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
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
	
	private Spinner s;
	private PortManagerDialog portManagerDialog;
	
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
	
	private PortManagerInterface mFinishInterface;
	
	public interface PortManagerInterface {

		public void onPortManagerFinish();
	}
	
	public void setPortManagerInterface(PortManagerInterface finishInterface)
	{
		mFinishInterface = finishInterface;
	}

	public PortManagerDialog(Context context) {
		super(context);
		
		portManagerDialog = this;
		
		setContentView(R.layout.port_manager);
		setTitle(R.string.PortManager);
		setCanceledOnTouchOutside(false);
		
		((Button)findViewById(R.id.buttonOK)).setOnClickListener(this);
	    ((Button)findViewById(R.id.buttonCancel)).setOnClickListener(this);
	    ((Button)findViewById(R.id.buttonManualAdd)).setOnClickListener(this);
	    ((Button)findViewById(R.id.buttonEdit)).setOnClickListener(this);
	    ((Button)findViewById(R.id.buttonRemove)).setOnClickListener(this);
	    
	    s = (Spinner) findViewById(R.id.spinnerCurrentDevice);
	    
	    setListDevices();
	}
	
	public void setListDevices()
	{
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		
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
		   	    
		   	    if (arr.length > 2)
		   	    {
		   	    	String name = arr[0];
		   	    	
		   	    	if (name.startsWith("TYPE_") == true)
	   			   	{
		   	    		name = arr[1];
	   			   	}
		   	    	
		   	    	arrLines.add(name);
			   	    
			   	    if (currentName.length() > 0 && 
			   	    	name.contains(currentName) == true)
			   	    {
			   	    	sel = arrLines.size() - 1;
			   	    }
		   	    }
		   	}
	   	}
        
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, arrLines);
        s.setAdapter(adapter);
        
        if (sel == -1)
        {
        	sel = 0;
        }
        
        if (sel >=  arrLines.size())
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
		    case R.id.buttonEdit:
		    	onEdit(v);
		    	break; 
		    case R.id.buttonRemove:
		    	onRemove(v);
		    	break; 
		    default:                
		        break;
		   }
		}   
	
	public void onRemove(View v) {
	   	
	   	int sel = s.getSelectedItemPosition();
	   	
	   	if (sel != -1)
   		{
	   		SpinnerAdapter adp = s.getAdapter();
   			String currentName = (String)adp.getItem(sel);
   			
   			SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
   		   	String strPorts = sharedPrefs.getString("Ports", "");
   		   	
   		   	if (!strPorts.isEmpty())
		   	{
   		   		ArrayList<String> arrLines = new ArrayList<String>();
			   	String []arrPortLines = strPorts.split("\n");
		   		
		   		for (String strLine : arrPortLines) {
			   	    String []arr = strLine.split(",");
			   	    
			   	    if (arr.length > 2)
			   	    {
			   	    	String name = arr[0];
			   	    	
			   	    	if (name.startsWith("TYPE_") == true)
		   			   	{
			   	    		name = arr[1];
		   			   	}
			   	    	
			   	    	arrLines.add(name);
			   	    
			   	    	if (name.contains(currentName) == false)
			   	    	{
			   	    		arrLines.add(strLine);
			   	    	}
			   	    }
			   	}
		   		
		   		strPorts = "";
			   	
			   	for(int i = 0; i < arrLines.size(); i++)
			   	{
			   		String []arrPortLines2 = arrLines.get(i).split(",");
			   		
			   		if (arrPortLines2.length > 2)
			   		{
			   			strPorts += arrLines.get(i) + "\n";
			   		}
			   	}
			   	
			   	SharedPreferences.Editor editor = sharedPrefs.edit();
		    	editor.putString("Ports", strPorts);
				editor.commit();
		   	}
   			
	   		setListDevices();
   		}
    }
	
	public void onEdit(View v) {
	   	
	   	int sel = s.getSelectedItemPosition();
	   	 
   		if (sel != -1)
   		{
   			SpinnerAdapter adp = s.getAdapter();
   			String currentName = (String)adp.getItem(sel);
   			
   			SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
   		   	String strPorts = sharedPrefs.getString("Ports", "");
   		   	String line = "";
   		   	String portType = "TYPE_TCPIP";
   		   	
   		   	if (!strPorts.isEmpty())
   		   	{
   			   	String []arrPortLines = strPorts.split("\n");
   		   		
   		   		for (String strLine : arrPortLines) {
   			   	    String []arr = strLine.split(",");
   			   	    
   			   	    if (arr.length > 2)
   			   	    {
   			   	    	String strName = "";
		   			   	  
   			   	    	if (arr[0].startsWith("TYPE_") == true)
		   			   	{
   			   	    		strName = arr[1];
   			   	    		portType = arr[0];
		   			   	}
   			   	    	else
   			   	    	{
   			   	    		strName = arr[0];
   			   	    	}
	   			   	    
	   			   	    if (strName.contains(currentName) == true)
	   			   	    {
	   			   	    	line = strLine;
	   			   	    	break;
	   			   	    }
   			   	    }
   			   	}
   		   	}
   		   	
   		   	if (line.length() > 0)
	 		{
	 			if (portType.contains("TYPE_USB") == true)
			   	{
	 				  USBPortSettingsDialog dialogUSB = new USBPortSettingsDialog(portManagerDialog.getContext());
	            	  dialogUSB.setOnDismissListener(portManagerDialog);
	            	  dialogUSB.setDefaultSettings(line);
	            	  dialogUSB.show();
			   	}
	 			else
				{
	 				PortSettingsDialog dialog = new PortSettingsDialog(getContext());
	 		   		dialog.setOnDismissListener(this);
	 		   		dialog.setDefaultSettings(line);
	 		   		dialog.show();
				}
	 		}
   		}
    }
	
	public void onSearch(View v) {
	   
    }
	
	@Override
	public void onDismiss(DialogInterface dialogInterface)
	{
		setListDevices();
	}
	
	public void onManualAdd(View v) {
		
		UsbManager manager = (UsbManager) getContext().getSystemService(Context.USB_SERVICE);
		HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
		
		if (deviceList.size() > 0)//maybe should be 1, 0 only for test
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		    builder.setTitle(R.string.ChoosePortType)
		           .setItems(R.array.listPortTypes, new DialogInterface.OnClickListener() {
		               public void onClick(DialogInterface dialog, int which) {
		               if (which == 1)//usb
		               {
		            	   USBPortSettingsDialog dialogUSB = new USBPortSettingsDialog(portManagerDialog.getContext());
		            	   dialogUSB.setOnDismissListener(portManagerDialog);
		            	   dialogUSB.show();
		               }
		               else
		               {
		            	   	PortSettingsDialog dialogTCP = new PortSettingsDialog(portManagerDialog.getContext());
		            	   	dialogTCP.setOnDismissListener(portManagerDialog);
		            	   	dialogTCP.show();
		               }
		           }
		    }).show();
		}
		else
		{
			PortSettingsDialog dialog = new PortSettingsDialog(getContext());
   			dialog.setOnDismissListener(this);
   			dialog.show();
		}
    }
	
	public void onCancel(View v) {
	   	cancel();
    }
	
	public void onOK(View v) {
	   
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
	   	
		if (mFinishInterface != null)
		{
			mFinishInterface.onPortManagerFinish();
		}
		
	   	dismiss();
    }
}
