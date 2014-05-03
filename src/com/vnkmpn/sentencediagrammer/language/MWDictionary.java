package com.vnkmpn.sentencediagrammer.language;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import android.os.AsyncTask;
import android.util.Log;

public class MWDictionary extends AsyncTask<String, Void, Integer> {
	
	String key = "INVALID_KEY";
	ArrayList<String> speechTypes;
	ArrayList<String> definitions;
	
	public MWDictionary(String key) {
		this.key = key;
	}

	protected Integer doInBackground(String... args) {
		String leadingURL = "http://www.dictionaryapi.com/api/v1/references/collegiate/xml/";
		String keyTag = "?key=";
		String word = args[0];
		if (key.equals("INVALID_KEY"))
		{
			Log.d("MWDict", "invalid key used, returning");
			return -1;
		}

		HttpGet uri = new HttpGet(leadingURL + word + keyTag + key);

		speechTypes = new ArrayList<String>();
		definitions = new ArrayList<String>();

		DefaultHttpClient client = new DefaultHttpClient();
		HttpResponse resp = null;
		try {
			resp = client.execute(uri);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			Log.d("MWDict", "could not connect to dictionary site...");
			return -1;
		}

		StatusLine status = resp.getStatusLine();
		if (status.getStatusCode() != 200) {
			Log.d("Main", "HTTP error, invalid server status code: " + resp.getStatusLine());  
		}


		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Document doc = null;

		try {
			doc = builder.parse(resp.getEntity().getContent());
		} catch (SAXParseException spe) {
			Log.d("MWDict", "SAX Parse error - perhaps an invalid MW Dictionary API Key?");
			return -1;
		}catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {

		}

		NodeList functionalLabelElements = doc.getElementsByTagName("fl");
		if (functionalLabelElements.getLength() > 0) {

			for (int i = 0; i < functionalLabelElements.getLength(); i++) {
				Node element = functionalLabelElements.item(i);
				if (element.hasChildNodes())
				{
					Node child = functionalLabelElements.item(i).getChildNodes().item(0);
					String text = child.getNodeValue();
					speechTypes.add(text);
				}
			}
		} else {
			Log.d("MWDict", "No speech types found for " + word);
		}
		
		NodeList definingTextElements = doc.getElementsByTagName("dt");
		if (definingTextElements.getLength() > 0) {
			for (int i = 0; i < definingTextElements.getLength(); i++) {
				Node element = definingTextElements.item(i);
				if (element.hasChildNodes())
				{
					Node child = definingTextElements.item(i).getChildNodes().item(0);
					String text = child.getNodeValue();
					definitions.add(text);
				}
			}
		} else {
			Log.d("MWDict", "No definitions found for " + word);
		}
		return 0;
	}

	static String convertStreamToString(java.io.InputStream is) {
		java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}

	protected void onPostExecute(String feed) {
		// TODO: check this.exception 
		// TODO: do something with the feed
	}

	public ArrayList<String> getSpeechTypes() {
		return speechTypes;
	}

	public ArrayList<String> getDefinitions() {
		return definitions;
	}
}
