AAC app for children with speech impairments
=============================================

A generic, easy-to-learn communication tool for anyone with speech disabilities, which forms grammatically correct sentences when a series of pictograms are clicked and then speaks them aloud (text-to-speech). Because of the pictograms, this tool is especially good for children or those who have limited reading and writing abilities.

It is different from most existing products by being free and by its natural language generation with features such as inflection, tenses, using the user's gender (via settings) to alter the first-person sentences and more.

Also, my friends have found using this application to be a fun way to learn basics of new language. :)

The application includes 1000+ icons, categorized and accessible through search. It supports French pretty well, and there is a prototype for English.

Other features include:
* recent pictograms (each category has a tab listing most recently used ones)
* icons tagged with the 6 SPC colors (actions, names, descriptives etc.) for easier identification
* option to display text in capital letters
* option to softly speak each individual word when icon is pressed
* compatibility with both tablets and mobile phones


Below are the screen-shots on a mobile phone. On tablet everything is bigger and fits more icons.


<img src="https://github.com/vidma/aac-speech-android/raw/master/screenshots/en_phrase.png" width="300px"  title="experimental english support (based on french)" margin="10" />
<img src="https://github.com/vidma/aac-speech-android/raw/master/screenshots/category_en.png" width="300px"  title="most used icons displayed on top" margin="10" />
<img src="https://github.com/vidma/aac-speech-android/raw/master/screenshots/fr_new.png" width="300px"  title="as you can see french is supported fairly well" margin="10" />
<img src="https://github.com/vidma/aac-speech-android/raw/master/screenshots/screenshot-1326571249316.png" width="300px"  title="Gesture search (by drawing letters) within verbs category" margin="10" />

DONATE TO HELP THIS IMPROVE
-----------
If you liked the application, you may donate to allow me to add new features/improvements much more quickly: 

<a href="https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=DY6954755BM48"><img src="https://www.paypalobjects.com/en_US/CH/i/btn/btn_donateCC_LG.gif" border="0" alt="Donate safely through PayPal"></a>


INSTALATION
-----------
**Requirements**: Android 2.3+

* Install the application from Google Play: https://market.android.com/details?id=com.epfl.android.aac_speech
* make sure you have a text-to-speech engine properly installed (depending on your device, go to “Settings –> Language & Input-> Text-to-Speech output”,  or to “Settings->Install voice data”).
* To use French or English you have to set your phone/tablet language to the one you wish to use (go to your phone settings)

CURRENT ISSUES
-------------------------------

* many of the icons are not correctly part-of-speech tagged (this is hard manual work; we use some simple guesses), so have in mind that the application may not allways give expected results

RECENTLY FIXED
---------------------
* improved UI (Mar 2014)
 - landscape mode and better tablet support
 - hide offensive words by default
 - fast scrolling (first letter shown)


DEVELOPMENT
-----------
just use eclipse and import the the project


LICENSE
-------

This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/ .

**Content licence**: The application includes 5000+ <a href="http://arasaac.org/condiciones_uso.php">ARASAAC</a> pictograms limited to non-commercial use (Creative Commons) that must be redistributed under the same licence.

Author of the pictograms: Sergio Palao / Origin: ARASAAC / License: CC (BY-NC-SA)

REPORTING PROBLEMS
------------------

To report a bug or other problem, submit a ticket at GitHub: https://github.com/vidma/aac-speech-android/issues/new
