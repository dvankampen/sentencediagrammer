package com.vnkmpn.sentencediagrammer.language;

import java.util.LinkedList;

import android.content.Context;

public class Sentence {

	private LinkedList<Word> words;
	private Context ctx;

	public Sentence(Context ctx) {
		this.ctx = ctx;
		words = new LinkedList<Word>();
	}

	public Sentence(Context ctx, String text) {
		this.ctx = ctx;
		words = new LinkedList<Word>();
		this.addWords(text);
	}

	public void addWords(String text) {
		String[] wordArray = text.split(" ");
		for (int i = 0; i < wordArray.length; i++) {
			Word word = new Word(ctx, wordArray[i]);
			words.addLast(word);
		}
	}

	public String removeWordAsHtml() {
		return words.removeFirst().getHtml();
	}

	public String removeWordAsPlaintext() {
		return words.removeFirst().getPlaintext();
	}

	public int getRemainingWordCount() {
		return words.size();
	}

	public void addWord(String text) {
		Word word = new Word(ctx, text);
		this.words.addLast(word);
	}
}
