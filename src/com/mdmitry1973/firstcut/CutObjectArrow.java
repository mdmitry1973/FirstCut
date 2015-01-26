package com.mdmitry1973.firstcut;

import java.util.ArrayList;

import com.mdmitry1973.firstcut.CutObject.CutObjectType;

import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.Path.Direction;
import android.os.AsyncTask;

public class CutObjectArrow  extends CutObject {
	
	private float tailLengthPr= 0.75f; 
	private float capLengthPr = 0.75f;
	private float tailWidthPr = 0.5f;
	
	public CutObjectArrow()
	{
		super();
	}
	
	public CutObjectArrow(ArrayList<PointF> path)
	{
		super(path);
	}
	
	public float getTailLength()
	{
		return tailLengthPr; 
	}
	
	public float getCapLength()
	{
		return capLengthPr; 
	}
	
	public float getTailWidth()
	{
		return tailWidthPr; 
	}
	
	public void setTailLength(float tailLength)
	{
		this.tailLengthPr = tailLength; 
	}
	
	public void setCapLength(float capLength)
	{
		this.capLengthPr = capLength; 
	}
	
	public void setTailWidth(float tailWidth)
	{
		this.tailWidthPr = tailWidth; 
	}
	
	@Override
	public void add(ArrayList<PointF> path)
	{
		listPath = new ArrayList<PointF>(path);
	}
	
	@Override
	public CutObjectType getType()
	{
		return CutObjectType.Arrow;
	}
	
	@Override
	public Path getDrawPath()
	{
		drawPath.reset();
		
		if (listPath.size() > 1) 
		{
			RectF rect = new RectF(listPath.get(0).x, listPath.get(0).y, listPath.get(1).x, listPath.get(1).y);
			
			PointF centerCap = new PointF(rect.right, rect.top + (rect.height()/2));
			
			float capLength = rect.width() * capLengthPr;
			float tailLength = rect.width() * tailLengthPr;
			float tailWidth = rect.height() * tailWidthPr;
			
			drawPath.moveTo(centerCap.x, centerCap.y);
			drawPath.lineTo(rect.left + capLength, rect.top);
			drawPath.lineTo(rect.left + tailLength, (rect.top + (rect.height()/2)) - tailWidth/2);
			drawPath.lineTo(rect.left, (rect.top + (rect.height()/2)) - tailWidth/2);
			drawPath.lineTo(rect.left, (rect.top + (rect.height()/2)) + tailWidth/2);
			drawPath.lineTo(rect.left + tailLength, (rect.top + (rect.height()/2)) + tailWidth/2);
			drawPath.lineTo(rect.left + capLength, rect.bottom);
			drawPath.lineTo(centerCap.x, centerCap.y);
			
			drawPath.close();
			
			drawMatrix.reset();
			drawMatrix.setRotate(degrees, rect.centerX(), rect.centerY());
			drawPath.transform(drawMatrix);
		} 
		
		return drawPath;
	}
}
