package com.mdmitry1973.firstcut;

import java.io.File;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

public class OpenSaveDialog extends Dialog  {
	
	private OpenSaveDialog dialog;
	private File dirCurrent;
	private ListView listFilesControl;
	private EditText fileNameControl;
	private OpenSaveType type;
	private ArrayList<String> arrFiles = null;
	
	enum OpenSaveType {
		Open,
		Save
	};
	
	private OpenSaveDialogInterface deligate = null;
	
	public void SetOpenSaveInterface(OpenSaveDialogInterface deligate)
	{
		this.deligate = deligate;
	}
	
	public String getFileName()
	{
		return "" + fileNameControl.getText();
	}
	
	public OpenSaveDialog(Context context, File dirCurrent, OpenSaveType type) {
		super(context);
		
		setContentView(R.layout.open_save_dialog);
		setCanceledOnTouchOutside(false);
		
		dialog = this;
		
		this.dirCurrent = dirCurrent;
		this.type = type;
		
		Button buttonOK = (Button) findViewById(R.id.buttonOK);
		
		if (type == OpenSaveType.Open)
		{
			buttonOK.setText(R.string.Open);
		}
		else
		{
			buttonOK.setText(R.string.Save);
		}
		
		buttonOK.setOnClickListener(new View.OnClickListener() {
	        @Override
	        public void onClick(View v) {
	        	
	        	if (dialog.type == OpenSaveType.Save)
	        	{
		        	for(int i = 0; i < arrFiles.size(); i++)
		        	{
		        		if (arrFiles.get(i).contains(fileNameControl.getText()) == true)
		        		{
		        			AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
		    	        	builder.setMessage(R.string.Warning4).setTitle(R.string.Warning).
		    	        		setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		    	                    @Override
									public void onClick(DialogInterface dialogAlert, int id) {
		    	                    	deligate.OpenSaveFinishDialog((dialog.type == OpenSaveType.Open) ? 1 : 2, "" + fileNameControl.getText());
		    	                    	dialog.dismiss();
		    	                    }
		    	                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		    	                    @Override
									public void onClick(DialogInterface dialog, int id) {
		    	                        
		    	                    }
		    	                });
		    		    	AlertDialog dialogAlert = builder.create();
		    		    	
		    		    	dialogAlert.show();
		    		    	
		    		    	return;
		        		}
		        	}
	        	}
	        	
	        	deligate.OpenSaveFinishDialog((dialog.type == OpenSaveType.Open) ? 1 : 2, "" + fileNameControl.getText());
	        	dialog.dismiss();
	        }
		});
		
		
		Button buttonCancel = (Button) findViewById(R.id.buttonCancel);
		
		buttonCancel.setOnClickListener(new View.OnClickListener() {
	        @Override
	        public void onClick(View v) {
	        	
	        	deligate.OpenSaveFinishDialog(0, "");
	        	dialog.dismiss();
	        }
		});
		
		listFilesControl = (ListView) findViewById(R.id.listViewFiles);
		fileNameControl = (EditText) findViewById(R.id.editTextFileName);
		
		arrFiles = new ArrayList<String>();
		
		String[] arr = dirCurrent.list();
		
		if (arr != null)
		{
			for(int i = 0; i < arr.length; i++)
			{
				arrFiles.add(arr[i]);
			}
		}
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_list_item_1, arrFiles);
   
		listFilesControl.setAdapter(adapter);
		
		if (type == OpenSaveType.Open)
		{
			setTitle(R.string.Open);
			fileNameControl.setEnabled(false);
		}
		else
		{
			setTitle(R.string.Save);
		}
		
		listFilesControl.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                    long id) {
               
            	String str = arrFiles.get(position);
            	
            	fileNameControl.setText(str);
            }
        });
	}

}
