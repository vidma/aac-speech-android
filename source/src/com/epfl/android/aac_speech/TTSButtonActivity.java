package com.epfl.android.aac_speech;

import java.util.Locale;

import com.actionbarsherlock.app.SherlockActivity;
import com.epfl.android.aac_speech.lib.ArrayUtils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

/**
 * Provides an TTS activity with a speak button.
 * 
 * one needs to implement the onSpeakButtonClicked function
 * 
 * @author vidma
 * 
 */
public abstract class TTSButtonActivity extends SherlockActivity implements OnInitListener {

	// protected static String pref_lang = "FR";

	protected TextToSpeech mTts;

	protected abstract void onSpeakButtonClicked();

	private static final int REQ_TTS_STATUS_CHECK = 0;
	private ImageButton speakBtn = null;

	private static final String TAG = "TTSButtonActivity";

	public static final String LANG_FR = "fr";
	public static final String LANG_EN = "en";

	private static String[] SUPPORTED_LANGUAGES = { LANG_FR, LANG_EN };
	private static String DEFAULT_LANGUAGE = LANG_FR;

	public TTSButtonActivity() {
		super();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Check to be sure that TTS exists and is okay to use
		Intent checkIntent = new Intent();
		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkIntent, REQ_TTS_STATUS_CHECK);
	}

	@Override
	protected void onRestart() {
		// TODO: do I have any cursors?
		super.onRestart();
	}

	protected void speak(String text) {
		mTts.speak(text, TextToSpeech.QUEUE_ADD, null);
	}

	protected void ui_enable_tts() {
		speakBtn = (ImageButton) findViewById(R.id.speak);
		speakBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				onSpeakButtonClicked();
				// TODO: possibility to cancel
			}

		});
	}

	@Override
	public void onInit(int status) {
		// Now that the TTS engine is ready, we enable the button
		if (status == TextToSpeech.SUCCESS) {

			Locale lang = getCurrentLocale();
			int result = mTts.setLanguage(lang);

			if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
				// Lanuage data is missing or the language is not supported.
				Log.e(TAG, "Language is not available.");
			} else {

				speakBtn.setEnabled(true);
			}
		}
	}

	/**
	 * @return
	 */
	private static Locale getCurrentLocale() {
		Locale lang = Locale.FRENCH;

		if (LANG_EN.equals(getPreferedLanguage()))
			lang = Locale.ENGLISH;
		return lang;
	}

	/**
	 * 
	 * @return language code (en, fr) for the currently selected UI language
	 */
	public static String getPreferedLanguage() {
		String lang = java.util.Locale.getDefault().getLanguage();

		if (!ArrayUtils.contains(SUPPORTED_LANGUAGES, lang))
			lang = DEFAULT_LANGUAGE;
		
		return lang;
	}

	@Override
	public void onPause() {
		// TODO: save any changes made to data (the app may be killed)
		super.onPause();
		// if we're losing focus, stop talking
		if (mTts != null)
			mTts.stop();
	}

	@Override
	public void onDestroy() {
		if (mTts != null)
			mTts.shutdown();
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == REQ_TTS_STATUS_CHECK) {
			switch (resultCode) {
			case TextToSpeech.Engine.CHECK_VOICE_DATA_PASS:
				// TTS is up and running
				mTts = new TextToSpeech(this, this);
				Log.v(TAG, "TTS is installed okay");
				break;
			case TextToSpeech.Engine.CHECK_VOICE_DATA_BAD_DATA:
			case TextToSpeech.Engine.CHECK_VOICE_DATA_MISSING_DATA:
			case TextToSpeech.Engine.CHECK_VOICE_DATA_MISSING_VOLUME:
				// missing data, install it
				// TODO: handle multilanguage
				Log.v(TAG, "Need language stuff: " + resultCode);
				Intent installIntent = new Intent();
				installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
				startActivity(installIntent);
				break;
			case TextToSpeech.Engine.CHECK_VOICE_DATA_FAIL:
			default:
				Log.e(TAG, "Got a failure. TTS apparently not available");
			}

		}
	}

}