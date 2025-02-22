package com.neobot;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
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

    private static final long discordUserInputDelaySeconds = 5;
    private static final long timeAfterSpawnMsgDeletionMinutes = 5;
    private static final long slashCommandReplyTimeoutDeletionMinutes = 60;

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private static final List<WorldEvent> worldEvents = new ArrayList<WorldEvent>();

    public static void main(String[] args) throws InterruptedException {
        JDA jda = JDABuilder
            .createDefault(getToken())
            .setActivity(Activity.customStatus( "Under construction..." )) //"Waiting for launch day"))
            .addEventListeners(new Neobot())
            .enableIntents(GatewayIntent.MESSAGE_CONTENT) //, GatewayIntent.GUILD_MESSAGES)
            .build();
        jda.awaitReady();

        //Verify all necessary commands exist:
        List<SlashCommandData> newCommands = new ArrayList<SlashCommandData>();
        for(WorldBoss wb : WorldBoss.values()) {
            newCommands.add(Commands.slash(wb.getCommandName(), wb.getDisplayName() + " World Boss options"));
        }
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
        //If other non-world-boss commands are added, add them before this
        WorldBoss wb = WorldBoss.valueOf(event.getName());
        List<WorldEvent> events = wb.getEvents();
        event.reply("")
             .addActionRow(createButtons(events))
             .setEphemeral(true)
             .queue(message -> {
            // Schedule the message to be deleted slashCommandReplyTimeoutDeletionMinutes minutes after the spawn time
            scheduler.schedule(() -> {
                try {
                    message.deleteOriginal().queue();
                } catch (Exception e) {
                    log.warn("Ephemeral message failed to delete. Most likely the user already dismissed it.");
                }
            }, slashCommandReplyTimeoutDeletionMinutes, TimeUnit.MINUTES);
        });
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
        event.getChannel().sendMessage(messageText).queue(message -> {
            // Schedule the message to be deleted timeAfterSpawnMsgDeletionMinutes minutes after the spawn time
            scheduler.schedule(() -> message.delete().queue(), delayMinutes + timeAfterSpawnMsgDeletionMinutes, TimeUnit.MINUTES);
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