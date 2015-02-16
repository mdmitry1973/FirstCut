package com.mdmitry1973.firstcut;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.AdapterView.OnItemSelectedListener;

public class CutOptionsManagerDialog  extends Dialog 
								implements OnClickListener, 
								DialogInterface.OnDismissListener,
								CutOptionsDialog.CutOptionDialogInterface{
	
	public interface CutOptionsDialogInterface {

		public void SetOptions();
	}
	
	private Map<String, ArrayList<String>> mapCustomOptions = new HashMap<String, ArrayList<String>>();
	private ArrayList<String> arrCustomOptions  = new ArrayList<String>();
	private String currentListCutOptions;
	private List<String> hardCodeArrayList = new ArrayList<String>();
	private Map<String, ArrayList<String>> mapHardCodeOptions = new HashMap<String, ArrayList<String>>();
	private Spinner spinnerCutOptionsList;
	
	private Button buttonAdd;
	private Button buttonEdit;
	private Button buttonRemove;
    
	
	/*
	 * 
	 * None
	 * HP-GL Graphtec Cut Fast
	 * HP-GL Graphtec Cut Slow
	 * HP-GL Graphtec Pen
	 * HP-GL Mutoh Cut Fast
	 * HP-GL Mutoh Cut Slow
	 * HP-GL Mutoh Pen Fast
	 * HP-GL Mutoh Pen Slow
	 * HP-GL Roland Cut Fast
	 * HP-GL Roland Cut Slow
	 * GP-GL Cut Fast
	 * GP-GL Cut Slow
	 * 
	 * 
	 * 
hpgl graphtec
{SP(1,Tool,Cut,2,Plot,1,);}{FS(0,Pressure,27,30,1,0,1,0,);}{FD(0,FD,0,35,5,0,1,0,);}{VS(0,Speed,10,15,5,0,1,0,);}CT1;
Cut Fast = (1,0,0)(0,0,27)(0,1,35)(0,1,15)
Cut Slow = (1,0,0)(0,0,27)(0,1,35)(0,1,10)
Pen Plot = (1,0,1)(0,0,27)(0,1,0)(0,1,15)

mutoh
= {VS(0,Speed,1,100,5,0,1,0,cm/s);}{AS(0,Acceleration,1.0,4.0,0.5,1,1.0,1,G);}{ZF(0,Pressure,20,500,10,0,1,0,g);}{(2,UL8,)}
Cut Fast = (0,1,60)(0,1,1.5)(0,0,50)(2,0,UL8,0.025,2;LT8,2.025,1;)
Cut Slow = (0,1,20)(0,1,1.5)(0,0,50)(2,0,UL8,0.025,2;LT8,2.025,1;)
Pen Plot Fast = (0,1,90)(0,1,2)(0,0,50)(2,0,UL8,0.025,2;LT8,2.025,1;)
Pen Plot Slow = (0,1,45)(0,1,2)(0,0,50)(2,0,UL8,0.025,2;LT8,2.025,1;)
Pounce = (0,0,20)(0,0,1)(0,0,50)(2,1,UL8,0.025,2;LT8,2.025,1;

us cutter
= {SP(1,Tool,Cut,2,Plot,1,);}{FS(0,Pressure,2,510,1,0,1,0,);}{VS(0,Speed,10,960,5,0,1,0,);}{OffsetLocX(0,Offset X,-100.0,100.0,0.1,2,1,2,mm);}{OffsetLocY(0,Offset Y,-100.0,100.0,0.1,2,1,2,mm);}
Cut Fast = (1,0,0)(0,0,27)(0,0,800)(0,0,10.0)(0,0,0.0)
Cut Slow = (1,0,0)(0,0,27)(0,0,100)(0,0,10.0)(0,0,0.0)
Pen Plot = (1,0,1)(0,0,27)(0,0,200)(0,0,10.0)(0,0,0.0)

roland
= {FS(0,Pressure,30,70,1,0,1);}{SP(0,Tool,0,6,1,0,1);}{VS(0,Speed,10,50,10,0,1);}{AS(0,Acceleration,2,4,1,0,1);}{ZU(0,ZU,15,60,5,0,1);}
Tangential Cutter = (0,0,45)(0,1,1)(0,1,20)(0,1,2)(0,1,30)
Swivel Cutter = (0,0,45)(0,1,5)(0,1,20)(0,1,2)
Roller = (0,0,45)(0,1,2)(0,1,30)(0,1,3)
Pen Plot = (0,0,45)(0,1,6)(0,1,40)(0,1,4)

gerber
= {(2,Pounce,)}{ZF(0,Pressure,50,300,10,0,1,0,);}{VS(0,Speed,30,75,5,0,1,0,);}{AS(0,Acceleration,1.0,5.0,0.5,1,1.0,1,);}
Cut Fast = (2,0)(0,0,50)(0,1,60)(0,1,1.5)
Cut Slow = (2,0)(0,0,50)(0,1,40)(0,1,1.5)
Pen Plot = (2,0)(0,0,50)(0,1,45)(0,1,2)
Pounce = (2,1,UL8,0.025,2;LT8,2.025,1;)(0,0,50)(0,0,45)(0,0,2)

gpgl
= {J(0,Condition,1,8,1,0,1,0,),}{!(0,Speed,1,105,1,0,1,0,,,100,),}{*(0,Acceleration,1,8,1,0,1,0,,anch_3_4,),(0,Force,1,48,1,0,1,0,,anch_3_4,),}{L(0,Line Type,0,8,1,0,1,0,),}
Cut Fast = (0,0,1)(0,1,7)(0,0,4)(0,0,10)(0,0,0)
Cut Slow = (0,0,1)(0,1,3)(0,0,4)(0,0,10)(0,0,0)
Pen Plot = (0,0,1)(0,1,10)(0,0,4)(0,0,10)(0,0,0)


robo hpgl
= {L(0,Line Type,0,8,1,0,1,0,),}{B(0,Line scale,0,32000,1,0,1,0,),}{(2,Line Type,),}{!(0,Speed,0,10,1,0,1,0,),}
Cut Fast = (0,1,0)(0,1,100)(2,0,L100,1,100,50)(0,1,7)
Cut Slow = (0,1,0)(0,1,100)(2,0,L100,1,100,50)(0,1,3)
Pen Plot = (0,1,0)(0,1,100)(2,0,L100,1,100,50)(0,1,10)

	 * 
	 */
	
	
	private CutOptionsDialogInterface deligate = null;
	
	public void SetCutOptionsInterface(CutOptionsDialogInterface deligate)
	{
		this.deligate = deligate;
	}

	public CutOptionsManagerDialog(Context context) {
		super(context);
		
		setContentView(R.layout.cut_option_manager);
		setTitle(R.string.CutOptionsManager);
		setCanceledOnTouchOutside(false);
		
		((Button)findViewById(R.id.buttonOK)).setOnClickListener(this);
	    ((Button)findViewById(R.id.buttonCancel)).setOnClickListener(this);
	    
	    buttonAdd = ((Button)findViewById(R.id.buttonAdd));
		buttonEdit = ((Button)findViewById(R.id.buttonEdit));
		buttonRemove = ((Button)findViewById(R.id.buttonRemove));
		
		buttonAdd.setOnClickListener(this);
		buttonEdit.setOnClickListener(this);
		buttonRemove.setOnClickListener(this);
	    
		spinnerCutOptionsList = (Spinner) findViewById(R.id.spinnerOptionSets);
		
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		String customOptions = sharedPrefs.getString("customOptions", "");
		DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
		
		if (!customOptions.isEmpty())
		{
			/*
			 * <Root>
			 * <CutOptions name=\"HP-GL Graphtec Cut Fast\" ><option name=\"Speed\" val=\"VS15\" /></CutOptions>
			 * </Root>
			 * 
			 */
			
			try {
				DocumentBuilder xmlBuilder = xmlFactory.newDocumentBuilder();
				InputStream stream = new ByteArrayInputStream(customOptions.getBytes("UTF-8"));
				
				Document documentCurrent = xmlBuilder.parse(stream);
				Element elRoot = documentCurrent.getDocumentElement();
				
				Element elOption = (Element)elRoot.getFirstChild();
				
				while(elOption != null)
				{
					String name = elOption.getAttribute("name");
					
					Element elOptions = (Element)elOption.getFirstChild();
					ArrayList<String> arrOptions = new ArrayList<String>();
					
					while(elOptions != null)
					{
						String nameOp = elOptions.getAttribute("name");
						String valOp = elOptions.getAttribute("val");
						
						arrOptions.add(nameOp + "|" + valOp);
						
						elOptions = (Element)elOptions.getNextSibling();
					}
					
					mapCustomOptions.put(name, arrOptions);
					arrCustomOptions.add(name);
					
					elOption = (Element)elOption.getNextSibling();
				}
			} 
	   		catch (Exception e) 
	   		{
	        	Log.v("CutOptionsManagerDialog", "Error" + e);
	        } 
		}
		
		setCutOptions();
		
		spinnerCutOptionsList.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				
				if (position + 1 < hardCodeArrayList.size())
				{
					buttonEdit.setEnabled(false);
					buttonRemove.setEnabled(false);
				}
				else
				{
					buttonEdit.setEnabled(true);
					buttonRemove.setEnabled(true);
				}
				
				buttonAdd.setEnabled(!(position == 0));
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				
			}
	    });
	}
	
	public void setCutOptions()
	{
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		
		SpinnerAdapter adp = spinnerCutOptionsList.getAdapter();
		String currentName = "";
		int sel = spinnerCutOptionsList.getSelectedItemPosition();
		String strSel = "";
		
		if (adp != null && sel != -1)
		{
			strSel = (String)adp.getItem(sel);
		}
		else
		{
			strSel = sharedPrefs.getString("currentCutOptions", getContext().getResources().getString(R.string.None));
		}
		
		List<String> myArrayListSt = Arrays.asList(getContext().getResources().getStringArray(R.array.hardCodeCutOption));//new ArrayList<String>();
		List<String> myArrayList = new ArrayList<String>(myArrayListSt);
		DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
		
		myArrayList.add(0, getContext().getResources().getString(R.string.None));
		
		if (strSel.compareTo(getContext().getResources().getString(R.string.None)) == 0)
		{
			sel = 0;
		}
		
		hardCodeArrayList.clear();
		mapHardCodeOptions.clear();
		
		for(int n = 0; n < SendDataTask.strHardcodeCutOptions.length; n++)
		{
			try {
				DocumentBuilder xmlBuilder = xmlFactory.newDocumentBuilder();
				InputStream stream = new ByteArrayInputStream(SendDataTask.strHardcodeCutOptions[n].getBytes("UTF-8"));
				
				Document documentCurrent = xmlBuilder.parse(stream);
				Element elRoot = documentCurrent.getDocumentElement();
				
				String name = elRoot.getAttribute("name");
				
				hardCodeArrayList.add(name);
				
				Element elOptions = (Element)elRoot.getFirstChild();
				ArrayList<String> arrOptions = new ArrayList<String>();
				
				while(elOptions != null)
				{
					String nameOp = elOptions.getAttribute("name");
					String valOp = elOptions.getAttribute("val");
					
					arrOptions.add(nameOp + "|" + valOp);
					
					elOptions = (Element)elOptions.getNextSibling();
				}
				
				mapHardCodeOptions.put(name, arrOptions);
				
				if (strSel.compareTo(name) == 0)
				{
					sel = n + 1;
				}
			} 
	   		catch (Exception e) 
	   		{
	        	Log.v("CutOptionsManagerDialog", "Error" + e);
	        } 
		}
		
		for(int i = 0; i < arrCustomOptions.size(); i++)
		{
			myArrayList.add(arrCustomOptions.get(i));
			
			if (strSel.compareTo(arrCustomOptions.get(i)) == 0)
			{
				sel = myArrayList.size() - 1;
			}
		}
		
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, myArrayList);
        spinnerCutOptionsList.setAdapter(adapter);
        
        if (sel == -1 || sel > myArrayList.size())
        {
        	sel = 0;
        }
        
        spinnerCutOptionsList.setSelection(sel);
	}
	
	public void onClick(View v) {       
		  switch (v.getId()) {
		    case R.id.buttonOK:
		    	onOK(v);
		    break;  
		    case R.id.buttonCancel:
		    	onCancel(v);
		    break;  
		    case R.id.buttonAdd:
		    	onAdd(v);
		    break;  
		    case R.id.buttonEdit:
		    	onEdit(v);
		    	break; 
		    case R.id.buttonRemove:
		    	onRemove(v);
		    	break; 
		    default:                
		        break;
		   }
		}  
	
	public void onRemove(View v) {
	   	
	   	int sel = spinnerCutOptionsList.getSelectedItemPosition() - 1;
	   	
	   	if (sel != -1)
   		{
	   		SpinnerAdapter adp = spinnerCutOptionsList.getAdapter();
   			String currentName = (String)adp.getItem(sel);
   			
   			if (sel < hardCodeArrayList.size())
   			{
   				AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
   			    builder.setMessage(R.string.OptionSetNotRemove).setTitle(R.string.app_name).setPositiveButton("Ok", null);
   			    AlertDialog dialog = builder.create();
   			    dialog.show();
   			}
   			else
   			{
   				arrCustomOptions.remove(sel + 1 + hardCodeArrayList.size());
   				mapCustomOptions.remove(currentName);
   				setCutOptions();
   			}
   		}
    }
	
	public void onEdit(View v) {
	   	
   		SpinnerAdapter adp = spinnerCutOptionsList.getAdapter();
		int sel = spinnerCutOptionsList.getSelectedItemPosition();
		String strSel = adp.getItem(sel).toString();
		ArrayList<String> curOptions = null;
		
		if (sel - 1 < hardCodeArrayList.size())
		{
			curOptions = mapHardCodeOptions.get(strSel);
		}
		else
		{
			curOptions = mapCustomOptions.get(strSel);
		}
		
		CutOptionsDialog dialog = new CutOptionsDialog(getContext(), strSel, curOptions, true);
    	dialog.SetCutOptionInterface(this);
		dialog.show();
    }
	
	
	public void onAdd(View v) {
	   	
		SpinnerAdapter adp = spinnerCutOptionsList.getAdapter();
		int sel = spinnerCutOptionsList.getSelectedItemPosition();
		String strSel = adp.getItem(sel).toString();
		ArrayList<String> curOptions = null;
		
		if (sel - 1 < hardCodeArrayList.size())
		{
			curOptions = mapHardCodeOptions.get(strSel);
		}
		else
		{
			curOptions = mapCustomOptions.get(strSel);
		}
		
		CutOptionsDialog dialog = new CutOptionsDialog(getContext(), strSel, curOptions, false);
    	dialog.SetCutOptionInterface(this);
		dialog.show();
    }
	
	public void SetOptions(String name, ArrayList<String> options, boolean bEdit)
	{
		if (bEdit == false)
		{
			mapCustomOptions.put(name, options);
			arrCustomOptions.add(name);
		}
		
		setCutOptions();
	}
	
	public boolean CheckName(String name)
	{
		if (!mapHardCodeOptions.containsKey(name) && 
			!mapCustomOptions.containsKey(name))
		{
			return true;
		}
		
		return false;
	}
	
	@Override
	public void onDismiss(DialogInterface dialogInterface)
	{
		
	}
	
	public void onCancel(View v) {
	   	cancel();
    }
	
	public void onOK(View v) {
	   
		SpinnerAdapter adp = spinnerCutOptionsList.getAdapter();
		int sel = spinnerCutOptionsList.getSelectedItemPosition();
		String strSel = getContext().getResources().getString(R.string.None);
		String customOptions = "";
		
		if (sel == 0)
		{
			
		}
		else
		if (sel > 0 && sel - 1 < hardCodeArrayList.size())
		{
			strSel = hardCodeArrayList.get(sel - 1);
		}
		else
		{
			strSel = (String)adp.getItem(sel);
		}
		
		DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
		
		try
		{
			/*
			 * <Root>
			 * <CutOptions name=\"HP-GL Graphtec Cut Fast\" ><option name=\"Speed\" val=\"VS15\" /></CutOptions>
			 * </Root>
			 * 
			 */
			
			DocumentBuilder xmlBuilder = xmlFactory.newDocumentBuilder();
			Document doc = xmlBuilder.newDocument();
			Element root = doc.createElement("Root");
			
			doc.appendChild(root);
			
			for(int i = 0; i < arrCustomOptions.size(); i++)
			{
				String nameSetOption = arrCustomOptions.get(i);
				
				if (mapCustomOptions.containsKey(nameSetOption))
				{
					Element optionSetNode = doc.createElement("CutOptions");
					ArrayList<String> options = mapCustomOptions.get(nameSetOption);
					
					optionSetNode.setAttribute("name", nameSetOption);
					
					root.appendChild(optionSetNode);
					
					for(int n = 0; n < options.size(); n++)
					{
						String valOption = options.get(n);
						String [] strOption = valOption.split("\\|");
						
						if (strOption.length > 1)
						{
							Element optionNode = doc.createElement("option");
							
							optionNode.setAttribute("name", strOption[0]);
							optionNode.setAttribute("val", strOption[1]);
							
							optionSetNode.appendChild(optionNode);
						}
					}
				}
			}
			
			DOMSource domSource = new DOMSource(doc);
			StringWriter writer = new StringWriter();
			StreamResult result = new StreamResult(writer);
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.transform(domSource, result);
			customOptions = writer.toString();
		}
		catch (Exception e) 
   		{
        	Log.v("CutOptionsManagerDialog", "Error" + e);
        } 
		
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		
		SharedPreferences.Editor editor = sharedPrefs.edit();
    	editor.putString("currentCutOptions", strSel);
    	editor.putString("customOptions", customOptions);
		editor.commit();
		
	   	dismiss();
	   	
	   	deligate.SetOptions();
    }
}
