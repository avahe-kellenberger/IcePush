IcePush
=======

Copy the config file, otherwise some other shitty defaults will be used:
    cp config.copy config

Edit it, etc.

Run the server:
    ant jar-server
    java -jar IcePushServer.jar

Run the client:
    ant jar
    java -jar IcePush.jar

Run the bot (which connects to localhost):
    ant jar-bot
    java -jar IcePushBot.jar faggotbot
