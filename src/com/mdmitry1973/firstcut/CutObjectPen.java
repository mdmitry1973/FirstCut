package com.mdmitry1973.firstcut;

import java.util.ArrayList;

import com.mdmitry1973.firstcut.CutObject.CutObjectType;

import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;

public class CutObjectPen  extends CutObject  {
	
	
	public CutObjectPen()
	{
		super();
	}
	
	public CutObjectPen(ArrayList<PointF> path)
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
		return CutObjectType.Pen;
	}
	
	@Override
	public Path getDrawPath()
	{
		drawPath.reset();
	
		for (int n = 0; n < listPath.size(); n++)
		{
			PointF p1 = listPath.get(n);
			
			if (n == 0)
			{
				drawPath.moveTo(p1.x, p1.y);
			}
			else
			{
				drawPath.lineTo(p1.x, p1.y);
			}
		}
		
		return drawPath;
	}
}
