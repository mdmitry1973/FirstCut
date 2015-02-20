package com.mdmitry1973.firstcut;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

public class DeviceManagerDialog extends Dialog implements OnClickListener, DialogInterface.OnDismissListener{
	
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
	
	Button	buttonOK;
	Button	buttonCancel;
    Button	buttonRemoveDevice;
    Button	buttonAdd;
    Button	buttonSave;
	
	public DeviceManagerDialog(Context context) {
		super(context);
		
		setContentView(R.layout.device_manager);
		setTitle(R.string.DeviceManager);
		setCanceledOnTouchOutside(false);
		
		buttonOK = (Button)findViewById(R.id.buttonOK);
		buttonCancel = (Button)findViewById(R.id.buttonCancel);
	    buttonRemoveDevice = (Button)findViewById(R.id.buttonRemoveDevice);
	    buttonAdd = (Button)findViewById(R.id.buttonAdd);
	    buttonSave = (Button)findViewById(R.id.buttonSave);
		
		buttonOK.setOnClickListener(this);
		buttonCancel.setOnClickListener(this);
		buttonRemoveDevice.setOnClickListener(this);
		buttonAdd.setOnClickListener(this);
		buttonSave.setOnClickListener(this);
	    
	    Spinner spinner = (Spinner) findViewById(R.id.spinnerCurrentDevice);
	    
	    spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				
				buttonRemoveDevice.setEnabled(position > 2);
				buttonSave .setEnabled(position > 2);
				
				setDeviceProperties();
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				
			}
	    });
	    
	    setListDevices();
	    
	    setDeviceProperties();
	}
	
	public void setDeviceProperties()
	{
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		Spinner s = (Spinner) findViewById(R.id.spinnerCurrentDevice);
		
		SpinnerAdapter adp = s.getAdapter();
		int sel = s.getSelectedItemPosition();
		
    	String resolution = "1016";
    	String absoluteCommand="PA";
		String relativeCommand="PR";
		String upCommand="PU";
		String downCommand="PD";
		String initCommand="IN;";
		String separator=";";
		String end="";
		String currentDevice = (String)adp.getItem(sel);
		
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
					
					if (name.compareTo(currentDevice) == 0)
					{
						resolution = elFirstItem.getAttribute("resolution");
						absoluteCommand = elFirstItem.getAttribute("absolute");
						relativeCommand = elFirstItem.getAttribute("relative");
						upCommand = elFirstItem.getAttribute("up");
						downCommand = elFirstItem.getAttribute("down");
						initCommand = elFirstItem.getAttribute("init");
						separator = elFirstItem.getAttribute("separator");
						end = elFirstItem.getAttribute("end");
						
						EditText editTextDeviceName = (EditText) findViewById(R.id.editTextDeviceName);
						EditText editTextResolution = (EditText) findViewById(R.id.editTextResolution);
						EditText editTextInit = (EditText) findViewById(R.id.editTextInit);
						EditText editTextAbsolute = (EditText) findViewById(R.id.editTextAbsolute);
						EditText editTextRelative = (EditText) findViewById(R.id.editTextRelative);
						EditText editTextUp = (EditText) findViewById(R.id.editTextUp);
						EditText editTextDown = (EditText) findViewById(R.id.editTextDown);
						EditText editTextSep = (EditText) findViewById(R.id.editTextSep);
						EditText editTextEnd = (EditText) findViewById(R.id.editTextEnd);
						
						editTextInit.setText(initCommand);
						editTextAbsolute.setText(absoluteCommand);
						editTextRelative.setText(relativeCommand);
						editTextUp.setText(upCommand);
						editTextDown.setText(downCommand);
						editTextSep.setText(separator);
						editTextResolution.setText(resolution);
						editTextEnd.setText(end);
						
						editTextDeviceName.setText(name);
						
						break;
					}
					
					elFirstItem = (Element)elFirstItem.getNextSibling();
				}
	   		} 
	   		catch (Exception e) 
	   		{
	        	Log.v("DeviceManagerDialog", "Error" + e);
	        } 
	   	}
	}
	
	public void setListDevices()
	{
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		Spinner s = (Spinner) findViewById(R.id.spinnerCurrentDevice);
		
		SpinnerAdapter adp = s.getAdapter();
		int sel = s.getSelectedItemPosition();
		List<String> arrDevices = new ArrayList<String>();
		String currentDevice = sharedPrefs.getString("currentDevice", "HP-GL");
		int indexCur = 0;
		
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
						indexCur = arrDevices.size() - 1;
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
        s.setAdapter(adapter);
        
        if (sel == -1 || sel >= arrDevices.size())
        {
        	sel = indexCur;
        }
        
        s.setSelection(sel);
        
        buttonRemoveDevice.setEnabled(sel > 2);
		buttonSave .setEnabled(sel > 2);
	}
	
	
	public void onClick(View v) {       
		  switch (v.getId()) {
		    case R.id.buttonOK:
		    	onOK(v);
		    break;  
		    case R.id.buttonCancel:
		    	onCancel(v);
		    break;  
		    case R.id.buttonRemoveDevice:
		    	onRemove(v);
		    break;  
		    case R.id.buttonAdd:
		    	onAdd(v);
		    break;  
		    case R.id.buttonSave:
		    	onSave(v);
		    break;  
		    
		    default:                
		        break;
		   }
		}   
	
	public void onRemove(View v) {
		Spinner s = (Spinner) findViewById(R.id.spinnerCurrentDevice);
		SpinnerAdapter adp = s.getAdapter();
		int sel = s.getSelectedItemPosition();
		String currentDevice = (String)adp.getItem(sel);
		
		if (currentDevice.contentEquals("HP-GL") == true ||
			currentDevice.contentEquals("GP-GL") == true)
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        	builder.setMessage(R.string.Warning1).setTitle(R.string.Warning).setPositiveButton("Ok", null);
	    	AlertDialog dialog = builder.create();
	    	
	    	dialog.show();
		}
		else
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        	builder.setMessage(R.string.Warning2).setTitle(R.string.Warning).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
        	    public void onClick(DialogInterface dialog, int id) {
        	       
        	    	Spinner s = (Spinner) findViewById(R.id.spinnerCurrentDevice);
        			SpinnerAdapter adp = s.getAdapter();
        			int sel = s.getSelectedItemPosition();
        			String currentDevice = (String)adp.getItem(sel);
        			SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        			
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
        						
        						if (name.compareTo(currentDevice) == 0)
        						{
        							elRoot.removeChild(elFirstItem);
        							
        							break;
        						}
        						
        						elFirstItem = (Element)elFirstItem.getNextSibling();
        					}
        					
        					StringWriter sw = new StringWriter();
            		        TransformerFactory tf = TransformerFactory.newInstance();
            		        Transformer transformer = tf.newTransformer();
            		       
            		        DOMSource source = new DOMSource(documentCurrent);
            		        transformer.transform(source, new StreamResult(sw));
            		        
            		    	SharedPreferences.Editor editor = sharedPrefs.edit();
            		    	editor.putString("Devices", sw.toString());
            				editor.commit();
            				
            				setListDevices();
        		   		} 
        		   		catch (Exception e) 
        		   		{
        		        	Log.v("DeviceManagerDialog", "Error" + e);
        		        } 
        		   	}
        	     }}
        			
        			).setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
                	    public void onClick(DialogInterface dialog, int id) {
                 	       
               	     }});
	    	AlertDialog dialog = builder.create();
	    	
	    	dialog.show();
		}
    }
	
	public void onAdd(View v) {
	 
		EditText editTextDeviceName = (EditText) findViewById(R.id.editTextDeviceName);
		EditText editTextResolution = (EditText) findViewById(R.id.editTextResolution);
		EditText editTextInit = (EditText) findViewById(R.id.editTextInit);
		EditText editTextAbsolute = (EditText) findViewById(R.id.editTextAbsolute);
		EditText editTextRelative = (EditText) findViewById(R.id.editTextRelative);
		EditText editTextUp = (EditText) findViewById(R.id.editTextUp);
		EditText editTextDown = (EditText) findViewById(R.id.editTextDown);
		EditText editTextSep = (EditText) findViewById(R.id.editTextSep);
		EditText editTextEnd = (EditText) findViewById(R.id.editTextEnd);
	   	 
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		Spinner s = (Spinner) findViewById(R.id.spinnerCurrentDevice);
		
		ArrayAdapter<String> adp = (ArrayAdapter<String>) s.getAdapter();
		
		for(int i = 0; i < adp.getCount(); i++)
		{
			if (adp.getItem(i).compareTo(editTextDeviceName.getText().toString()) == 0)
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
			    builder.setMessage(R.string.DeviceWrning).setTitle(R.string.app_name).setPositiveButton("Ok", null);
			    AlertDialog dialog = builder.create();
			    dialog.show();
			    
				return;
			}
		}
		
		if (sharedPrefs.contains("Devices"))
	   	{
	   		try {
	   			
		   		DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder xmlBuilder = xmlFactory.newDocumentBuilder();
				InputStream stream = new ByteArrayInputStream(sharedPrefs.getString("Devices", "").getBytes("UTF-8"));
				
				Document documentCurrent = xmlBuilder.parse(stream);
				Element elRoot = documentCurrent.getDocumentElement();
				
				Element elFirstItem = documentCurrent.createElement("item");
				
				elFirstItem.setAttribute("name", editTextDeviceName.getText().toString());
				elFirstItem.setAttribute("resolution", editTextResolution.getText().toString());
				elFirstItem.setAttribute("absolute", editTextAbsolute.getText().toString());
				elFirstItem.setAttribute("relative", editTextRelative.getText().toString());
				elFirstItem.setAttribute("up", editTextUp.getText().toString());
				elFirstItem.setAttribute("down", editTextDown.getText().toString());
				elFirstItem.setAttribute("init", editTextInit.getText().toString());
				elFirstItem.setAttribute("separator", editTextSep.getText().toString());
				elFirstItem.setAttribute("end", editTextEnd.getText().toString());
						
				elRoot.appendChild(elFirstItem);
				
				StringWriter sw = new StringWriter();
		        TransformerFactory tf = TransformerFactory.newInstance();
		        Transformer transformer = tf.newTransformer();
		       
		        DOMSource source = new DOMSource(documentCurrent);
		        transformer.transform(source, new StreamResult(sw));
		        
		    	SharedPreferences.Editor editor = sharedPrefs.edit();
		    	editor.putString("Devices", sw.toString());
				editor.commit();
				
				adp.add(editTextDeviceName.getText().toString());
				
				s.setSelection(adp.getCount() - 1);
				adp.setNotifyOnChange(true);
	   		} 
	   		catch (Exception e) 
	   		{
	        	Log.v("DeviceManagerDialog", "Error" + e);
	        } 
	   	}
    }
	
	public void onSave(View v) {
	   	
		EditText editTextDeviceName = (EditText) findViewById(R.id.editTextDeviceName);
		EditText editTextResolution = (EditText) findViewById(R.id.editTextResolution);
		EditText editTextInit = (EditText) findViewById(R.id.editTextInit);
		EditText editTextAbsolute = (EditText) findViewById(R.id.editTextAbsolute);
		EditText editTextRelative = (EditText) findViewById(R.id.editTextRelative);
		EditText editTextUp = (EditText) findViewById(R.id.editTextUp);
		EditText editTextDown = (EditText) findViewById(R.id.editTextDown);
		EditText editTextSep = (EditText) findViewById(R.id.editTextSep);
		EditText editTextEnd = (EditText) findViewById(R.id.editTextEnd);
	   	 
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		Spinner s = (Spinner) findViewById(R.id.spinnerCurrentDevice);
		
		SpinnerAdapter adp = s.getAdapter();
		int sel = s.getSelectedItemPosition();
		String currentDevice = (String)adp.getItem(sel);
		
		if (sharedPrefs.contains("Devices"))
	   	{
	   		try {
	   			
		   		DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder xmlBuilder = xmlFactory.newDocumentBuilder();
				InputStream stream = new ByteArrayInputStream(sharedPrefs.getString("Devices", "").getBytes("UTF-8"));
				
				Document documentCurrent = xmlBuilder.parse(stream);
				Element elRoot = documentCurrent.getDocumentElement();
				
				Element elFirstItem = (Element)elRoot.getFirstChild();
				boolean saved = false;
				
				while(elFirstItem != null)
				{
					String name = elFirstItem.getAttribute("name");
					
					if (name.compareTo(currentDevice) == 0)
					{
						elFirstItem.setAttribute("resolution", editTextResolution.getText().toString());
						elFirstItem.setAttribute("absolute", editTextAbsolute.getText().toString());
						elFirstItem.setAttribute("relative", editTextRelative.getText().toString());
						elFirstItem.setAttribute("up", editTextUp.getText().toString());
						elFirstItem.setAttribute("down", editTextDown.getText().toString());
						elFirstItem.setAttribute("init", editTextInit.getText().toString());
						elFirstItem.setAttribute("separator", editTextSep.getText().toString());
						elFirstItem.setAttribute("end", editTextEnd.getText().toString());
						
						saved = true;
						
						break;
					}
					
					elFirstItem = (Element)elFirstItem.getNextSibling();
				}
				
				if (saved == true)
				{
					StringWriter sw = new StringWriter();
			        TransformerFactory tf = TransformerFactory.newInstance();
			        Transformer transformer = tf.newTransformer();
			       
			        DOMSource source = new DOMSource(documentCurrent);
			        transformer.transform(source, new StreamResult(sw));
			        
			    	SharedPreferences.Editor editor = sharedPrefs.edit();
			    	editor.putString("Devices", sw.toString());
					editor.commit();
				}
				else
				{
					AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		        	builder.setMessage(R.string.Warning3).setTitle(R.string.Warning).setPositiveButton("Ok", null);
			    	AlertDialog dialog = builder.create();
			    	
			    	dialog.show();
				}
	   		} 
	   		catch (Exception e) 
	   		{
	        	Log.v("DeviceManagerDialog", "Error" + e);
	        } 
	   	}
    }
	
	
	//@Override
	public void onDismiss(DialogInterface dialogInterface)
	{
		//setListDevices();
	}
	
	public void onCancel(View v)
	{
		dismiss();
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
	    	editor.putString("currentDevice", currentName);
			editor.commit();
		}
	   	 
	   	dismiss();
    }
}
