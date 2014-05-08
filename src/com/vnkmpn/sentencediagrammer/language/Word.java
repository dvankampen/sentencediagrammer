package com.vnkmpn.sentencediagrammer.language;

import android.annotation.SuppressLint;
import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class Word {
	private String mText = "";
	private ArrayList<SpeechType> mSpeechTypes;
	private ArrayList<String> mDefinitions;

	private String mKey = "INVALID_KEY";
	private MWDictionary mDictionaryEntry;

	@SuppressLint("DefaultLocale")
	public Word(Context context,String word) {
		this.mText = word;

		try {
			InputStream inputStream = context.getAssets().open("dictionary.properties");
			Properties properties = new Properties();
			properties.load(inputStream);
			this.mKey = properties.getProperty("KEY");
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.mDictionaryEntry = (MWDictionary) new MWDictionary(mKey).execute(word.toLowerCase());

		Integer status = -2;
		try {
			status = mDictionaryEntry.get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (status == 0) 
		{
			this.mSpeechTypes = mDictionaryEntry.getSpeechTypes();
			this.mDefinitions = mDictionaryEntry.getDefinitions();
		}
	}

	public String getPlaintext() {

		return mText.concat(" ");
	}

	@SuppressLint("DefaultLocale")
	public SpeechType getType() {
		if (mSpeechTypes != null) {
			return mSpeechTypes.get(0);
		} else {
			return SpeechType.INVALID;
		}
	}

	public ArrayList<SpeechType> getAllTypes() {
		return mSpeechTypes;
	}

	public ArrayList<String> getDefinition() {
		return mDefinitions;
	}
}
