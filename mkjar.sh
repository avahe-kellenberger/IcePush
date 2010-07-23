#!/bin/bash
cd bin
jar cvfm ../IcePush.jar ../manifest.txt com/glgames/game/* com/glgames/shared/* images/* data/*
cd ../
#jarsigner IcePush.jar "mykey"
scp IcePush.jar cobol@strictfp.com:/var/www/docs/icepush.strictfp.com/public/play/IcePush.jar