AAC app for children with speech disabilities
=============================================

ABOUT
-----

An Android application for children with speech disabilities,  a generic and easy to learn communication method for anyone with speech disabilities that forms sentences from a list of pictograms clicked. It is different from most of existing products by being free, by it's natural language processing features (inflection, tenses, using the setting user gender to alter first person sentences1 and more) and by usage of gesture search on mobile phones where keyboard is not reliable because is small screen.

The application includes ~5000 icons obtained from arasaac.org, where ~900 are properly categorized, and others are accessible through search. It supports French language well, and there is an early prototype for English too.

Other features include:

* recent phrases and pictogram history (each category lists most used ones on top)
accessibility
* for easier identification icons tagged with the 6 SPC colors (actions, names, descriptives etc)
* option to display text in capital letters
* compatibility with both Tablets and mobile phones

Also, my friends have found it to be a fun toy to learn basics of foreign language :)


Below application screen-shots on mobile phone. On tablet everything is bigger and fits more icons.


<img src="https://github.com/vidma/aac-speech-android/raw/master/screenshots/shot_000011.png" width="300px"  title="experimental english support (based on french)" margin="10" />
<img src="https://github.com/vidma/aac-speech-android/raw/master/screenshots/shot_000029.png" width="300px"  title="most used icons displayed on top" margin="10" />
<img src="https://github.com/vidma/aac-speech-android/raw/master/screenshots/shot_000030.png" width="300px"  title="as you can see french is supported fairly well" margin="10" />
<img src="https://github.com/vidma/aac-speech-android/raw/master/screenshots/screenshot-1326571249316.png" width="300px"  title="Gesture search (by drawing letters) within verbs category" margin="10" />


INSTALATION
-----------

<img src="https://github.com/vidma/aac-speech-android/raw/master/qr-code2.png" />


* enable installing apps from not-verified-sources (Settings->Applications; tick 'Unknown sources')
* Download and install the latest apk: https://github.com/vidma/aac-speech-android/raw/master/acc_speech_latest.apk
* Install gesture search http://www.google.com/mobile/gesture-search/  or from android market: https://market.android.com/details?id=com.google.android.apps.gesturesearch
(note the tablets I tested the app with did not support Gesture Search -- you will get some more poor homemade search capabilities)
* Run the app; then go to application's Menu->Preferences and click on 'Install pictograms'.


to use French or English you have to set your phone/tablet language to you wish to use (go to your phone Settings). English support is very experimental. 

you will also need text to speech engine to be installed (go to Settings->Install voice data etc). 

For much higher quality voices you may get the SVOX voices from android market ( https://market.android.com/details?id=com.svox.classic ) for around 3 EUR (include free fully featured two week trial). Note: I'm not affiliated with them, just used and liked their quality.


CURRENT ISSUES AND WORK-AROUNDS
-------------------------------

* personal pronouns like me, te, etc do not work if seleted before the verb. just select the verb first, and the pronoun afterwards and it will be placed in the right position.
* many of the icons are not correctly part-of-speech tagged (this is hard manual work; we use some simple guesses), so have in mind that the application may not allways give expected results


REPORTING PROBLEMS
------------------

To report a bug or other problem, submit a ticket at GitHub: https://github.com/vidma/aac-speech-android/issues/new

LICENSE
-------

This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/ .

CONTENT LICENSE
-------

The application includes 5000+ <a href="http://arasaac.org/condiciones_uso.php">ARASAAC</a> pictograms limited to non-commercial use (Creative Commons) that must be redistributed under the same licence.

* Author of the pictograms: Sergio Palao
* Origin: ARASAAC
* License: CC (BY-NC-SA)



DEVELOPMENT
-----------
just use eclipse and import the the project


FUTURE WORK
-----------
* clean up the icon-set
    * see if better categorizations are possible
    * provide better part-of-speech tags
    * remove duplicate icons so that only best icons are selected for each word (currently any icon is selected)
* extend/improve natural language processing
    * automatic guessing, compound words: avoir besoin etc, add other question and negation types (ne..que, ne..guerre etc, in addition to ne .. pas)
* extend user interface
    * more options for word icons (e.g. overriding number/gender for each icon)
    * better UI for tablets: have filtering options on the side of category view 
* implement English properly
* misc    
    * could we further speed up the loading time?
    * Gesture search do not handle French accents yet (bug in Google's app)
currently we use the ascii version of French word for search

