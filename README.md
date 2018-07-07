# Usage
## Bootstraping the bot
1. Create a telegram bot as instructed in Telegram's official docs
2. Create a file `radar.yml` in the root of the project. Write the bot's token in it as follows: `telegram-token: <telegram_bot_token>`
3. `docker-compose down; docker-compose build; docker-compose up`
4. After Docker started, in a separate terminal window: `docker exec -ti radar sbt`. This will open an SBT console.
5. In SBT console: `reStart`. This will start the bot.

## Subscribing to notifications
1. Write `/start <key>` to the bot. `<key>` is any string of your choice. It will be used to encrypt your credentials for online services like Google if you ever need to send them to the bot. This command tells the bot to start sending you notifications. No notifications will be sent until you enter this command, and you must enter it each time the bot is restarted.
2. Optional step: if you want to enable Codementor and are able to sign in to it via Google (that is your Codementor account is connected to Google account), you need to let the bot know your credentials to login to Google: `/credentials google <email> <password>`. The credentials will be encrypted via DES algorithm with the key you have specified in step (1), and stored in the database. Whenever the bot needs to access them, it will use your key to decrypt them. The key is not stored anywhere, so you must supply it each time you issue the `/start` command.

# Supported Feeds
- **Facebook Events** - `radar.FacebookEvents`. Pages targeted are configured from the `radar.Main` object's `targets` entry. Every entry of the list is a page name and will map to Facebook URL: `https://www.facebook.com/pg/<page>/events/`. E.g. `HUB.4.0` is mapped to `https://www.facebook.com/pg/HUB.4.0/events/` All the upcoming events will be scraped from that page.
- **Codementor Requests** - `radar.Codementor`. Requests from [this](https://www.codementor.io/m/dashboard/open-requests?expertise=related) page get scraped. 

# Extending and Tweaking the bot
All the supported feeds are implemented as separate Akka Actors. You can write new actors to target other sites using existing actors as examples. Actors are bootstrapped from the `radar.Main` class, search for `actorOf` there.

The delay between scrapings can be configured for every actor individually - see their `preStart` method. The chat bot is also implemented as an actor, and the delay between its notifications can also be configured via its `preStart` method.

# Caveats
Google may challenge you with Captcha when trying to log in to Codementor. You will see it in the log output: Codementor will report that it had logged in successfully, but the page will still be Google's one. If this happens, go [here](https://accounts.google.com/DisplayUnlockCaptcha), press the button and try again.
