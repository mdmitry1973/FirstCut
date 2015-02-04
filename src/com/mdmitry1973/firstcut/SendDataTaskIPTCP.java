package com.mdmitry1973.firstcut;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.util.Log;

class SendDataTaskIPTCP extends SendDataTask
{
	public MainActivity mainActivity;
	public OutputStream outpu = null;
	public InputStream input = null;
	public Socket socket = null;
	public String strName;
	public String strPortNumber;
	public String strTextIP;
	public String strErrorMessage = "";
	
	public SendDataTaskIPTCP(String strName,
						String strPortNumber,
						String strTextIP,
						ArrayList<CutObject> objects,
						MainActivity mainActivity) 
	{
		super(mainActivity);
		
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
			
			if (outpu != null)
			{
				outpu.close();
			}
			
			if (socket != null)
			{
				socket.close();
			}
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
   protected Boolean doInBackground(String... urls) {
         
			try {
				
				if (mainActivity.bEnableFilePort == true)
				{
					File tripDataFile = new File(mainActivity.getExternalCacheDir(), "test_output.txt");
					
					outpu = new BufferedOutputStream(new FileOutputStream(tripDataFile));
				}
				else
				{
					InetSocketAddress remoteAddr = new InetSocketAddress(strTextIP, Integer.parseInt(strPortNumber));
					
					socket = new Socket();
					
					socket.connect(remoteAddr, 15*1000);
					
					if (socket.isConnected())
					{
						outpu = socket.getOutputStream();
						input =	socket.getInputStream();
					}
				}
				
				if (socket.isConnected())
				{
					send();
				}
				else
				{
					strErrorMessage = mainActivity.getResources().getString(R.string.NoConnection);
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
   	
   	if (!strErrorMessage.isEmpty())
   	{
   		AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
	    builder.setMessage(strErrorMessage).setTitle(R.string.app_name).setPositiveButton("Ok", null);
	    AlertDialog dialog = builder.create();
	    dialog.show();
   	}
  }
}