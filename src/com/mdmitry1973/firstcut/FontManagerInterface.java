package com.mdmitry1973.firstcut;

import java.util.ArrayList;

import org.apache.fontbox.ttf.TrueTypeFont;

import android.graphics.Path;
import android.graphics.RectF;
import android.util.DisplayMetrics;

public interface FontManagerInterface {

	//public TrueTypeFont getCurrentFont2();
	//public TrueTypeFont getFont(String fontName);
	//public Path glyphPath(Character c);
	//public ArrayList<Path> glyphPaths(String s, RectF r);
	//public Path glyphPath(String fontName, Character c);
	public ArrayList<Path> glyphPaths(String fontName, String s, RectF r);
	public DisplayMetrics getDisplayMetrics();
	public TrueTypeFont getFont(String fontName);
}
