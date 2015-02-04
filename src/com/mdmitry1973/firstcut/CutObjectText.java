package com.mdmitry1973.firstcut;

import java.util.ArrayList;

import com.mdmitry1973.firstcut.CutObject.CutObjectType;

import android.content.SharedPreferences;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.Path.Direction;
import android.util.DisplayMetrics;

import java.io.File;

import org.apache.fontbox.ttf.OS2WindowsMetricsTable;
import org.apache.fontbox.ttf.TrueTypeFont;

public class CutObjectText   extends CutObject  {
	
	private String strText = "";
	private String fontName = "";
	private FontManagerInterface fontMnager = null;
	private Matrix matrixGlyphs = null;
	private RectF maxGlyphBox = null;
	private Path textPath = null;
	private float glyphSpace = 0;
	
	public CutObjectText()
	{
		super();
		
		matrixGlyphs = new Matrix();
		maxGlyphBox = new RectF();
		textPath = new Path();
	}
	
	public CutObjectText(ArrayList<PointF> path)
	{
		super(path);
		
		matrixGlyphs = new Matrix();
		maxGlyphBox = new RectF();
		textPath = new Path();
	}
	
	public void setFontManger(FontManagerInterface fontMnager)
	{
		this.fontMnager = fontMnager;
	}
	
	@Override
	public void setCurrentPrefs(SharedPreferences sharedPref)
	{
		super.setCurrentPrefs(sharedPref);
		
		fontName = sharedPref.getString("currentFont", "droid_sans");
		glyphSpace = sharedPref.getFloat("TextSpace", 0);
	}
	
	public void setGlyphSpace(float glyphSpace)
	{
		this.glyphSpace = glyphSpace;
	}
	
	public float getGlyphSpace()
	{
		return glyphSpace;
	}
	
	public void setText(String strText)
	{
		this.strText = strText;
	}
	
	public String getText()
	{
		return strText;
	}
	
	public void setFontName(String fontName)
	{
		this.fontName = fontName;
	}
	
	public String getFontName()
	{
		return fontName;
	}
	
	public ArrayList<Path> getTextPathes(String str, float xUnit, float yUnit)
	{
		ArrayList<Path> pathes = new ArrayList<Path>();
		Matrix drawMatrix1 = new Matrix();
		Matrix matrixGlyphs1 = new Matrix();
		RectF maxGlyphBox1 = new RectF();
		
		if (listPath.size() > 1 && !str.isEmpty()) 
		{
			ArrayList<Path> arrPath = fontMnager.glyphPaths(fontName, str, maxGlyphBox1);
			DisplayMetrics displayMetrics = fontMnager.getDisplayMetrics();
			float glyphSpacePix = glyphSpace * displayMetrics.xdpi;
			TrueTypeFont font = fontMnager.getFont(fontName);
			OS2WindowsMetricsTable metric = font.getOS2Windows();
			
			short nAverageCharWidth = metric.getAverageCharWidth();
			
			float maxWidth = maxGlyphBox1.width();
			float maxHeight = maxGlyphBox1.height();
			
			RectF rect = new RectF(listPath.get(0).x, listPath.get(0).y, listPath.get(1).x, listPath.get(1).y);
			
			rect.sort();
			
			drawMatrix1.reset();
			drawMatrix1.setRotate(degrees, rect.centerX(), rect.centerY());
			
			float dx = rect.left;
			float dy = rect.bottom;
			float locationX = 0;
			float locationY = 0;
			float factorY = rect.height()/maxHeight;
			float factorX = factorY;//rect.width()/locationX;
			
			//textPath.reset();
			
			nAverageCharWidth = (short)((float)nAverageCharWidth*factorY);
			
			for(int i = 0; i < arrPath.size(); i++)
			{
				RectF bounds = new RectF();
				RectF bounds2 = new RectF();
				Path pathGlyph1 = new Path();
				Path pathGlyph2 = new Path();
				Path p = arrPath.get(i);
				
				if (p.isEmpty())
				{
					locationX = locationX + nAverageCharWidth;
					continue;
				}
				
				pathGlyph1.reset();
				pathGlyph2.reset();
				
				//rotate 180
				matrixGlyphs1.reset();
				matrixGlyphs1.setScale(1, -1);
				p.transform(matrixGlyphs1, pathGlyph1);
				pathGlyph1.offset(0, 0);
				
				//scale to real size
				matrixGlyphs1.reset();
				matrixGlyphs1.setScale(factorX, factorY);
				pathGlyph1.transform(matrixGlyphs1, pathGlyph2);
				
				pathGlyph2.computeBounds(bounds, false);
				bounds2 = new RectF(bounds);
				
				//if (i == 0)
				//{
				//	locationX = locationX + bounds.left;
				//}
				
				bounds2.offsetTo(0 + dx + locationX, bounds.top + dy + locationY);
				//bounds2.offsetTo(locationX, bounds.top + locationY);
				
				matrixGlyphs1.reset();
				matrixGlyphs1.setRectToRect(bounds, bounds2, Matrix.ScaleToFit.FILL);
				
				pathGlyph2.transform(matrixGlyphs1, pathGlyph1);
				
				
				
				//textPath.addPath(pathGlyph2, locationX, locationY);
				//pathGlyph2.offset(locationX, locationY);
				//RectF bounds22 = new RectF();
				//pathGlyph2.computeBounds(bounds22, true);
				//pathGlyph2.offset(locationX - bounds22.left, bounds22.top - locationY);
				//pathGlyph2.close();
				pathGlyph1.transform(drawMatrix1, pathGlyph2);
				pathes.add(pathGlyph2);
				
				//bounds.offsetTo(locationX, bounds.top);
				
				locationX = locationX + bounds.width() + glyphSpacePix;
			}
			
			//textPathResult.addPath(textPath, dx, dy);
			//textPathResult.transform(drawMatrix1);
			//textPathResult.close();
		}
		
		return pathes;
	}
	
	/*
	public ArrayList<Path> getTextPath222(String str)
	{
		ArrayList<Path> textPathResult = new ArrayList<Path>();
		
		if (listPath.size() > 1 && !str.isEmpty()) 
		{
			ArrayList<Path> arrPath = fontMnager.glyphPaths(fontName, str, maxGlyphBox);
			DisplayMetrics displayMetrics = fontMnager.getDisplayMetrics();
			float glyphSpacePix = glyphSpace * displayMetrics.xdpi;
			TrueTypeFont font = fontMnager.getFont(fontName);
			OS2WindowsMetricsTable metric = font.getOS2Windows();
			
			short nAverageCharWidth = metric.getAverageCharWidth();
			
			float maxWidth = maxGlyphBox.width();
			float maxHeight = maxGlyphBox.height();
			
			RectF rect = new RectF(listPath.get(0).x, listPath.get(0).y, listPath.get(1).x, listPath.get(1).y);
			
			rect.sort();
			
			drawMatrix.reset();
			drawMatrix.setRotate(degrees, rect.centerX(), rect.centerY());
			
			//if (strText.isEmpty())
			//{
			//	textPathResult.addRect(rect, Direction.CW);
			//}
			
			float dx = rect.left;
			float dy = rect.bottom;
			float locationX = 0;
			float locationY = 0;
			float factorY = rect.height()/maxHeight;
			float factorX = factorY;
			
			textPath.reset();
			
			nAverageCharWidth = (short)((float)nAverageCharWidth*factorY);
			
			for(int i = 0; i < arrPath.size(); i++)
			{
				RectF bounds = new RectF();
				RectF bounds2 = new RectF();
				Path pathGlyph1 = new Path();
				Path pathGlyph2 = new Path();
				
				Path p = arrPath.get(i);
				
				if (p.isEmpty())
				{
					locationX = locationX + nAverageCharWidth;
					continue;
				}
				
				pathGlyph1.reset();
				pathGlyph2.reset();
				
				matrixGlyphs.reset();
				matrixGlyphs.setScale(1, -1);
				p.transform(matrixGlyphs, pathGlyph1);
				
				matrixGlyphs.reset();
				matrixGlyphs.setScale(factorX, factorY);
				pathGlyph1.transform(matrixGlyphs, pathGlyph2);
				
				pathGlyph2.computeBounds(bounds, false);
				bounds2 = new RectF(bounds);
				
				bounds2.offsetTo(0, bounds.top);
				
				matrixGlyphs.reset();
				matrixGlyphs.setRectToRect(bounds, bounds2, Matrix.ScaleToFit.FILL);
				
				pathGlyph2.transform(matrixGlyphs);
				
				//textPath.addPath(pathGlyph2, locationX, locationY);
				{
					pathGlyph2.offset(dx + locationX, dy);
					
				}
				textPathResult.add(pathGlyph2);
				bounds.offsetTo(locationX, bounds.top);
				
				locationX = locationX + bounds.width() + glyphSpacePix;
			}
			
			//textPathResult.addPath(textPath, dx, dy);
			
			//textPathResult.transform(drawMatrix);
			//textPathResult.close();
		}
		
		return textPathResult;
	}
	*/
	
	
	public Path getTextPath(Path textPathResult, String str)
	{
		textPathResult.reset();
		
		if (listPath.size() > 1 && !str.isEmpty()) 
		{
			ArrayList<Path> arrPath = fontMnager.glyphPaths(fontName, str, maxGlyphBox);
			DisplayMetrics displayMetrics = fontMnager.getDisplayMetrics();
			float glyphSpacePix = glyphSpace * displayMetrics.xdpi;
			TrueTypeFont font = fontMnager.getFont(fontName);
			OS2WindowsMetricsTable metric = font.getOS2Windows();
			
			short nAverageCharWidth = metric.getAverageCharWidth();
			
			float maxWidth = maxGlyphBox.width();
			float maxHeight = maxGlyphBox.height();
			
			RectF rect = new RectF(listPath.get(0).x, listPath.get(0).y, listPath.get(1).x, listPath.get(1).y);
			
			rect.sort();
			
			drawMatrix.reset();
			drawMatrix.setRotate(degrees, rect.centerX(), rect.centerY());
			
			if (strText.isEmpty())
			{
				textPathResult.addRect(rect, Direction.CW);
			}
			
			float dx = rect.left;
			float dy = rect.bottom;
			float locationX = 0;
			float locationY = 0;
			
			RectF bounds = new RectF();
			RectF bounds2 = new RectF();
			Path pathGlyph1 = new Path();
			Path pathGlyph2 = new Path();
			
			float factorY = rect.height()/maxHeight;
			float factorX = factorY;
			
			textPath.reset();
			
			nAverageCharWidth = (short)((float)nAverageCharWidth*factorY);
			
			for(int i = 0; i < arrPath.size(); i++)
			{
				Path p = arrPath.get(i);
				
				if (p.isEmpty())
				{
					locationX = locationX + nAverageCharWidth;
					continue;
				}
				
				pathGlyph1.reset();
				pathGlyph2.reset();
				
				matrixGlyphs.reset();
				matrixGlyphs.setScale(1, -1);
				p.transform(matrixGlyphs, pathGlyph1);
				
				matrixGlyphs.reset();
				matrixGlyphs.setScale(factorX, factorY);
				pathGlyph1.transform(matrixGlyphs, pathGlyph2);
				
				pathGlyph2.computeBounds(bounds, false);
				bounds2 = new RectF(bounds);
				
				bounds2.offsetTo(0, bounds.top);
				
				matrixGlyphs.reset();
				matrixGlyphs.setRectToRect(bounds, bounds2, Matrix.ScaleToFit.FILL);
				
				pathGlyph2.transform(matrixGlyphs);
				
				textPath.addPath(pathGlyph2, locationX, locationY);
				bounds.offsetTo(locationX, bounds.top);
				
				locationX = locationX + bounds.width() + glyphSpacePix;
			}
			
			textPathResult.addPath(textPath, dx, dy);
			
			textPathResult.transform(drawMatrix);
			textPathResult.close();
		}
		
		return textPathResult;
	}
	
	
	@Override
	public void add(ArrayList<PointF> path)
	{
		listPath = new ArrayList<PointF>(path);
	}
	
	@Override
	public CutObjectType getType()
	{
		return CutObjectType.Text;
	}
	
	@Override
	public Path getDrawPath()
	{
		drawPath.reset();
		
		String str = strText;
		
		if (str.isEmpty())
		{
			str = "Example";
		}
		
		if (listPath.size() > 1) 
		{
			getTextPath(drawPath, str);
		}
		
		return drawPath;
	}
}
