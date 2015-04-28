package com.Rahat.myEditor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {

	private EditText editText;
	private Button modeTogglerButton;
	
	private boolean isInReadMode;
	
	private InputMethodManager imm;
	private ClipboardManager clipboard;
	
	private String backUpString;
	
	private int selectionStart;
	
	private SharedPreferences sharedPrefs;
	
	public static final String storageFolderName="My Editor Files";
	private FileNamePicker fileNamePickerDialog;
	
	private File currentOpenedFile;
	private FileChooserDialog fileChooserDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Full Screen
		/*if (ViewConfiguration.get(this).hasPermanentMenuKey()) {
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}*/
		setContentView(R.layout.activity_main);
		
		initializeAll();
		
		doNewIntent(getIntent());
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    
	    //settings activity request code was 0
	    if(requestCode==0){
	    	updateFromPrefs();
	    }
	}
	
	@Override
	public void onBackPressed() {
		tryExit();
	}
	
	private void tryExit() {
		new AlertDialog.Builder(this)
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setTitle(R.string.title_exitEditor_exit)
		.setMessage(R.string.text_exit_confirmation)
		.setPositiveButton("Yes",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog,
							int which) {
						finish();
					}

				}).setNegativeButton("No", null).show();
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			this.startActivityForResult(SettingsActivity.start(this),0);
			return true;
		}
		
		if(id==R.id.action_openFile){
			
			if(fileChooserDialog==null){
				
				fileChooserDialog=new FileChooserDialog();
				
				fileChooserDialog.setOnFileChosenListener(
						new FileChooserDialog.OnFileChosenListener() {
					
					@Override
					public void onFileChosen(File file) {
						openFile(file);
					}
				});
			}
			
			fileChooserDialog.show(getFragmentManager(), "fileChooser");
			
			return true;
		}
		
		if(id==R.id.action_saveFile){
			if(currentOpenedFile!=null){
				writeToFile(currentOpenedFile);
			}
			else
				saveFile();
			return true;
		}
		
		if(id==R.id.action_saveAs){
			saveFile();
			return true;
		}
		
		if(id==R.id.action_readMode) {
			if(!isInReadMode){
				hideSoftKeyBoard();
				goToReadMode();
			}
			return true;
		}
		
		if(id==R.id.action_restoreBack)
		{
			restoreBack();
			return true;
		}
		
		if(id==R.id.action_deleteSelection)
		{
			deleteSelection();
			return true;
		}
		
		if(id==R.id.action_copySelection)
		{
			copySelection();
			return true;
		}
		
		if(id==R.id.action_copyAll) {
			copyAllToClipBoard();
			return true;
		}
		
		if(id==R.id.action_paste) {
			//storing backUp
			backUpString=editText.getText().toString();
			pasteFromClipBoard();
			return true;
		}
		
		if(id==R.id.action_clearText){
			//storing backUp
			backUpString=editText.getText().toString();
			clearText();
			return true;
		}
		
		if(id==R.id.action_addCurrentTime){
			addCurrentTime();
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	private void initializeAll() {
		editText=(EditText) findViewById(R.id.editText);
		imm = (InputMethodManager)getSystemService(
			      Context.INPUT_METHOD_SERVICE);
		clipboard= (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

		//load font size
		sharedPrefs=PreferenceManager.getDefaultSharedPreferences(this);
		updateFromPrefs();
		//avoid copy paste pop ups
		editText.setLongClickable(false);

		/*editText.setText("Assalamu Alaikum wa Rahmatullah\n"
				+ "This is a text\n"
				+ "And a new Line\n"
				+ "Another line\n"
				+ "\n\nMy app\n"
				+ "\n\nMy name is Rahat\n"
				+ "\n\n\nboom."
				+ "\n\n\nThis is an android app\n\n\n\n0000");*/
		
		modeTogglerButton=(Button) findViewById(R.id.button);
		
		isInReadMode=false;
		modeTogglerButton.setText(R.string.button_text_read);
		
		modeTogglerButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(isInReadMode)
					goToWtiteMode();			
				else
					goToReadMode();
			}
		});
		
		backUpString="";
		selectionStart=-1;
	}

	private void updateFromPrefs() {
		editText.setTextSize(Float.parseFloat(sharedPrefs.getString
				("pref_font_size", "15")));
		boolean suggestion=sharedPrefs.getBoolean
				("pref_text_suggestion", true);
		
		if(suggestion)
			editText.setInputType(InputType.TYPE_CLASS_TEXT
					| InputType.TYPE_TEXT_FLAG_MULTI_LINE
					);
		else
			editText.setInputType(InputType.TYPE_CLASS_TEXT
					| InputType.TYPE_TEXT_FLAG_MULTI_LINE
					| InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
					);
	}


	private void goToReadMode() {
		modeTogglerButton.setText(R.string.button_text_write);
		isInReadMode=true;
		
		editText.setFocusable(false);
		//editText.setLongClickable(false);

	}

	private void goToWtiteMode() {
		modeTogglerButton.setText(R.string.button_text_read);
		isInReadMode=false;
		
		editText.setFocusableInTouchMode(true);
		//editText.setLongClickable(true);
	}
	

	private void restoreBack() {
		if(backUpString.length()!=0)
			editText.setText(backUpString);
	}

	private void deleteSelection()
	{
		if(editText.getSelectionStart()==editText.getSelectionEnd())
		{
			if(selectionStart==-1)//no previous selection
			{
				//get cursor position
				selectionStart=editText.getSelectionStart();
				//highlight to end
				//editText.setSelection(selectionStart, editText.getText().length());
				
				hideSoftKeyBoard();
				
				Toast.makeText(getApplicationContext(), "Now Select The End Point", 
					      Toast.LENGTH_SHORT).show();
			}
			else
			{
				int selectionEnd=editText.getSelectionEnd();
				
				if(selectionStart>selectionEnd)
				{
					int temp=selectionStart;
					selectionStart=selectionEnd;
					selectionEnd=temp;
				}
				
				deleteSelection(selectionStart,selectionEnd);
			}
		}
		else
		{
			int selectionStart=editText.getSelectionStart();
			int selectionEnd=editText.getSelectionEnd();
			
			deleteSelection(selectionStart, selectionEnd);
		}
	}
	
	private void deleteSelection(int startIndex, int endIndex) {
		
		String text=editText.getText().toString();
		String subStringFirst=text.substring(0, startIndex);
		String subStringLast=text.substring(endIndex,
				text.length());
		
		hideSoftKeyBoard();
		
		backUpString=text;				
		editText.setText(subStringFirst+subStringLast);
		
		//set the cursor to the deleted point
		editText.setSelection(startIndex);
		
		Toast.makeText(getApplicationContext(), "Selected Section Deleted", 
			      Toast.LENGTH_SHORT).show();
		
		//set selectionStart to initial value
		selectionStart=-1;
	}


	private void copySelection()
	{
		if(editText.getSelectionStart()==editText.getSelectionEnd())
		{
			if(selectionStart==-1)//no previous selection
			{
				//get cursor position
				selectionStart=editText.getSelectionStart();
				//highlight to end
				//editText.setSelection(selectionStart, editText.getText().length());
				
				hideSoftKeyBoard();
				
				Toast.makeText(getApplicationContext(), "Now Select The End Point", 
					      Toast.LENGTH_SHORT).show();
			}
			else
			{
				int selectionEnd=editText.getSelectionEnd();
				
				if(selectionStart>selectionEnd)
				{
					int temp=selectionStart;
					selectionStart=selectionEnd;
					selectionEnd=temp;
				}
				
				editText.setSelection(selectionStart, selectionEnd);
				
				ClipData clip = ClipData.newPlainText("text",editText.getText()
						.toString().substring(selectionStart,
								selectionEnd));
				clipboard.setPrimaryClip(clip);
				
				hideSoftKeyBoard();
				
				Toast.makeText(getApplicationContext(), "Text Copied", 
					      Toast.LENGTH_SHORT).show();
				
				//set selectionStart to initial value
				selectionStart=-1;
			}
		}
		else
			Toast.makeText(getApplicationContext(), "Already Copied.", 
			      Toast.LENGTH_SHORT).show();
	}
	
	private void copyAllToClipBoard() {
		ClipData clip = ClipData.newPlainText("text",editText.getText());
		clipboard.setPrimaryClip(clip);
		
		Toast.makeText(getApplicationContext(), "Full Text Copied", 
			      Toast.LENGTH_SHORT).show();
	}
	
	private void pasteFromClipBoard() {
		ClipData clip=clipboard.getPrimaryClip();
		
		if(clip==null)
		{
			Toast.makeText(getApplicationContext(), "No Text Found",
					Toast.LENGTH_SHORT).show();
			
			return;
		}
		
		ClipData.Item item = clip.getItemAt(0);
		String text = item.getText().toString();

		insertString(text);
		
		Toast.makeText(getApplicationContext(), "Text Pasted",
				Toast.LENGTH_SHORT).show();
	}

	private void clearText(){
		editText.setText("");
		Toast.makeText(getApplicationContext(), "Text Cleared",
				Toast.LENGTH_SHORT).show();
	}
	
    private void doNewIntent(Intent intent)
    {

        if (intent != null 
                && (Intent.ACTION_VIEW.equals(intent.getAction()) 
                || Intent.ACTION_EDIT.equals(intent.getAction()))) {
        	
        	if(intent.getScheme().equals("file")) {
        		
        		try {
        			File file = new File(new URI(intent.getData().toString()));
        			InputStream in=new FileInputStream(file);
        			
        			readFile(in);
        			currentOpenedFile=file;
    				
        		}catch (IOException ie){
        			Toast.makeText(this,"Can't read file " + ie.getMessage(),
        					Toast.LENGTH_LONG).show();       			
        		} catch (URISyntaxException e) {
        			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        			
        		} catch (IllegalArgumentException e) {
        			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        			
        		}
        	}
        	
        	else if (intent.getScheme().equals("content"))
            {
                try {
                    InputStream attachment = getContentResolver().openInputStream(intent.getData());
                    readFile(attachment);
                
                } catch (Exception e) {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
            
        }
    }
	
	private void readFile(InputStream in) throws IOException {
		BufferedReader br = new BufferedReader( new InputStreamReader(in) , 8192*2 );
        StringBuilder sb = new StringBuilder();
        String text;
        while((text=br.readLine()) != null)
        {
            sb.append(text).append("\n");
        }
        in.close();
        br.close();
       
        editText.setText(sb.toString());
        goToReadMode();
        
        //sb.setLength(0);
	}
	
	private void hideSoftKeyBoard()
	{
		imm.hideSoftInputFromWindow(editText.getWindowToken(),0);
	}
	
	private void saveFile()
	{
		/* Checks if external storage is available for read and write */
		String state = Environment.getExternalStorageState();
		if (!Environment.MEDIA_MOUNTED.equals(state)) {
			Toast.makeText(this, "Media is Mounted\nFailed to Write",Toast.LENGTH_LONG).show();
	        return;
	    }
		
		showFileNamePickerDialog();
		fileChooserDialog=null;//to initiate filechooser dialog again with new file

	}
	
	private void showFileNamePickerDialog()
	{
		if(fileNamePickerDialog==null)
		{
			//initialize dialogFragment
			fileNamePickerDialog=new FileNamePicker();
			fileNamePickerDialog.setInputListener(new TextInputListener() {
				
				@Override
				public void inputGiven(String text) {
					tryWriteToFile(text);
				}
			});
		}
		
		if(currentOpenedFile!=null)
			fileNamePickerDialog.setFileName(currentOpenedFile.getName());
		
		//hiding keyBoard
		imm.hideSoftInputFromWindow(editText.getWindowToken(),0);
		
		fileNamePickerDialog.show(getFragmentManager(),"picker");
		//showAgain
		imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.SHOW_IMPLICIT);
	}
	
	private void tryWriteToFile(String fileName)
	{
		// Find the root of the external storage.
        // See http://developer.android.com/guide/topics/data/data-
        // storage.html#filesExternal
        File root = Environment.getExternalStorageDirectory();
        // See
        // http://stackoverflow.com/questions/3551821/android-write-to-sd-card-folder
        File dir = new File(root.getAbsolutePath() + "/"+storageFolderName);
        if(!dir.exists())
        	dir.mkdirs();
        final File file = new File(dir, fileName);
        
        //checking replace action
        if(file.exists())
        {

    		new AlertDialog.Builder(this)
    		.setIcon(android.R.drawable.ic_dialog_alert)
    		.setTitle(R.string.title_replaceDialog)
    		.setMessage("Do you want to replace "+fileName+" ?")
    		.setPositiveButton("Yes",
    				new DialogInterface.OnClickListener() {
    					@Override
    					public void onClick(DialogInterface dialog,
    							int which) {
    						writeToFile(file);
    					}

    				}).setNegativeButton("No", null).show();
        }
        else
        {
        	writeToFile(file);
        }
	}
	
	private void writeToFile(File file)
	{
        try {
            FileOutputStream f = new FileOutputStream(file);
            PrintWriter pw = new PrintWriter(f);
            pw.write(editText.getText().toString());
            pw.flush();
            pw.close();
            f.close();
            
            Toast.makeText(this,"Saved Successfully :\n"+file.getName(), Toast.LENGTH_LONG).show();
            //set this as currenet file
            currentOpenedFile=file;
            
        } catch (IOException e) {
        	Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
	}
	
	private void addCurrentTime()
	{
		Date date=new Date();
		String dateText=DateFormat.format("dd/MMM/yyyy (E) hh:mm:ss a", date).toString();		
		
		insertString(dateText);
	}
	
	private void insertString(String text)
	{
		int cursorPos=editText.getSelectionStart();
		String mainText=editText.getText().toString();
		String subStringFirst=mainText.substring(0, cursorPos);
		String subStringLast=mainText.substring(cursorPos,
				mainText.length());
		
		editText.setText(subStringFirst+text+subStringLast);
		//move cursor to the last of the inserted text
		editText.setSelection(cursorPos+text.length());
	}
	
	private void openFile(File file)
	{
		
        try {
			InputStream in=new FileInputStream(file);
			
			readFile(in);
			currentOpenedFile=file;
			
		}catch (IOException ie){
			Toast.makeText(this,"Can't read file " + ie.getMessage(),
					Toast.LENGTH_LONG).show(); 			
		}
	}
}
