#!/bin/bash
cd content

# upzip the current archive
unzip -o aac_speech_data.zip -d aac_speech_data

# update the data files
cp *.data aac_speech_data/
zip -u -b aac_speech_data/ aac_speech_data.zip
