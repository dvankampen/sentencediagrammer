package com.vnkmpn.sentencediagrammer;

import java.util.ArrayList;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.vnkmpn.sentencediagrammer.diagram.SentenceDiagramLayout;
import com.vnkmpn.sentencediagrammer.diagram.SentenceTextView;
import com.vnkmpn.sentencediagrammer.language.Word;

@SuppressLint("DefaultLocale")
public class MainActivity extends Activity implements OnClickListener {

	private SentenceTextView mSentenceView;

	private TextView mExpText;

	private SentenceDiagramLayout mSentenceDiagram;

	private RelativeLayout mMainLayout;

	private ImageButton mButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mButton = (ImageButton) findViewById(R.id.listenButton);
		mButton.setOnClickListener(this);
		Typeface type = Typeface.createFromAsset(getAssets(),"fonts/RobotoCondensed-Regular.ttf"); 

		mExpText = new TextView(this);
		mExpText.setText("please press the green microphone to begin");
		mExpText.setTypeface(type);
		mExpText.setId(1);
		mSentenceView = new SentenceTextView(this);
		mSentenceView.setId(2);
		TextView titleText = (TextView)findViewById(R.id.titleText);
		titleText.setTypeface(type);
		titleText.setAlpha(0f);

		mSentenceDiagram = new SentenceDiagramLayout(this);
		mSentenceDiagram.setId(4);

		mMainLayout = (RelativeLayout)findViewById(R.id.main_layout);

		RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		RelativeLayout.LayoutParams params3 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

		params1.addRule(RelativeLayout.BELOW, mButton.getId());
		params2.addRule(RelativeLayout.BELOW, mSentenceView.getId());
		params3.addRule(RelativeLayout.BELOW, mSentenceDiagram.getId());

		mMainLayout.addView(mSentenceView, params1);
		mMainLayout.addView(mSentenceDiagram, params2);
		mMainLayout.addView(mExpText, params3);

		startAnimation();	
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.d("Main", "building options menu");
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.d("Main", "menu item selected");
		switch (item.getItemId()) {
		case R.id.menu_clear:
			break;
		case R.id.menu_preferences:
			Intent intent = new Intent(MainActivity.this,
					SettingsActivity.class);
			startActivity(intent);
			break;
		case R.id.menu_about:
			LayoutInflater layoutInflater 
			= (LayoutInflater)getBaseContext()
			.getSystemService(LAYOUT_INFLATER_SERVICE);  
			View popupView = layoutInflater.inflate(R.layout.about_popup, null);  
			final PopupWindow popupWindow = new PopupWindow(
					popupView, 
					LayoutParams.WRAP_CONTENT,  
					LayoutParams.WRAP_CONTENT); 

			Button btnDismiss = (Button)popupView.findViewById(R.id.dismiss);
			btnDismiss.setOnClickListener(new Button.OnClickListener(){

				@Override
				public void onClick(View v) {
					popupWindow.dismiss();
				}});

			popupWindow.showAtLocation(this.findViewById(R.id.listenButton), Gravity.CENTER, 0, 0);
			break;
		case R.id.test_sentence_1:
			runTest(1);
			break;
		case R.id.test_sentence_2:
			runTest(2);
			break;
		default:
			break;
		}
		Log.d("Main", "*** done processing menu selection");
		return super.onOptionsItemSelected(item);
	}

	private void runTest(int testNumber) {
		String test;
		ArrayList<String> tests = new ArrayList<String>();

		eraseSentenceAndDiagram();
		switch (testNumber) {
		case 1:
			test = getResources().getString(R.string.test1);
			tests.add(test);
			break;
		case 2:
			test = getResources().getString(R.string.test2);
			tests.add(test);
			break;
		default:
			break;
		}
		validateAndAddToDiagram(null, tests);
	}

	private void showMsg(String msg) {
		Toast toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.CENTER, toast.getXOffset() / 2, toast.getYOffset() / 2);
		toast.show();
	}

	public void onClick(View arg0) {
		notifyRecording(true);
		eraseSentenceAndDiagram();
		SpeechRecognizer sr = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());
		startListening(sr);
	}

	private void startListening(final SpeechRecognizer sr) {

		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

		final boolean performLiveDiagramming = sharedPrefs.getBoolean(SettingsFragment.KEY_PREF_LIVE_DIAG, false);

		Log.i("Speech", "live diagramming is " + (performLiveDiagramming ? "on" : "off"));

		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getApplicationContext().getPackageName());
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);

		intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);

		final Runnable stopListening = new Runnable() {
			@Override
			public void run() {
				sr.stopListening();
			}
		};
		final Handler handler = new Handler();

		sr.setRecognitionListener( new RecognitionListener() {

			@Override
			public void onBeginningOfSpeech() {
				Log.d("Speech", "onBeginningOfSpeech");
			}

			@Override
			public void onEndOfSpeech() {
				Log.d("Speech", "onEndOfSpeech");
				handler.removeCallbacks(stopListening);
			}

			@Override
			public void onError(int error) {
				Log.d("Speech", "onError");

				notifyWarning();
				handler.removeCallbacks(stopListening);

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
			@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
			public void onPartialResults(Bundle partialResults) {
				Log.d("Speech", "onPartialResults");
				if (performLiveDiagramming) {

					float[] confidences = partialResults.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES);
					ArrayList<String> phrases = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

					validateAndAddToDiagram(confidences, phrases);
				}
			}

			@SuppressLint("DefaultLocale")
			@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
			@Override
			public void onResults(Bundle results) {
				Log.d("Speech", "onResults");
				notifyRecording(false);

				float[] confidences = results.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES);
				ArrayList<String> phrases = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

				validateAndAddToDiagram(confidences, phrases);
			}

			@Override
			public void onRmsChanged(float rmsdB) {
			}
			@Override
			public void onBufferReceived(byte[] arg0) {
			}
			@Override
			public void onEvent(int arg0, Bundle arg1) {
			}
			@Override
			public void onReadyForSpeech(Bundle params) {
			}
		});
		sr.startListening(intent);
	}

	@SuppressLint("NewApi")
	public void startAnimation() {
		long firstStageDuration = 1500;
		long secondStageDuration = 500;
		float firstStageDistance = -500f;
		float secondStageDistance = 100f;
		TextView titleText = (TextView) findViewById(R.id.titleText);

		mButton.setEnabled(false);

		ObjectAnimator buttonDropIn = ObjectAnimator.ofFloat(mButton,
				"translationY", firstStageDistance, secondStageDistance);
		buttonDropIn.setDuration(firstStageDuration);
		ObjectAnimator buttonFadeIn = ObjectAnimator.ofFloat(mButton, "alpha",
				0f, 1f);
		buttonFadeIn.setDuration(firstStageDuration);

		ObjectAnimator titleFadeIn = ObjectAnimator.ofFloat(titleText, "alpha",
				0f, 1f);
		titleFadeIn.setDuration(firstStageDuration);

		ObjectAnimator bounceUp = ObjectAnimator.ofFloat(mButton,  "translationY", secondStageDistance, 0f);
		bounceUp.setDuration(secondStageDuration);

		AnimatorSet stageOne = new AnimatorSet();

		stageOne.play(buttonDropIn).with(buttonFadeIn).with(titleFadeIn);

		ObjectAnimator titleFadeOut = ObjectAnimator.ofFloat(titleText, "alpha",  1f, 0f);
		titleFadeOut.setDuration(secondStageDuration);

		ObjectAnimator sentenceFadeIn = ObjectAnimator.ofFloat(mSentenceView, "alpha",  0f, 1f);
		sentenceFadeIn.setDuration(secondStageDuration);

		AnimatorSet stageTwo = new AnimatorSet();

		stageTwo.play(bounceUp).with(titleFadeOut).with(sentenceFadeIn).after(stageOne);
		stageTwo.start();

		mButton.postDelayed(new Runnable() {
			@Override
			public void run() {
				mButton.setBackgroundResource(R.drawable.greenroundcorners);
				mButton.setEnabled(true);
			}
		}, firstStageDuration + secondStageDuration);
	}

	private void notifyRecording(boolean enabled) 
	{
		long duration = 100;
		float distance = -50f;
		float origin;
		float destination;
		final int nextColor;

		mButton.setEnabled(false);

		origin = 0;
		destination = distance;

		if (enabled) {
			nextColor = R.drawable.redroundcorners;
		} else {
			nextColor = R.drawable.greenroundcorners;
		}
		ObjectAnimator buttonMove = ObjectAnimator.ofFloat(mButton,
				"translationY", origin, destination);
		buttonMove.setDuration(duration);

		ObjectAnimator buttonMove2 = ObjectAnimator.ofFloat(mButton,
				"translationY", destination, origin);
		buttonMove2.setDuration(duration);

		AnimatorSet bounce = new AnimatorSet();

		bounce.playSequentially(buttonMove, buttonMove2);
		bounce.start();

		mButton.postDelayed(new Runnable() {
			@Override
			public void run() {
				mButton.setBackgroundResource(nextColor);
				mButton.setEnabled(true);
			}
		}, (2*duration));
	}

	public void notifyWarning() {
		long halfStageDuration = 50;
		long fullStageDuration = 100;
		float leftOffset = -25f;
		float rightOffset = 25f;
		float origin = 0;

		mButton.setEnabled(false);

		mButton.setBackgroundResource(R.drawable.yellowroundcorners);

		ObjectAnimator halfRightMove = ObjectAnimator.ofFloat(mButton,
				"translationX", origin, rightOffset);
		halfRightMove.setDuration(halfStageDuration);

		ObjectAnimator halfLeftMove = ObjectAnimator.ofFloat(mButton,
				"translationX", rightOffset, origin);
		halfLeftMove.setDuration(halfStageDuration);

		ObjectAnimator fullLeftMove = ObjectAnimator.ofFloat(mButton,
				"translationX", rightOffset, leftOffset);
		fullLeftMove.setDuration(fullStageDuration);

		ObjectAnimator fullRightMove = ObjectAnimator.ofFloat(mButton,
				"translationX", leftOffset, rightOffset);
		fullRightMove.setDuration(fullStageDuration);

		ObjectAnimator fullLeftMove2 = fullLeftMove.clone();
		fullLeftMove2.setDuration(fullStageDuration);

		ObjectAnimator halfRightMove2 = ObjectAnimator.ofFloat(mButton,
				"translationX", leftOffset, origin);
		halfRightMove2.setDuration(halfStageDuration);

		AnimatorSet wobble = new AnimatorSet();

		mButton.postDelayed(new Runnable() {
			@Override
			public void run() {
				mButton.setBackgroundResource(R.drawable.greenroundcorners);
				mButton.setEnabled(true);
			}
		}, (4*fullStageDuration));

		wobble.playSequentially(halfRightMove, fullLeftMove, fullRightMove, fullLeftMove2, halfRightMove2);

		wobble.start();
	}

	protected void validateAndAddToDiagram(float[] confidences, ArrayList<String> phrases) {
		int best = 0;
		float highestConfidence = 0;
		if (confidences != null) {
			for (int i = 0; i < confidences.length; i++) {
				if (confidences[i] >= highestConfidence) {
					highestConfidence = confidences[i];
					best = i;
				}
			}
		} else {
			/* its a partial result, so we just have to trust the first thing we get back */
			best = 0;
			highestConfidence = 1;
		}

		/* an arbitrary confidence threshold */
		if (highestConfidence > .6) {
			String phrase = phrases.get(best);
			if (phrase.equals("")) {
				Log.d("Main", "empty result phrase");
				return;
			}
			String[] words = phrase.split(" ");
			int phraseSize = words.length;
			int sentenceSize = mSentenceView.getWordCount();
			if (phraseSize > sentenceSize) {
				/* starting from the end of our current sentence, add whatever new words we have */
				for (int i = sentenceSize; i < phraseSize; i++) {
					Word word = new Word(this, words[i].toLowerCase().trim());
					addWordToSentence(word);
				}
			}

		} else {
			/* will only get here when the final results come in */
			showMsg( "Please say that again...");
			eraseSentenceAndDiagram();
		}
	}

	private void eraseSentenceAndDiagram() {
		/* remove the explanatory text if its ever showing */
		if (mMainLayout.findViewById(mExpText.getId()) != null) {
			mMainLayout.removeView(mExpText);
		}
		/* clear the text view */
		mSentenceView.clear();
		/* clear all words from the diagram area */
		mSentenceDiagram.removeAllViews();
	}

	private void addWordToSentence(Word word) {
		Log.d("Main", "adding " + word.getPlaintext().trim() + " to diagram");
		mSentenceView.addWord(word);
		mSentenceDiagram.addWord(word);
	}
}
