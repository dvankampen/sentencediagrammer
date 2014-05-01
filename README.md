# README

## Configuring your Merriam Webster Dictionary Connection
The Sentence diagramming application looks up words and gets their part of speech from a service provided by the[Merriam Webster developer API](http://www.dictionaryapi.com/).  Create an account on that site, and request a developer key.  Once you have an account and a key, you can view it any time using the "My Keys" link once you are signed in.  Copy that key, and paste it into assets/dictionary.properties in this project, formatting it like:

KEY=YOUR-KEY-HERE

This file is on the .gitignore list to prevent developers from checking in their personal development API - this should not be shared.  If/when this project is published to Google Play, a more formalized release-ready API key will need to be retrieved.

## Using the Application
The application's UI is intentionally very stark and minimal.  The only button the user should see is the one that starts the voice recording - this should make the application as intuitive to use as possible -- only one clear path forward.

## Testing
No unit tests are yet written, but that is on the TODO list.  The project is intended to be built using [ANT](http://ant.apache.org/), which will help both in unit testing, as well as building for development vs. building for release.
