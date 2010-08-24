#!/bin/bash
cd bin
mkdir -p com/glgames/game/ui com/glgames/shared images data
cp ../src/com/glgames/game/*.java com/glgames/game/
cp ../src/com/glgames/game/ui/*.java com/glgames/game/ui/
cp ../src/com/glgames/shared/*.java com/glgames/shared/
cp ../src/images/* images/
cp ../src/data/* data/
javac com/glgames/game/*.java com/glgames/game/ui/*.java com/glgames/shared/*.java
rm com/glgames/game/*.java com/glgames/game/ui/*.java com/glgames/shared/*.java
jar cvfm ../IcePush.jar ../manifest.txt com/glgames/game/* com/glgames/shared/* images/* data/*
cd ..
#jarsigner IcePush.jar "mykey"
#scp IcePush.jar cobol@strictfp.com:/var/www/docs/icepush.strictfp.com/public/play/IcePush.jar
