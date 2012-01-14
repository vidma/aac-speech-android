package com.epfl.android.aac_speech;

import com.epfl.android.aac_speech.data.DBHelper;
import com.epfl.android.aac_speech.lib.ZipDownloaderTask;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.util.Log;

public class PreferencesActivity extends PreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		Preference installIconsPref = (Preference) findPreference("installIcons");

		installIconsPref
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference preference) {
						// TODO Auto-generated method stub
						update_pictograms();
						return true;
					}
				});
	}

	void update_pictograms() {
		Log.i("update_pictograms", "starting...");

		Resources res = getResources();

		final ProgressDialog progr_dlg = new ProgressDialog(
				PreferencesActivity.this);
		progr_dlg.setMessage(res.getString(R.string.update_icons_wait_msg));
		progr_dlg.setCancelable(true);
		progr_dlg.setMax(ZipDownloaderTask.PROGRESS_RANGE);
		progr_dlg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progr_dlg.setProgress(0);
		progr_dlg.show();

		class PictogramZipDownloaderTask extends ZipDownloaderTask {
			@Override
			protected void onProgressUpdate(Integer... progress) {
				Log.i("onProgressUpdate", "pr:" + progress[0]);

				if (progr_dlg.isShowing()) {
					progr_dlg.setProgress(progress[0]);
				}
				// TODO Auto-generated method stub
				super.onProgressUpdate(progress);
			}

			@Override
			protected String doInBackground(String... params) {
				String result = super.doInBackground(params);

				if (result != null && result.equals(DONE)) {
					DBHelper dbHelper = new DBHelper(getContentResolver());
					dbHelper.forceUpdateDatabase(getApplicationContext());
				}
				return result;
			}

			@Override
			protected void onPostExecute(String result) {
				progr_dlg.dismiss();
				// TODO Auto-generated method stub
				super.onPostExecute(result);
			}
		}
		;
		/* TODO: Make sure all resources are referenced with this path!!! */
		ZipDownloaderTask.OUTPUT_DIR = getExternalFilesDir(null);
		Log.i("upd picts", "output dir: " + ZipDownloaderTask.OUTPUT_DIR);
		// download URL
		final AsyncTask<String, Integer, String> task = new PictogramZipDownloaderTask()
				.execute(MainActivity.APP_CONTENT_FILE_DOWN_URL);

		/* Allow cancellations */
		progr_dlg.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				task.cancel(true);
			}
		});

	}
}