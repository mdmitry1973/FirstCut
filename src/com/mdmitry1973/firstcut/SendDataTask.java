package com.mdmitry1973.firstcut;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

abstract class SendDataTask extends AsyncTask<String, Integer, Boolean>
{
	public ProgressDialog progressDialog;
	public ArrayList<CutObject> objects = null;
	double xdpi;
	double ydpi;
	SharedPreferences sharedPrefs;
	String data = "";
	boolean bCancel = false;
	
	public SendDataTask(Context context)
	{
		progressDialog = new ProgressDialog(context);
		progressDialog.setTitle(context.getResources().getString(R.string.sending_data));
		progressDialog.setMessage(context.getResources().getString(R.string.please_wait));
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.setMax(100);
		progressDialog.setCancelable(false);
		
		progressDialog.setButton(DialogInterface.BUTTON_NEUTRAL, context.getResources().getString(R.string.Cancel), new DialogInterface.OnClickListener() 
		{
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		    	bCancel = true;
		  }
		});
		
		progressDialog.show();
	}
	
	public void setXDpi(double xdpi)
	{
		this.xdpi = xdpi;
	}
	
	public void setYDpi(double ydpi)
	{
		this.ydpi = ydpi;
	}
	
	public void setPref(SharedPreferences sharedPrefs)
	{
		this.sharedPrefs = sharedPrefs;
	}
	
	public Boolean checkForSendData(int nMaxSend, int nSentObj)
	{
		publishProgress((int) ((nSentObj / (float) nMaxSend) * 100));
		
		if (data.length() > 100000)
		{
			sendDataToPort(data.getBytes());
			data = "";
		}
		
		return true;
	}
	
	public Boolean send()
	{
		try {
	    	String resolution = "1016";
	    	String absoluteCommand="PA";
			String relativeCommand="PR";
			String upCommand="PU";
			String downCommand="PD";
			String initCommand="IN;";
			String separator=";";
			String currentDevice = sharedPrefs.getString("currentDevice", "HP-GL");
			int nRotate = sharedPrefs.getInt(currentDevice + "_Rotate", 0);
			boolean bFlipVer = sharedPrefs.getBoolean(currentDevice + "_FlipVer", false);
			boolean bFlipHoz = sharedPrefs.getBoolean(currentDevice + "_FlipHoz", false);
			float fPaperWidth = 11f;
			float fPaperHeigh = 8.9f;
			float fPaperWidthRes = 0;
			float fPaperHeighRes = 0;
			float fCenterXRes = 0;
			float fCenterYRes = 0;
			
			{
				int currentPaperIndex = sharedPrefs.getInt("currentPaperIndex", 0);
				String strPapers = sharedPrefs.getString("Papers", "");
				
				if (!strPapers.isEmpty())
			   	{
					List<String> arrLines = new ArrayList<String>();
				   	String []arrPaperLines = strPapers.split("\n");
			   		
			   		for (int i = 0; i < arrPaperLines.length; i++) 
			   		{
			   			if (i == currentPaperIndex)
			   			{
					   	    String []arr = arrPaperLines[i].split(";");
					   	    
					   	    if (arr.length > 2)
					   	    {
					   	    	fPaperWidth = Float.parseFloat(arr[1]);
					   			fPaperHeigh = Float.parseFloat(arr[2]);
					   	    }
					   	    
					   	    break;
			   			}
				   	}
			   	}
			}
			
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
						
						break;
					}
					
					elFirstItem = (Element)elFirstItem.getNextSibling();
				}
		   	}
		   	
		   	int res = Integer.parseInt(resolution);
		   	float xUnit = (float)(res/xdpi);
	  		float yUnit = (float)(res/ydpi);
	  		
	  		fPaperWidthRes = fPaperWidth*(float)res;
			fPaperHeighRes = fPaperHeigh*(float)res;
			
			fCenterXRes = fPaperWidthRes/2;
			fCenterYRes = fPaperHeighRes/2;
			
			Matrix devMatrix = new Matrix();
			
			devMatrix.setRotate(nRotate, fCenterXRes, fCenterYRes);
			devMatrix.postScale(bFlipHoz ? -1 : 1, bFlipVer ? -1 : 1, fCenterXRes, fCenterYRes);
			
			if (absoluteCommand.length() != 0)
			{
				data = initCommand + absoluteCommand + separator;
			}
			else
			{
				data = initCommand;
			}
			
			data = data + upCommand + separator;
			
			int nMaxSend = objects.size();
			int nSentObj = 0;
			
			for (int j = 0; j < objects.size(); j++) 
			{
				if (bCancel)
				{
					break;
				}
				
				CutObject path = objects.get(j);
				
				if (path.getType() == CutObject.CutObjectType.Box)
				{
					if (path.size() == 2)
					{
						RectF rect = new RectF(path.get(0).x*xUnit, path.get(0).y*yUnit, path.get(1).x*xUnit, path.get(1).y*yUnit);
						
						Matrix boxMatrix = new Matrix();
						boxMatrix.setRotate(path.getDegree(), rect.centerX(), rect.centerY());
						boxMatrix.mapRect(rect);
						
						devMatrix.mapRect(rect);
						

						data += String.format("%s%d,%d%s%s%d,%d,%d,%d,%d,%d,%d,%d,%d,%d%s", 
								upCommand,
								(int)(rect.left), (int)(rect.top),
								separator,
								downCommand,
								(int)(rect.left), (int)(rect.top),
								(int)(rect.right), (int)(rect.top),
								(int)(rect.right), (int)(rect.bottom),
								(int)(rect.left), (int)(rect.bottom),
								(int)(rect.left), (int)(rect.top),
								separator
								);
						
						/*
						PointF p1 = path.get(0);
						PointF p3 = path.get(1);
						
						PointF p2 = new PointF(path.get(1).x, path.get(0).y);
						PointF p4 = new PointF(path.get(0).x, path.get(1).y);
						
						float [] dst = new float[8];
						float [] src = new float[8];
						
						src[0] = p1.x;
						src[1] = p1.y;
						src[2] = p2.x;
						src[3] = p2.y;
						src[4] = p3.x;
						src[5] = p3.y;
						src[6] = p4.x;
						src[7] = p4.y;
						
						devMatrix.mapPoints(dst, src);
						
						p1.x = dst[0];
						p1.y = dst[1];
						p2.x = dst[2];
						p2.y = dst[3];
						p3.x = dst[4];
						p3.y = dst[5];
						p4.x = dst[6];
						p4.y = dst[7];
						
						data += String.format("%s%d,%d%s%s%d,%d,%d,%d,%d,%d,%d,%d,%d,%d%s", 
								upCommand,
								(int)(p1.x*xUnit), (int)(p1.y*yUnit),
								separator,
								downCommand,
								(int)(p1.x*xUnit), (int)(p1.y*yUnit),
								(int)(p2.x*xUnit), (int)(p2.y*yUnit),
								(int)(p3.x*xUnit), (int)(p3.y*yUnit),
								(int)(p4.x*xUnit), (int)(p4.y*yUnit),
								(int)(p1.x*xUnit), (int)(p1.y*yUnit),
								separator
								);
								*/
					}
				}
				else
					if (path.getType() == CutObject.CutObjectType.Arrow)
					{
						CutObjectArrow arrowObj = (CutObjectArrow)path;
						Path pathArrow = new Path();
						RectF rect = new RectF(path.get(0).x*xUnit, path.get(0).y*yUnit, path.get(1).x*xUnit, path.get(1).y*yUnit);
					
						PointF centerCap = new PointF(rect.right, rect.top + (rect.height()/2));
						
						float capLength = rect.width() * arrowObj.getCapLength();
						float tailLength = rect.width() * arrowObj.getTailLength();
						float tailWidth = rect.height() * arrowObj.getTailWidth();
						
						pathArrow.moveTo(centerCap.x, centerCap.y);
						pathArrow.lineTo(rect.left + capLength, rect.top);
						pathArrow.lineTo(rect.left + tailLength, (rect.top + (rect.height()/2)) - tailWidth/2);
						pathArrow.lineTo(rect.left, (rect.top + (rect.height()/2)) - tailWidth/2);
						pathArrow.lineTo(rect.left, (rect.top + (rect.height()/2)) + tailWidth/2);
						pathArrow.lineTo(rect.left + tailLength, (rect.top + (rect.height()/2)) + tailWidth/2);
						pathArrow.lineTo(rect.left + capLength, rect.bottom);
						pathArrow.lineTo(centerCap.x, centerCap.y);
						
						pathArrow.close();
						
						Matrix arrowMatrix = new Matrix();
						arrowMatrix.setRotate(arrowObj.getDegree(), rect.centerX(), rect.centerY());
						pathArrow.transform(arrowMatrix);
						
						
						PathMeasure pathMeasure = new PathMeasure(pathArrow, false);
						ArrayList<PointF> outputPoints = new ArrayList<PointF>();
						PointF prePoint = new PointF(0, 0);
						float pathLength = pathMeasure.getLength();
						
						for (float distance = 0; distance < pathLength; distance = distance + 1) 
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
									if (Math.abs(newPoint.x - prePoint.x) >= 1.0f ||
										Math.abs(newPoint.y - prePoint.y) >= 1.0f)
									{
										prePoint = new PointF(newPoint.x, newPoint.y);
										outputPoints.add(newPoint);
									}
								}
							}
						}
						
						float [] pts = new float[2];
						
						for(int n = 0; n < outputPoints.size(); n++)
						{
							pts[0] = outputPoints.get(n).x;
							pts[1] = outputPoints.get(n).y;
							
							devMatrix.mapPoints(pts);
							
							if (n == 0)
							{
								data += String.format("%s%d,%d%s%s%d,%d", 
										upCommand,
										(int)pts[0], (int)pts[1],
										separator,
										downCommand,
										(int)pts[0], (int)pts[1]);
							}
							else
							{
								data += String.format(",%d,%d", 
										(int)pts[0], (int)pts[1]);
							}
						}
						
						data += separator;
					}
					else
						if (path.getType() == CutObject.CutObjectType.Star)
						{
							CutObjectStar starObj = (CutObjectStar)path;
							Path pathStar = new Path();
							
							RectF rect = new RectF(path.get(0).x*xUnit, path.get(0).y*yUnit, 
									path.get(1).x*xUnit, path.get(1).y*yUnit);
							
							float x = rect.centerX();
							float y = rect.centerY();
							float radius = rect.width()/2;
							float innerRadiusPix = (float) (radius * ((float)starObj.getInnerRadius()/100.0f)); 
							
							pathStar = starObj.setStar(x, y, radius, innerRadiusPix, starObj.getNumOfPt());
							
							Matrix starMatrix = new Matrix();
							starMatrix.reset();
							starMatrix.setRotate(-19 + starObj.getDegree(), rect.centerX(), rect.centerY());
							pathStar.transform(starMatrix);
							
							PathMeasure pathMeasure = new PathMeasure(pathStar, false);
							ArrayList<PointF> outputPoints = new ArrayList<PointF>();
							PointF prePoint = new PointF(0, 0);
							float pathLength = pathMeasure.getLength();
							
							for (float distance = 0; distance < pathLength; distance = distance + 1) 
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
										if (Math.abs(newPoint.x - prePoint.x) >= 1.0f ||
											Math.abs(newPoint.y - prePoint.y) >= 1.0f)
										{
											prePoint = new PointF(newPoint.x, newPoint.y);
											outputPoints.add(newPoint);
										}
									}
								}
							}
							
							float [] pts = new float[2];
							
							for(int n = 0; n < outputPoints.size(); n++)
							{
								pts[0] = outputPoints.get(n).x;
								pts[1] = outputPoints.get(n).y;
								
								devMatrix.mapPoints(pts);
								
								if (n == 0)
								{
									data += String.format("%s%d,%d%s%s%d,%d", 
											upCommand,
											(int)pts[0], (int)pts[1],
											separator,
											downCommand,
											(int)pts[0], (int)pts[1]);
								}
								else
								{
									data += String.format(",%d,%d", 
											(int)pts[0], (int)pts[1]);
								}
							}
							
							data += separator;
						}
				else
				if (path.getType() == CutObject.CutObjectType.Circle)
				{
					if (path.size() == 2)
					{
						PointF p1 = path.get(0);
						PointF p2 = path.get(1);
						Path pathCircle = new Path();
						RectF rectCircle = new RectF((float)(p1.x*xUnit), (float)(p1.y*yUnit), 
												(float)(p2.x*xUnit), (float)(p2.y*yUnit));
						
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
									if (Math.abs(newPoint.x - prePoint.x) >= 1.0f ||
										Math.abs(newPoint.y - prePoint.y) >= 1.0f)// ||
										///distance == pathMeasure.getLength() - 1)
									{
										prePoint = new PointF(newPoint.x, newPoint.y);
										outputPoints.add(newPoint);
									}
								}
							}
						}
						
						float [] pts = new float[2];
						
						for(int n = 0; n < outputPoints.size(); n++)
						{
							pts[0] = outputPoints.get(n).x;
							pts[1] = outputPoints.get(n).y;
							
							devMatrix.mapPoints(pts);
							
							if (n == 0)
							{
								data += String.format("%s%d,%d%s%s%d,%d", 
										upCommand,
										(int)pts[0], (int)pts[1],
										separator,
										downCommand,
										(int)pts[0], (int)pts[1]);
							}
							else
							{
								data += String.format(",%d,%d", 
										(int)pts[0], (int)pts[1]);
							}
						}
						
						data += separator;
					}
				}
				else
				if (path.getType() == CutObject.CutObjectType.Text)
				{
					CutObjectText textObj = (CutObjectText)path;
					ArrayList<Path> pathes = textObj.getTextPathes(textObj.getText());
					
					nMaxSend = nMaxSend + (pathes.size() - 1);
					
					for(int nn = 0; nn < pathes.size(); nn++)
					{
						Path textPath = pathes.get(nn);
						PathMeasure pathMeasure = new PathMeasure(textPath, false);
						ArrayList<PointF> outputPoints = new ArrayList<PointF>();
						PointF prePoint = new PointF(0, 0);
						
						do
						{
							outputPoints.clear();
							
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
										if (Math.abs(newPoint.x - prePoint.x) >= 1.0f ||
											Math.abs(newPoint.y - prePoint.y) >= 1.0f)// ||
											///distance == pathMeasure.getLength() - 1)
										{
											prePoint = new PointF(newPoint.x, newPoint.y);
											outputPoints.add(newPoint);
										}
									}
								}
							}
							
							float [] pts = new float[2];
						
							for(int n = 0; n < outputPoints.size(); n++)
							{
								pts[0] = outputPoints.get(n).x;
								pts[1] = outputPoints.get(n).y;
								
								devMatrix.mapPoints(pts);
								
								if (n == 0)
								{
									data += String.format("%s%d,%d%s%s%d,%d", 
											upCommand,
											(int)pts[0], (int)pts[1],
											separator,
											downCommand,
											(int)pts[0], (int)pts[1]);
								}
								else
								{
									data += String.format(",%d,%d", 
											(int)pts[0], (int)pts[1]);
								}
							}
							
							data += separator;
							
							if (bCancel)
							{
								break;
							}
						}
						while(pathMeasure.nextContour());
						
						nSentObj++;
						
						data += String.format(upCommand + separator);
						
						checkForSendData(nMaxSend, nSentObj);
						
						//publishProgress((int) ((j / (float) nMaxSend) * 100));
						
						//if (data.length() > 100000)
						//{
						//	sendDataToPort(data.getBytes());
						//	data = "";
						//}
					}
				}
				else
				{
					float [] pts = new float[2];
					
					for (int i = 0; i < path.size(); i++) 
					{
						PointF point = path.get(i);
						
						float x = point.x*xUnit;
						float y = point.y*yUnit;
						
						pts[0] = x;
						pts[1] = y;
						
						devMatrix.mapPoints(pts);
						
						if (i == 0)
						{
							data += String.format("%s%d,%d%s%s%s", upCommand, (int)pts[0], (int)pts[1], separator, downCommand, separator);
							data += String.format(downCommand);
						}
					   
						data += String.format("%d,%d,", (int)pts[0], (int)pts[1]);
					}
					
					data = data.substring(0, data.length() - 1);
					data += separator;
				}
				
				nSentObj++;
		   
				data += String.format(upCommand + separator);
				
				checkForSendData(nMaxSend, nSentObj);
				
				//publishProgress((int) ((j / (float) nMaxSend) * 100));
				
				//if (data.length() > 100000)
				//{
				//	sendDataToPort(data.getBytes());
				//	data = "";
				//}
			}
			
			if (data.length() > 0)
			{
				sendDataToPort(data.getBytes());
			}
			
			closePort();
		
		} catch (Exception e) {
 	            //return e.toString();
 	        	Log.v("MainActivity", "Error" + e);
 	    } finally {
 	           
 	           
 	    }
		
		return true;
	}
	
	@Override
	protected void  onProgressUpdate(Integer... values)  
	{
		progressDialog.setProgress(values[0]);
	}

	@Override
	protected Boolean doInBackground(String... params) {
		// TODO Auto-generated method stub
		return null;
	}
	
	abstract void sendDataToPort(byte[] data);
	abstract void closePort();
	
}
