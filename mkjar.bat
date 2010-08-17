cd bin
jar cvfm ../IcePush.jar ../manifest.txt com/glgames/game/* com/glgames/shared/* images/* data/*
cd ../
pscp IcePush.jar root@strictfp.com:/var/www/icepush/IcePush.jar