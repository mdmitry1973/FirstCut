package com.mdmitry1973.firstcut;

import java.io.File;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

public class OpenSaveShareDialog extends Dialog implements OpenSaveDialogInterface  {
	
	OpenSaveShareDialog dialog;
	MainActivity activite;
	
	enum OpenSaveShareType {
		Open,
		Save,
		Share
	};
	
	public OpenSaveShareDialog(Context context) {
		super(context);
	}
	
	public OpenSaveShareDialog(Context context, MainActivity activite) {
		super(context);
		
		setContentView(R.layout.open_save_share);
		setCanceledOnTouchOutside(false);
		
		dialog = this;
		this.activite = activite;
		
		ImageButton imageButtonOpen = (ImageButton) findViewById(R.id.imageButtonOpen);
		
		imageButtonOpen.setOnClickListener(new View.OnClickListener() {
	        @Override
	        public void onClick(View v) {
	        	
	        	File externalCacheDir = dialog.getContext().getExternalCacheDir();
	    		File storeDir = new File(externalCacheDir, "Store");
	    		
	    		if (storeDir.exists() == false)
	    		{
	    			storeDir.mkdir();
	    		}
	    		
	    		OpenSaveDialog saveDialog = new OpenSaveDialog(dialog.getContext(), storeDir, OpenSaveDialog.OpenSaveType.Open);
	    		saveDialog.SetOpenSaveInterface(dialog);
	    		saveDialog.show();
	    		
	        	dialog.dismiss();
	        }
		});
		
		
		ImageButton imageButtonSave = (ImageButton) findViewById(R.id.imageButtonSave);
		
		imageButtonSave.setOnClickListener(new View.OnClickListener() {
	        @Override
	        public void onClick(View v) {
	        	
	        	File externalCacheDir = dialog.getContext().getExternalCacheDir();
	    		File storeDir = new File(externalCacheDir, "Store");
	    		
	    		if (storeDir.exists() == false)
	    		{
	    			storeDir.mkdir();
	    		}
	    		
	    		OpenSaveDialog saveDialog = new OpenSaveDialog(dialog.getContext(), storeDir, OpenSaveDialog.OpenSaveType.Save);
	    		saveDialog.SetOpenSaveInterface(dialog);
	    		saveDialog.show();
	        }
		});
		
		ImageButton imageButtonShare = (ImageButton) findViewById(R.id.imageButtonShare);
		
		imageButtonShare.setOnClickListener(new View.OnClickListener() {
	        @Override
	        public void onClick(View v) {
	        	
	        	dialog.activite.ShareData();
	        	dialog.dismiss();
	        }
		});
	}
	
	@Override
	public void OpenSaveFinishDialog(int res, String name)
	{
		if (res == 1)
		{
			File externalCacheDir = dialog.getContext().getExternalCacheDir();
			File storeDir = new File(externalCacheDir, "Store");
			File filepath = new File(storeDir, name);
			
			activite.OpenData(filepath);
		}
		else
		if (res == 2)
		{
			File externalCacheDir = dialog.getContext().getExternalCacheDir();
			File storeDir = new File(externalCacheDir, "Store");
			File filepath = new File(storeDir, name);
			
			activite.SaveData(filepath);
		}
		
		dialog.dismiss();
	}

}
