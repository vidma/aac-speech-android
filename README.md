AAC app for children with speech impairments
=============================================

ABOUT
-----

An Android **application for people with speech disabilities**,  a generic and easy to learn communication method for anyone with speech disabilities that forms grammatically correct sentences from a list of pictograms clicked and reads them (text-to-speech). Because of pictograms it's especially good for children on ones who have limited reading abilities.

It is different from most of existing products by being **free**, by it's grammatically correct **natural language generation** features (inflection, tenses, using the setting user gender to alter first person sentences1 and more) and by usage of gesture search on mobile phones where keyboard is not reliable because is small screen.

Also, my friends have found it to be a **fun toy to learn basics of foreign language** :)

The application includes ~5000 icons obtained from arasaac.org, where ~900 are properly categorized, and others are accessible through search. It supports French language well, and there is an early prototype for English too.

Other features include:

* recent phrases and pictogram history (each category lists most used ones on top)
accessibility
* for easier identification icons tagged with the 6 SPC colors (actions, names, descriptives etc)
* option to display text in capital letters
* compatibility with both Tablets and mobile phones


Below are the screen-shots on mobile phone. On tablet everything is bigger and fits more icons.


<img src="https://github.com/vidma/aac-speech-android/raw/master/screenshots/en_phrase.png" width="300px"  title="experimental english support (based on french)" margin="10" />
<img src="https://github.com/vidma/aac-speech-android/raw/master/screenshots/category_en.png" width="300px"  title="most used icons displayed on top" margin="10" />
<img src="https://github.com/vidma/aac-speech-android/raw/master/screenshots/fr_new.png" width="300px"  title="as you can see french is supported fairly well" margin="10" />
<img src="https://github.com/vidma/aac-speech-android/raw/master/screenshots/screenshot-1326571249316.png" width="300px"  title="Gesture search (by drawing letters) within verbs category" margin="10" />

DONATE TO HELP THIS IMPROVE
-----------
If you liked the application, you are very welcome to donate. That would allow me to add new features and improvements more quickly:

<a href="https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=DY6954755BM48"><img src="https://www.paypalobjects.com/en_US/CH/i/btn/btn_donateCC_LG.gif" border="0" alt="Donate safely through PayPal"></a>



INSTALATION
-----------
**Requirements**: Android 2.3+


* Install the application from Android Market: https://market.android.com/details?id=com.epfl.android.aac_speech 
* Install gesture search http://www.google.com/mobile/gesture-search/  or from android market: https://market.android.com/details?id=com.google.android.apps.gesturesearch
(note the tablets I tested the app with did not support Gesture Search -- you will get some more poor homemade search capabilities)
* Run the application
    * This will ask you to download the datafiles to continue (select Yes). This will download the 5000+ icons and the vocabularies (~20MB), so better do this on a stable WiFi connection and this will take couple of minutes.
    * You may later update the dataset from menu->Settings->Update icons


About Languages and Text-to-Speech:
* to use French or English you have to set your phone/tablet language to the one you wish to use (go to your phone Settings)
* you will also need text to speech engine to be installed (go to Settings->Install voice data etc). 
    * For much higher quality voices you may get the SVOX voices from android market ( https://market.android.com/details?id=com.svox.classic ) for around 3 EUR (include free fully featured two week trial). Note: I'm not affiliated with them, just used and liked their quality.
* English support is still experimental



CURRENT ISSUES
-------------------------------

* many of the icons are not correctly part-of-speech tagged (this is hard manual work; we use some simple guesses), so have in mind that the application may not allways give expected results

ISSUES RECENTLY FIXED
---------------------
* landscape mode and better tablet support (Mar 2014)


DEVELOPMENT
-----------
just use eclipse and import the the project



LICENSE
-------

This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/ .

**Content licence**: The application includes 5000+ <a href="http://arasaac.org/condiciones_uso.php">ARASAAC</a> pictograms limited to non-commercial use (Creative Commons) that must be redistributed under the same licence.

* Author of the pictograms: Sergio Palao
* Origin: ARASAAC
* License: CC (BY-NC-SA)


REPORTING PROBLEMS
------------------

To report a bug or other problem, submit a ticket at GitHub: https://github.com/vidma/aac-speech-android/issues/new


