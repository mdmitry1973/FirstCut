package com.mdmitry1973.firstcut;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

public class SendDialod  extends Dialog  implements OnClickListener {
	
	private Spinner spinnerPorts;
	private Spinner spinnerDevices;
	private SendDialoginterface licOk;
	
	public void setSendDialoginterface(SendDialoginterface licOk)
	{
		this.licOk = licOk;
	}

	public SendDialod(Context context) {
		super(context);
		
		setContentView(R.layout.send_dialog);
		setTitle(R.string.Send);
		setCanceledOnTouchOutside(false);
		
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		
		((Button)findViewById(R.id.buttonSend)).setOnClickListener(this);
		((Button)findViewById(R.id.buttonCancel)).setOnClickListener(this);
		
		spinnerPorts = (Spinner)findViewById(R.id.spinnerPorts);
		spinnerDevices = (Spinner)findViewById(R.id.spinnerDevices);
		
		{
			SpinnerAdapter adp = spinnerPorts.getAdapter();
			
			String strPorts = sharedPrefs.getString("Ports", "");
		   	List<String> arrLines = new ArrayList<String>();
		   	String currentName = sharedPrefs.getString("currentPort", "");
		   	int sel = 0;
		   	
		   	if (!strPorts.isEmpty())
		   	{
			   	String []arrPortLines = strPorts.split("\n");
		   		
		   		for (String strLine : arrPortLines) {
			   	    String []arr = strLine.split(",");
			   	    
				   	 if (arr.length > 2)
				   	 {
				   	    arrLines.add(arr[0]);
				   	    
				   	    if (currentName.length() > 0 && 
				   	    	arr[0].contains(currentName) == true)
				   	    {
				   	    	sel = arrLines.size() - 1;
				   	    }
				   	 }
			   	}
		   	}
	        
	        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, arrLines);
	        spinnerPorts.setAdapter(adapter);
	        spinnerPorts.setSelection(sel);
		}
		
		{
			SpinnerAdapter adp = spinnerDevices.getAdapter();
			int sel = 0;
			List<String> arrDevices = new ArrayList<String>();
			String currentDevice = sharedPrefs.getString("currentDevice", "HP-GL");
			
		   	if (sharedPrefs.contains("Devices"))
		   	{
		   		try {
		   			
			   		DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
					DocumentBuilder xmlBuilder = xmlFactory.newDocumentBuilder();
					InputStream stream = new ByteArrayInputStream(sharedPrefs.getString("Devices", "").getBytes("UTF-8"));
					
					Document documentCurrent = xmlBuilder.parse(stream);
					Element elRoot = documentCurrent.getDocumentElement();
					
					Element elFirstItem = (Element)elRoot.getFirstChild();
					
					while(elFirstItem != null)
					{
						String name = elFirstItem.getAttribute("name");
						
						arrDevices.add(name);
						
						if (name.compareTo(currentDevice) == 0)
						{
							sel = arrDevices.size() - 1;
						}
						
						elFirstItem = (Element)elFirstItem.getNextSibling();
					}
		   		} 
		   		catch (Exception e) 
		   		{
		        	Log.v("DeviceManagerDialog", "Error" + e);
		        } 
		   	}
		   	
	        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, arrDevices);
	        spinnerDevices.setAdapter(adapter);
	        spinnerDevices.setSelection(sel);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
	    case R.id.buttonSend:
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
	   	 
	   	cancel();
    }
	
	public void onOK(View v) {
		
		int curPort = spinnerPorts.getSelectedItemPosition();
		int curDevice = spinnerDevices.getSelectedItemPosition();
		
		SpinnerAdapter adpPorts = spinnerPorts.getAdapter();
		String currentPortsName = "";
		
		currentPortsName = (String)adpPorts.getItem(curDevice);
		
		SpinnerAdapter adpDevices = spinnerDevices.getAdapter();
		String currentDevicesName = "";
		
		currentDevicesName = (String)adpDevices.getItem(curDevice);
		
		
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		SharedPreferences.Editor editor = sharedPrefs.edit();
		
		if (currentDevicesName.length() != 0)
		{
	    	editor.putString("currentDevice", currentDevicesName);
	    	editor.putString("currentPort", currentPortsName);
		}
		
		editor.commit();
		
		licOk.OnSendDialog();
		
		dismiss();
	}
}
