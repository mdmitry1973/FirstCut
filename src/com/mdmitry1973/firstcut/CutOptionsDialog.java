package com.mdmitry1973.firstcut;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

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

public class CutOptionsDialog  extends Dialog implements OnClickListener , DialogInterface.OnDismissListener{
	
	private Button buttonAdd;
	private Button buttonEdit;
	private Button buttonRemove;
	private Spinner spinnerOptions;
	private EditText editTextName;
	private EditText editTextOptionName;
	private EditText editTextOptionVal;
	
	private String name = "";
	private ArrayList<String> options = null;
	private boolean bEdit;
	
	public interface CutOptionDialogInterface {

		public void SetOptions(String name, ArrayList<String> options, boolean bEdit);
		public boolean CheckName(String name);
	}
	
	private CutOptionDialogInterface deligate = null;
	
	public void SetCutOptionInterface(CutOptionDialogInterface deligate)
	{
		this.deligate = deligate;
	}

	public CutOptionsDialog(Context context, String name, ArrayList<String> opts, boolean bEdit) {
		super(context);
		
		setContentView(R.layout.cut_options);
		setTitle(R.string.CutOptionsManager);
		setCanceledOnTouchOutside(false);
		
		this.name = name;
		this.options = opts;
		this.bEdit = bEdit;
		
		buttonAdd = (Button)findViewById(R.id.buttonAdd);
		buttonEdit = (Button)findViewById(R.id.buttonEdit);
		buttonRemove = (Button)findViewById(R.id.buttonRemove);
		spinnerOptions = (Spinner)findViewById(R.id.spinnerOptions);
		editTextName = (EditText)findViewById(R.id.editTextName);
		editTextOptionName = (EditText)findViewById(R.id.editTextOptionName);
		editTextOptionVal = (EditText)findViewById(R.id.editTextOptionVal);
		
		buttonAdd.setOnClickListener(this);
		buttonEdit.setOnClickListener(this);
		buttonRemove.setOnClickListener(this);
		((Button)findViewById(R.id.buttonOK)).setOnClickListener(this);
	    ((Button)findViewById(R.id.buttonCancel)).setOnClickListener(this);
	    
	    setOptionsSpin();
	    
	    spinnerOptions.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				
				String str1 = "";
				String str2 = "";
				
				if (position >= 0 && options != null)
				{
					buttonEdit.setEnabled(true);
					buttonRemove.setEnabled(true);
					
					String strOpt = options.get(position);
					
					String [] str = strOpt.split("\\|");
					
					if (str.length > 1)
					{
						str1 = str[0];
						str2 = str[1];
					}
				}
				
				editTextOptionName.setText(str1);
				editTextOptionVal.setText(str2);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				editTextOptionName.setText("");
				editTextOptionVal.setText("");
				
			}
	    });
	}
	
	public void setOptionsSpin()
	{
		SpinnerAdapter adp = spinnerOptions.getAdapter();
		int sel = spinnerOptions.getSelectedItemPosition();
		ArrayList<String> optionsTemp = new ArrayList<String>();
		
		for(int i = 0; i < options.size(); i++)
		{
			String strOpt = options.get(i);
			String [] str = strOpt.split("\\|");
			
			if (str.length > 1)
			{
				optionsTemp.add(str[0]);
			}
		}
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, optionsTemp);
		spinnerOptions.setAdapter(adapter);
        
        if (sel == -1 || sel > options.size())
        {
        	sel = 0;
        }
        
        spinnerOptions.setSelection(sel);
        
		buttonEdit.setEnabled(options.size() != 0);
		buttonRemove.setEnabled(options.size() != 0);
	}
	
	public void onClick(View v) {       
		  switch (v.getId()) {
		  	case R.id.buttonAdd:
		    	onAdd(v);
		    break; 
		  	case R.id.buttonEdit:
		    	onEdit(v);
		    break; 
		    case R.id.buttonRemove:
		    	onRemove(v);
		    break; 
		    case R.id.buttonOK:
		    	onOK(v);
		    break;  
		    case R.id.buttonCancel:
		    	onCancel(v);
		    break;  
		    default:                
		        break;
		   }
		}  
	
	@Override
	public void onDismiss(DialogInterface dialogInterface)
	{
		
	}
	
	public void onAdd(View v) {
		
		String str1 = editTextOptionName.getText().toString();
		String str2 = editTextOptionVal.getText().toString();
		
		if (!str1.isEmpty() && !str2.isEmpty())
		{
			SpinnerAdapter adp = spinnerOptions.getAdapter();
			boolean found = false;
			
			for(int i = 0; i < adp.getCount(); i++)
			{
				if (adp.getItem(i).toString().compareTo(str1) == 0)
				{
					found = true;
					break;
				}
			}
			
			if (found == false)
			{
				options.add(str1 + "|" + str2);
				
				setOptionsSpin();
				
				spinnerOptions.setSelection(options.size() - 1);
			}
			else
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
			    builder.setMessage(R.string.OptionWrning2).setTitle(R.string.app_name).setPositiveButton("Ok", null);
			    AlertDialog dialog = builder.create();
			    dialog.show();
			}
		}
    }
	
	public void onEdit(View v) {
		int sel = spinnerOptions.getSelectedItemPosition();
		
		if (sel >= 0 && sel < options.size())
		{
			String str1 = editTextOptionName.getText().toString();
			String str2 = editTextOptionVal.getText().toString();
			
			options.set(sel, str1 + "|" + str2);
			
			setOptionsSpin();
		}
    }
	
	public void onRemove(View v) {
		int sel = spinnerOptions.getSelectedItemPosition();
		
		if (sel >= 0 && sel < options.size())
		{
			options.remove(sel);
			
			setOptionsSpin();
		}
    }
	
	public void onCancel(View v) {
	   	cancel();
    }
	
	public void onOK(View v) {
	   
		if (editTextName.getText().toString().isEmpty())
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		    builder.setMessage(R.string.OptionWrning3).setTitle(R.string.app_name).setPositiveButton("Ok", null);
		    AlertDialog dialog = builder.create();
		    dialog.show();
		    
			return;
		}
		
		if (bEdit == true ||
			(deligate.CheckName(editTextName.getText().toString()) && bEdit == false))
		{
			dismiss();
	   	
	   		deligate.SetOptions(editTextName.getText().toString(), options, bEdit);
		}
		else
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		    builder.setMessage(R.string.OptionWrning).setTitle(R.string.app_name).setPositiveButton("Ok", null);
		    AlertDialog dialog = builder.create();
		    dialog.show();
		}
    }
}
