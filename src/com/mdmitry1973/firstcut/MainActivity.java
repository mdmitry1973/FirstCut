package com.mdmitry1973.firstcut;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.Socket;
import java.nio.charset.CharsetEncoder;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.apache.fontbox.ttf.CMAPEncodingEntry;
import org.apache.fontbox.ttf.CMAPTable;
import org.apache.fontbox.ttf.GlyfCompositeComp;
import org.apache.fontbox.ttf.GlyfCompositeDescript;
import org.apache.fontbox.ttf.GlyfDescript;
import org.apache.fontbox.ttf.GlyfSimpleDescript;
import org.apache.fontbox.ttf.Glyph2D;
import org.apache.fontbox.ttf.GlyphData;
import org.apache.fontbox.ttf.GlyphDescription;
import org.apache.fontbox.ttf.GlyphTable;
import org.apache.fontbox.ttf.OS2WindowsMetricsTable;
import org.apache.fontbox.ttf.PostScriptTable;
import org.apache.fontbox.ttf.TTFParser;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.fontbox.util.BoundingBox;

import com.mdmitry1973.firstcut.CutObject.CutObjectType;
import com.mdmitry1973.firstcut.PaperManagerDialog.PaperManagerDialogInterface;

import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.ToggleButton;

public class MainActivity extends Activity 
		implements 	DialogInterface.OnDismissListener, 
					ToolsDialogInterface, 
					SendDialoginterface, 
					PaperManagerDialog.PaperManagerDialogInterface,
					PortManagerDialog.PortManagerInterface,
					ToolOptionInterface,
					FontManagerInterface,
					CutOptionsManagerDialog.CutOptionsDialogInterface {
	
	public ProgressDialog progressDialog;
	
	private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
	
	PageViewer pageViewer;
	RulerViewer rulerVer;
	RulerViewer rulerHor;
	
	public boolean bEnableFilePort = false;
	public boolean bEnableLog = true;
	MainActivity m_activity = null;
	
	Button clearBtn;
	ImageButton saveOpenShareBtn;
    ToggleButton handBtn;
    ToggleButton toolBtn;
    ImageButton toolOptionsBtn;
    
    EditText xEdit;
    EditText yEdit;
    EditText widthEdit;
    EditText heightEdit;
    
    EditText editTextZoom;
    TextView textViewZoom;
	
	enum ToolType {
		Line,
		Pen,
		Box,
		Circle,
		Text,
		Star,
		Arrow,
		Hand,
		Resize,
		None;
		
		public final static boolean compare(MainActivity.ToolType toolType, CutObject.CutObjectType objectType)
		{
			if (toolType == MainActivity.ToolType.Line && 
				objectType == CutObject.CutObjectType.Line)
			{
				return true;
			}
			
			if (toolType == MainActivity.ToolType.Pen && 
					objectType == CutObject.CutObjectType.Pen)
				{
					return true;
				}
			
			if (toolType == MainActivity.ToolType.Box && 
					objectType == CutObject.CutObjectType.Box)
				{
					return true;
				}
			
			if (toolType == MainActivity.ToolType.Circle && 
					objectType == CutObject.CutObjectType.Circle)
				{
					return true;
				}
			
			if (toolType == MainActivity.ToolType.Text && 
					objectType == CutObject.CutObjectType.Text)
				{
					return true;
				}
			
			if (toolType == MainActivity.ToolType.Star && 
					objectType == CutObject.CutObjectType.Star)
				{
					return true;
				}
			
			if (toolType == MainActivity.ToolType.Arrow && 
					objectType == CutObject.CutObjectType.Arrow)
				{
					return true;
				}
			return false;
		}
		
		public final static MainActivity.ToolType objectTypeToToolType(CutObject.CutObjectType objectType)
		{
			if (objectType == CutObject.CutObjectType.Line)
			{
				return MainActivity.ToolType.Line;
			}
			
			if (objectType == CutObject.CutObjectType.Pen)
			{
				return MainActivity.ToolType.Pen;
			}
			
			if (objectType == CutObject.CutObjectType.Box)
			{
				return MainActivity.ToolType.Box;
			}
			
			if (objectType == CutObject.CutObjectType.Circle)
			{
				return MainActivity.ToolType.Circle;
			}
			
			if (objectType == CutObject.CutObjectType.Text)
			{
				return MainActivity.ToolType.Text;
			}
			
			if (objectType == CutObject.CutObjectType.Star)
			{
				return MainActivity.ToolType.Star;
			}
			
			if (objectType == CutObject.CutObjectType.Arrow)
			{	
				return MainActivity.ToolType.Arrow;
			}
			
			return MainActivity.ToolType.None;
		}
	};
	
	ToolType nCurrentType = ToolType.Line;
	
	/*
	 * Devices structure
	 <Devices>
	    <item 
	    	name="HP-GL"
	    	resolution="1016"
	      	absolute="PA"
	      	relative="PR"
	      	up="PU"
	      	down="PD"
	      	init="IN;"
	      	separator=";"
	    	>
	      <Settings>
	      	<item 
	      		name="Tool"
	      		type="choice"
	      		format="SP%d;"
	      		>
	      		<value name="Pen" number="1"/> 
	      		<value name="Knife" number="2"/>
	      	</item>
	      </Settings>
	    </item>
	    <item
	    	name="GP-GL"
	      	resolution="254"
	      	absolute=""
	      	relative=""
	      	up="M"
	      	down="D"
	      	init=""
	      	separator=","
	    	>
	 	</item>
	 </Devices>
	 
	 Port format {type,name,...}
	 PORT_USB,Name,Data\n
	 PORT_TCPIP,Name,Port,IP\n
	 */
	
	class CharacterPathFont
	{
		public Map<Character, Path> mapGlyph = null;
		public TrueTypeFont font = null;
		
		public CharacterPathFont(TrueTypeFont font)
		{
			mapGlyph = new HashMap<Character, Path>();
			this.font = font;
		}
	}
	
	String currentFontName = "";
	//private Map<String, CharacterPath> mapGlyph = new HashMap<String, CharacterPath>();
	private Map<String, CharacterPathFont> mapFonts = new HashMap<String, CharacterPathFont>();
	public static ProgressDialog  progressLoadFontDialog = null;
	
	@Override
	public TrueTypeFont getFont(String fontName)
	{
		return mapFonts.get(fontName).font;
	}
	
	public Path glyphPath(String fontName, Character c)
	{
		if (mapFonts.containsKey(fontName))
		{
			TrueTypeFont font = mapFonts.get(fontName).font;
			
			if (mapFonts.get(fontName).mapGlyph.containsKey(c))
			{
				return mapFonts.get(fontName).mapGlyph.get(c);
			}
			
			if (mapFonts.get(fontName).mapGlyph.size() > 100)
			{
				mapFonts.get(fontName).mapGlyph.clear();
			}
			
			try{
			
				OS2WindowsMetricsTable metric = font.getOS2Windows();
				
				if (c >= metric.getFirstCharIndex() &&
					c <= metric.getLastCharIndex())
				{
					float ver = font.getVersion();
					GlyphTable glyphTqable = font.getGlyph();
					PostScriptTable ps = font.getPostScript();
		            String[] names = ps.getGlyphNames();
		            
		            CMAPTable cmapTable = font.getCMAP();
		            CMAPEncodingEntry[] cmaps = cmapTable.getCmaps();
		            int[] glyphToCCode = null;
		            if (cmaps.length == 1)
		            	glyphToCCode = cmaps[0].getGlyphIdToCharacterCode();
		            else {
		            	for( int i = 0; i < cmaps.length; i++ )
		            	{
			                if( cmaps[i].getPlatformId() == CMAPTable.PLATFORM_WINDOWS &&
			                    cmaps[i].getPlatformEncodingId() == CMAPTable.ENCODING_UNICODE )
			                {
			                    glyphToCCode = cmaps[i].getGlyphIdToCharacterCode();
			                }
			            }
		            }
		            
					if (glyphTqable != null)
					{
						for(int j = 0; j < glyphToCCode.length; j++)
						{
							if (glyphToCCode[j] == c)
							{
								GlyphData dataGlyph[] = glyphTqable.getGlyphs();
								
								if (dataGlyph.length > j && dataGlyph[j] != null)
								{
									GlyphDescription desc = dataGlyph[j].getDescription();
									
									if (desc != null)
									{
										////short nOfCon = dataGlyph[j].getNumberOfContours();
										//BoundingBox box = dataGlyph[j].getBoundingBox();
										
										AndroidGlyph2D g2d = new AndroidGlyph2D(desc, (short) 0, 0);
										
										Path path = g2d.getPath();
										
										//RectF bounds = new RectF();
										
										//path.computeBounds(bounds, false);
										
										mapFonts.get(fontName).mapGlyph.put(c, path);
										
										return path;
									}
								}
								else
								{
									Log.d("glyphPath", String.format("dataGlyph[%d] != null c=" + c, j));
								}
							}
						}
					}
				}
			}
			catch(Exception ex)
			{
				Log.d("glyphPath", "" + ex);
			}
		}
		else
		{
			Log.d("glyphPath", "Not load font" + fontName);
		}
		
		return null;
	}
	
	@Override
	public ArrayList<Path> glyphPaths(String fontName, String s, RectF r)
	{
		ArrayList<Path> arrPathes = new ArrayList<Path>();
		RectF r1 = new RectF();
		
		for(int k = 0; k < s.length(); k++)
		{
			if (s.charAt(k) == ' ')
			{
				arrPathes.add(new Path());
			}
			else
			{
				Character c = s.charAt(k);
				Path path = glyphPath(fontName, c);
				
				if (path != null)
				{
					arrPathes.add(path);
					
					path.computeBounds(r1, true);
					
					r.union(r1);
				}
				else
				{
					Log.d("glyphPath", "glyphPath == null");
				}
			}
		}
		
		return arrPathes;
	}
	
	public ArrayList<Path> glyphPaths(String s, RectF r)
	{
		return glyphPaths(currentFontName, s,  r);
	}
	
	public Path glyphPath(Character c)
	{
		return glyphPath(currentFontName, c);
	}
	
	public void setCurrentFont()
	{
		LoadFont(currentFontName);
	}
	
	@Override
	public DisplayMetrics getDisplayMetrics()
	{
		return getResources().getDisplayMetrics();
	}
	
	private class LoadFont extends AsyncTask<String, Void, Boolean> {
        
		String currentFontName; 
		boolean bBold;
		boolean bItalic;
	 	
	 	public LoadFont(String currentFontName, boolean bBold, boolean bItalic) 
	 	{
	 		this.currentFontName = currentFontName; 
	 		this.bBold = bBold;
	 		this.bItalic = bItalic;
	 	}
	 
	 	@Override
        protected Boolean doInBackground(String... fontNames) {
              
      			try {
      				
      				InputStream stream = null;
      				String fontName = currentFontName;
      				
      				if (fontName.contains(".ttf"))
      				{
      					String[] fontdirs = { "/system/fonts", "/system/font", "/data/fonts" };
      					
      					for(int j = 0; j < fontdirs.length; j++)
      					{
      						File fontFile = new File(fontdirs[j], fontName); 
      						
      						if (fontFile.exists())
      						{
      							stream = new FileInputStream(fontFile);
      							break;
      						}
      					}
      				}
      				
      				if (stream == null)
      				{
      					if (fontName.startsWith("droid_sans") == true)
	      				{
      						if (bBold == true)
      						{
      							stream = getResources().openRawResource(R.raw.droid_sans_b);
      						}
      						else
      						{
      							stream = getResources().openRawResource(R.raw.droid_sans);
      						}
	      				}
	      				else
	      					if (fontName.startsWith("anonymous_pro") == true)
	      					{
	      						if (bBold == true && bItalic == true)
	      						{
	      							stream = getResources().openRawResource(R.raw.anonymous_pro_bi);
	      						}
	      						else
	      						if (bItalic == true)
		      					{
		      							stream = getResources().openRawResource(R.raw.anonymous_pro_i);
		      					}
		      					else
	      						if (bBold == true)
	      						{
	      							stream = getResources().openRawResource(R.raw.anonymous_pro_b);
	      						}
	      						else
	      						{
	      							stream = getResources().openRawResource(R.raw.anonymous_pro);
	      						}
	      					}
	      					else
							if (fontName.startsWith("arimo") == true)
							{
								if (bBold == true && bItalic == true)
	      						{
	      							stream = getResources().openRawResource(R.raw.arimo_bi);
	      						}
	      						else
	      						if (bItalic == true)
		      					{
		      							stream = getResources().openRawResource(R.raw.arimo_i);
		      					}
		      					else
	      						if (bBold == true)
	      						{
	      							stream = getResources().openRawResource(R.raw.arimo_b);
	      						}
	      						else
	      						{
	      							stream = getResources().openRawResource(R.raw.arimo);
	      						}
							}
	      				else
	      				{
	      					stream = getResources().openRawResource(R.raw.droid_sans);
	      				}
      				}
      				
      				TTFParser parser = new TTFParser();
      				
      				try {
      					mapFonts.put(fontName, new CharacterPathFont(parser.parseTTF(stream)));
      				} catch (IOException e) {
      					// TODO Auto-generated catch block
      					e.printStackTrace();
      				}
      	            
      	        } catch (Exception e) {
      	            //return e.toString();
      	        	Log.v("MainActivity", "Error" + e);
      	        } finally {
      	           
      	           
      	        }
            	
            	return true;
        }
	 	
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(Boolean result) {
        	progressLoadFontDialog.dismiss();
       }
    }
	
	public void LoadFont(String fontName)
	{
		if (!mapFonts.containsKey(fontName))
		{
			progressLoadFontDialog = new ProgressDialog(this);
			progressLoadFontDialog.setTitle(getResources().getString(R.string.LoadFontTitle));
			progressLoadFontDialog.setMessage(getResources().getString(R.string.LoadFontMsg));
			progressLoadFontDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressLoadFontDialog.setCanceledOnTouchOutside(false);
			progressLoadFontDialog.show();
			
			SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
			int bBold = sharedPrefs.getInt("currentFontBold", 0);
			int bItalic = sharedPrefs.getInt("currentFontItalic", 0);
			
			new LoadFont(fontName, bBold > 0, bItalic > 0).execute(fontName);
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		m_activity = this;
		
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
	   	
		if (!sharedPrefs.contains("Papers"))
	   	{
			SharedPreferences.Editor editor = sharedPrefs.edit();
	    	editor.putString("Papers", "Letter;8.5;11\nLetter Landscape;11;8.5\n");
	    	editor.putInt("currentPaperIndex", 0);
			editor.commit();
	   	}
		
		currentFontName = sharedPrefs.getString("currentFont", "droid_sans");
		
	   	if (!sharedPrefs.contains("Devices"))
	   	{
	   		try 
	   		{
				DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder xmlBuilder = xmlFactory.newDocumentBuilder();
				Document document = xmlBuilder.newDocument();
				Element elDevices = document.createElement("Devices");
				document.appendChild(elDevices);
				
				//HP-GL
				
				Element elItemHP = document.createElement("item");
				
				elDevices.appendChild(elItemHP);
				
				elItemHP.setAttribute("name", "HP-GL");
				elItemHP.setAttribute("resolution", "1016");
				elItemHP.setAttribute("absolute", "PA");
				elItemHP.setAttribute("relative", "PR");
				elItemHP.setAttribute("up", "PU");
				elItemHP.setAttribute("down", "PD");
				elItemHP.setAttribute("init", "IN;");
				elItemHP.setAttribute("separator", ";");
				
				Element elSettingsHP = document.createElement("Settings");
				
				elItemHP.appendChild(elSettingsHP);
				
				Element elItemSettingsHP = document.createElement("item");
				
				elSettingsHP.appendChild(elItemSettingsHP);
				
				elItemSettingsHP.setAttribute("name", "Tool");
				elItemSettingsHP.setAttribute("type", "choice");
				elItemSettingsHP.setAttribute("format", "SP%d;");
				
				Element elValue = document.createElement("value");
				
				elItemSettingsHP.appendChild(elValue);
				elValue.setAttribute("Pen", "1");
				
				elValue = document.createElement("value");
				elItemSettingsHP.appendChild(elValue);
				elValue.setAttribute("Knife", "2");
				
				//GP-GL
				
				Element elItemGP = document.createElement("item");
				
				elDevices.appendChild(elItemGP);
				
				elItemGP.setAttribute("name", "GP-GL");
				elItemGP.setAttribute("resolution", "254");
				//elItemGP.setAttribute("absolute", "");
				//elItemGP.setAttribute("relative", "");
				elItemGP.setAttribute("up", "M");
				elItemGP.setAttribute("down", "D");
				//elItemGP.setAttribute("init", "IN;");
				elItemGP.setAttribute("separator", ",");
				
				//GP-GL Robo
				
				Element elItemGPRobo = document.createElement("item");
				
				elDevices.appendChild(elItemGPRobo);
				
				elItemGPRobo.setAttribute("name", "GP-GL Robo");
				elItemGPRobo.setAttribute("resolution", "508");
				//elItemGP.setAttribute("absolute", "");
				//elItemGP.setAttribute("relative", "");
				elItemGPRobo.setAttribute("up", "M");
				elItemGPRobo.setAttribute("down", "D");
				//elItemGP.setAttribute("init", "IN;");
				elItemGPRobo.setAttribute("separator", ",");
				
				
				//
				
				StringWriter sw = new StringWriter();
		        TransformerFactory tf = TransformerFactory.newInstance();
		        Transformer transformer = tf.newTransformer();
		        //transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		        //transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		        //transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		        //transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

		        DOMSource source = new DOMSource(document);
		        transformer.transform(source, new StreamResult(sw));
		        
		    	SharedPreferences.Editor editor = sharedPrefs.edit();
		    	editor.putString("Devices", sw.toString());
		    	editor.putString("currentDevice", "HP-GL");
				editor.commit();
			}
			catch (Exception e) 
			{
				Log.v("MainActivity", "e=" + e);
			} 
	   	}
	   	
	   	rulerHor = (RulerViewer) findViewById(R.id.rulerHorView);
	   	rulerVer = (RulerViewer) findViewById(R.id.rulerVerView);
	   	
	   	xEdit = (EditText) findViewById(R.id.editTextX);
	    yEdit = (EditText) findViewById(R.id.editTextY);
	    widthEdit = (EditText) findViewById(R.id.editTextWidth);
	    heightEdit = (EditText) findViewById(R.id.editTextHeight);
	    
	    xEdit.setOnKeyListener(new OnKeyListener() {
	        
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
				    (keyCode == KeyEvent.KEYCODE_ENTER)) 
				{
				              // Perform action on key press
				              //Toast.makeText(HelloFormStuff.this, edittext.getText(), Toast.LENGTH_SHORT).show();
				         //     return true;
				}
				
				return false;
			}
	    });
	   	
	   	rulerVer.setVertical(true);
	   	
	   	rulerHor.setSecondRuler(rulerVer);
	   	rulerVer.setSecondRuler(rulerHor);
		
		pageViewer = (PageViewer) findViewById(R.id.pageCutView);
		
		pageViewer.setVertical(rulerVer);
		pageViewer.setHorizantal(rulerHor);
		pageViewer.SetCurrentTool(nCurrentType);
		pageViewer.setMainActivity(this);
		
		editTextZoom = (EditText)findViewById(R.id.editTextZoom);
		
		editTextZoom.setOnEditorActionListener(new OnEditorActionListener() {
		    @Override
		    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		        boolean handled = false;
		        if (actionId == EditorInfo.IME_ACTION_DONE) {
		            //sendMessage();
		        	try
		        	{
		        		String strNewZoom = editTextZoom.getText().toString();
		        		float newZoom = Float.parseFloat(strNewZoom)/100;
		        		float max = pageViewer.getZoomMax();
		        		float min = pageViewer.getZoomMin();
		        	
		        		if (newZoom > min && newZoom < max)
		        		{
		        			pageViewer.setZoom(newZoom);
		        		}
		        		else
		        		{
		        			ResetUIInfo();
		        		}
		        	}
		        	catch(NumberFormatException ex)
		        	{
		        		ResetUIInfo();
		        	}
		        }
		        
		        return handled;
		    }
		});
		
	 	clearBtn = (Button)findViewById(R.id.buttonClear);
        clearBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				//glyphPath = getGlyphPath();
				pageViewer.invalidate();
				pageViewer.RemoveCurrent();
			}
		});
        
        saveOpenShareBtn = (ImageButton)findViewById(R.id.buttonSaveOpenShare);
        saveOpenShareBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				OpenSaveShareDialog dialog = new OpenSaveShareDialog(m_activity, m_activity);
	    		dialog.show();
			}
		});
        
        toolOptionsBtn = (ImageButton)findViewById(R.id.buttonToolOptions);
        toolOptionsBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				
				CutObject curentObj = pageViewer.getCurrentObject();
				ToolType tooltype = nCurrentType;
				
				if (curentObj != null)
				{
					tooltype = ToolType.objectTypeToToolType(curentObj.getType());
				}
				
				if (tooltype == MainActivity.ToolType.Arrow)
				{
					ToolOptionDialogArrow dialog = new ToolOptionDialogArrow(m_activity, curentObj);
					dialog.setToolOptionInterface(m_activity);
		    		dialog.show();
				}
				else
				if (tooltype == MainActivity.ToolType.Star)
				{
					ToolOptionDialogStar dialog = new ToolOptionDialogStar(m_activity, curentObj);
					dialog.setToolOptionInterface(m_activity);
		    		dialog.show();
				}
				else
					if (tooltype == MainActivity.ToolType.Text)
					{
						ToolOptionDialogText dialog = new ToolOptionDialogText(m_activity, curentObj);
						dialog.setToolOptionInterface(m_activity);
			    		dialog.show();
					}
				else
				{
					ToolOptionDialogRect dialog = new ToolOptionDialogRect(m_activity, curentObj);
					dialog.setToolOptionInterface(m_activity);
		    		dialog.show();
				}
				
				
			}
		});
        
        toolBtn = (ToggleButton)findViewById(R.id.buttonTools);
        toolBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				boolean on = ((ToggleButton)v).isChecked();
				
				if (on == false)
				{
					((ToggleButton)v).setChecked(true);
				}
				
				ToolsDialog dialog = new ToolsDialog(m_activity);
				dialog.SetToolInterface(m_activity);
	    		dialog.show();
			}
		});
	
        ResetUIInfo();
	}
	
	@Override
	public void onDismiss(DialogInterface dialogInterface)
	{
		
	}
	 
	@Override
	public void OnSendDialog()
	{
		sendData();
	}
	
	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

	    @Override
		public void onReceive(Context context, Intent intent) {
	        String action = intent.getAction();
	        if (ACTION_USB_PERMISSION.equals(action)) {
	            synchronized (this) {
	                UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

	                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
	                    if(device != null){
	                    	
	                    	SendDataTaskUSB usbTask = new SendDataTaskUSB(device, pageViewer.getObjects(), 
	                    											(UsbManager)getSystemService(Context.USB_SERVICE), 
	                    											m_activity);
	                    	
	                    	usbTask.setXDpi(getResources().getDisplayMetrics().xdpi);
	                    	usbTask.setYDpi(getResources().getDisplayMetrics().ydpi);
	                    	usbTask.setPref(PreferenceManager.getDefaultSharedPreferences(m_activity));
	   		   				
	   		   				usbTask.execute("");
	                   }
	                } 
	                else {
	                    Log.d("MainActivity", "permission denied for device " + device);
	                }
	            }
	        }
	    }
	};
	
	public void sendData()
	{
		Log.v("MainActivity", "sendData");
		
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
	   	
	   	String strPorts = sharedPrefs.getString("Ports", "");
	   	String strCurrent = sharedPrefs.getString("currentPort", "");
	   	String strCurrentLine = "";
	   	
	   	if (!strPorts.isEmpty())
	   	{
		   	String []arrPortLines = strPorts.split("\n");
	   		
	   		for (String strLine : arrPortLines) 
	   		{
		   	    String []arr = strLine.split(",");
		   	    String strName = arr[0];
		   	    
		   	    if (strName.startsWith("TYPE_") == true)
			   	{
		   	    	strName = arr[1];
			   	}
		   	    
		   	    if (strCurrent.length() > 0 && strName.compareTo(strCurrent) == 0)
		   	    {
		   	    	strCurrentLine = strLine;
		   	    	break;
		   	    }
		   	}
	   	}
	   	
	   	if (!strCurrentLine.isEmpty())
	   	{
	   		String []arr = strCurrentLine.split(",");
	   		
	   		if (arr.length > 2)
	   		{
	   			String strType = "TYPE_TCPIP";
	   			String strName = arr[0];
	   			int offset = 0;
	   			
		   	    if (strName.startsWith("TYPE_") == true)
			   	{
		   	    	strType = arr[0];
		   	    	strName = arr[1];
		   	    	offset++;
			   	}
		   	   
		   	    if (strType.startsWith("TYPE_TCPIP") == true)
		   	    {
		   	    	ConnectivityManager connMgr = (ConnectivityManager) 
   	    			getSystemService(Context.CONNECTIVITY_SERVICE);
   	    			NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
   	    					
   	    			if (networkInfo != null && networkInfo.isConnected()) 
   	    			{
   	    				String strPortNumber = arr[1 + offset];
   			   			String strTextIP = arr[2 + offset];
   			   			
   		   				SendDataTaskIPTCP tcpTask = new SendDataTaskIPTCP(strName, strPortNumber, strTextIP, pageViewer.getObjects(), m_activity);
   		   				
   		   				tcpTask.setXDpi(getResources().getDisplayMetrics().xdpi);
   		   				tcpTask.setYDpi(getResources().getDisplayMetrics().ydpi);
   		   				tcpTask.setPref(PreferenceManager.getDefaultSharedPreferences(m_activity));
   		   				
   		   				tcpTask.execute("");
   	    			}
   	    			else
   	    			{
   	    				AlertDialog.Builder builder = new AlertDialog.Builder(this);
   	    		    	builder.setMessage(R.string.no_network).setTitle(R.string.app_name).setPositiveButton("Ok", null);
   	    		    	AlertDialog dialog = builder.create();
   	    		    	dialog.show();
   	    			}
		   	    }
		   	    else
		   	    if (strType.startsWith("TYPE_USB") == true)
		   	    {
		   	    	String strUSBPortData = arr[1 + offset];
		   	    	
		   	    	UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
	  				
	  				HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
	  	    		Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
	  	    		while(deviceIterator.hasNext()){
	  	    		    UsbDevice device = deviceIterator.next();
	  	    		    
	  	    		    if (device != null && strUSBPortData.contains(device.getDeviceName()))
	  	    		    {
	  	    		    	PendingIntent mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
	  	    		    	IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
	  	    		    	registerReceiver(mUsbReceiver, filter);

	  	    		    	manager.requestPermission(device, mPermissionIntent);
	  	    		    
	  	    		    	break;
	  	    		    }
	  	    		}
		   	    }
	   		}
	   	}
	}
	
	public void onSend(View v) {
	   	Log.v("MainActivity", "onSend");
	   	 
	   	SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
	   	
	   	if (sharedPrefs.contains("Ports"))
	   	{
	   		SendDialod  dialog = new SendDialod(this);
	   		dialog.setSendDialoginterface(this);
			dialog.show();
	   	}
	   	else
	   	{
	   		PortManagerDialog dialog = new PortManagerDialog(this);
	   		dialog.setPortManagerInterface(this);
			dialog.show();
	   	}
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.action_exit:
	        {
	        	 finish();
	        	 return true;
	        }
	        
	        case R.id.action_about:
	        {
	        	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	        	builder.setMessage("mdmitry1973@gmail.com\nFont folders:/system/fonts;/system/font;/data/fonts").setTitle(R.string.action_about).setPositiveButton("Ok", null);
	        	builder.setNeutralButton("Send log file", new DialogInterface.OnClickListener() {
	                   @Override
					public void onClick(DialogInterface dialog, int id) {
	                	   File logFle = new File(getExternalCacheDir(), "main.log");
	                	   
	                	   if (logFle.exists())
	                	   {
		           				Intent i = new Intent(Intent.ACTION_SEND);
		           				i.setType("message/rfc822");
		           				i.putExtra(Intent.EXTRA_EMAIL, new String[] {"mdmitry1973@gmail.com"});
		           				i.putExtra(Intent.EXTRA_SUBJECT, "Log information");
		           				i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(logFle));
		           				
		           				try {
		           					
		           					startActivity(Intent.createChooser(i, "Send mail..."));
		           					
		           				} catch (Exception ex) {
		           					AppendLogData("" + ex);
		           				}
	           		
	                	   }
	                   }
	        	}
	        	);
	        	
		    	AlertDialog dialog = builder.create();
		    	
		    	dialog.show();
	        }
	        return true;
	        
	        case R.id.action_device_manager:
	        {
	        	DeviceManagerDialog dialog = new DeviceManagerDialog(this);
	    		dialog.show();
	        }
	        return true;
	        
	        case R.id.action_port_manager:
	        {
	    		PortManagerDialog dialog = new PortManagerDialog(this);
	    		dialog.show();
	        }
	        return true;
	        
	        case R.id.action_paper_manager:
	        {
	        	PaperManagerDialog dialog = new PaperManagerDialog(this);
	        	dialog.SetPaperManagerDialogInterface(this);
	    		dialog.show();
	        }
	        return true;
	        
	        case R.id.action_cut_options_manager:
	        {
	        	CutOptionsManagerDialog dialog = new CutOptionsManagerDialog(this);
	        	dialog.SetCutOptionsInterface(this);
	    		dialog.show();
	        }
	        return true;
	        
	        case R.id.action_settings:
	        {
	        	SettingsDialog dialog = new SettingsDialog(this);
	    		dialog.show();
	        }
	        return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}

	@Override
	public void ToolsChanged(ToolType type) {
		
		Drawable drawableTop = null;
		
		if (type == ToolType.Line)
		{
			drawableTop = getResources().getDrawable(R.drawable.line);
		}
		else
			if (type == ToolType.Pen)
			{
				drawableTop = getResources().getDrawable(R.drawable.pen);
			}
			else
				if (type == ToolType.Box)
				{
					drawableTop = getResources().getDrawable(R.drawable.box);
				}
				else
					if (type == ToolType.Circle)
					{
						drawableTop = getResources().getDrawable(R.drawable.circle);
					}
					else
						if (type == ToolType.Text)
						{
							setCurrentFont();
							
							drawableTop = getResources().getDrawable(R.drawable.text);
						}
						else
							if (type == ToolType.Hand)
							{
								drawableTop = getResources().getDrawable(R.drawable.move);
							}
							else
								if (type == ToolType.Resize)
								{
									drawableTop = getResources().getDrawable(R.drawable.resize);
								}
								else
		if (type == ToolType.Star)
		{
			drawableTop = getResources().getDrawable(R.drawable.star);
		}
		else
			if (type == ToolType.Arrow)
			{
				drawableTop = getResources().getDrawable(R.drawable.arrow);
			}
		
		toolBtn.setCompoundDrawablesWithIntrinsicBounds(null, drawableTop , null, null);
		
		nCurrentType = type;
		pageViewer.SetCurrentTool(nCurrentType);
	}
	
	public void WriteObjectsToString(Writer out)
	{
		try{
			
			//Locale fmtLocale = Locale.getDefault();
			//NumberFormat formatter = NumberFormat.getInstance(fmtLocale);
			
			ArrayList<CutObject> listPaths = pageViewer.getObjects();
			DisplayMetrics metrics = getResources().getDisplayMetrics();
	  		BufferedWriter fileBuffer = new BufferedWriter(out);
	  		
	  		float xUnit = 1016/metrics.xdpi;
	  		float yUnit = 1016/metrics.ydpi;
	  		
	  		fileBuffer.write("IN;");
	  		fileBuffer.newLine();
	  		
			for(int i = 0; i < listPaths.size(); i++)
			{
				CutObject object = listPaths.get(i);
				
				if (object.getType() == CutObject.CutObjectType.Box)
				{
					if (object.size() == 2)
					{
						PointF p1 = object.get(0);
						PointF p2 = object.get(1);
						
						fileBuffer.write(String.format("BOX;%f,%f,%f,%f,%f;", 
								p1.x*xUnit, p1.y*yUnit, p2.x*xUnit, p2.y*yUnit,
								object.getDegree()));
					}
				}
				else
				if (object.getType() == CutObject.CutObjectType.Circle)
				{
					if (object.size() == 2)
					{
						PointF p1 = object.get(0);
						PointF p2 = object.get(1);
						
						fileBuffer.write(String.format("CIRCLE;%f,%f,%f,%f,%f;", 
								p1.x*xUnit, p1.y*yUnit, p2.x*xUnit, p2.y*yUnit,
								object.getDegree()));
					}
				}
				else
				if (object.getType() == CutObject.CutObjectType.Text)
				{
					CutObjectText objectText = (CutObjectText)object;
					
					if (object.size() == 2)
					{
						PointF p1 = object.get(0);
						PointF p2 = object.get(1);
						String str = objectText.getText();
						
						str = str.replace(",", "<comma>");
						
						fileBuffer.write(String.format("TEXT;%f,%f,%f,%f,%f,%f,%s,%s;", 
								p1.x*xUnit, p1.y*yUnit, p2.x*xUnit, p2.y*yUnit,
								object.getDegree(), objectText.getGlyphSpace()*yUnit, 
								objectText.getFontName(), str));
					}
				}
				else
				if (object.getType() == CutObject.CutObjectType.Line)
				{
					if (object.size() > 1)
					{
						PointF p1 = object.get(0);
						PointF p2 = object.get(1);
						
						fileBuffer.write(String.format("LINE;%f,%f;%f,%f,%f;", 
								p1.x*xUnit, p1.y*yUnit, p2.x*xUnit, p2.y*yUnit,
								object.getDegree()));
					}
				}
				else
				if (object.getType() == CutObject.CutObjectType.Pen)
				{
					if (object.size() > 1)
					{
						PointF p1 = object.get(0);
						
						fileBuffer.write(String.format("PEN;%f,%f,%f", 
								object.getDegree(), p1.x*xUnit, p1.y*yUnit));
						
						for(int t = 1; t < object.size(); t++)
						{
							PointF p2 = object.get(t);
							
							fileBuffer.write(String.format(",%f,%f",
									p2.x*xUnit, p2.y*yUnit));
						}
						
						fileBuffer.write(";");
					}
				}
				else
				if (object.getType() == CutObject.CutObjectType.Star)
				{
					CutObjectStar starObject = (CutObjectStar)object;
					
					if (object.size() == 2)
					{
						PointF p1 = object.get(0);
						PointF p2 = object.get(1);
						
						long nInnerRadius = starObject.getInnerRadius();
						long nNumOfPt = starObject.getNumOfPt();
						
						fileBuffer.write(String.format("STAR;%f,%f,%f,%f,%d,%d,%f;", 
								p1.x*xUnit, p1.y*yUnit, p2.x*xUnit, p2.y*yUnit,
								nInnerRadius, nNumOfPt, object.getDegree()));
					}
				}
				else
					if (object.getType() == CutObject.CutObjectType.Arrow)
					{
						CutObjectArrow starObject = (CutObjectArrow)object;
						
						if (object.size() == 2)
						{
							PointF p1 = object.get(0);
							PointF p2 = object.get(1);
							
							float fCapLength = starObject.getCapLength();
							float fTailLength = starObject.getTailLength();
							float fTailWidth = starObject.getTailWidth();
							
							fileBuffer.write(String.format("ARROW;%f,%f,%f,%f,%f,%f,%f,%f;", 
									p1.x*xUnit, p1.y*yUnit, p2.x*xUnit, p2.y*yUnit,
									fCapLength, fTailLength, fTailWidth, object.getDegree()));
						}
					}
				
				fileBuffer.newLine();
			}
			
			fileBuffer.close();
  		}
		catch(Exception ex)
		{
			Log.v("MainActivity", "SaveData " + ex);
		}
	}
	
	public void SaveData(File filePath)
	{
		try{
			
			WriteObjectsToString(new FileWriter(filePath));
  		}
		catch(Exception ex)
		{
			Log.v("MainActivity", "SaveData " + ex);
		}
	}
	
	public void OpenData(File filePath)
	{
		try{
			DisplayMetrics metrics = getResources().getDisplayMetrics();
			float xUnit = 1016/metrics.xdpi;
	  		float yUnit = 1016/metrics.ydpi;
			
			ArrayList<CutObject> listPaths = new ArrayList<CutObject>();
			BufferedReader fileBuffer = new BufferedReader(new FileReader(filePath));
			
	  		while(fileBuffer.ready())
			{
	  			String serviceLine = fileBuffer.readLine();
	  			
	  			if (serviceLine != null)
	  			{
	  				if (serviceLine.startsWith("ARROW;") == true)//header
	  				{
	  					CutObjectArrow obj = new CutObjectArrow();
	  					
	  					String strData = serviceLine.substring(6);
	  					
	  					String []arrData = strData.split(",");
	  					
	  					if (arrData.length == 8)
	  					{
	  						ArrayList<PointF> points = new ArrayList<PointF>();
	  						float x1 = Float.parseFloat(arrData[0]);
	  						float y1 = Float.parseFloat(arrData[1]);
	  						float x2 = Float.parseFloat(arrData[2]);
	  						float y2 = Float.parseFloat(arrData[3]);
	  						
	  						float fCapLength = Float.parseFloat(arrData[4]);
	  						float fTailLength = Float.parseFloat(arrData[5]);
	  						float fTailWidth = Float.parseFloat(arrData[6]);
	  						float fDegrees = Float.parseFloat(arrData[7].replaceFirst(";", ""));
	  						
	  						PointF p1 = new PointF(x1/xUnit, y1/yUnit);
							PointF p2 = new PointF(x2/xUnit, y2/yUnit);
							
							points.add(p1);
							points.add(p2);
							
							obj.add(points);
							
							obj.setCapLength(fCapLength);
							obj.setTailLength(fTailLength);
							obj.setTailWidth(fTailWidth);
							obj.setDegree(fDegrees);
							
							listPaths.add(obj);
	  					}
	  					else
	  					{
	  						assert(true);
	  					}
	  				}
	  				else
	  					if (serviceLine.startsWith("STAR;") == true)//header
		  				{
	  						CutObjectStar obj = new CutObjectStar();
		  					
		  					String strData = serviceLine.substring(5);
		  					
		  					String []arrData = strData.split(",");
		  					
		  					if (arrData.length == 7)
		  					{
		  						ArrayList<PointF> points = new ArrayList<PointF>();
		  						float x1 = Float.parseFloat(arrData[0]);
		  						float y1 = Float.parseFloat(arrData[1]);
		  						float x2 = Float.parseFloat(arrData[2]);
		  						float y2 = Float.parseFloat(arrData[3]);
		  						
		  						long nInnerRadius = Integer.parseInt(arrData[4]);
								long nNumOfPt = Integer.parseInt(arrData[5]);
								float fDegrees = Float.parseFloat(arrData[6].replaceFirst(";", ""));
		  						
		  						PointF p1 = new PointF(x1/xUnit, y1/yUnit);
								PointF p2 = new PointF(x2/xUnit, y2/yUnit);
								
								points.add(p1);
								points.add(p2);
								
								obj.add(points);
								
								obj.setInnerRadius((int)nInnerRadius);
								obj.setNumOfPt((int)nNumOfPt);
								obj.setDegree(fDegrees);
								
								listPaths.add(obj);
		  					}
		  					else
		  					{
		  						assert(true);
		  					}
		  				}
		  				else
		  					if (serviceLine.startsWith("PEN;") == true)//header
			  				{
		  						CutObjectPen obj = new CutObjectPen();
		  						String strData = serviceLine.substring(4);
		  						String []arrData = strData.split(",");
			  					
			  					if (arrData.length >= 3)
			  					{
			  						float fDegrees = Float.parseFloat(arrData[0]);
			  						ArrayList<PointF> points = new ArrayList<PointF>();
			  						
			  						for(int j = 1; j < arrData.length;)
			  						{
				  						float x1 = Float.parseFloat(arrData[j]);
				  						
				  						j++;
				  						
				  						float y1 = Float.parseFloat(arrData[j].replaceFirst(";", ""));
				  						
				  						PointF p1 = new PointF(x1/xUnit, y1/yUnit);
										
										points.add(p1);
			  						}
									
									obj.add(points);
									
									obj.setDegree(fDegrees);
									
									listPaths.add(obj);
			  					}
			  					else
			  					{
			  						assert(true);
			  					}
			  				}
			  				else
			  					if (serviceLine.startsWith("LINE;") == true)//header
				  				{
			  						CutObjectLine obj = new CutObjectLine();
			  						String strData = serviceLine.substring(5);
			  						String []arrData = strData.split(",");
				  					
				  					if (arrData.length == 5)
				  					{
				  						ArrayList<PointF> points = new ArrayList<PointF>();
				  						float x1 = Float.parseFloat(arrData[0]);
				  						float y1 = Float.parseFloat(arrData[1]);
				  						float x2 = Float.parseFloat(arrData[2]);
				  						float y2 = Float.parseFloat(arrData[3]);
				  						
				  						float fDegrees = Float.parseFloat(arrData[4].replaceFirst(";", ""));
				  						
				  						PointF p1 = new PointF(x1/xUnit, y1/yUnit);
										PointF p2 = new PointF(x2/xUnit, y2/yUnit);
										
										points.add(p1);
										points.add(p2);
										
										obj.add(points);
										
										obj.setDegree(fDegrees);
										
										listPaths.add(obj);
				  					}
				  					else
				  					{
				  						assert(true);
				  					}
				  				}
				  				else
				  					if (serviceLine.startsWith("CIRCLE;") == true)//header
					  				{
				  						CutObjectCircle obj = new CutObjectCircle();
				  						String strData = serviceLine.substring(7);
				  						String []arrData = strData.split(",");
					  					
					  					if (arrData.length == 5)
					  					{
					  						ArrayList<PointF> points = new ArrayList<PointF>();
					  						float x1 = Float.parseFloat(arrData[0]);
					  						float y1 = Float.parseFloat(arrData[1]);
					  						float x2 = Float.parseFloat(arrData[2]);
					  						float y2 = Float.parseFloat(arrData[3]);
					  						
					  						float fDegrees = Float.parseFloat(arrData[4].replaceFirst(";", ""));
					  						
					  						PointF p1 = new PointF(x1/xUnit, y1/yUnit);
											PointF p2 = new PointF(x2/xUnit, y2/yUnit);
											
											points.add(p1);
											points.add(p2);
											
											obj.add(points);
											
											obj.setDegree(fDegrees);
											
											listPaths.add(obj);
					  					}
					  					else
					  					{
					  						assert(true);
					  					}
					  				}
					  				else
					  					if (serviceLine.startsWith("BOX;") == true)//header
						  				{
					  						CutObjectRect obj = new CutObjectRect();
					  						String strData = serviceLine.substring(4);
						  					String []arrData = strData.split(",");
						  					
						  					if (arrData.length == 5)
						  					{
						  						ArrayList<PointF> points = new ArrayList<PointF>();
						  						float x1 = Float.parseFloat(arrData[0]);
						  						float y1 = Float.parseFloat(arrData[1]);
						  						float x2 = Float.parseFloat(arrData[2]);
						  						float y2 = Float.parseFloat(arrData[3]);
						  						
						  						float fDegrees = Float.parseFloat(arrData[4].replaceFirst(";", ""));
						  						
						  						PointF p1 = new PointF(x1/xUnit, y1/yUnit);
												PointF p2 = new PointF(x2/xUnit, y2/yUnit);
												
												points.add(p1);
												points.add(p2);
												
												obj.add(points);
												
												obj.setDegree(fDegrees);
												
												listPaths.add(obj);
						  					}
						  					else
						  					{
						  						assert(true);
						  					}
						  				}
						  				else
						  					if (serviceLine.startsWith("TEXT;") == true)//header
							  				{
						  						CutObjectText obj = new CutObjectText();
						  						String strData = serviceLine.substring(5);
						  						String []arrData = strData.split(",");
							  					
							  					if (arrData.length == 8)
							  					{
							  						ArrayList<PointF> points = new ArrayList<PointF>();
							  						float x1 = Float.parseFloat(arrData[0]);
							  						float y1 = Float.parseFloat(arrData[1]);
							  						float x2 = Float.parseFloat(arrData[2]);
							  						float y2 = Float.parseFloat(arrData[3]);
							  						float fDegrees = Float.parseFloat(arrData[4]);
							  						float fSpace = Float.parseFloat(arrData[5]);
							  						String fontName = arrData[6];
							  						String textStr = arrData[7];
							  						
							  						PointF p1 = new PointF(x1/xUnit, y1/yUnit);
													PointF p2 = new PointF(x2/xUnit, y2/yUnit);
													
													points.add(p1);
													points.add(p2);
													
													obj.add(points);
													
													obj.setDegree(fDegrees);
													obj.setGlyphSpace(fSpace/xUnit);
													obj.setText(textStr);
													obj.setFontName(fontName);
													obj.setFontManger(this);
													
													listPaths.add(obj);
							  					}
							  					else
							  					{
							  						assert(true);
							  					}
							  				}
						  					else
	  				if (serviceLine.startsWith("IN;") == true)//header
	  				{
	  					
	  				}
	  				else
	  				if (serviceLine.startsWith("PA;PU") == true)//line or pen
		  			{
	  					String []arrCommands = serviceLine.split(";");
	  					
	  					if (arrCommands.length > 2)
	  					{
	  						String line = arrCommands[2].substring(2);
	  						String []arrPoints = line.split(",");
	  						ArrayList<PointF> points = new ArrayList<PointF>();
	  						
		  					for(int i = 0; i < arrPoints.length; i = i + 2)
		  					{
		  						PointF point = new PointF();
		  						int x = Integer.parseInt(arrPoints[i]);
		  						int y = Integer.parseInt(arrPoints[i + 1]);
		  						
		  						point.x = (float)x/xUnit;
		  						point.y = (float)y/yUnit;
		  						
		  						points.add(point);
		  					}
		  					
		  					if (points.size() > 1)
		  					{
		  						
		  						CutObject obj = null;//CutObject.CreateObject(CutObject.CutObjectType);//new CutObject();
		  						
		  						if (points.size() > 2)
			  					{
		  							//obj.setType(CutObject.CutObjectType.Pen);
		  							obj = CutObject.CreateObject(CutObject.CutObjectType.Pen);
			  					}
		  						else
		  						{
		  							//obj.setType(CutObject.CutObjectType.Line);
		  							obj = CutObject.CreateObject(CutObject.CutObjectType.Line);
		  						}
		  						
		  						obj.add(points);
		  						
		  						listPaths.add(obj);
		  					}
	  					}
		  			}
	  			}
			}
	  		
	  		fileBuffer.close();
	  		
	  		pageViewer.setObjects(listPaths);
	  		pageViewer.DrawBitmap();
	  		pageViewer.invalidate();
  		}
		catch(Exception ex)
		{
			Log.v("MainActivity", "OpenData " + ex);
		}
	}
	
	public void ShareData()
	{
		StringWriter outputWriter = new StringWriter();
		
		try {
		
			WriteObjectsToString(outputWriter);
			
			Intent i = new Intent(Intent.ACTION_SEND);
			i.setType("message/rfc822");
			i.putExtra(Intent.EXTRA_EMAIL, new String[] {""});
			i.putExtra(Intent.EXTRA_SUBJECT, "Cut job");
			i.putExtra(Intent.EXTRA_TEXT, outputWriter.toString());
			
			startActivity(Intent.createChooser(i, "Send mail..."));
			
		} catch (Exception ex) {
			Log.v("MainActivity", "ShareData " + ex);
		}
	}
	
	public void ResetUIInfo()
	{
		editTextZoom.setText(String.format("%.0f", pageViewer.getZoom() * 100));
		
		CutObject obj = pageViewer.getCurrentObject();
		float xInch = 0;
		float yInch = 0;
		float widthInch = 0;
		float heightInch = 0;
		
		if (obj != null)
		{
			RectF boundSelRect = CutObject.getComputeBounds(obj.getListPoints());
			
			if (boundSelRect.isEmpty() == false)
			{
				DisplayMetrics metrics = getResources().getDisplayMetrics();
				SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
				
		    	int currentUnit = sharedPrefs.getInt("unit", 0);
		    	
				xInch = (float)boundSelRect.left / metrics.xdpi;
				yInch = (float)boundSelRect.top / metrics.ydpi;
				widthInch = (float)boundSelRect.width() / metrics.xdpi;
				heightInch = (float)boundSelRect.height() / metrics.ydpi;
				
				if (currentUnit == 1)//mm
				{
					xInch = xInch * 0.0393701f;
					yInch = yInch * 0.0393701f;
					widthInch = widthInch * 0.0393701f;
					heightInch = heightInch * 0.0393701f;
				}
				else
				if (currentUnit == 2)//cm
				{
					xInch = xInch * 0.393701f;
					yInch = yInch * 0.393701f;
					widthInch = widthInch * 0.393701f;
					heightInch = heightInch * 0.393701f;
				}
			}
		}
		
		xEdit.setText(String.format("%.2f", xInch));
	    yEdit.setText(String.format("%.2f", yInch));
	    widthEdit.setText(String.format("%.2f", widthInch));
	    heightEdit.setText(String.format("%.2f", heightInch));
	}
	
	@Override
	public void ResetPaper()
	{
		pageViewer.ResetPaperSize();
		pageViewer.RecalcSize();
	}
	
	public void SetOptions()
	{
		
	}
	
	@Override
	public void onPortManagerFinish()
	{
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
	   	
	   	if (sharedPrefs.contains("Ports"))
	   	{
	   		SendDialod  dialog = new SendDialod(this);
	   		dialog.setSendDialoginterface(this);
			dialog.show();
	   	}
	}
	
	@Override
	public void onToolOptionFinish()
	{
		if (nCurrentType == ToolType.Text)
		{
			SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
			currentFontName = sharedPrefs.getString("currentFont", "droid_sans");
			
			setCurrentFont();
		}
		
		pageViewer.resetSelBox();
		pageViewer.DrawBitmap();
		pageViewer.invalidate();
	}
	
	@Override
	public void onToolOptionApply()
	{
		if (nCurrentType == ToolType.Text)
		{
			SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
			currentFontName = sharedPrefs.getString("currentFont", "droid_sans");
			
			setCurrentFont();
		}
		
		pageViewer.resetSelBox();
		pageViewer.DrawBitmap();
		pageViewer.invalidate();
	}
	
	public void AppendLogData(String line)
	{
		if (bEnableLog == true)
		{
			File tripDataFile = new File(getExternalCacheDir(), "main.log");
			
			if (tripDataFile != null)
			{
				Calendar rightNow = Calendar.getInstance();
		
				int year = rightNow.get(Calendar.YEAR);
				int month = rightNow.get(Calendar.MONTH) + 1;
				int date = rightNow.get(Calendar.DATE);
				int hour = rightNow.get(Calendar.HOUR_OF_DAY);
				int minute = rightNow.get(Calendar.MINUTE);
				int second = rightNow.get(Calendar.SECOND);
		
				try {
					BufferedWriter writeTripData = new BufferedWriter(new FileWriter(tripDataFile, 
														tripDataFile.length() > 50000 ? false : true));
					
					writeTripData.write("" + year + "/" +  
											month + "/" + 
											date + " " + 
											hour + ":" + 
											minute + ":" + 
											second + " " + line);
					writeTripData.newLine();
					
					writeTripData.close();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}


