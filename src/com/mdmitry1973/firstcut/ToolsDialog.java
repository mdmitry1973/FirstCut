package com.mdmitry1973.firstcut;

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

public class ToolsDialog extends Dialog  {
	
	ToolsDialog dialog;
	
	private ToolsDialogInterface deligate = null;
	
	public void SetToolInterface(ToolsDialogInterface deligate)
	{
		this.deligate = deligate;
	}
	
	public ToolsDialog(Context context) {
		super(context);
		
		setContentView(R.layout.tools);
		setCanceledOnTouchOutside(false);
		
		setTitle(R.string.Tools);
		
		dialog = this;
		
		ImageButton imageButtonLine = (ImageButton) findViewById(R.id.imageButtonLine);
		
		imageButtonLine.setOnClickListener(new View.OnClickListener() {
	        @Override
	        public void onClick(View v) {
	        	
	        	deligate.ToolsChanged(MainActivity.ToolType.Line);
	        	dialog.dismiss();
	        }
		});
		
		
		ImageButton imageButtonPen = (ImageButton) findViewById(R.id.imageButtonPen);
		
		imageButtonPen.setOnClickListener(new View.OnClickListener() {
	        @Override
	        public void onClick(View v) {
	        	
	        	deligate.ToolsChanged(MainActivity.ToolType.Pen);
	        	dialog.dismiss();
	        }
		});
		
		ImageButton imageButtonHand = (ImageButton) findViewById(R.id.imageButtonHand);
		
		imageButtonHand.setOnClickListener(new View.OnClickListener() {
	        @Override
	        public void onClick(View v) {
	        	
	        	deligate.ToolsChanged(MainActivity.ToolType.Hand);
	        	dialog.dismiss();
	        }
		});
		
		ImageButton imageButtonResize = (ImageButton) findViewById(R.id.imageButtonResize);
		
		imageButtonResize.setOnClickListener(new View.OnClickListener() {
	        @Override
	        public void onClick(View v) {
	        	
	        	deligate.ToolsChanged(MainActivity.ToolType.Resize);
	        	dialog.dismiss();
	        }
		});
		
		ImageButton imageButtonBox = (ImageButton) findViewById(R.id.imageButtonBox);
		
		imageButtonBox.setOnClickListener(new View.OnClickListener() {
	        @Override
	        public void onClick(View v) {
	        	
	        	deligate.ToolsChanged(MainActivity.ToolType.Box);
	        	dialog.dismiss();
	        }
		});
		
		ImageButton imageButtonCircle = (ImageButton) findViewById(R.id.imageButtonCircle);
		
		imageButtonCircle.setOnClickListener(new View.OnClickListener() {
	        @Override
	        public void onClick(View v) {
	        	
	        	deligate.ToolsChanged(MainActivity.ToolType.Circle);
	        	dialog.dismiss();
	        }
		});
		
		ImageButton imageButtonText = (ImageButton) findViewById(R.id.imageButtonText);
		
		imageButtonText.setOnClickListener(new View.OnClickListener() {
	        @Override
	        public void onClick(View v) {
	        	
	        	deligate.ToolsChanged(MainActivity.ToolType.Text);
	        	dialog.dismiss();
	        }
		});
		
		ImageButton imageButtonStar = (ImageButton) findViewById(R.id.imageButtonStar);
		
		imageButtonStar.setOnClickListener(new View.OnClickListener() {
	        @Override
	        public void onClick(View v) {
	        	
	        	deligate.ToolsChanged(MainActivity.ToolType.Star);
	        	dialog.dismiss();
	        }
		});
		
		ImageButton imageButtonArrow = (ImageButton) findViewById(R.id.imageButtonArrow);
		
		imageButtonArrow.setOnClickListener(new View.OnClickListener() {
	        @Override
	        public void onClick(View v) {
	        	
	        	deligate.ToolsChanged(MainActivity.ToolType.Arrow);
	        	dialog.dismiss();
	        }
		});
	}
}
