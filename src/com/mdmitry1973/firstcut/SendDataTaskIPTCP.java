package com.mdmitry1973.firstcut;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PointF;
import android.graphics.RectF;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.util.Log;

class SendDataTaskIPTCP extends SendDataTask
{
	public MainActivity mainActivity;
	public OutputStream outpu = null;
	public InputStream input = null;
	public String strName;
	public String strPortNumber;
	public String strTextIP;
	
	public SendDataTaskIPTCP(String strName,
						String strPortNumber,
						String strTextIP,
						ArrayList<CutObject> objects,
						ProgressDialog progressDialog,
						MainActivity mainActivity) 
	{
		super(progressDialog);
		
		this.strName = strName;
		this.strPortNumber = strPortNumber;
		this.strTextIP = strTextIP;
		this.objects = objects;
		this.mainActivity = mainActivity;
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
				
				
				if (mainActivity.bEnableFilePort == true)
				{
					File tripDataFile = new File(mainActivity.getExternalCacheDir(), "test_output.txt");
					
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