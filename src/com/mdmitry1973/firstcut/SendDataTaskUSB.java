package com.mdmitry1973.firstcut;

import java.util.ArrayList;
import java.util.Arrays;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.util.Log;

class SendDataTaskUSB extends SendDataTask
{
	public MainActivity mainActivity;
	public UsbDevice device;
	public UsbManager manager;
	UsbInterface intf = null;
	UsbEndpoint endpoint = null;
	UsbDeviceConnection connection = null;
	boolean forceClaim = true;
	
	public SendDataTaskUSB(UsbDevice device,
						ArrayList<CutObject> objects,
						UsbManager manager,
						MainActivity mainActivity) 
	{
		super(mainActivity);
		
		this.device = device;
		this.objects = objects;
		this.manager = manager;
		this.mainActivity = mainActivity;
	}
	
	@Override
	public void sendDataToPort(byte[] data)
	{
		int TIMEOUT = 1000;
		int tryTimes = 10;
		int packigeSize = 512;
		int send = data.length;
		//int offset = 0;
		
		//mainActivity.AppendLogData(String.format("data.length=%d data=%s", data.length, new String(data)));
		
		try {
	 		while(data.length > 0)
	 		{
	 			int res = connection.bulkTransfer(endpoint, data, data.length < packigeSize ? data.length : packigeSize, TIMEOUT);
	 			
	 			if (res == data.length)
	 			{
 				//	mainActivity.AppendLogData(String.format("offset=%d", offset));
	 				break;
	 			}
	 			
	 			if (res > 0)
	 			{
	 				data = Arrays.copyOfRange(data, res, data.length);
	 				
	 				//offset = offset + res;
	 				
	 				//if (offset + res == data.length)
		 			//{
	 				//	mainActivity.AppendLogData(String.format("offset=%d", offset));
		 			//	break;
		 			//}
	 				
	 				//mainActivity.AppendLogData(String.format("data.length=%d data=%s", data.length, new String(data)));
	 				Thread.sleep(100);
	 			}
	 			else
	 			{
	 				mainActivity.AppendLogData(String.format("res=%d data.length=%d", res, data.length));
	 				
	 				if (tryTimes <= 0)
	 				{
	 					assert(true);
	 					break;
	 				}
	 				
	 				Thread.sleep(1000);
	 				tryTimes--;
	 			}
	 		}
		} 
		catch (Exception e) 
		{
			Log.v("SendDataTaskUSB", "Error" + e);
		} 
		finally 
		{
          
		}
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
				
				//UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
				
				//if (bEnableFilePort == true)
				//{
					//File tripDataFile = new File(getExternalCacheDir(), "test_output.txt");
					
					//outpu = new BufferedOutputStream(new FileOutputStream(tripDataFile));
				//}
				//else
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