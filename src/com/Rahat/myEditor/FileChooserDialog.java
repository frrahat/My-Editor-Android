package com.Rahat.myEditor;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * @author Rahat
 * date : 19-04-15
 * dialog for browsing folders and selecting file
 */

public class FileChooserDialog extends DialogFragment {

	TextView textView;
	ListView fileNameListView;
	OnFileChosenListener onFileChosenListener;
	private BaseAdapter adapter;
	private ArrayList<File> displayFiles;
	private File parentDir;
	private File root;

	@SuppressLint("InflateParams")
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		final Dialog dialog = new Dialog(getActivity());

		dialog.setContentView(R.layout.dialog_file_chooser);
		dialog.setTitle(R.string.title_dialog_file_chooser);
		dialog.setCancelable(true);

		Button buttonBack = (Button) dialog.findViewById(R.id.button_back);
		buttonBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (parentDir.getParentFile()!=null) {
					parentDir = parentDir.getParentFile();
					updateDisplayFiles();
				}
			}
		});

		Button buttonCancel = (Button) dialog.findViewById(R.id.button_cancel);
		buttonCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});

		final LayoutInflater layoutInflater = getActivity().getLayoutInflater();

		textView = (TextView) dialog.findViewById(R.id.textView_fileChooser);
		fileNameListView = (ListView) dialog
				.findViewById(R.id.listView_file_names);

		// adapter for listview
		adapter = new BaseAdapter() {

			@SuppressLint("InflateParams")
			@Override
			public View getView(int position, View view, ViewGroup parent) {
				if (view == null) {
					view = layoutInflater.inflate(
							R.layout.file_chooser_list_item, null);
				}
				TextView textView = (TextView) view
						.findViewById(R.id.textView_fileName);

				textView.setText(displayFiles.get(position).getName());
				
				ImageView img = (ImageView) view
						.findViewById(R.id.imageView_fileBrowse);
				
				if (displayFiles.get(position).isDirectory())
					img.setImageResource(R.drawable.folder);
				else
					img.setImageResource(R.drawable.file);

				return view;
			}

			@Override
			public long getItemId(int position) {
				return position;
			}

			@Override
			public Object getItem(int position) {
				return position;
			}

			@Override
			public int getCount() {
				return displayFiles.size();
			}
		};

		// has SD card
		if (Environment.getExternalStorageState() != null) {
			root = new File(Environment.getExternalStorageDirectory(),
					MainActivity.storageFolderName);
			if(!root.exists()){
				root.mkdirs();
			}
		} else {
			root = Environment.getDataDirectory();
		}

		displayFiles = new ArrayList();
		parentDir = root;

		updateDisplayFiles();

		fileNameListView.setAdapter(adapter);
		fileNameListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				// TODO Auto-generated method stub
				if (displayFiles.get(position).isDirectory()) {
					parentDir = displayFiles.get(position);
					updateDisplayFiles();
				} else {
					onFileChosenListener.onFileChosen(displayFiles
							.get(position));
					
					dialog.dismiss();
				}
			}
		});

		return dialog;

	}

	public static interface OnFileChosenListener {
		public void onFileChosen(File file);
	}

	public void setOnFileChosenListener(OnFileChosenListener listener) {
		this.onFileChosenListener = listener;
	}

	private void updateDisplayFiles() {

		textView.setText(parentDir.getAbsolutePath());

		File allFiles[] = parentDir.listFiles();

		displayFiles.clear();

		if (allFiles != null) {
			for (int i = 0; i < allFiles.length; i++) {
				displayFiles.add(allFiles[i]);
			}
			
			Collections.sort(displayFiles,new Comparator<File>() {

				@Override
				public int compare(File lhs, File rhs) {
					return lhs.getName().toLowerCase()
							.compareTo(rhs.getName().toLowerCase());
				}
			});
		} /*else {
			Toast.makeText(getActivity(), "No Appropriate file found",
					Toast.LENGTH_SHORT).show();
		}*/

		adapter.notifyDataSetChanged();
	}
}
