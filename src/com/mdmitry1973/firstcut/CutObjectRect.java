package com.mdmitry1973.firstcut;

import java.util.ArrayList;

import com.mdmitry1973.firstcut.CutObject.CutObjectType;

import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;

public class CutObjectRect  extends CutObject  {

	
	public CutObjectRect()
	{
		super();
	}
	
	public CutObjectRect(ArrayList<PointF> path)
	{
		super(path);
	}
	
	@Override
	public void add(ArrayList<PointF> path)
	{
		listPath = new ArrayList<PointF>(path);
	}
	
	@Override
	public CutObjectType getType()
	{
		return CutObjectType.Box;
	}
	
	@Override
	public Path getDrawPath()
	{
		drawPath.reset();
		
		if (listPath.size() > 1) 
		{
			RectF rect = new RectF(listPath.get(0).x, listPath.get(0).y, listPath.get(1).x, listPath.get(1).y);
			
			drawMatrix.reset();
			drawMatrix.setRotate(degrees, rect.centerX(), rect.centerY());
			
			rect.sort();
			
			drawPath.addRect(rect, Direction.CW);
			drawPath.transform(drawMatrix);
			
			drawPath.close();
		}
		
		return drawPath;
	}
}
