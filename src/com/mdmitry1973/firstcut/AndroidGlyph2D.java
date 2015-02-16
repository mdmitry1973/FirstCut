package com.mdmitry1973.firstcut;

import org.apache.fontbox.ttf.GlyfDescript;
import org.apache.fontbox.ttf.GlyphDescription;

import android.graphics.Path;
import android.graphics.Point;
import android.util.Log;

public class AndroidGlyph2D {
	
	private short leftSideBearing = 0;
    private int advanceWidth = 0;
    private PointGlyph2D[] points = null;
    private Path glyphPath;

    /**
     * Constructor.
     * 
     * @param gd the glyph description
     * @param lsb leftSideBearing
     * @param advance advanceWidth
     */
    public AndroidGlyph2D(GlyphDescription gd, short lsb, int advance) 
    {
        leftSideBearing = lsb;
        advanceWidth = advance;
        describe(gd);
    }

    /**
     * Returns the advanceWidth value.
     * 
     * @return the advanceWidth
     */
    public int getAdvanceWidth() 
    {
        return advanceWidth;
    }

    /**
     * Returns the leftSideBearing value.
     * 
     * @return the leftSideBearing
     */
    public short getLeftSideBearing() 
    {
        return leftSideBearing;
    }

    /**
     * Set the points of a glyph from the GlyphDescription.
     */
    private void describe(GlyphDescription gd) 
    {
        int endPtIndex = 0;
        points = new PointGlyph2D[gd.getPointCount()];
        for (int i = 0; i < gd.getPointCount(); i++) 
        {
            boolean endPt = gd.getEndPtOfContours(endPtIndex) == i;
            if (endPt) 
            {
                endPtIndex++;
            }
            points[i] = new PointGlyph2D(
                    gd.getXCoordinate(i),
                    gd.getYCoordinate(i),
                    (gd.getFlags(i) & GlyfDescript.ON_CURVE) != 0,
                    endPt);
        }
    }
    
    /**
     * Returns the path describing the glyph.
     * 
     * @return the GeneralPath of the glyph
     */
    public Path getPath() 
    {
        if (glyphPath == null)
        {
            glyphPath = calculatePath();
        }
        return glyphPath;
    }
    
    private Path calculatePath()
    {
        Path path = new Path();
        int numberOfPoints = points.length;
        int i = 0;
        boolean endOfContour = true;
        PointGlyph2D startingPoint = null;
        PointGlyph2D lastCtrlPoint = null;
        Point lastPoint = null;
        
        while (i < numberOfPoints) 
        {
        	PointGlyph2D point = points[i%numberOfPoints];
        	PointGlyph2D nextPoint1 = points[(i+1)%numberOfPoints];
        	PointGlyph2D nextPoint2 = points[(i+2)%numberOfPoints];
            // new contour
            if (endOfContour) 
            {
                // skip endOfContour points
                if (point.endOfContour)
                {
                    i++;
                    continue;
                }
                // move to the starting point
                path.moveTo(point.x, point.y);
                endOfContour = false;
                startingPoint = point;
            }
            // lineTo
            if (point.onCurve && nextPoint1.onCurve) 
            {
                path.lineTo(nextPoint1.x, nextPoint1.y);
                lastPoint = new Point(nextPoint1.x, nextPoint1.y);
                i++;
                if (point.endOfContour || nextPoint1.endOfContour)
                {
                    endOfContour = true;
                    path.close();
                    //lastPoint = null;
                }
                
                continue;
            } 
            // quadratic bezier
            if (point.onCurve && !nextPoint1.onCurve && nextPoint2.onCurve) 
            {
                if (nextPoint1.endOfContour)
                {
                    // use the starting point as end point
                    path.quadTo(nextPoint1.x, nextPoint1.y, startingPoint.x, startingPoint.y);
                    lastPoint = new Point(startingPoint.x, startingPoint.y);
                }
                else
                {
                    path.quadTo(nextPoint1.x, nextPoint1.y, nextPoint2.x, nextPoint2.y);
                    lastPoint = new Point(nextPoint2.x, nextPoint2.y);
                }
                if (nextPoint1.endOfContour || nextPoint2.endOfContour)
                {
                    endOfContour = true;
                    path.close();
                    //lastPoint = null;
                }
                i+=2;
                lastCtrlPoint = nextPoint1;
                
                continue;
            } 
            if (point.onCurve && !nextPoint1.onCurve && !nextPoint2.onCurve) 
            {
                // interpolate endPoint
                int endPointX = midValue(nextPoint1.x, nextPoint2.x);
                int endPointY = midValue(nextPoint1.y, nextPoint2.y);
                path.quadTo(nextPoint1.x, nextPoint1.y, endPointX, endPointY);
                lastPoint = new Point(nextPoint2.x, nextPoint2.y);
                if (point.endOfContour || nextPoint1.endOfContour || nextPoint2.endOfContour)
                {
                    path.quadTo(nextPoint2.x, nextPoint2.y, startingPoint.x, startingPoint.y);
                    lastPoint = new Point(startingPoint.x, startingPoint.y);
                    endOfContour = true;
                    path.close();
                    //lastPoint = null;
                }
                i+=2;
                lastCtrlPoint = nextPoint1;
                
                continue;
            } 
            if (!point.onCurve && !nextPoint1.onCurve) 
            {
                //Point2D lastEndPoint = path.getCurrentPoint();
                Point lastEndPoint = lastPoint;
                
                if (lastPoint != null)
                {
	                // calculate new control point using the previous control point
	                lastCtrlPoint = new PointGlyph2D(midValue(lastCtrlPoint.x, (int)lastEndPoint.x),//.getX()), 
	                        midValue(lastCtrlPoint.y, (int)lastEndPoint.y));//.getY()));
	                // interpolate endPoint
	                int endPointX = midValue((int)lastEndPoint.x/*.getX()*/, nextPoint1.x);
	                int endPointY = midValue((int)lastEndPoint.y/*.getY()*/, nextPoint1.y);
	                path.quadTo(lastCtrlPoint.x, lastCtrlPoint.y, endPointX, endPointY);
	                lastPoint = new Point(endPointX, endPointY);
	                if (point.endOfContour || nextPoint1.endOfContour)
	                {
	                    endOfContour = true;
	                    path.close();
	                    //lastPoint = null;
	                }
                }
                else
                {
                	Log.d("AndroidGlyph2D", "calculatePath lastPoint == null");
                }
                i++;
                continue;
            } 
            if (!point.onCurve && nextPoint1.onCurve) 
            {
                path.quadTo(point.x, point.y, nextPoint1.x, nextPoint1.y);
                lastPoint = new Point(nextPoint1.x, nextPoint1.y);
                if (point.endOfContour || nextPoint1.endOfContour)
                {
                    endOfContour = true;
                    path.close();
                    //lastPoint = null;
                }
                i++;
                lastCtrlPoint = point;
               
                continue;
            } 
            System.err.println("Unknown glyph command!!");
            break;
        }
        return path;
    }

    private int midValue(int a, int b) 
    {
        return a + (b - a)/2;
    }

    /**
     * This class represents one point of a glyph.  
     *
     */
    private class PointGlyph2D
    {

        public int x = 0;
        public int y = 0;
        public boolean onCurve = true;
        public boolean endOfContour = false;

        public PointGlyph2D(int xValue, int yValue, boolean onCurveValue, boolean endOfContourValue) 
        {
            x = xValue;
            y = yValue;
            onCurve = onCurveValue;
            endOfContour = endOfContourValue;
        }

        public PointGlyph2D(int xValue, int yValue) 
        {
            this(xValue, yValue, false, false);
        }
    }

}
