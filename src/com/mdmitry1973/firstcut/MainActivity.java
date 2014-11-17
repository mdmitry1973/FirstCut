package com.mdmitry1973.firstcut;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.Socket;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.mdmitry1973.firstcut.PaperManagerDialog.PaperManagerDialogInterface;

import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ToggleButton;

public class MainActivity extends Activity 
		implements 	DialogInterface.OnDismissListener, 
					ToolsDialogInterface, 
					SendDialoginterface, 
					PaperManagerDialog.PaperManagerDialogInterface,
					PortManagerDialog.PortManagerInterface {
	
	public ProgressDialog progressDialog;
	
	private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
	
	PageViewer pageViewer;
	RulerViewer rulerVer;
	RulerViewer rulerHor;
	
	boolean bEnableFilePort = false;
	MainActivity m_activity = null;
	
	Button clearBtn;
	ImageButton saveOpenShareBtn;
    ToggleButton handBtn;
    ToggleButton toolBtn;
    
    EditText xEdit;
    EditText yEdit;
    EditText widthEdit;
    EditText heightEdit;
	
	enum ToolType {
		Line,
		Pen,
		Box,
		Circle,
		Text,
		Hand,
		Resize
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
	 
	 Port format {type,name,...}
	 PORT_USB,Name,Data\n
	 PORT_TCPIP,Name,Port,IP\n
	 */
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		m_activity = this;
		
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
	   	
		if (!sharedPrefs.contains("Papers"))
	   	{
			SharedPreferences.Editor editor = sharedPrefs.edit();
	    	editor.putString("Papers", "Letter;8.5;11\nLetter Landscape;11;8.5\n");
	    	editor.putInt("currentPaperIndex", 0);
			editor.commit();
	   	}
		
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
	   	
	   	xEdit = (EditText) findViewById(R.id.editTextX);
	    yEdit = (EditText) findViewById(R.id.editTextY);
	    widthEdit = (EditText) findViewById(R.id.editTextWidth);
	    heightEdit = (EditText) findViewById(R.id.editTextHeight);
	    
	    xEdit.setOnKeyListener(new OnKeyListener() {
	        
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
				    (keyCode == KeyEvent.KEYCODE_ENTER)) 
				{
				              // Perform action on key press
				              //Toast.makeText(HelloFormStuff.this, edittext.getText(), Toast.LENGTH_SHORT).show();
				         //     return true;
				}
				
				return false;
			}
	    });
	   	
	   	rulerVer.setVertical(true);
	   	
	   	rulerHor.setSecondRuler(rulerVer);
	   	rulerVer.setSecondRuler(rulerHor);
		
		pageViewer = (PageViewer) findViewById(R.id.pageCutView);
		
		pageViewer.setVertical(rulerVer);
		pageViewer.setHorizantal(rulerHor);
		pageViewer.setMainActivity(this);
		
	 	clearBtn = (Button)findViewById(R.id.buttonClear);
        clearBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				pageViewer.RemoveCurrent();
			}
		});
        
        saveOpenShareBtn = (ImageButton)findViewById(R.id.buttonSaveOpenShare);
        saveOpenShareBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				OpenSaveShareDialog dialog = new OpenSaveShareDialog(m_activity, m_activity);
	    		dialog.show();
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
				
				ToolsDialog dialog = new ToolsDialog(m_activity);
				dialog.SetToolInterface(m_activity);
	    		dialog.show();
			}
		});
	}
	
	@Override
	public void onDismiss(DialogInterface dialogInterface)
	{
		
	}
	
	 private abstract class SendDataTask extends AsyncTask<String, Void, Boolean>
	 {
		public ArrayList<CutObject> objects = null;
		
		public Boolean send()
		{
			try {
				
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
				
				String data = initCommand + absoluteCommand + separator;
					
				for (int j = 0; j < objects.size(); j++) 
				{
					CutObject path = objects.get(j);
					
					if (path.getType() == CutObject.CutObjectType.Box)
					{
						ArrayList<PointF> points = path.getObjectPath();
						
						if (points.size() == 2)
						{
							PointF p1 = new PointF(points.get(0).x, points.get(0).y);
							PointF p3 = new PointF(points.get(1).x, points.get(1).y);
							
							PointF p2 = new PointF(points.get(1).x, points.get(0).y);
							PointF p4 = new PointF(points.get(0).x, points.get(1).y);
							
							data += String.format("PA;PU%d,%d;PD%d,%d,%d,%d,%d,%d,%d,%d,%d,%d;", 
									(int)(p1.x/xdpi*res), (int)(p1.y/ydpi*res),
									(int)(p1.x/xdpi*res), (int)(p1.y/ydpi*res),
									(int)(p2.x/xdpi*res), (int)(p2.y/ydpi*res),
									(int)(p3.x/xdpi*res), (int)(p3.y/ydpi*res),
									(int)(p4.x/xdpi*res), (int)(p4.y/ydpi*res),
									(int)(p1.x/xdpi*res), (int)(p1.y/ydpi*res)
									);
						}
					}
					else
					if (path.getType() == CutObject.CutObjectType.Circle)
					{
						ArrayList<PointF> points = path.getObjectPath();
						
						if (points.size() == 2)
						{
							PointF p1 = new PointF(points.get(0).x, points.get(0).y);
							PointF p2 = new PointF(points.get(1).x, points.get(1).y);
							Path pathCircle = new Path();
							RectF rectCircle = new RectF((float)(p1.x/xdpi*res), (float)(p1.y/xdpi*res), 
													(float)(p2.x/ydpi*res), (float)(p2.y/ydpi*res));
							
							rectCircle.sort();
							
							pathCircle.addOval(rectCircle, Path.Direction.CCW);
							pathCircle.close();
							
							PathMeasure pathMeasure = new PathMeasure(pathCircle, false);
							ArrayList<PointF> outputPoints = new ArrayList<PointF>();
							PointF prePoint = new PointF(0, 0);
							
							for (float distance = 0; distance < pathMeasure.getLength(); distance++) 
							{
								float[] pos = new float[2];
								float[] tan = new float[2];
								
								if (pathMeasure.getPosTan(distance, pos, tan))
								{
									PointF newPoint = new PointF(pos[0], pos[1]);
									
									if (distance == 0)
									{
										prePoint = new PointF(newPoint.x, newPoint.y);
										outputPoints.add(newPoint);
									}
									else
									{
										if (Math.abs(newPoint.x - prePoint.x) > 1.0f ||
											Math.abs(newPoint.y - prePoint.y) > 1.0f ||
											distance == pathMeasure.getLength() - 1)
										{
											prePoint = new PointF(newPoint.x, newPoint.y);
											outputPoints.add(newPoint);
										}
									}
								}
							}
							
							for(int n = 0; n < outputPoints.size(); n++)
							{
								if (n == 0)
								{
									data += String.format("PA;PU%d,%d;PD%d,%d", 
											(int)outputPoints.get(n).x, (int)outputPoints.get(n).y,
											(int)outputPoints.get(n).x, (int)outputPoints.get(n).y);
								}
								else
								{
									data += String.format(",%d,%d", 
											(int)outputPoints.get(n).x, (int)outputPoints.get(n).y);
								}
							}
							data += separator;
						}
					}
					else
					{
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
					}
			   
					data += String.format(upCommand + separator);
				}
				
				sendDataToPort(data.getBytes());
				closePort();
			
			} catch (Exception e) {
	  	            //return e.toString();
	  	        	Log.v("MainActivity", "Error" + e);
	  	    } finally {
	  	           
	  	           
	  	    }
			
			return true;
		}

		@Override
		protected Boolean doInBackground(String... params) {
			// TODO Auto-generated method stub
			return null;
		}
		
		abstract void sendDataToPort(byte[] data);
		abstract void closePort();
	 }
	
	 private class SendDataTaskIPTCP extends SendDataTask//AsyncTask<String, Void, Boolean> implements  SendDataTask
	 {
		public OutputStream outpu = null;
		public InputStream input = null;
	 	public String strName;
	 	public String strPortNumber;
	 	public String strTextIP;
	 	
	 	public SendDataTaskIPTCP(String strName,
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
	 	public void sendDataToPort(byte[] data)
	 	{
	 		try {
				outpu.write(data);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	 	}
	 	
	 	@Override
	 	public void closePort()
	 	{
	 		try {
				outpu.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	 	}
	 
	 	@Override
        protected Boolean doInBackground(String... urls) {
              
  			try {
  				Socket socket = null;
  				
  				
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
  				
  				send();
  				
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
	 
	 private class SendDataTaskUSB extends SendDataTask
	 {
	 	public UsbDevice device;
	 	UsbInterface intf = null;
    	UsbEndpoint endpoint = null;
    	UsbDeviceConnection connection = null;
    	boolean forceClaim = true;
	 	
	 	public SendDataTaskUSB(UsbDevice device,
	 						ArrayList<CutObject> objects) 
	 	{
	 		this.device = device;
	 		this.objects = objects;
	 	}
	 	
	 	@Override
	 	public void sendDataToPort(byte[] data)
	 	{
	 		int TIMEOUT = 1000;
	 		
	 		int res = connection.bulkTransfer(endpoint, data, data.length, TIMEOUT);
	 	}
	 	
	 	@Override
	 	public void closePort()
	 	{
	 		connection.releaseInterface(intf);
			connection.close();
	 	}
	 
	 	@Override
        protected Boolean doInBackground(String... urls) {
              
  			try {
  				
  				UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
  				
  				if (bEnableFilePort == true)
  				{
  					//File tripDataFile = new File(getExternalCacheDir(), "test_output.txt");
  					
  					//outpu = new BufferedOutputStream(new FileOutputStream(tripDataFile));
  				}
  				else
  				{
  					intf = device.getInterface(0);
    		    	endpoint = intf.getEndpoint(0);
    		    	connection = manager.openDevice(device); 
    		    	connection.claimInterface(intf, forceClaim);
  				}
  				
  				send();
  	            
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
	 
	public void OnSendDialog()
	{
		sendData();
	}
	
	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

	    public void onReceive(Context context, Intent intent) {
	        String action = intent.getAction();
	        if (ACTION_USB_PERMISSION.equals(action)) {
	            synchronized (this) {
	                UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

	                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
	                    if(device != null){
	                    	
	                    	new SendDataTaskUSB(device, pageViewer.getObjects()).execute("");
	                   }
	                } 
	                else {
	                    Log.d("MainActivity", "permission denied for device " + device);
	                }
	            }
	        }
	    }
	};
	
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
		   	    String strName = arr[0];
		   	    
		   	    if (strName.startsWith("TYPE_") == true)
			   	{
		   	    	strName = arr[1];
			   	}
		   	    
		   	    if (strCurrent.length() > 0 && strName.compareTo(strCurrent) == 0)
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
	   			String strType = "TYPE_TCPIP";
	   			String strName = arr[0];
	   			int offset = 0;
	   			
		   	    if (strName.startsWith("TYPE_") == true)
			   	{
		   	    	strType = arr[0];
		   	    	strName = arr[1];
		   	    	offset++;
			   	}
	   			
		   	    if (strType.startsWith("TYPE_TCPIP") == true)
		   	    {
		   	    	ConnectivityManager connMgr = (ConnectivityManager) 
   	    			getSystemService(Context.CONNECTIVITY_SERVICE);
   	    			NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
   	    					
   	    			if (networkInfo != null && networkInfo.isConnected()) 
   	    			{
   	    				String strPortNumber = arr[1 + offset];
   			   			String strTextIP = arr[2 + offset];
   			   			
   			   	    	progressDialog = new ProgressDialog(this);
   		   				progressDialog.setTitle(getResources().getString(R.string.sending_data));
   		   				progressDialog.setMessage(getResources().getString(R.string.please_wait));
   		   				progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
   		   				progressDialog.setCanceledOnTouchOutside(false);
   		   				progressDialog.show();
   		   				
   		   		    	new SendDataTaskIPTCP(strName, strPortNumber, strTextIP, pageViewer.getObjects()).execute("");
   			   	    
   	    			}
   	    			else
   	    			{
   	    				AlertDialog.Builder builder = new AlertDialog.Builder(this);
   	    		    	builder.setMessage(R.string.no_network).setTitle(R.string.app_name).setPositiveButton("Ok", null);
   	    		    	AlertDialog dialog = builder.create();
   	    		    	dialog.show();
   	    			}
		   	    }
		   	    else
		   	    if (strType.startsWith("TYPE_USB") == true)
		   	    {
		   	    	String strUSBPortData = arr[1 + offset];
		   	    	
		   	    	UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
	  				
	  				HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
	  	    		Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
	  	    		while(deviceIterator.hasNext()){
	  	    		    UsbDevice device = deviceIterator.next();
	  	    		    
	  	    		    if (device != null && strUSBPortData.contains(device.getDeviceName()))
	  	    		    {
	  	    		    	PendingIntent mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
	  	    		    	IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
	  	    		    	registerReceiver(mUsbReceiver, filter);

	  	    		    	manager.requestPermission(device, mPermissionIntent);
	  	    		    	
	  	    		    	progressDialog = new ProgressDialog(this);
	  			   			progressDialog.setTitle(getResources().getString(R.string.sending_data));
	  			   			progressDialog.setMessage(getResources().getString(R.string.please_wait));
	  			   			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	  			   			progressDialog.setCanceledOnTouchOutside(false);
	  			   			progressDialog.show();
	  	    		    	
	  	    		    	break;
	  	    		    }
	  	    		}
		   	    }
	   		}
	   	}
	}
	
	public void onSend(View v) {
	   	Log.v("MainActivity", "onSend");
	   	 
	   	SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
	   	
	   	if (sharedPrefs.contains("Ports"))
	   	{
	   		SendDialod  dialog = new SendDialod(this);
	   		dialog.setSendDialoginterface(this);
			dialog.show();
	   	}
	   	else
	   	{
	   		PortManagerDialog dialog = new PortManagerDialog(this);
	   		dialog.setPortManagerInterface(this);
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
	        	PaperManagerDialog dialog = new PaperManagerDialog(this);
	        	dialog.SetPaperManagerDialogInterface(this);
	    		dialog.show();
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
			else
				if (type == ToolType.Box)
				{
					drawableTop = getResources().getDrawable(R.drawable.box);
				}
				else
					if (type == ToolType.Circle)
					{
						drawableTop = getResources().getDrawable(R.drawable.circle);
					}
					else
						if (type == ToolType.Text)
						{
							drawableTop = getResources().getDrawable(R.drawable.text);
						}
						else
							if (type == ToolType.Hand)
							{
								drawableTop = getResources().getDrawable(R.drawable.move);
							}
							else
								if (type == ToolType.Resize)
								{
									drawableTop = getResources().getDrawable(R.drawable.resize);
								}
		
		toolBtn.setCompoundDrawablesWithIntrinsicBounds(null, drawableTop , null, null);
		
		pageViewer.SetCurrentTool(type);
	}
	
	public void WriteObjectsToString(Writer out)
	{
		try{
			
			ArrayList<CutObject> listPaths = pageViewer.getObjects();
			DisplayMetrics metrics = getResources().getDisplayMetrics();
	  		BufferedWriter fileBuffer = new BufferedWriter(out);
	  		
	  		fileBuffer.write("IN;");
	  		fileBuffer.newLine();
	  		
			for(int i = 0; i < listPaths.size(); i++)
			{
				CutObject object = listPaths.get(i);
				
				if (object.getType() == CutObject.CutObjectType.Box)
				{
					ArrayList<PointF> points = object.getObjectPath();
					
					if (points.size() == 2)
					{
						PointF p1 = new PointF(points.get(0).x, points.get(0).y);
						PointF p3 = new PointF(points.get(1).x, points.get(1).y);
						
						PointF p2 = new PointF(points.get(1).x, points.get(0).y);
						PointF p4 = new PointF(points.get(0).x, points.get(1).y);
						
						fileBuffer.write(String.format("PA;PU%d,%d;PD%d,%d,%d,%d,%d,%d,%d,%d,%d,%d;", 
								(int)(p1.x/metrics.xdpi*1016), (int)(p1.y/metrics.ydpi*1016),
								(int)(p1.x/metrics.xdpi*1016), (int)(p1.y/metrics.ydpi*1016),
								(int)(p2.x/metrics.xdpi*1016), (int)(p2.y/metrics.ydpi*1016),
								(int)(p3.x/metrics.xdpi*1016), (int)(p3.y/metrics.ydpi*1016),
								(int)(p4.x/metrics.xdpi*1016), (int)(p4.y/metrics.ydpi*1016),
								(int)(p1.x/metrics.xdpi*1016), (int)(p1.y/metrics.ydpi*1016)
								));
						fileBuffer.newLine();
					}
				}
				else
				if (object.getType() == CutObject.CutObjectType.Circle)
				{
					ArrayList<PointF> points = object.getObjectPath();
					
					if (points.size() == 2)
					{
						PointF p1 = new PointF(points.get(0).x, points.get(0).y);
						PointF p2 = new PointF(points.get(1).x, points.get(1).y);
						Path path = new Path();
						RectF rect = new RectF(p1.x/metrics.xdpi*1016, p1.y/metrics.ydpi*1016, 
												p2.x/metrics.xdpi*1016, p2.y/metrics.ydpi*1016);
						
						rect.sort();
						
						path.addOval(rect, Path.Direction.CCW);
						path.close();
						
						PathMeasure pathMeasure = new PathMeasure(path, false);
						ArrayList<PointF> outputPoints = new ArrayList<PointF>();
						PointF prePoint = new PointF(0, 0);
						
						for (float distance = 0; distance < pathMeasure.getLength(); distance++) 
						{
							float[] pos = new float[2];
							float[] tan = new float[2];
							
							if (pathMeasure.getPosTan(distance, pos, tan))
							{
								PointF newPoint = new PointF(pos[0], pos[1]);
								
								if (distance == 0)
								{
									prePoint = new PointF(newPoint.x, newPoint.y);
									outputPoints.add(newPoint);
								}
								else
								{
									if (Math.abs(newPoint.x - prePoint.x) > 1.0f ||
										Math.abs(newPoint.y - prePoint.y) > 1.0f ||
										distance == pathMeasure.getLength() - 1)
									{
										prePoint = new PointF(newPoint.x, newPoint.y);
										outputPoints.add(newPoint);
									}
								}
							}
						}
						
						for(int n = 0; n < outputPoints.size(); n++)
						{
							if (n == 0)
							{
								fileBuffer.write(String.format("PA;PU%d,%d;PD%d,%d", 
										(int)outputPoints.get(n).x, (int)outputPoints.get(n).y,
										(int)outputPoints.get(n).x, (int)outputPoints.get(n).y));
							}
							else
							{
								fileBuffer.write(String.format(",%d,%d", 
										(int)outputPoints.get(n).x, (int)outputPoints.get(n).y));
							}
						}
						fileBuffer.write(";");
						fileBuffer.newLine();
					}
				}
				else
				if (object.getType() == CutObject.CutObjectType.Line)
				{
					ArrayList<PointF> points = object.getObjectPath();
					
					if (points.size() > 1)
					{
						PointF p1 = points.get(0);
						PointF p2 = points.get(1);
						
						fileBuffer.write(String.format("PA;PU%d,%d;PD%d,%d,%d,%d;", 
								(int)(p1.x/metrics.xdpi*1016), (int)(p1.y/metrics.ydpi*1016),
								(int)(p1.x/metrics.xdpi*1016), (int)(p1.y/metrics.ydpi*1016),
								(int)(p2.x/metrics.xdpi*1016), (int)(p2.y/metrics.ydpi*1016)));
						fileBuffer.newLine();
					}
				}
				else
				if (object.getType() == CutObject.CutObjectType.Pen)
				{
					ArrayList<PointF> points = object.getObjectPath();
					
					if (points.size() > 1)
					{
						PointF p1 = points.get(0);
						
						fileBuffer.write(String.format("PA;PU%d,%d;PD%d,%d", 
								(int)(p1.x/metrics.xdpi*1016), (int)(p1.y/metrics.ydpi*1016),
								(int)(p1.x/metrics.xdpi*1016), (int)(p1.y/metrics.ydpi*1016)));
						
						for(int t = 1; t < points.size(); t++)
						{
							PointF p2 = points.get(t);
							
							fileBuffer.write(String.format(",%d,%d",
									(int)(p2.x/metrics.xdpi*1016), (int)(p2.y/metrics.ydpi*1016)));
						}
						
						fileBuffer.write(";");
						
						fileBuffer.newLine();
					}
				}
				else
				if (object.getType() == CutObject.CutObjectType.Box)
				{
					
				}
				
				fileBuffer.newLine();
			}
			
			fileBuffer.close();
  		}
		catch(Exception ex)
		{
			Log.v("MainActivity", "SaveData " + ex);
		}
	}
	
	public void SaveData(File filePath)
	{
		try{
			
			WriteObjectsToString(new FileWriter(filePath));
  		}
		catch(Exception ex)
		{
			Log.v("MainActivity", "SaveData " + ex);
		}
	}
	
	public void OpenData(File filePath)
	{
		try{
			ArrayList<CutObject> listPaths = new ArrayList<CutObject>();
			DisplayMetrics metrics = getResources().getDisplayMetrics();
			BufferedReader fileBuffer = new BufferedReader(new FileReader(filePath));
			
	  		while(fileBuffer.ready())
			{
	  			String serviceLine = fileBuffer.readLine();
	  			
	  			if (serviceLine != null)
	  			{
	  				if (serviceLine.startsWith("IN;") == true)//header
	  				{
	  					
	  				}
	  				else
	  				if (serviceLine.startsWith("PA;PU") == true)//line or pen
		  			{
	  					String []arrCommands = serviceLine.split(";");
	  					
	  					if (arrCommands.length > 2)
	  					{
	  						String line = arrCommands[2].substring(2);
	  						String []arrPoints = line.split(",");
	  						ArrayList<PointF> points = new ArrayList<PointF>();
	  						
		  					for(int i = 0; i < arrPoints.length; i = i + 2)
		  					{
		  						PointF point = new PointF();
		  						int x = Integer.parseInt(arrPoints[i]);
		  						int y = Integer.parseInt(arrPoints[i + 1]);
		  						
		  						point.x = (float)x * metrics.xdpi / 1016.0f;
		  						point.y = (float)y * metrics.ydpi / 1016.0f;
		  						
		  						points.add(point);
		  					}
		  					
		  					if (points.size() > 1)
		  					{
		  						CutObject obj = new CutObject();
		  						
		  						if (points.size() > 2)
			  					{
		  							obj.setType(CutObject.CutObjectType.Pen);
			  					}
		  						else
		  						{
		  							obj.setType(CutObject.CutObjectType.Line);
		  						}
		  						
		  						obj.add(points);
		  						
		  						listPaths.add(obj);
		  					}
	  					}
		  			}
	  			}
			}
	  		
	  		pageViewer.setObjects(listPaths);
	  		pageViewer.DrawBitmap();
	  		pageViewer.invalidate();
			
			fileBuffer.close();
  		}
		catch(Exception ex)
		{
			Log.v("MainActivity", "OpenData " + ex);
		}
	}
	
	public void ShareData()
	{
		StringWriter outputWriter = new StringWriter();
		
		try {
		
			WriteObjectsToString(outputWriter);
			
			Intent i = new Intent(Intent.ACTION_SEND);
			i.setType("message/rfc822");
			i.putExtra(Intent.EXTRA_EMAIL, new String[] {""});
			i.putExtra(Intent.EXTRA_SUBJECT, "Cut job");
			i.putExtra(Intent.EXTRA_TEXT, outputWriter.toString());
			
			startActivity(Intent.createChooser(i, "Send mail..."));
			
		} catch (Exception ex) {
			Log.v("MainActivity", "ShareData " + ex);
		}
	}
	
	public void setSelObjectCoor(RectF rect)
	{
		if (rect.isEmpty() == true)
		{
			return;
		}
		
		DisplayMetrics metrics = getResources().getDisplayMetrics();
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		
    	int currentUnit = sharedPrefs.getInt("unit", 0);
    	
		float xInch = (float)rect.left / metrics.xdpi;
		float yInch = (float)rect.top / metrics.ydpi;
		
		float widthInch = (float)rect.width() / metrics.xdpi;
		float heightInch = (float)rect.height() / metrics.ydpi;
		
		if (currentUnit == 1)//mm
		{
			xInch = xInch * 0.0393701f;
			yInch = yInch * 0.0393701f;
			widthInch = widthInch * 0.0393701f;
			heightInch = heightInch * 0.0393701f;
		}
		else
		if (currentUnit == 2)//cm
		{
			xInch = xInch * 0.393701f;
			yInch = yInch * 0.393701f;
			widthInch = widthInch * 0.393701f;
			heightInch = heightInch * 0.393701f;
		}
		
		xEdit.setText(String.format("%.2f", xInch));
	    yEdit.setText(String.format("%f.2", yInch));
	    widthEdit.setText(String.format("%.2f", widthInch));
	    heightEdit.setText(String.format("%.2f", heightInch));
	}
	
	public void ResetPaper()
	{
		pageViewer.ResetPaperSize();
		pageViewer.RecalcSize();
	}
	
	public void onPortManagerFinish()
	{
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
	   	
	   	if (sharedPrefs.contains("Ports"))
	   	{
	   		SendDialod  dialog = new SendDialod(this);
	   		dialog.setSendDialoginterface(this);
			dialog.show();
	   	}
	}
}


