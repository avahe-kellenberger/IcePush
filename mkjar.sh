#!/bin/bash
keytool -genkey
keytool -selfcert
cd bin
jar cvfm ../IcePush.jar ../manifest.txt com/glgames/game/* com/glgames/shared/*
cd images
jar uvf ../../IcePush.jar *
cd ../../
jarsigner IcePush.jar "mykey"
scp IcePush.jar tekk@192.168.0.110:/var/www/docs/icepush.strictfp.com/public/play/IcePush.jar

