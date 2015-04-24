package com.Rahat.myEditor;

import java.io.File;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class FileListActivity extends ListActivity {

	String[] fileNames;
	File[] allFiles;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_file_list);
		File root = Environment.getExternalStorageDirectory();
        File dir = new File(root.getAbsolutePath() + "/"+MainActivity.storageFolderName);
        if(!dir.exists()){
        	dir.mkdirs();
        }
        
        allFiles=dir.listFiles();
        fileNames=new String[allFiles.length];
        
        for(int i=0;i<allFiles.length;i++){
        	fileNames[i]=allFiles[i].getName();
        }
		// Binding resources Array to ListAdapter
        if(fileNames.length!=0){
        	this.setListAdapter(new ArrayAdapter<String>(this, R.layout.activity_file_list, R.id.nameLabel, fileNames));
        }
		
        ListView listView=getListView();
        listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				
				Intent resultIntent = new Intent();
				resultIntent.putExtra("fileName", fileNames[position]);
				setResult(Activity.RESULT_OK, resultIntent);
				finish();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.file_list, menu);
		return true;
	}
}
