package com.neobot;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
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

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class Neobot extends ListenerAdapter {

    private static final Logger log = LoggerFactory.getLogger(Neobot.class);
    
    //Bns live character lookup page (maybe NEO later?)
    //  http://na-bns.ncsoft.com/ingame/bs/character/search/info?c=

    private static final String botInviteURL = "https://discord.com/oauth2/authorize?client_id=1341938087156252672&permissions=563484677240896&integration_type=0&scope=bot";

    private static final long discordUserInputDelaySeconds = 5;
    private static final long timeAfterSpawnMsgDeletionMinutes = 5;

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private static final List<WorldEvent> worldEvents = new ArrayList<WorldEvent>();

    private HashMap<TextChannel, Boolean> heavensReachChannels = new HashMap<TextChannel, Boolean>();
    private HashMap<TextChannel, Boolean> viridianCoastChannels = new HashMap<TextChannel, Boolean>();

    public static void main(String[] args) throws InterruptedException {
        JDA jda = JDABuilder
            .createDefault(getToken())
            .setActivity(Activity.customStatus( "Fighting demons"))
            .addEventListeners(new Neobot())
            .enableIntents(GatewayIntent.MESSAGE_CONTENT)
            .build();
        jda.awaitReady();

        //Verify all necessary commands exist:
        List<SlashCommandData> newCommands = new ArrayList<SlashCommandData>();
        for(WorldBoss wb : WorldBoss.values()) {
            newCommands.add(Commands.slash(wb.getCommandName(), wb.getDisplayName() + " World Boss options"));
        }
        newCommands.add(Commands.slash("server", "Select which server you are playing on").addOption(OptionType.STRING, "server", "Use Heaven's Reach (HR) or Viridian Coast (VC) to set the server for this channel"));
        newCommands.add(Commands.slash("global", "Enable/disable countdowns from other discord servers").addOption(OptionType.BOOLEAN, "global", "Use TRUE to enable and FALSE to disable global countdowns for this channel"));
        jda.updateCommands().addCommands(newCommands).queue();

        //Add all the world events to a static list for use here
        worldEvents.addAll(Arrays.asList(WorldEvent.values()));
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
                 .addActionRow(Button.link(botInviteURL, "Add the bot user"))
                 .setEphemeral(true).queue();
            return;
        }

        if(event.getName().equals("server")) {
            TextChannel channel = event.getChannel().asTextChannel();
            OptionMapping option = event.getOption("server");
            if(option == null) {
                if(heavensReachChannels.containsKey(channel)) {
                    event.reply("This channel is currently set to Heaven's Reach.").setEphemeral(true).queue();
                } else if(viridianCoastChannels.containsKey(channel)) {
                    event.reply("This channel is currently set to Viridian Coast.").setEphemeral(true).queue();
                } else {
                    event.reply("This channel is not currently set to a server. Use /server <servername> to set the server.").setEphemeral(true).queue();
                }
                return;
            }

            //TODO - make it so that Global ON/OFF is maintained when switching servers
            String serverName = event.getOption("server").getAsString().toLowerCase();
            if(serverName.contains("h")) {
                viridianCoastChannels.remove(channel);
                heavensReachChannels.putIfAbsent(channel, false);
                event.reply("This channel has been added to the Heaven's Reach server list.").queue();
            } else if (serverName.contains("v")) {
                heavensReachChannels.remove(channel);
                viridianCoastChannels.putIfAbsent(channel, false);
                event.reply("This channel has been added to the Viridian Coast server list.").queue();
            } else {
                event.reply("Your server name was not recognized. No changes were made.").setEphemeral(true).queue();
            }
            return;
        }

        if(event.getName().equals("global")) {
            TextChannel channel = event.getChannel().asTextChannel();
            OptionMapping option = event.getOption("global");
            if(option == null) {
                if(heavensReachChannels.containsKey(channel)) {
                    Boolean value = heavensReachChannels.get(channel);
                    event.reply("This channel is currently set to Global " + (value ? "ON" : "OFF") + ". It will " + (value ? "" : "NOT ") + "receive messages from other servers.").setEphemeral(true).queue();
                } else if(viridianCoastChannels.containsKey(channel)) {
                    Boolean value = viridianCoastChannels.get(channel);
                    event.reply("This channel is currently set to Global " + (value ? "ON" : "OFF") + ". It will " + (value ? "" : "NOT ") + "receive messages from other servers.").setEphemeral(true).queue();
                } else {
                    event.reply("You first need to specify a game server for this channel using /server.").setEphemeral(true).queue();
                }
                return;
            }

            Boolean globalOption;
            try {
                globalOption = event.getOption("global").getAsBoolean();
            } catch (IllegalStateException e) {
                event.reply("You need to provide TRUE or FALSE for the /global command.").setEphemeral(true).queue();
                return;
            }
            
            if(heavensReachChannels.containsKey(channel)) {
                heavensReachChannels.put(channel, globalOption);
                if(globalOption) {
                    event.reply("Global ON. You will now receive countdowns from other Heaven's Reach servers.").queue();
                } else {
                    event.reply("Global OFF. You will NOT receive countdowns from other servers.").queue();
                }
            } else if(viridianCoastChannels.containsKey(channel)) {
                viridianCoastChannels.put(channel, globalOption);
                if(globalOption) {
                    event.reply("Global ON. You will now receive countdowns from other Viridian Coast servers.").queue();
                } else {
                    event.reply("Global OFF. You will NOT receive countdowns from other servers.").queue();
                }
            } else {
                event.reply("You first need to specify a game server for this channel using /server.").setEphemeral(true).queue();
            }
            return;
        }

        //If other non-world-boss commands are added, add them before this
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

        if(buttonInputs.size() > 1) {
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
    //TODO these channel button helpers can be refactored to be more generic
    private List<Button> getChannelButtons(String buttonIDPrefix, int start) {
        List<Button> actionRowList = new ArrayList<Button>();
        for(int i = start; i < start+5; i++) {
            actionRowList.add(Button.secondary(buttonIDPrefix + i, String.valueOf(i)));
        }
        return actionRowList;
    }

    private List<Button> getChannelButtons1to5(String buttonIDPrefix) {
        return getChannelButtons(buttonIDPrefix, 1);
    }

    private List<Button> getChannelButtons6to10(String buttonIDPrefix) {
        return getChannelButtons(buttonIDPrefix, 6);
    }

    private String discordTimestamp(long unixTimestamp) {
        return "<t:" + unixTimestamp + ":R>";
    }

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

    private void sendEventTimestampMessage(ButtonInteractionEvent event, String bossName, String channel, long delayMinutes) {
        Instant now = Instant.now();
        event.getMessage().delete().queue();
        event.deferEdit().queue();
        Instant spawnTime = now.plus(Duration.ofMinutes(delayMinutes)).minus(Duration.ofSeconds(discordUserInputDelaySeconds));
        long unixTimestamp = spawnTime.getEpochSecond();
        String quote = bossName.startsWith("Mutated") ? "> " : "";
        String messageText = quote + "`" + bufferedBossString(bossName, 30-quote.length()) + "|" + bufferedChannelString(channel) + "|`     " + discordTimestamp(unixTimestamp);
        TextChannel textChannel = event.getChannel().asTextChannel();

        //Send the default message
        timestampMessage(textChannel, messageText, delayMinutes);

        //Send global countdowns:
        //If channel is a Heaven's Reach channel
        if(heavensReachChannels.containsKey(textChannel)) {
            heavensReachChannels.forEach((key, value) -> {
                if(value && !key.equals(textChannel)) {
                    timestampMessage(key, messageText, delayMinutes);
                }
            });
        }
        //If channel is a Viridian Coast channel
        if(viridianCoastChannels.containsKey(textChannel)) {
            viridianCoastChannels.forEach((key, value) -> {
                if(value && !key.equals(textChannel)) {
                    timestampMessage(key, messageText, delayMinutes);
                }
            });
        }
    }

    private void timestampMessage(TextChannel channel, String messageText, long delayMinutes) {
        channel.sendMessage(messageText).queue(message -> {
            // Schedule the message to be deleted timeAfterSpawnMsgDeletionMinutes minutes after the spawn time
            try {
                scheduler.schedule(() -> message.delete().queue(), delayMinutes + timeAfterSpawnMsgDeletionMinutes, TimeUnit.MINUTES);
            } catch (Exception e) {
                log.warn("Spawn timer message failed to delete. Most likely a server admin deleted it.");
            }
        });
    }

    private void sendChannelPromptMessage(ButtonInteractionEvent event, String prompt, String buttonIDprefix) {
        event.reply(event.getUser().getAsMention() + prompt)
                .addActionRow(getChannelButtons1to5(buttonIDprefix))
                .addActionRow(getChannelButtons6to10(buttonIDprefix))
                .setEphemeral(true)
                .queue();
    }

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

    private String bufferedBossString(String bossName, int len) {
        return centeredString(bossName, len);
    }

    private String bufferedChannelString(String channel) {
        return centeredString("Channel " + channel, 15);
    }

    /**
     * Centers a String s into a padded string of given length, prioritizing Left padding
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
}