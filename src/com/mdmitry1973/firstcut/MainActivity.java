package com.mdmitry1973.firstcut;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.Socket;
import java.nio.charset.CharsetEncoder;
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

import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ToggleButton;

public class MainActivity extends Activity implements DialogInterface.OnDismissListener, ToolsDialogInterface  {
	
	public ProgressDialog progressDialog;
	
	PageViewer pageViewer;
	RulerViewer rulerVer;
	RulerViewer rulerHor;
	
	boolean bEnableFilePort = true;
	MainActivity m_activity = null;
	
	Button clearBtn;
    //Button lineBtn;
    //Button penBtn;
    ToggleButton handBtn;
    ToggleButton toolBtn;
	
	enum ToolType {
		Line,
		Pen,
		Box,
		Hand
	};
	
	/*
	 * Devices structure
	 <Devices>
	    <item 
	    	name="HP-GL"
	    	resolution="1016"
	      	absolute="PA"
	      	relative="PR"
	      	up="PU"
	      	down="PD"
	      	init="IN;"
	      	separator=";"
	    	>
	      <Settings>
	      	<item 
	      		name="Tool"
	      		type="choice"
	      		format="SP%d;"
	      		>
	      		<value name="Pen" number="1"/> 
	      		<value name="Knife" number="2"/>
	      	</item>
	      </Settings>
	    </item>
	    <item
	    	name="GP-GL"
	      	resolution="254"
	      	absolute=""
	      	relative=""
	      	up="M"
	      	down="D"
	      	init=""
	      	separator=","
	    	>
	 	</item>
	 </Devices>
	 */
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		m_activity = this;
		
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
	   	
	   	if (!sharedPrefs.contains("Devices"))
	   	{
	   		try 
	   		{
				DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder xmlBuilder = xmlFactory.newDocumentBuilder();
				Document document = xmlBuilder.newDocument();
				Element elDevices = document.createElement("Devices");
				document.appendChild(elDevices);
				
				//HP-GL
				
				Element elItemHP = document.createElement("item");
				
				elDevices.appendChild(elItemHP);
				
				elItemHP.setAttribute("name", "HP-GL");
				elItemHP.setAttribute("resolution", "1016");
				elItemHP.setAttribute("absolute", "PA");
				elItemHP.setAttribute("relative", "PR");
				elItemHP.setAttribute("up", "PU");
				elItemHP.setAttribute("down", "PD");
				elItemHP.setAttribute("init", "IN;");
				elItemHP.setAttribute("separator", ";");
				
				Element elSettingsHP = document.createElement("Settings");
				
				elItemHP.appendChild(elSettingsHP);
				
				Element elItemSettingsHP = document.createElement("item");
				
				elSettingsHP.appendChild(elItemSettingsHP);
				
				elItemSettingsHP.setAttribute("name", "Tool");
				elItemSettingsHP.setAttribute("type", "choice");
				elItemSettingsHP.setAttribute("format", "SP%d;");
				
				Element elValue = document.createElement("value");
				
				elItemSettingsHP.appendChild(elValue);
				elValue.setAttribute("Pen", "1");
				
				elValue = document.createElement("value");
				elItemSettingsHP.appendChild(elValue);
				elValue.setAttribute("Knife", "2");
				
				//GP-GL
				
				Element elItemGP = document.createElement("item");
				
				elDevices.appendChild(elItemGP);
				
				elItemGP.setAttribute("name", "GP-GL");
				elItemGP.setAttribute("resolution", "254");
				//elItemGP.setAttribute("absolute", "");
				//elItemGP.setAttribute("relative", "");
				elItemGP.setAttribute("up", "M");
				elItemGP.setAttribute("down", "D");
				//elItemGP.setAttribute("init", "IN;");
				elItemGP.setAttribute("separator", ",");
				
				StringWriter sw = new StringWriter();
		        TransformerFactory tf = TransformerFactory.newInstance();
		        Transformer transformer = tf.newTransformer();
		        //transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		        //transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		        //transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		        //transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

		        DOMSource source = new DOMSource(document);
		        transformer.transform(source, new StreamResult(sw));
		        
		    	SharedPreferences.Editor editor = sharedPrefs.edit();
		    	editor.putString("Devices", sw.toString());
		    	editor.putString("currentDevice", "HP-GL");
				editor.commit();
			}
			catch (Exception e) 
			{
				Log.v("MainActivity", "e=" + e);
			} 
	   	}
	   	
	   	rulerHor = (RulerViewer) findViewById(R.id.rulerHorView);
	   	rulerVer = (RulerViewer) findViewById(R.id.rulerVerView);
	   	
	   	rulerVer.setVertical(true);
	   	
	   	rulerHor.setSecondRuler(rulerVer);
	   	rulerVer.setSecondRuler(rulerHor);
		
		pageViewer = (PageViewer) findViewById(R.id.pageCutView);
		
		pageViewer.setVertical(rulerVer);
		pageViewer.setHorizantal(rulerHor);
		
	 	clearBtn = (Button)findViewById(R.id.buttonClear);
        clearBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				//drawView.setErase(false);
				///drawView.setBrushSize(mediumBrush);
				//drawView.setLastBrushSize(mediumBrush);
				//brushDialog.dismiss();
				pageViewer.Clear();
			}
		});
        
        toolBtn = (ToggleButton)findViewById(R.id.buttonTools);
        toolBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				boolean on = ((ToggleButton)v).isChecked();
				
				if (on == false)
				{
					((ToggleButton)v).setChecked(true);
				}
				
				handBtn.setChecked(false);
				
				ToolsDialog dialog = new ToolsDialog(m_activity);
				dialog.SetToolInterface(m_activity);
	    		dialog.show();
			}
		});
        
        handBtn = (ToggleButton)findViewById(R.id.buttonHand);
        handBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				boolean on = ((ToggleButton)v).isChecked();
				
				if (on == false)
				{
					((ToggleButton)v).setChecked(true);
				}
				
				pageViewer.SetCurrentTool(MainActivity.ToolType.Hand);
				
				toolBtn.setChecked(false);
			}
		});
	}
	
	@Override
	public void onDismiss(DialogInterface dialogInterface)
	{
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
	   	
	   	if (sharedPrefs.contains("Ports"))
	   	{
	   		sendData();
	   	}
	}
	
	 private class SendDataTask extends AsyncTask<String, Void, Boolean> 
	 {
	        
	 	public String strName;
	 	public String strPortNumber;
	 	public String strTextIP;
	 	public ArrayList<CutObject> objects;
	 	 
	 	public SendDataTask(String strName,
	 						String strPortNumber,
	 						String strTextIP,
	 						ArrayList<CutObject> objects) 
	 	{
	 		this.strName = strName;
	 		this.strPortNumber = strPortNumber;
	 		this.strTextIP = strTextIP;
	 		this.objects = objects;
	 	}
	 
	 	@Override
        protected Boolean doInBackground(String... urls) {
              
  			try {
  				Socket socket = null;
  				OutputStream outpu;
  				InputStream input;
  				double xdpi = getResources().getDisplayMetrics().xdpi;
		    	double ydpi = getResources().getDisplayMetrics().ydpi;
		    	SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(m_activity);
		    	String resolution = "1016";
		    	String absoluteCommand="PA";
    			String relativeCommand="PR";
				String upCommand="PU";
				String downCommand="PD";
				String initCommand="IN;";
				String separator=";";
				String currentDevice = sharedPrefs.getString("currentDevice", "HP-GL");
				
			   	if (sharedPrefs.contains("Devices"))
			   	{
			   		DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
					DocumentBuilder xmlBuilder = xmlFactory.newDocumentBuilder();
					InputStream stream = new ByteArrayInputStream(sharedPrefs.getString("Devices", "").getBytes("UTF-8"));
					
					Document documentCurrent = xmlBuilder.parse(stream);
					Element elRoot = documentCurrent.getDocumentElement();
					
					Element elFirstItem = (Element)elRoot.getFirstChild();
					
					while(elFirstItem != null)
					{
						if (elFirstItem.getAttribute("name").compareTo(currentDevice) == 0)
						{
							resolution = elFirstItem.getAttribute("resolution");
							absoluteCommand = elFirstItem.getAttribute("absolute");
							relativeCommand = elFirstItem.getAttribute("relative");
							upCommand = elFirstItem.getAttribute("up");
							downCommand = elFirstItem.getAttribute("down");
							initCommand = elFirstItem.getAttribute("init");
							separator = elFirstItem.getAttribute("separator");
							
							break;
						}
					}
			   	}
			   	
			   	int res = Integer.parseInt(resolution);
  				
  				if (bEnableFilePort == true)
  				{
  					File tripDataFile = new File(getExternalCacheDir(), "test_output.txt");
  					
  					outpu = new BufferedOutputStream(new FileOutputStream(tripDataFile));
  				}
  				else
  				{
  					socket = new Socket(strTextIP, Integer.parseInt(strPortNumber));
  					
  					outpu = socket.getOutputStream();
  	  				input =	socket.getInputStream();
  				}
  				
  				String data = initCommand + absoluteCommand + separator;
  				
  				for (int j = 0; j < objects.size(); j++) 
  				{
  					CutObject path = objects.get(j);
  					
  					for (int i = 0; i < path.size(); i++) 
  					{
  						PointF point = path.get(i);
  						
  						double x = point.x/xdpi;
  						double y = point.y/ydpi;
  						
  						x = x*res;
  						y = y*res;
  						
  						if (i == 0)
  						{
  							data += String.format("%s%d,%d%s", upCommand, (int)y, (int)x, separator);
  							data += String.format(downCommand);
  						}
					   
  						data += String.format("%d,%d,", (int)y, (int)x);
  					}
  					
  					data = data.substring(0, data.length() - 1);
  					data += separator;
				   
  					data += String.format(upCommand + separator);
				}
  				
  				outpu.write(data.getBytes());
  				outpu.close();
  				
  				if (socket != null)
  				{
  					socket.close();
  				}
  	            
  	        } catch (Exception e) {
  	            //return e.toString();
  	        	Log.v("MainActivity", "Error" + e);
  	        } finally {
  	           
  	           
  	        }
        	
        	return true;
        }
	 	
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(Boolean result) {
        	progressDialog.dismiss();
        	
       }
	}
	
	public void sendData()
	{
		Log.v("MainActivity", "sendData");
		
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
	   	
	   	String strPorts = sharedPrefs.getString("Ports", "");
	   	String strCurrent = sharedPrefs.getString("currentPort", "");
	   	String strCurrentLine = "";
	   	
	   	if (!strPorts.isEmpty())
	   	{
		   	String []arrPortLines = strPorts.split("\n");
	   		
	   		for (String strLine : arrPortLines) 
	   		{
		   	    String []arr = strLine.split(",");
		   	    
		   	    if (strCurrent.length() > 0 && arr[0].compareTo(strCurrent) == 0)
		   	    {
		   	    	strCurrentLine = strLine;
		   	    	break;
		   	    }
		   	}
	   	}
	   	
	   	if (!strCurrentLine.isEmpty())
	   	{
	   		String []arr = strCurrentLine.split(",");
	   		
	   		if (arr.length > 2)
	   		{
	   			String strName = arr[0];
	   			String strPortNumber = arr[1];
	   			String strTextIP = arr[2];
	   			
	   			progressDialog = new ProgressDialog(this);
	   			progressDialog.setTitle(getResources().getString(R.string.sending_data));
	   			progressDialog.setMessage(getResources().getString(R.string.please_wait));
	   			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	   			progressDialog.setCanceledOnTouchOutside(false);
	   			progressDialog.show();
	   				
	   		    new SendDataTask(strName, strPortNumber, strTextIP, pageViewer.getObjects()).execute("");
	   		
	   		}
	   	}
	}
	
	public void onSend(View v) {
	   	Log.v("MainActivity", "onSend");
	   	 
	   	SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
	   	
	   	if (sharedPrefs.contains("Ports"))
	   	{
	   		sendData();
	   	}
	   	else
	   	{
	   		//DriverManagerDialog newFragment = new DriverManagerDialog();
	   	    //newFragment.show(getFragmentManager(), "DriverManagerDialog");

	   		PortManagerDialog dialog = new PortManagerDialog(this);
	   		dialog.setOnDismissListener(this);
			dialog.show();
	   	}
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.action_exit:
	        {
	        	 finish();
	        	 return true;
	        }
	        
	        case R.id.action_about:
	        {
	        	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	        	builder.setMessage("mdmitry1973@gmail.com").setTitle(R.string.action_about).setPositiveButton("Ok", null);
		    	AlertDialog dialog = builder.create();
		    	
		    	dialog.show();
	        }
	        return true;
	        
	        case R.id.action_device_manager:
	        {
	        	DeviceManagerDialog dialog = new DeviceManagerDialog(this);
	    		dialog.show();
	        }
	        return true;
	        
	        case R.id.action_port_manager:
	        {
	    		PortManagerDialog dialog = new PortManagerDialog(this);
	    		dialog.show();
	        }
	        return true;
	        
	        case R.id.action_paper_manager:
	        {
	        	
	        }
	        return true;
	        
	        case R.id.action_settings:
	        {
	        	SettingsDialog dialog = new SettingsDialog(this);
	    		dialog.show();
	        }
	        return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}

	@Override
	public void ToolsChanged(ToolType type) {
		
		Drawable drawableTop = null;
		
		if (type == ToolType.Line)
		{
			drawableTop = getResources().getDrawable(R.drawable.line);
		}
		else
			if (type == ToolType.Pen)
			{
				drawableTop = getResources().getDrawable(R.drawable.pen);
			}
		
		toolBtn.setCompoundDrawablesWithIntrinsicBounds(null, drawableTop , null, null);
		
		pageViewer.SetCurrentTool(type);
	}
}


