package com.mdmitry1973.firstcut;

import java.util.ArrayList;

import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;

public class CutObject {
	
	enum CutObjectType {
		Line,
		Pen,
		Box
	};

	private ArrayList<PointF> listPath;
	private CutObjectType type;
	private Matrix matrixPath;
	
	public CutObject()
	{
		type = CutObjectType.Line;
		matrixPath = new Matrix();
	}
	
	public CutObject(ArrayList<PointF> path, CutObjectType type)
	{
		listPath = path;
		this.type = type;
		matrixPath = new Matrix();
		
		close();
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
	
	public Matrix getMatrix()
	{
		return matrixPath;
	}
	
	public void setMatrix(Matrix matrixPath)
	{
		this.matrixPath = matrixPath;
		
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
	
	public void add(ArrayList<PointF> path)
	{
		listPath = path;
	}
	
	public void add(ArrayList<PointF> path, CutObjectType type)
	{
		listPath = path;
		this.type = type;
	}
	
	public void add(PointF point)
	{
		listPath.add(point);
	}
	
	public void setType(CutObjectType type)
	{
		this.type = type;
	}
	
	public CutObjectType getType()
	{
		return type;
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
}
