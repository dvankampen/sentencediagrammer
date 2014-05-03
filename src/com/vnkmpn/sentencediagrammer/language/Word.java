package com.vnkmpn.sentencediagrammer.language;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import com.vnkmpn.sentencediagrammer.R;

public class Word {
	private String text = "";
	private ArrayList<String> types;
	private ArrayList<String> definitions;

	private String key = "INVALID_KEY";
	private Context ctx = null;
	private MWDictionary dictionaryEntry;

	public Word(Context context,String word) {
		this.ctx = context;
		this.text = word;

		try {
			InputStream inputStream = context.getAssets().open("dictionary.properties");
			Properties properties = new Properties();
			properties.load(inputStream);
			this.key = properties.getProperty("KEY");
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.dictionaryEntry = (MWDictionary) new MWDictionary(key).execute(word);

		Integer status = -2;
		try {
			status = dictionaryEntry.get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (status == 0) 
		{
			this.types = dictionaryEntry.getSpeechTypes();
			this.definitions = dictionaryEntry.getDefinitions();
		}
	}

	public String getPlaintext() {

		return text.concat(" ");
	}

	@SuppressLint("DefaultLocale")
	public String getType() {
		if (types != null) // make sure we got something
		{
			if (types.size() > 0) //make sure a type exists in the something
			{
				return types.get(0).toLowerCase();
			}
		}
		return null;
	}

	public ArrayList<String> getAllTypes() {
		ArrayList<String> types = null;
		types = dictionaryEntry.getSpeechTypes();
		return types;
	}

	public ArrayList<String> getDefinition() {
		return definitions;
	}

	public String getHtml() {
		CharSequence color = "";
		String startFontTag = "<font color='";
		String closeFont = "'>";
		String endFontTag = "</font>";
		String type = this.getType();
		if (type != null) {
			if (type.equals("noun")) {
				color = ctx.getResources().getText(R.string.noun);
			} else if (type.equals("verb")) {
				color = ctx.getResources().getText(R.string.verb);
			} else if (type.equals("adjective")) {
				color = ctx.getResources().getText(R.string.adjective);
			} else if (type.contains("article")) {
				color = ctx.getResources().getText(R.string.article);
			} else if (type.equals("adverb")) {
				color = ctx.getResources().getText(R.string.adverb);
			} else if (type.equals("pronoun")) {
				color = ctx.getResources().getText(R.string.pronoun);
			} else if (type.equals("conjunction")) {
				color = ctx.getResources().getText(R.string.conjunction);
			} else if (type.equals("preposition")) {
				color = ctx.getResources().getText(R.string.preposition);
			} else {
				Log.d("Main",  this.getPlaintext() + " is a " + type);
			}
			return startFontTag + color + closeFont + this.getPlaintext() + endFontTag;
		} else {
			return "<b><u>" + this.getPlaintext() + "</u></b>";
		}
	}

}
