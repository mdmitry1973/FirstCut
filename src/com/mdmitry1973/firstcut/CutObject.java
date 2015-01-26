package com.mdmitry1973.firstcut;

import java.util.ArrayList;

import android.content.SharedPreferences;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;

public abstract class CutObject {
	
	enum CutObjectType {
		Line,
		Pen,
		Box,
		Circle,
		Star,
		Arrow,
		Text;
	};

	protected ArrayList<PointF> listPath;
	protected Path drawPath;
	protected float degrees;
	protected Matrix drawMatrix;
	
	public CutObject()
	{
		listPath = new ArrayList<PointF>();
		drawPath = new Path();
		drawMatrix = new Matrix();
		degrees = 0;
	}
	
	public CutObject(ArrayList<PointF> path)
	{
		listPath = new ArrayList<PointF>(path);
		drawPath = new Path();
		drawMatrix = new Matrix();
		degrees = 0;
		
		close();
	}
	
	public final static CutObject CreateObject(CutObjectType type)
	{
		CutObject object = null;
		
		if (type == CutObjectType.Box)
		{
			object = new CutObjectRect();
		}
		if (type == CutObjectType.Circle)
		{
			object = new CutObjectCircle();
		}
		if (type == CutObjectType.Line)
		{
			object = new CutObjectLine();
		}
		if (type == CutObjectType.Pen)
		{
			object = new CutObjectPen();
		}
		if (type == CutObjectType.Arrow)
		{
			object = new CutObjectArrow();
		}
		if (type == CutObjectType.Star)
		{
			object = new CutObjectStar();
		}
		if (type == CutObjectType.Text)
		{
			object = new CutObjectText();
		}
		
		return object;
	}
	
	public final static CutObject CreateObject(ArrayList<PointF> path, CutObjectType type)
	{
		CutObject object = null;
		
		if (type == CutObjectType.Box)
		{
			object = new CutObjectRect(path);
		}
		if (type == CutObjectType.Circle)
		{
			object = new CutObjectCircle(path);
		}
		if (type == CutObjectType.Line)
		{
			object = new CutObjectLine(path);
		}
		if (type == CutObjectType.Pen)
		{
			object = new CutObjectPen(path);
		}
		if (type == CutObjectType.Arrow)
		{
			object = new CutObjectArrow(path);
		}
		if (type == CutObjectType.Star)
		{
			object = new CutObjectStar(path);
		}
		if (type == CutObjectType.Text)
		{
			object = new CutObjectText(path);
		}
		
		return object;
	}
	
	public void setCurrentPrefs(SharedPreferences sharedPref)
	{
		setDegree(sharedPref.getLong("Rotate", 0));
	}
	
	public void setComputeBounds(RectF rect)
	{
		RectF currentTRect = getComputeBounds();
		
		Matrix mt = new Matrix();
		
		mt.setRectToRect(currentTRect, rect, Matrix.ScaleToFit.FILL);
		
		setMatrix(mt);
	}
	
	public static RectF getComputeBounds(ArrayList<PointF> points)
	{
		RectF rect = new RectF(0, 0, 0, 0);
		
		if (points.size() > 0)
		{
			rect.left = points.get(0).x;
			rect.top = points.get(0).y;
			rect.right = points.get(0).x;
			rect.bottom = points.get(0).y;
		}
		
		for(int i = 0; i < points.size(); i++)
		{
			PointF p = points.get(i);
			
			if (p.x < rect.left)
			{
				rect.left = p.x;
			}
			
			if (p.y < rect.top)
			{
				rect.top = p.y;
			}
			
			if (p.x > rect.right)
			{
				rect.right = p.x;
			}
			
			if (p.y > rect.bottom)
			{
				rect.bottom = p.y;
			}
		}
		
		return rect;
	}
	
	public RectF getComputeBounds()
	{
		return getComputeBounds(listPath);
	}
	
	public float getDegree()
	{
		return degrees;
	}
	
	public void setDegree(float degrees)
	{
		this.degrees = degrees;
	}
	
	public void setMatrix(Matrix matrixPath)
	{
		float[] dist = new float[listPath.size()*2];
		float[] src = new float[listPath.size()*2];
		
		for(int i = 0, j = 0; i < src.length; i = i + 2, j++)
		{
			src[i] = listPath.get(j).x;
			src[i + 1] = listPath.get(j).y;
		}
		
		matrixPath.mapPoints(dist, src);
		
		listPath.clear();
		
		for(int i = 0; i < dist.length; i = i + 2)
		{
			PointF p = new PointF(dist[i], dist[i + 1]);
			listPath.add(p);
		}
	}
	
	public void close()
	{
		
	}
	
	public ArrayList<PointF> getListPoints()
	{
		return listPath;
	}
	
	public void add(ArrayList<PointF> path)
	{
		listPath = new ArrayList<PointF>(path);
	}
	
	public void add(PointF point)
	{
		listPath.add(point);
	}
	
	public CutObjectType getType()
	{
		return CutObjectType.Box;
	}
	
	public int size()
	{
		return listPath.size();
	}
	
	public void remove(int i)
	{
		listPath.remove(i);
	}
	
	
	public PointF get(int i)
	{
		return listPath.get(i);
	}
	
	public void clear()
	{
		listPath.clear();
	}
	
	public Path getDrawPath()
	{
		return drawPath;
	}
}
