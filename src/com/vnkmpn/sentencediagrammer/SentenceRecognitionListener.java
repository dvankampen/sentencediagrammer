package com.vnkmpn.sentencediagrammer;

import java.util.ArrayList;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.Toast;

public class SentenceRecognitionListener implements RecognitionListener{
	private Context context;
	private String bestSentence = "";
	SentenceRecognitionListener(Context ctx) {
		this.context = ctx;
	}

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
			Toast.makeText(context, strlist.get(bestGuessIndex) , Toast.LENGTH_SHORT).show();
		} else {
			Log.d("Speech", "low confidence");
			Toast toast = Toast.makeText(context, "Please say that again...",  Toast.LENGTH_SHORT);
			toast.show();
		}
	}

	@Override
	public void onRmsChanged(float rmsdB) {
		//Log.d("Speech", "onRmsChanged");
	}
	
	public String getSentence() {
		return bestSentence;
	}

}
