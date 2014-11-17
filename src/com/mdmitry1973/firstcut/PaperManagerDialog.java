package com.mdmitry1973.firstcut;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.AdapterView.OnItemSelectedListener;

public class PaperManagerDialog  extends Dialog implements OnClickListener , DialogInterface.OnDismissListener{
	
	public interface PaperManagerDialogInterface {

		public void ResetPaper();
	}
	
	private String currentListPaper;
	
	private Spinner spinnerPaperList;
	
	private EditText editName;
	private EditText editWidth;
	private EditText editHeight;
	
	private PaperManagerDialogInterface deligate = null;
	
	public void SetPaperManagerDialogInterface(PaperManagerDialogInterface deligate)
	{
		this.deligate = deligate;
	}

	public PaperManagerDialog(Context context) {
		super(context);
		
		setContentView(R.layout.paper_manager);
		setTitle(R.string.PaperManager);
		setCanceledOnTouchOutside(false);
		
		((Button)findViewById(R.id.buttonOK)).setOnClickListener(this);
	    ((Button)findViewById(R.id.buttonCancel)).setOnClickListener(this);
	    ((Button)findViewById(R.id.buttonAdd)).setOnClickListener(this);
	    ((Button)findViewById(R.id.buttonEdit)).setOnClickListener(this);
	    ((Button)findViewById(R.id.buttonDel)).setOnClickListener(this);
	    
	    editName = ((EditText)findViewById(R.id.editTextName));
	    editWidth = ((EditText)findViewById(R.id.editTextWidth));
	    editHeight = ((EditText)findViewById(R.id.editTextHeight));
		
		spinnerPaperList = (Spinner) findViewById(R.id.spinnerPaperList);
		
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		
		currentListPaper = sharedPrefs.getString("Papers", "");
		    
		setPaperList();
		
		spinnerPaperList.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				setPaperProperties();
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				
			}
	    });
	}
	
	public void setPaperProperties()
	{
		SpinnerAdapter adp = spinnerPaperList.getAdapter();
		int sel = spinnerPaperList.getSelectedItemPosition();
		
		String strPapers = currentListPaper;
	   	List<String> arrLines = new ArrayList<String>();
	   	
	   	if (!strPapers.isEmpty())
	   	{
		   	String []arrPaperLines = strPapers.split("\n");
		   	
		   	if (arrPaperLines.length > sel)
		   	{
		   		String line = arrPaperLines[sel];
		   		String []arr = line.split(";");
		   		
		   		editName.setText(arr[0]);
			    editWidth.setText(arr[1]);
			    editHeight.setText(arr[2]);
		   	}
	   	}
	}
	
	public void setPaperList()
	{
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		
		SpinnerAdapter adp = spinnerPaperList.getAdapter();
		String currentName = "";
		int sel = spinnerPaperList.getSelectedItemPosition();
		
		if (adp != null && sel != -1)
		{
			//currentName = (String)adp.getItem(sel);
		}
		else
		{
			sel = sharedPrefs.getInt("currentPaperIndex", 0);
		}
		
	   	String strPapers = currentListPaper;
	   	List<String> arrLines = new ArrayList<String>();
	   	
	   	if (!strPapers.isEmpty())
	   	{
		   	String []arrPaperLines = strPapers.split("\n");
	   		
	   		for (String strLine : arrPaperLines) {
		   	    String []arr = strLine.split(";");
		   	    
		   	    if (arr.length > 2)
		   	    {
			   	    arrLines.add(arr[0]);
		   	    }
		   	}
	   	}
        
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, arrLines);
        spinnerPaperList.setAdapter(adapter);
        
        if (sel == -1 || sel > arrLines.size())
        {
        	sel = 0;
        }
        
        spinnerPaperList.setSelection(sel);
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
		    case R.id.buttonDel:
		    	onRemove(v);
		    	break; 
		    default:                
		        break;
		   }
		}  
	
	public void onRemove(View v) {
	   	
	   	int sel = spinnerPaperList.getSelectedItemPosition();
	   	
	   	if (sel != -1)
   		{
	   		SpinnerAdapter adp = spinnerPaperList.getAdapter();
   			String currentName = (String)adp.getItem(sel);
   			String strPapers = currentListPaper;
   		   	
   		   	if (!strPapers.isEmpty())
		   	{
   		   		ArrayList<String> arrLines = new ArrayList<String>();
			   	String []arrPapersLines = strPapers.split("\n");
			   	int index = 0;
		   		
		   		for (String strLine : arrPapersLines) {
			   	   
			   	    if (sel != index)
			   	    {
			   	    	arrLines.add(strLine);
			   	    }
			   	    
			   	    index++;
			   	}
		   		
		   		strPapers = "";
			   	
			   	for(int i = 0; i < arrLines.size(); i++)
			   	{
			   		strPapers += arrLines.get(i) + "\n";
			   	}
			   	
			   	currentListPaper = strPapers;
		   	}
   			
   		   	setPaperList();
   		   	setPaperProperties();
   		}
    }
	
	public void onEdit(View v) {
	   	
		int sel = spinnerPaperList.getSelectedItemPosition();
		String name = editName.getText().toString();
		String strWidth =  editWidth.getText().toString();
		String strHeight = editHeight.getText().toString();
	   	
	   	if (sel != -1)
   		{
	   		SpinnerAdapter adp = spinnerPaperList.getAdapter();
   			String currentName = (String)adp.getItem(sel);
   			String strPapers = currentListPaper;
   		   	
   		   	if (!strPapers.isEmpty())
		   	{
   		   		ArrayList<String> arrLines = new ArrayList<String>();
			   	String []arrPapersLines = strPapers.split("\n");
			   	int index = 0;
		   		
		   		for (String strLine : arrPapersLines) {
			   	   
			   	    if (sel == index)
			   	    {
			   	    	arrLines.add((name + ";" +  strWidth + ";" + strHeight));
			   	    }
			   	    else
			   	    {
			   	    	arrLines.add(strLine);
			   	    }
			   	    
			   	    index++;
			   	}
		   		
		   		strPapers = "";
			   	
			   	for(int i = 0; i < arrLines.size(); i++)
			   	{
			   		strPapers += arrLines.get(i) + "\n";
			   	}
			   	
			   	currentListPaper = strPapers;
		   	}
   			
   		   	setPaperList();
   		   	setPaperProperties();
   		}
    }
	
	
	public void onAdd(View v) {
	   	
		String strPapers = currentListPaper;
		String name = editName.getText().toString();
		String strWidth =  editWidth.getText().toString();
		String strHeight = editHeight.getText().toString();
		
		int c = spinnerPaperList.getAdapter().getCount();
		
		for(int i = 0; i < c; i++)
		{
			String strItem = spinnerPaperList.getAdapter().getItem(i).toString();
			
			if (strItem.contains(name))
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		    	builder.setMessage(R.string.ChangeName).setTitle(R.string.app_name).setPositiveButton("Ok", null);
		    	AlertDialog dialog = builder.create();
		    	dialog.show();
		    	
		    	return;
			}
		}
		
		String newLine = (name + ";" +  strWidth + ";" + strHeight + "\n");
		
		while(strPapers.endsWith(" "))
		{
			strPapers = strPapers.substring(0, strPapers.length() - 1);
		}
		
		strPapers = strPapers + newLine;
		
		currentListPaper = strPapers;
		
		setPaperList();
		setPaperProperties();
    }
	
	@Override
	public void onDismiss(DialogInterface dialogInterface)
	{
		//setListDevices();
	}
	
	public void onCancel(View v) {
	   	cancel();
    }
	
	public void onOK(View v) {
	   
		SpinnerAdapter adp = spinnerPaperList.getAdapter();
		int sel = spinnerPaperList.getSelectedItemPosition();
		
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		
		SharedPreferences.Editor editor = sharedPrefs.edit();
    	editor.putInt("currentPaperIndex", sel);
    	editor.putString("Papers", currentListPaper);
		editor.commit();
	   	 
	   	dismiss();
	   	
	   	deligate.ResetPaper();
    }
}
