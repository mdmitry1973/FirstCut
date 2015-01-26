package com.mdmitry1973.firstcut;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.util.Log;

abstract class SendDataTask extends AsyncTask<String, Integer, Boolean>
{
	public ProgressDialog progressDialog;
	public ArrayList<CutObject> objects = null;
	double xdpi;
	double ydpi;
	SharedPreferences sharedPrefs;
	
	public SendDataTask(ProgressDialog progressDialog)
	{
		this.progressDialog = progressDialog;
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
			
			String data = "";
			
			if (absoluteCommand.length() != 0)
			{
				data = initCommand + absoluteCommand + separator;
			}
			else
			{
				data = initCommand;
			}
			
			data = data + upCommand + separator;
			
			for (int j = 0; j < objects.size(); j++) 
			{
				CutObject path = objects.get(j);
				
				if (path.getType() == CutObject.CutObjectType.Box)
				{
					if (path.size() == 2)
					{
						PointF p1 = path.get(0);
						PointF p3 = path.get(1);
						
						PointF p2 = new PointF(path.get(1).x, path.get(0).y);
						PointF p4 = new PointF(path.get(0).x, path.get(1).y);
						
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
					}
				}
				else
					if (path.getType() == CutObject.CutObjectType.Arrow)
					{
						CutObjectArrow arrowObj = (CutObjectArrow)path;
						Path pathArrow = new Path();
						RectF rect = new RectF(path.get(0).x*xUnit, path.get(0).y*yUnit, 
								path.get(1).x*xUnit, path.get(1).y*yUnit);
						
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
						
						for(int n = 0; n < outputPoints.size(); n++)
						{
							if (n == 0)
							{
								data += String.format("%s%d,%d%s%s%d,%d", 
										upCommand,
										(int)outputPoints.get(n).x, (int)outputPoints.get(n).y,
										separator,
										downCommand,
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
							
							for(int n = 0; n < outputPoints.size(); n++)
							{
								if (n == 0)
								{
									data += String.format("%s%d,%d%s%s%d,%d", 
											upCommand,
											(int)outputPoints.get(n).x, (int)outputPoints.get(n).y,
											separator,
											downCommand,
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
						
						for(int n = 0; n < outputPoints.size(); n++)
						{
							if (n == 0)
							{
								data += String.format("%s%d,%d%s%s%d,%d", 
										upCommand,
										(int)outputPoints.get(n).x, (int)outputPoints.get(n).y,
										separator,
										downCommand,
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
				if (path.getType() == CutObject.CutObjectType.Text)
				{
					CutObjectText textObj = (CutObjectText)path;
					ArrayList<Path> pathes = textObj.getTextPathes(textObj.getText());
					
					for(int nn = 0; nn < pathes.size(); nn++)
					{
						Path textPath = pathes.get(nn);
						PathMeasure pathMeasure = new PathMeasure(textPath, false);
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
						
						for(int n = 0; n < outputPoints.size(); n++)
						{
							if (n == 0)
							{
								data += String.format("%s%d,%d%s%s%d,%d", 
										upCommand,
										(int)outputPoints.get(n).x, (int)outputPoints.get(n).y,
										separator,
										downCommand,
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
						
						double x = point.x*xUnit;
						double y = point.y*yUnit;
						
						if (i == 0)
						{
							data += String.format("%s%d,%d%s%s%s", upCommand, (int)y, (int)x, separator, downCommand, separator);
							data += String.format(downCommand);
						}
					   
						data += String.format("%d,%d,", (int)y, (int)x);
					}
					
					data = data.substring(0, data.length() - 1);
					data += separator;
				}
		   
				data += String.format(upCommand + separator);
				
				publishProgress((int) ((j / (float) objects.size()) * 100));
				
				if (data.length() > 100000)
				{
					sendDataToPort(data.getBytes());
					data = "";
				}
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
