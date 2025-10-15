package com.neobot;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class Neobot extends ListenerAdapter {

    private static final Logger log = LoggerFactory.getLogger(Neobot.class);
    
    //Bns live character lookup page (maybe NEO later?)
    //  http://na-bns.ncsoft.com/ingame/bs/character/search/info?c=

    //Commented out for now until the bot is functional in other servers (if ever)
    // private static final String botInviteURL = "";

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    private static final int customEmbedColor = 3 << 16 | 252 << 8 | 190;

    private static final List<FieldEvent> fieldEvents = new ArrayList<FieldEvent>();

    private static long neobotChannel;

    public static void main(String[] args) throws InterruptedException {
        neobotChannel = getNeobotChannel();

        JDA jda = JDABuilder
            .createDefault(getToken())
            .setActivity(Activity.customStatus( "Use /help to learn about me!"))
            .addEventListeners(new Neobot())
            .enableIntents(GatewayIntent.MESSAGE_CONTENT)
            .build();
        jda.awaitReady();

        //Verify all necessary commands exist:
        List<SlashCommandData> newCommands = new ArrayList<SlashCommandData>();
        newCommands.add(Commands.slash("help", "About the bot"));
        jda.updateCommands().addCommands(newCommands).queue();

        //Load field events from csv file
        loadFieldEvents();
        //Schedeule Silverfrost Field Boss timers
        scheduleFBTimers(jda);
    }

    @Override
    public void onReady(ReadyEvent event) {
        event.getJDA().getTextChannelById(neobotChannel).sendMessage("**NEOBot has been restarted. WARNING: Any previously scheduled reminders may have been lost.**").queue();
        log.info("Bot successfully deployed.");
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        //TODO - Needs to be refactored if people want to use Direct Message for server stuff. I think it's best to be disabled
        if(!ChannelType.TEXT.equals(event.getChannel().getType())) {
            event.reply("Sorry, this bot is only configured to use /server or /global commands in text channels. If enough people request it, I will consider implementing DM or other channel types.").setEphemeral(true).queue();
            return;
        }

        //If guild is detached, the bot is most likely added as an App instead of a Bot User
        if(event.getGuild().isDetached()) {
            log.info("Detached guild detected.");
            event.reply("This bot needs to be added as a user to your server and have permissions in this channel.")
                //  .addActionRow(Button.link(botInviteURL, "Add the bot user"))
                 .setEphemeral(true).queue();
            return;
        }

        // /help
        if(event.getName().equals("help")) {
            EmbedBuilder eb = new EmbedBuilder();
            eb.setAuthor("NEOBot");
            eb.setColor(customEmbedColor);
            eb.setDescription("Hi! Thanks for checking out my bot. Currently, it is only used in a specific channel to send timers for world events (NA).");
            eb.setFooter("@teqsupport");
            event.reply("")
                 .addEmbeds(eb.build())
                 .setEphemeral(true)
                 .queue();
            return;
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String buttonId =  event.getComponentId();
        List<String> buttonInputs = Arrays.asList(buttonId.split("-"));

        if(buttonInputs.size() > 0) {
            //Check for reminder buttons first
            if(buttonInputs.get(0).equals("reminder") && buttonInputs.size() >= 2) {
                try {
                    long unixTimestamp = Long.parseLong(buttonInputs.get(1));
                    event.reply(event.getUser().getAsMention() + " Okay! I will remind you about this event " + discordTimestamp(unixTimestamp) + ".")
                         .setEphemeral(true)
                         .queue();
                    scheduler.schedule(() -> event.getUser().openPrivateChannel()
                                                        .flatMap(channel -> channel.sendMessage("Reminder: There's an event happening soon! " + event.getMessage().getJumpUrl())) 
                                                        .queue(),
                                                        unixTimestamp - Instant.now().getEpochSecond(),
                                                        TimeUnit.SECONDS);
                } catch (NumberFormatException e) {
                    log.error("Invalid reminder button ID: " + buttonId);
                    event.reply("Sorry, something went wrong. Please try again.").setEphemeral(true).queue();
                } catch (Exception e) {
                    log.error("Error scheduling reminder: " + e.getMessage());
                    event.reply("Sorry, something went wrong. Please try again.").setEphemeral(true).queue();
                }
                return;
            }
        }
    }

    @Override
    public void onUserContextInteraction(UserContextInteractionEvent event) {
        log.info("User context interaction detected: " + event.getName());
    }

    /**
     * Turns a long Unix timestamp into a Discord-formatted relative timestamp
     * @param unixTimestamp
     * @return String containing a date timestamp formatted for use in discord message text
     */
    private String discordTimestamp(long unixTimestamp) {
        return "<t:" + unixTimestamp + ":R>";
    }

    /**
     * Tries to get "neobot-token" from the environment config, otherwise looks for a "neobot.properties"
     * @return String token
     */
    private static String getToken() {
        //Try loading from environment variable
        String token = System.getenv("neobot-token");
        if(token != null) return token;

        //Try loading from properties file
        Properties properties = new Properties();
        try {
            properties.load(Neobot.class.getClassLoader().getResourceAsStream("neobot.properties"));
            return properties.getProperty("token");
        } catch (NullPointerException e) {
            log.error("Token not found. Please create a neobot.properties file in the resources folder with the token.");
        } catch (IOException e) {
            log.error("Error reading neobot.properties file.");
        }

        return null;
    }

    /**
     * Loads the neobot channel ID from a properties file if it exists
     */
    private static long getNeobotChannel() {
        //Try loading from environment variable
        String channelString = System.getenv("neobot-channel");
        if(channelString != null) {
            try {
                long channelId = Long.parseLong(channelString);
                log.info("Loaded neobot channel ID from environment variable: " + channelId);
                return channelId;
            } catch (NumberFormatException e) {
                log.error("Invalid neobot-channel ID in environment variable. Using default channel ID.");
            }
        }

        //Load the neobot channel from properties file if it exists
        Properties properties = new Properties();
        try {
            properties.load(Neobot.class.getClassLoader().getResourceAsStream("neobot.properties"));
            String channelIdString = properties.getProperty("channel");
            if(channelIdString != null) {
                long channelId = Long.parseLong(channelIdString);
                log.info("Loaded neobot channel ID from properties file: " + channelId);
                return channelId;
            }
        } catch (NullPointerException e) {
            log.warn("neobot.properties file not found in resources folder. Using default channel ID.");
        } catch (IOException e) {
            log.error("Error reading neobot.properties file.");
        } catch (NumberFormatException e) {
            log.error("Invalid neobot-channel ID in properties file. Using default channel ID.");
        }
        return 0L;
    }

    /**
     * Centers a String s into a padded String of given length, prioritizing Left padding
     * @param s
     * @param length
     * @return
     */
    public String centeredString(String s, int length) {
        if(length <1) return "";
        if(s == null) return "";
        int diff = length - s.length();
        if(diff <1) return s;
        int right = diff/2;
        int left = diff-right;
        String result = "";
        for(int i=0; i<left; i++) {
            result += " ";
        }
        result += s;
        for(int i=0; i<right; i++) {
            result += " ";
        }
        return result;
    }

    private static void loadFieldEvents() {
        //Load field events from csv file
        try (var reader = new java.io.BufferedReader(new java.io.InputStreamReader(Neobot.class.getClassLoader().getResourceAsStream("FieldEvent.csv")))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if(parts.length == 3) {
                    String time = parts[0].trim();
                    String dayString = parts[1].trim().toUpperCase();
                    String location = parts[2].trim();
                    
                    try {
                        java.time.DayOfWeek day = java.time.DayOfWeek.valueOf(dayString);
                        fieldEvents.add(new FieldEvent(time, location, day));
                    } catch (IllegalArgumentException e) {
                        log.warn("Invalid day of week in FieldEvent.csv: " + dayString);
                    }
                } else {
                    log.warn("Invalid line in FieldEvent.csv: " + line);
                }
            }
        } catch (IOException e) {
            log.error("Error reading FieldEvent.csv file.");
        } catch (NullPointerException e) {
            log.error("FieldEvent.csv file not found in resources folder.");
        }
    }

    private static void scheduleFBTimers(JDA jda) {
        //Silverfrost Field Boss Timers
        Runnable fbTimerTask = () -> {
            System.out.println("Checking for scheduled FB spawns...");

            Instant now = Instant.now();
            ZonedDateTime zdt = now.atZone(java.time.ZoneId.of("-06:00"));
            long minutesPrior = 60;
            TextChannel neobotTextChannel = jda.getTextChannelById(neobotChannel);

            fieldEvents.stream()
                .filter(fe -> fe.getDay().getValue() == zdt.getDayOfWeek().getValue())
                .forEach(fe -> {
                    Instant eventTime = fe.getInstant();
                    long secondsUntilEvent = Duration.between(now, eventTime).getSeconds();
                    if( secondsUntilEvent >= minutesPrior*60-60 && secondsUntilEvent <= minutesPrior*60 ) {
                        String messageText = "`Silverfrost Field Boss at " + fe.getLocation() + "`     " + "<t:" + eventTime.getEpochSecond() + ":R>    @ " + fe.getFormattedTime() + " ST";
                        List<Button> fieldBossButtons = new ArrayList<Button>();
                        fieldBossButtons.add(Button.secondary("reminder-" + eventTime.minusSeconds(900).getEpochSecond(), "15 min reminder"));
                        fieldBossButtons.add(Button.secondary("reminder-" + eventTime.minusSeconds(600).getEpochSecond(), "10 min reminder"));
                        fieldBossButtons.add(Button.secondary("reminder-" + eventTime.minusSeconds(300).getEpochSecond(), "5 min reminder"));
                        neobotTextChannel.sendMessage(messageText).addActionRow(fieldBossButtons).queue(
                            msg -> {
                                try {
                                    scheduler.schedule(() -> msg.delete().queue(), 120, TimeUnit.MINUTES);
                                } catch (Exception e) {
                                    log.warn("Field boss spawn message failed to delete. Most likely a server admin deleted it.");
                                }
                            }
                            );
                    }
                });
        };

        //Run every 1 minute, starting at the next full minute
        //long delay = 60 - Instant.now().getEpochSecond() % 60;
        scheduler.scheduleAtFixedRate(fbTimerTask, 0, 1, TimeUnit.MINUTES);
    }
}