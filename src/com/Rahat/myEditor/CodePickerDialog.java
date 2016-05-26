package com.Rahat.myEditor;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class CodePickerDialog extends DialogFragment {
	
	private EditText nameEditText;
	private TextInputListener inputListener;
	//private String code;
	
	@SuppressLint("InflateParams")
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
		
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View pickerView=inflater.inflate(R.layout.dialog_code_picker, null);
        nameEditText=(EditText) pickerView.findViewById(R.id.codeInputEditText);
        
        /*if(code!=null){
        	//setting fileName name for quick input
	        nameEditText.setText(code);
			
			int cursorIndex=code.lastIndexOf(".");
			if(cursorIndex>=0)
				nameEditText.setSelection(cursorIndex);
        }
        else{
        	nameEditText.setText(".txt");
        }*/
        
        builder.setView(pickerView)
        // Add action buttons
               .setPositiveButton(R.string.codeNamePickerOk, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int id) {
                	   if(inputListener!=null)
                	   inputListener.inputGiven(nameEditText.getText().toString());
                   }
               })
               .setNegativeButton(R.string.codeNamePickerCancel, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       CodePickerDialog.this.getDialog().cancel();
                   }
               });   
              
        return builder.create();
    }
	
	public EditText getFocusingEditText(){
		return nameEditText;
	}
	
	public void setInputListener(TextInputListener listener)
	{
		this.inputListener=listener;
	}
}

