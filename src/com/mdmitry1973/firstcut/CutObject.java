package com.mdmitry1973.firstcut;

import java.util.ArrayList;

import android.graphics.PointF;

public class CutObject {
	
	enum CutObjectType {
		Line,
		Pen,
		Box
	};

	private ArrayList<PointF> listPath;
	private CutObjectType type;
	
	public CutObject()
	{
		type = CutObjectType.Line;
	}
	
	public CutObject(ArrayList<PointF> path, CutObjectType type)
	{
		listPath = path;
		this.type = type;
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
