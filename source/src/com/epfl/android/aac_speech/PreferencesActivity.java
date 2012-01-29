package com.epfl.android.aac_speech;

import com.epfl.android.aac_speech.data.DBHelper;
import com.epfl.android.aac_speech.lib.ZipDownloaderTask;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
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
						// PreferencesActivity.this
						update_pictograms(PreferencesActivity.this);
						return true;
					}
				});
	}

	/**
	 * downloads the datafiles (icons + database data) and updates the database
	 * 
	 * @param context
	 *            - must be SomeActivity.this but not app context
	 */
	public static void update_pictograms(final Context context) {

		Log.i("update_pictograms", "starting...");

		final ProgressDialog progr_dlg = new ProgressDialog(context);
		progr_dlg.setMessage(context.getResources().getString(
				R.string.update_icons_wait_msg));
		progr_dlg.setCancelable(true);
		progr_dlg.setMax(ZipDownloaderTask.PROGRESS_RANGE);
		progr_dlg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progr_dlg.setProgress(0);
		progr_dlg.show();

		class PictogramZipDownloaderTask extends ZipDownloaderTask {
			@Override
			protected void onProgressUpdate(Integer... progress) {
				Log.i("onProgressUpdate", "pr:" + progress[0]);

				int current_progress = progress[0];
				if (progr_dlg.isShowing()) {
					progr_dlg.setProgress(current_progress);

					/* if downloading is done, we're updating DB */
					if (current_progress == ZipDownloaderTask.PROGRESS_DONE) {
						progr_dlg.setMessage(context.getResources().getString(
								R.string.files_downloaded_updating_db));
						progr_dlg.setProgress(100);
						progr_dlg.setIndeterminate(true);

					}
				}
				// TODO Auto-generated method stub
				super.onProgressUpdate(progress);
			}

			@Override
			protected String doInBackground(String... params) {
				String result = super.doInBackground(params);
				Log.i("aac update_pictograms", "Downloading finished");

				if (result != null && result.equals(DONE)) {
					DBHelper dbHelper = new DBHelper(
							context.getContentResolver());
					Log.i("aac update_pictograms", "get CR");

					dbHelper.forceUpdateDatabase(context);
					Log.i("aac update_pictograms", "DB updated");
				}
				return result;
			}

			@Override
			protected void onPostExecute(String result) {
				super.onPostExecute(result);

				progr_dlg.dismiss();

				// TODO: after all is done we have to restart the main activity
				if (context instanceof Activity) {
					Activity a = (Activity) context;

					PendingIntent restart_intent = PendingIntent.getActivity(a
							.getBaseContext(), 0, new Intent(a.getIntent()), a
							.getIntent().getFlags());
					a.finish();
					AlarmManager mgr = (AlarmManager) a
							.getSystemService(Context.ALARM_SERVICE);
					mgr.set(AlarmManager.RTC,
							System.currentTimeMillis() + 2000, restart_intent);
					System.exit(0);
				}
			}
		}
		;
		/* TODO: Make sure all resources are referenced with this path!!! */

		ZipDownloaderTask.OUTPUT_DIR = context.getExternalFilesDir(null);
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