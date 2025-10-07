package com.neobot;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
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

    private static final long discordUserInputDelaySeconds = 5;
    private static final long timeAfterSpawnMsgDeletionMinutes = 5;

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private static final List<WorldEvent> worldEvents = new ArrayList<WorldEvent>();

    private static final int customEmbedColor = 3 << 16 | 252 << 8 | 190;

    private static final List<FieldEvent> fieldEvents = new ArrayList<FieldEvent>();

    private HashMap<TextChannel, Boolean> heavensReachChannels = new HashMap<TextChannel, Boolean>();
    private HashMap<TextChannel, Boolean> viridianCoastChannels = new HashMap<TextChannel, Boolean>();

    //Test channel
    //private static final long neobotChannel = 1342573327247741039L;
    //Prod channel
    private static final long neobotChannel = 1420223400869363834L;

    public static void main(String[] args) throws InterruptedException {
        JDA jda = JDABuilder
            .createDefault(getToken())
            .setActivity(Activity.customStatus( "Use /help to learn about me!"))
            .addEventListeners(new Neobot())
            .enableIntents(GatewayIntent.MESSAGE_CONTENT)
            .build();
        jda.awaitReady();

        //Verify all necessary commands exist:
        List<SlashCommandData> newCommands = new ArrayList<SlashCommandData>();
        for(WorldBoss wb : WorldBoss.values()) {
            newCommands.add(Commands.slash(wb.getCommandName(), wb.getDisplayName() + " World Boss options"));
        }
        newCommands.add(Commands.slash("help", "About the bot"));
        newCommands.add(Commands.slash("wb", "Show all of the world boss slash commands"));
        jda.updateCommands().addCommands(newCommands).queue();

        //Add all the world events to a static list for use here
        worldEvents.addAll(Arrays.asList(WorldEvent.values()));

        //Load field events from csv file
        loadFieldEvents();
        //Schedeule Silverfrost Field Boss timers
        scheduleFBTimers(jda);
    }

    @Override
    public void onReady(ReadyEvent event) {
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

        // /wb
        if(event.getName().equals("wb")) {
            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle("World Boss Commands:");
            eb.setDescription(createWBDescription());
            eb.setColor(customEmbedColor);
            event.reply("")
                 .addEmbeds(eb.build())
                 .setEphemeral(true)
                 .queue();
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

        //All other world boss slash commands handled here
        WorldBoss wb = WorldBoss.valueOf(event.getName());
        List<WorldEvent> events = wb.getEvents();
        event.reply("")
             .addActionRow(createButtons(events))
             .setEphemeral(true)
             .queue();
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
                    event.reply(event.getUser().getAsMention() + " Okay! I will remind you " + discordTimestamp(unixTimestamp) + ".")
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
        } else if(buttonInputs.size() > 1) {
            String buttonPrefix = buttonInputs.get(0) + "-";
            String channel = buttonInputs.get(1);
            WorldEvent we = worldEvents.stream().filter(e -> e.getButtonPrefix().equals(buttonPrefix)).findFirst().orElse(null);
            if(we != null) {
                sendEventTimestampMessage(event, we.getDisplayName(), channel, we.getSpawnDelayMinutes());
            }
        } else {
            WorldEvent we = worldEvents.stream().filter(e -> e.getButtonId().equals(buttonId)).findFirst().orElse(null);
            if(we != null) {
                sendChannelPromptMessage(event, we.getButtonReplyText(), we.getButtonPrefix());
            }
        }
    }

    //Helper methods
    /**
     * Creates Buttons for use in an ActionRow
     * @param buttonIDPrefix
     * @param startIndex
     * @return List of 5 Button that are labeled starting with startIndex given
     */
    private List<Button> createActionRowOfButtons(String buttonIDPrefix, int startIndex) {
        List<Button> actionRowList = new ArrayList<Button>();
        for(int i = startIndex; i < startIndex+5; i++) {
            actionRowList.add(Button.secondary(buttonIDPrefix + i, String.valueOf(i)));
        }
        return actionRowList;
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
     * Tries to get "neobot-token" from the environment config, otherwise looks for a "token.properties"
     * @return String token
     */
    private static String getToken() {
        //Try loading from environment variable
        String token = System.getenv("neobot-token");
        if(token != null) return token;

        //Try loading from properties file
        Properties properties = new Properties();
        try {
            properties.load(Neobot.class.getClassLoader().getResourceAsStream("token.properties"));
            return properties.getProperty("token");
        } catch (NullPointerException e) {
            log.error("Token not found. Please create a token.properties file in the resources folder with the token.");
        } catch (IOException e) {
            log.error("Error reading token.properties file.");
        }

        return null;
    }

    /**
     * Sends a timestamped message in the event's text channel formatted nicely with the boss name and respawn timer
     * @param event
     * @param bossName
     * @param channel
     * @param delayMinutes
     */
    private void sendEventTimestampMessage(ButtonInteractionEvent event, String bossName, String channel, long delayMinutes) {
        Instant now = Instant.now();
        event.getMessage().delete().queue();
        event.deferEdit().queue();
        Instant spawnTime = now.plus(Duration.ofMinutes(delayMinutes)).minus(Duration.ofSeconds(discordUserInputDelaySeconds));
        long unixTimestamp = spawnTime.getEpochSecond();
        String quote = bossName.startsWith("Mutated") ? "> " : "";
        channel = "Channel " + channel;
        String messageText = quote + "`" + centeredString(bossName, 30-quote.length()) + "|" + centeredString(channel, 15) + "|`     " + discordTimestamp(unixTimestamp);
        TextChannel textChannel = event.getChannel().asTextChannel();

        //Send the default message
        sendTimestampMessage(textChannel, messageText, delayMinutes);

        //Send global countdowns:
        //If channel is a Heaven's Reach channel
        if(heavensReachChannels.containsKey(textChannel)) {
            heavensReachChannels.forEach((key, value) -> {
                if(value && !key.equals(textChannel)) {
                    sendTimestampMessage(key, messageText, delayMinutes);
                }
            });
        }
        //If channel is a Viridian Coast channel
        if(viridianCoastChannels.containsKey(textChannel)) {
            viridianCoastChannels.forEach((key, value) -> {
                if(value && !key.equals(textChannel)) {
                    sendTimestampMessage(key, messageText, delayMinutes);
                }
            });
        }
    }

    /**
     * Sends a text message to a channel that deletes itself after:
     *      delayMinutes + timeAfterSpawnMsgDeletionMinutes
     * @param channel
     * @param messageText
     * @param delayMinutes
     */
    private void sendTimestampMessage(TextChannel channel, String messageText, long delayMinutes) {
        channel.sendMessage(messageText).queue(message -> {
            try {
                scheduler.schedule(() -> message.delete().queue(), delayMinutes + timeAfterSpawnMsgDeletionMinutes, TimeUnit.MINUTES);
            } catch (Exception e) {
                log.warn("Spawn timer message failed to delete. Most likely a server admin deleted it.");
            }
        });
    }

    /**
     * Sends a reply to a ButtonInteractionEvent with given prompt that has two ActionRow of 5 Button each with button IDs 1-10
     * @param event
     * @param prompt
     * @param buttonIDprefix
     */
    private void sendChannelPromptMessage(ButtonInteractionEvent event, String prompt, String buttonIDprefix) {
        event.reply(event.getUser().getAsMention() + prompt)
                .addActionRow(createActionRowOfButtons(buttonIDprefix, 1))
                // .addActionRow(createActionRowOfButtons(buttonIDprefix, 6))
                // .addActionRow(createActionRowOfButtons(buttonIDprefix, 11))
                // .addActionRow(createActionRowOfButtons(buttonIDprefix, 16))
                // .addActionRow(createActionRowOfButtons(buttonIDprefix, 21))
                .setEphemeral(true)
                .queue();
    }

    /**
     * Creates a List of up to 5 Button for use in an ActionRow
     * @param events
     * @return List of Buttons for given list of WorldEvent, or null if the list has more than 5 WorldEvent
     */
    private List<Button> createButtons(List<WorldEvent> events) {
        if(events == null) return null;
        if(events.size() > 5) {
            log.error("Too many events for a single row of buttons.");
            return null;
        }
        List<Button> actionRowList = new ArrayList<Button>();
        for(WorldEvent event : events) {
            actionRowList.add(Button.primary(event.getButtonId(), event.getLabel()));
        }
        return actionRowList;
    }

    /**
     * Centers a String s into a padded String of given length, prioritizing Left padding
     * @param s
     * @param length
     * @return
     */
    private String centeredString(String s, int length) {
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

    /**
     * Creates a description for the /wb command using all available world boss commands
     * @return
     */
    private String createWBDescription() {
        String description = "";
        for(WorldBoss wb : WorldBoss.values()) {
            description += "**/" + wb.getCommandName() + "**\n";
        }
        return description;
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

            

            fieldEvents.stream()
                .filter(fe -> fe.getDay().getValue() == zdt.getDayOfWeek().getValue())
                .forEach(fe -> {
                    Instant eventTime = fe.getInstant();
                    long secondsUntilEvent = Duration.between(now, eventTime).getSeconds();
                    if(secondsUntilEvent >= 245 && secondsUntilEvent < 300) {
                        FieldEvent nextEvent = fieldEvents.indexOf(fe) == fieldEvents.size() - 1 ? fieldEvents.get(0) : fieldEvents.get(fieldEvents.indexOf(fe) + 1);
                        Instant nextEventTime = nextEvent.getInstant();
                        String messageText = "`Silverfrost Field Boss Spawn Imminent! (" + fe.getLocation() + ")`     " + "<t:" + eventTime.getEpochSecond() + ":R>    @ " + fe.getFormattedTime() + " ST\n" + 
                        "`Next Field Boss: (" + nextEvent.getLocation() + ")`     " + "<t:" + nextEventTime.getEpochSecond() + ":R>     @ "+ nextEvent.getFormattedTime() + " ST";
                        
                        jda.getTextChannelById(neobotChannel).sendMessage(messageText).queue(
                            //Automated deletion commented out while I test
                            // msg -> {
                            //     try {
                            //         scheduler.schedule(() -> msg.delete().queue(), 60, TimeUnit.MINUTES);
                            //     } catch (Exception e) {
                            //         log.warn("Field boss spawn message failed to delete. Most likely a server admin deleted it.");
                            //     }
                            // }
                            );
                    }
                });

            //Check for gold boss spawns
            ArrayList<Integer> goldBossHours = new ArrayList<Integer>(Arrays.asList(10, 13, 16, 19, 22, 1));
            if(goldBossHours.contains(zdt.getHour()+1) && zdt.getMinute() == 40) {
                ZonedDateTime eventTime = zdt.plusHours(1).withMinute(0).withSecond(0).withNano(0);
                List<Role> roles = jda.getRolesByName("GoldBoss", true);
                //Find the first mentionable role named "GoldBoss"
                Role goldBossRole = roles.stream().filter(r -> r.isMentionable()).findFirst().orElse(null);
                String roleMention = goldBossRole == null ? "" : goldBossRole.getAsMention() + " ";
                List<Button> goldBossButtons = new ArrayList<Button>();
                goldBossButtons.add(Button.secondary("reminder-" + eventTime.minusMinutes(10).toInstant().getEpochSecond(), "10 min reminder"));
                goldBossButtons.add(Button.secondary("reminder-" + eventTime.minusMinutes(5).toInstant().getEpochSecond(), "5 min reminder"));
                jda.getTextChannelById(neobotChannel).sendMessage(roleMention + "Gold boss announce " + "<t:" + eventTime.toInstant().getEpochSecond() + ":R>" + ".").addActionRow(goldBossButtons).queue();
            }
        };

        //Run every 1 minute, starting at the next full minute
        //long delay = 60 - Instant.now().getEpochSecond() % 60;
        scheduler.scheduleAtFixedRate(fbTimerTask, 0, 1, TimeUnit.MINUTES);
    }
}