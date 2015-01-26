package com.mdmitry1973.firstcut;

import java.util.ArrayList;

import android.content.SharedPreferences;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.Log;

public class CutObjectStar  extends CutObject  {

	private long numOfPt = 5; 
	private long innerRadius = 50;
	
	public CutObjectStar()
	{
		super();
	}
	
	public CutObjectStar(ArrayList<PointF> path)
	{
		super(path);
	}
	
	@Override
	public void setCurrentPrefs(SharedPreferences sharedPref)
	{
		super.setCurrentPrefs(sharedPref);
		
		innerRadius = sharedPref.getLong("Star_InnerRadius", 50);
		numOfPt = sharedPref.getLong("Star_NumPoints", 5);
	}
	
	public long getNumOfPt()
	{
		return numOfPt;
	}
	
	public void setNumOfPt(int numOfPt)
	{
		this.numOfPt = numOfPt;
	}
	
	public long getInnerRadius()
	{
		return innerRadius;
	}
	
	public void setInnerRadius(int innerRadius)
	{
		this.innerRadius = innerRadius;
	}
	
	@Override
	public void add(ArrayList<PointF> path)
	{
		listPath = new ArrayList<PointF>(path);
	}
	
	@Override
	public CutObjectType getType()
	{
		return CutObjectType.Star;
	}
	
	public Path setStar(float x, float y, float radius, float innerRadius, long numOfPt)
	{
		double section = 2.0 * Math.PI/numOfPt;
		float x1 = 0; 
		float y1 = 0;
			  
		drawPath.reset();
		
		x1 = (float)(x + radius * Math.cos(0));//Math.PI/2)); 
		y1 = (float)(y + radius * Math.sin(0));
		
		drawPath.moveTo(x1, y1);
		
		x1 = (float)(x + innerRadius * Math.cos(0 + section/2.0)); //Math.PI/2 + section/2.0)); 
		y1 = (float)(y + innerRadius * Math.sin(0 + section/2.0));
		
		drawPath.lineTo(x1, y1);
		  
		  for(int ii = 1; ii < numOfPt; ii++)
		  {
			  drawPath.lineTo(
		    (float)(x + radius * Math.cos(section * ii)), 
		    (float)(y + radius * Math.sin(section * ii)));
			  drawPath.lineTo(
		     (float)(x + innerRadius * Math.cos(section * ii + section/2.0)), 
		     (float)(y + innerRadius * Math.sin(section * ii + section/2.0)));
		  }
		  
		  drawPath.close();
		  
		  return drawPath;
	}
	
	@Override
	public Path getDrawPath()
	{
		drawPath.reset();
		
		if (listPath.size() > 1) 
		{
			RectF rect = new RectF(listPath.get(0).x, listPath.get(0).y, listPath.get(1).x, listPath.get(1).y);
			
			float x = rect.centerX();
			float y = rect.centerY();
			float radius = rect.width()/2;
			float innerRadiusPix = (float) (radius * ((float)innerRadius/100.0f)); 
			
			setStar(x, y, radius, innerRadiusPix, numOfPt);
			
			drawMatrix.reset();
			drawMatrix.setRotate(-19 + degrees, rect.centerX(), rect.centerY());
			drawPath.transform(drawMatrix);
		} 
		
		return drawPath;
	}
}
