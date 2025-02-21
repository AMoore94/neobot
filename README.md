To run the bot, you need a Discord bot token. If you already have one, create a system environment variable called neobot-token that contains the token.

If you don't already have one, you will need to go to the Discord Developer Portal and create a new application.
Then, go to the OAuth2 tab in the sidebar and look at the OAuth2 URL Generator. Check Bot. Check the bot permissions you want it to have.
I am not sure which all are necessary at this time - you can choose Administrator for blanket rights if you are testing the bot in a private Discord server.
Then, go to the Bot tab in the sidebar and click Reset Token. Put that token in the token.properties file described above.

The .gitignore file should automatically ignore any file named token.properties, but if it does not, DO NOT COMMIT YOUR TOKEN.
If you do commit your token, it is not a big deal. You can simply generate a new one like before - but until you do, your application is vulnerable.
