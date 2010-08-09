cd bin
jar cvfm ../IcePush.jar ../manifest.txt com/glgames/game/* com/glgames/shared/* images/* data/*
cd ../
pscp IcePush.jar cobol@local.strictfp.com:/var/www/docs/icepush.strictfp.com/public/play/IcePush.jar