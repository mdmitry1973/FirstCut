package com.mdmitry1973.firstcut;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

public class USBPortSettingsDialog extends Dialog implements OnClickListener{
	
	private Spinner spinnerUSBPorts;
	private EditText editName;
	
	public USBPortSettingsDialog(Context context) {
		super(context);
		
		setContentView(R.layout.port_usb_settings);
		setTitle(R.string.PortSettings);
		setCanceledOnTouchOutside(false);
		
		editName = ((EditText)findViewById(R.id.editTextName));
		spinnerUSBPorts = ((Spinner)findViewById(R.id.spinnerUSBPorts));
	   
		((Button)findViewById(R.id.buttonOk)).setOnClickListener(this);
		((Button)findViewById(R.id.buttonCancel)).setOnClickListener(this);
		
		UsbManager manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
		List<String> arrLines = new ArrayList<String>();
    	
    	if (manager != null)
    	{
    		HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
    		Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
    		while(deviceIterator.hasNext()){
    		    UsbDevice device = deviceIterator.next();
    		    
    		    if (device != null)
    		    {
    		    	//strMsgDev += String.format("DeviceName=%s\n", device.getDeviceName());
    		    	//strMsgDev += String.format("ManufacturerName=%s\n", device);
    		    	//strMsgDev += String.format("ProductId=%d\n", device.getProductId());	        		    	 
    		    	//strMsgDev += String.format("VendorId=%d\n", device.getVendorId());
    		    	
    		    	arrLines.add(device.getDeviceName());
    		    }
    		}
    	}
    	else
    	{
    		AlertDialog.Builder builder = new AlertDialog.Builder(context);
        	builder.setMessage(R.string.NotFindPorts).setTitle("Error").setPositiveButton("Ok", null);
	    	AlertDialog dialog = builder.create();
	    	
	    	dialog.show();
	    	
	    	//dismiss();
    	}
    	
    	ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, arrLines);
    	
    	spinnerUSBPorts.setAdapter(adapter);
    	spinnerUSBPorts.setSelection(0);
	}
	
	public void setDefaultSettings(String line)
	{
		String []arr = line.split(",");
		
		if (arr.length > 1)
		{
			String strType = arr[0];
			String strName = "";
			String strPort = "";
			
			if (strType.startsWith("TYPE_") == true)
			{
				strName = arr[1];
				strPort = arr[2];
			}
			else
			{
				strName = arr[0];
				strPort = arr[1];
			}
			
			//editName.setText(strName);
			//editTextIP.setText(strIP);
	   		//editPortNumber.setText(strPort);
			
			SpinnerAdapter adp = spinnerUSBPorts.getAdapter();
			
			for(int i = 0; i < adp.getCount(); i++)
			{
				if (strPort.contentEquals((String)adp.getItem(i)) == true)
				{
					editName.setText(strName);
					spinnerUSBPorts.setSelection(i);
					break;
				}
			}
		}
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
	   	Log.v("PortManagerDialog", "onCancel");
	   	 
	   	cancel();
    }
	
	public void onOK(View v) {
	   	Log.v("PortManagerDialog", "onOK");
	   	
	   	if (editName.getText().length() > 0)
	    {
		   	String portSettings = "";
		   	SpinnerAdapter adp = spinnerUSBPorts.getAdapter();
			int sel = spinnerUSBPorts.getSelectedItemPosition();
		   	
			if (sel < adp.getCount())
			{
				String itemName = (String) adp.getItem(sel);
				
			   	portSettings += "TYPE_USB";
			   	portSettings += ",";
			   	portSettings += editName.getText();
			   	portSettings += ",";
			   	portSettings += itemName;
			   	
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
				   	    	if (arr[0].contentEquals(editName.getText()) == false)
				   	    	{
				   	    		arrLines.add(s);
				   	    	}
				   	    	else
				   	    	{
				   	    		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
						    	builder.setMessage(R.string.ChangePortName).setTitle(R.string.app_name).setPositiveButton("Ok", null);
						    	AlertDialog dialog = builder.create();
						    	dialog.show();
						    	
						    	return;
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
			}
	    }
	   	else
	   	{
	   		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		    builder.setMessage(R.string.EmptyName).setTitle(R.string.app_name).setPositiveButton("Ok", null);
		    AlertDialog dialog = builder.create();
		    dialog.show();
	   	}
	   	 
		dismiss();
    }
}
