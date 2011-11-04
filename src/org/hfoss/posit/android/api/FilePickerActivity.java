package org.hfoss.posit.android.api;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.hfoss.posit.android.R;
import org.hfoss.posit.android.plugin.acdivoca.AcdiVocaDbHelper;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

// @see http://www.dreamincode.net/forums/topic/190013-creating-simple-file-chooser/
public class FilePickerActivity extends ListActivity {

	public static final String TAG = "FilePicker";
	public static final int ACTION_CHOOSER = 1;
	public static final int RESULT_OK = 1;
	public static final String HOME_DIRECTORY = "/sdcard/acdivoca";
	public static final String HOME_DIRECTORY_MCHN = "/sdcard/acdivoca/mchn";
	public static final String HOME_DIRECTORY_AGRI = "/sdcard/acdivoca/agri";

	private File currentDir;
	private FileArrayAdapter adapter;
	private int mBeneficiaryType; 



	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		currentDir = new File(HOME_DIRECTORY);

		Intent intent = this.getIntent();
		 Bundle extras = intent.getExtras();
			if (extras == null) {
				return;
			}
		mBeneficiaryType= extras.getInt(AcdiVocaDbHelper.FINDS_TYPE);
		if (mBeneficiaryType == AcdiVocaDbHelper.FINDS_TYPE_MCHN) 
			currentDir = new File(HOME_DIRECTORY_MCHN);
		else 
			currentDir = new File(HOME_DIRECTORY_AGRI);
		
		File files[] = currentDir.listFiles();
		List<String> datafiles = new ArrayList<String>();
		try {
			for (File ff: files) {
				if (ff.isFile()) {
					datafiles.add(ff.getName());
				}
			}
		} catch (Exception e) {
			Log.e(TAG, "IO Exception");
			e.printStackTrace();
		}
		
		if (datafiles.size() == 0) 
			setContentView(R.layout.acdivoca_list_files);

        adapter = new FileArrayAdapter(this, R.layout.acdivoca_list_files, datafiles );
        this.setListAdapter(adapter);
	}
	
	
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        String filename = adapter.getItem(position);
	    Intent returnIntent = new Intent();
	    returnIntent.putExtra(Intent.ACTION_CHOOSER, filename);
	    returnIntent.putExtra(AcdiVocaDbHelper.FINDS_TYPE, mBeneficiaryType);

		setResult(RESULT_OK, returnIntent);
		finish();
    }

	class FileArrayAdapter extends ArrayAdapter<String>{

		private Context c;
		private int id;
		private List<String>items;

		public FileArrayAdapter(Context context, int textViewResourceId,
				List<String> filenames) {
			super(context, textViewResourceId, filenames);
			c = context;
			id = textViewResourceId;
			items = filenames;
		}
		public String getItem(int i){
			return items.get(i);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.acdivoca_list_files_row, null);
			}
			final String filename = items.get(position);
			if (filename != null) {
				TextView t1 = (TextView) v.findViewById(R.id.filename);
				t1.setText(filename);
			}
			return v;
		}

	}



}
