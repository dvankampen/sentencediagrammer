package com.vnkmpn.sentencediagrammer.language;

public enum SpeechType {
	
	INVALID(""),
	NOUN("noun"), 
	VERB("verb"), 
	ADJECTIVE("adjective"),
	ADVERB("adverb"),
	PREPOSITION("preposition"),
	PRONOUN("pronoun"),
	CONTRACTION("contraction"),
	ARTICLE("article"),
	CONJUNCTION("conjunction"),
	ABBREVIATION("abbreviation");
	 
	private String type;
 
	private SpeechType(String s) {
		type = s;
	}
 
	public String getName() {
		return type;
	}

}
