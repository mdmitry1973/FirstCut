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

public class CutObjectLine   extends CutObject  {
	
	
	public CutObjectLine()
	{
		super();
	}
	
	public CutObjectLine(ArrayList<PointF> path)
	{
		super(path);
	}
	
	@Override
	public void add(ArrayList<PointF> path)
	{
		listPath = new ArrayList<PointF>(path);
	}
	
	public void add(PointF point)
	{
		listPath.add(point);
	}
	
	@Override
	public CutObjectType getType()
	{
		return CutObjectType.Line;
	}
	
	public int size()
	{
		return listPath.size();
	}
	
	public ArrayList<PointF> getObjectPath()
	{
		return listPath;
	}
	
	public PointF get(int i)
	{
		return listPath.get(i);
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
		
		//drawPath.close();
		
		return drawPath;
	}
}
