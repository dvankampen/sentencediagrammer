package com.vnkmpn.sentencediagrammer;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {

	private static final int CLEAR_MENU_ITEM = Menu.FIRST;
	private static final int SAVE_MENU_ITEM = CLEAR_MENU_ITEM + 1;

	private SpeechRecognizer sr;
	
	private String key = "INVALID_KEY";

	private SentenceRecognitionListener listener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Typeface type = Typeface.createFromAsset(getAssets(),"fonts/RobotoCondensed-Regular.ttf"); 
		((TextView)findViewById(R.id.titleText)).setTypeface(type);

		((TextView)findViewById(R.id.sentenceText)).setTypeface(type);

		findViewById(R.id.listenButton).setOnClickListener(this);

		sr = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());

		listener = new SentenceRecognitionListener();

		sr.setRecognitionListener(listener);

		try {
			InputStream inputStream = getAssets().open("dictionary.properties");
			Properties properties = new Properties();
			properties.load(inputStream);
			Log.d("Main","The properties are now loaded");
			key = properties.getProperty("KEY");
		} catch (IOException e) {
			e.printStackTrace();
		}
		startAnimation();
	}

	private ArrayList<String> lookUpWord(String word) {
		MWDictionaryLookup dict = (MWDictionaryLookup) new MWDictionaryLookup().execute(word, key);
		ArrayList<String> types = null;
		try {
			types = dict.get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return types;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, CLEAR_MENU_ITEM, 0, "Clear");
		menu.add(0, SAVE_MENU_ITEM, 0, "Save");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case CLEAR_MENU_ITEM:
			((TextView)findViewById(R.id.sentenceText)).setText("");
			break;
		case SAVE_MENU_ITEM:
			showMsg("Save");
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void showMsg(String msg) {
		Toast toast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER, toast.getXOffset() / 2, toast.getYOffset() / 2);
		toast.show();
	}

	public void onClick(View arg0) {
		recording(true);
		sr.startListening(RecognizerIntent.getVoiceDetailsIntent(getApplicationContext()));
	}


	public void setSentenceText(String text) {
		((TextView)findViewById(R.id.sentenceText)).append(text);
	}

	@SuppressLint("NewApi")
	public void startAnimation() {
		long firstStageDuration = 1500;
		long secondStageDuration = 500;
		float firstStageDistance = -500f;
		float secondStageDistance = 100f;
		ImageButton button = (ImageButton) findViewById(R.id.listenButton);
		TextView titleText = (TextView) findViewById(R.id.titleText);

		ObjectAnimator buttonDropIn = ObjectAnimator.ofFloat(button,
				"translationY", firstStageDistance, secondStageDistance);
		buttonDropIn.setDuration(firstStageDuration);
		ObjectAnimator buttonFadeIn = ObjectAnimator.ofFloat(button, "alpha",
				0f, 1f);
		buttonFadeIn.setDuration(firstStageDuration);

		ObjectAnimator titleFadeIn = ObjectAnimator.ofFloat(titleText, "alpha",
				0f, 1f);
		titleFadeIn.setDuration(firstStageDuration);

		ObjectAnimator bounceUp = ObjectAnimator.ofFloat(button,  "translationY", secondStageDistance, 0f);
		bounceUp.setDuration(secondStageDuration);

		AnimatorSet stageOne = new AnimatorSet();

		stageOne.play(buttonDropIn).with(buttonFadeIn).with(titleFadeIn);


		ObjectAnimator titleFadeOut = ObjectAnimator.ofFloat(titleText, "alpha",  1f, 0f);
		titleFadeOut.setDuration(secondStageDuration);

		AnimatorSet stageTwo = new AnimatorSet();

		stageTwo.play(bounceUp).with(titleFadeOut).after(stageOne);
		stageTwo.start();

	}

	/**
	 * 
	 * @author dave
	 *
	 */

	class SentenceRecognitionListener implements RecognitionListener{

		private String bestSentence = "";

		@Override
		public void onBeginningOfSpeech() {
			Log.d("Speech", "onBeginningOfSpeech");
			bestSentence = "";
		}

		@Override
		public void onBufferReceived(byte[] buffer) {
			Log.d("Speech", "onBufferReceived");
		}

		@Override
		public void onEndOfSpeech() {
			Log.d("Speech", "onEndOfSpeech");
		}

		@Override
		public void onError(int error) {
			Log.d("Speech", "onError");
			switch (error)
			{
			case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
				Log.d("Speech", "insufficient permissions");
				break;
			case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
				Log.d("Speech", "recognizer busy");
				break;
			case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
				Log.d("Speech", "timeout");
				break;
			default:
				Log.d("Speech", "error: " + error);
				break;
			}
		}

		@Override
		public void onEvent(int eventType, Bundle params) {
			Log.d("Speech", "onEvent");
		}

		@Override
		public void onPartialResults(Bundle partialResults) {
			Log.d("Speech", "onPartialResults");
		}

		@Override
		public void onReadyForSpeech(Bundle params) {
			Log.d("Speech", "onReadyForSpeech");
		}


		@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
		@Override
		public void onResults(Bundle results) {
			Log.d("Speech", "onResults");

			recording(false);

			float[] confidenceList = results.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES);
			int bestGuessIndex = 0;
			float highestConfidence = 0;
			for (int i = 0; i < confidenceList.length; i++) {
				if (confidenceList[i] >= highestConfidence) {
					highestConfidence = confidenceList[i];
					bestGuessIndex = i;
				}
			}
			ArrayList<String> strlist = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

			if (highestConfidence > .6) {
				Log.d("Speech", "resultant string=" + strlist.get(bestGuessIndex) + ", with confidence of " + highestConfidence);
				bestSentence = strlist.get(bestGuessIndex);
				((TextView)findViewById(R.id.sentenceText)).append(bestSentence);
			} else {
				Log.d("Speech", "low confidence");
				showMsg( "Please say that again...");
			}
			String input = ((TextView)findViewById(R.id.sentenceText)).getText().toString();
			String[] words = input.split( " " );

			for (int i = 0; i < words.length; i++) {
				ArrayList<String> types = lookUpWord(words[i]);
				if (types != null) {
				for (int j = 0; j < types.size(); j++) {
					showMsg( words[i] + " is a " + types.get(j));
				}
				} else {
					showMsg("word lookup failed - check your dictionary API Key");
				}
			}
		}

		@Override
		public void onRmsChanged(float rmsdB) {
		}
	}

	private void recording(boolean enabled) 
	{
		long firstStageDuration = 500;
		float distance = -300f;
		float origin;
		float destination;

		ImageButton button = (ImageButton) findViewById(R.id.listenButton);

		if (enabled) {
			origin = 0;
			destination = distance;
			button.setBackgroundResource(R.drawable.redroundcorners);
		} else {
			origin = distance;
			destination = 0;
			button.setBackgroundResource(R.drawable.greenroundcorners);
		}
		ObjectAnimator buttonMove = ObjectAnimator.ofFloat(button,
				"translationY", origin, destination);
		buttonMove.setDuration(firstStageDuration);
		buttonMove.start();
	}
}
