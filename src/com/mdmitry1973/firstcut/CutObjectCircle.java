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

public class CutObjectCircle  extends CutObject {
	
	public CutObjectCircle()
	{
		super();
	}
	
	public CutObjectCircle(ArrayList<PointF> path)
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
		return CutObjectType.Circle;
	}
	
	@Override
	public Path getDrawPath()
	{
		drawPath.reset();
	
		RectF rect = new RectF(listPath.get(0).x, listPath.get(0).y, listPath.get(1).x, listPath.get(1).y);
		
		rect.sort();
		
		drawPath.addOval(rect, Direction.CW);
		
		drawPath.close();
		
		return drawPath;
	}
}
