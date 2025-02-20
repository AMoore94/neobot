package com.neobot;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class Neobot extends ListenerAdapter {

    private static final Long testChannelId = 1341942656816775209L;
    private static final Logger log = LoggerFactory.getLogger(Neobot.class);

    /* TODO (HARD-VERYHARD) create an enumerable called WorldEvent that contains the following:
        1. Event parent name (jiangshi)
        2. Event button id  (jiangshiDied)
        3. Event label (Regular Jiangshi boss died)
        4. Event button reply text (, which channel did the boss die in?)
        4. Event button prefix (j-)
        5. Event spawn delay (5 minutes)
    TODO move the static properties to WorldEvent and use the enumerable to get the values
    TODO load those enum values into the interactions below
    TODO generalize the interactions to use the enum values
    TODO create more WorldEvents in the enum and slash command interactions for them */
    
    //TODO (MED) Create a slash (or text) command that links the user to bns live character lookup page (maybe NEO later)
    //  http://na-bns.ncsoft.com/ingame/bs/character/search/info?c=
    //  optional: use a button link!

    //TODO (HARD-VERYHARD) Create a slash (or text) command that scrapes the bns live character lookup page and
    //  extracts that data into a nice format for a discord message (I would use Selenium but whatever floats your boat)

    private static final long bossRespawnTimeMinutes = 5;
    private static final long lightningBossSpawnDelayMinutes = 2;
    private static final long lightningBossRespawnDelayMinutes = 8;
    private static final long discordUserInputDelaySeconds = 2;

    public static void main(String[] args) throws InterruptedException {
        //TODO (EASY) extract token loading to a helper method
        Properties properties = new Properties();
        String token = null;
        try {
            properties.load(Neobot.class.getClassLoader().getResourceAsStream("token.properties"));
            token = properties.getProperty("token");
        } catch (NullPointerException e) {
            log.error("Token not found. Please create a token.properties file in the resources folder with the token.");
        } catch (IOException e) {
            log.error("Error reading token.properties file.");
        }

        //TODO (EASY) set the bot's status to something appropriate
        JDA jda = JDABuilder
            .createDefault(token)
            .setActivity(null)
            .addEventListeners(new Neobot())
            .enableIntents(GatewayIntent.MESSAGE_CONTENT) //, GatewayIntent.GUILD_MESSAGES)
            .build();
        jda.awaitReady();

        //Already added commands:
        //jda.updateCommands().addCommands(Commands.slash("jiangshi", "Jiangshi World Boss options")).queue();
    }

    @Override
    public void onReady(ReadyEvent event) {
        log.warn("Bot successfully deployed.");
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        //TODO (MED-HARD) I am considering timing these messages out after 10 min - 1 hr, but I'm not sure if that's a good idea due to how many Tasks could be scheduled
        if(event.getName().equals("jiangshi")) {
            event.reply("")
            .addActionRow(
                Button.primary("jiangshiDied", "Regular Jiangshi boss died"),
                Button.primary("jiangshiLightningStart", "Jiangshi lightning started"),
                Button.primary("jiangshiLightningDied", "Jiangshi lightning boss died"))
            .queue();
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        Instant now = Instant.now();
        String buttonId =  event.getComponentId();

        //TODO (MED) update these messages to be past tense after the set amount of time has passed - Try using net.dv8tion.jda.api.utils.concurrent.Task
        //TODO (EASY) send these as regular messages instead of reply - currently with deletion of the original message, the reference is just "original message was deleted"
        if(buttonId.startsWith("j-")) {
            event.getMessage().delete().queue();
            long unixTimestamp = now.plus(Duration.ofMinutes(bossRespawnTimeMinutes)).minus(Duration.ofSeconds(discordUserInputDelaySeconds)).getEpochSecond();
            event.reply("Jiangshi boss will respawn in Channel " + buttonId.substring(2) + discordTimestamp(unixTimestamp)).queue();
        } else if(buttonId.startsWith("jl-")) {
            event.getMessage().delete().queue();
            long unixTimestamp = now.plus(Duration.ofMinutes(lightningBossSpawnDelayMinutes)).minus(Duration.ofSeconds(discordUserInputDelaySeconds)).getEpochSecond();
            event.reply(":zap: Lightning :zap: Jiangshi boss will spawn in Channel " + buttonId.substring(3) + discordTimestamp(unixTimestamp)).queue();
        } else if(buttonId.startsWith("jlx-")) {
            event.getMessage().delete().queue();
            long unixTimestamp = now.plus(Duration.ofMinutes(lightningBossRespawnDelayMinutes)).minus(Duration.ofSeconds(discordUserInputDelaySeconds)).getEpochSecond();
            event.reply("Jiangshi boss will spawn in Channel " + buttonId.substring(4) + discordTimestamp(unixTimestamp)).queue();
        }

        //TODO (EASY) make these messages ephemeral so that only the user that clicked the button can see them
        //TODO (MED) time out these buttons if the user does not respond within ... lets say 5-10 seconds? - Try using net.dv8tion.jda.api.utils.concurrent.Task
        if(buttonId.equals("jiangshiDied")) {
            event.reply(event.getUser().getAsMention() + ", which channel did the boss die in?")
                .addActionRow(getChannelButtons1to5("j-"))
                .addActionRow(getChannelButtons6to10("j-"))
                .queue();
        } else if(buttonId.equals("jiangshiLightningStart")) {
            event.reply(event.getUser().getAsMention() + ", which channel did the lightning start in?")
                .addActionRow(getChannelButtons1to5("jl-"))
                .addActionRow(getChannelButtons6to10("jl-"))
                .queue();
        } else if(buttonId.equals("jiangshiLightningDied")) {
            event.reply(event.getUser().getAsMention() + ", which channel did the lightning boss die in?")
                .addActionRow(getChannelButtons1to5("jlx-"))
                .addActionRow(getChannelButtons6to10("jlx-"))
                .queue();
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        //Ignore all messages from bots
        if (event.getAuthor().isBot()) return;

        String message = event.getMessage().getContentRaw();

        //Ignore all messages that don't start with !
        if (!message.startsWith("!")) return;

        TextChannel channel = event.getChannel().asTextChannel();
        List<Long> allowedChannels = new ArrayList<Long>();
        allowedChannels.add(testChannelId);
        //If you want to test text-based commands, you can add the channel ID here

        if (allowedChannels.contains(channel.getIdLong())) {
            //!hello
            if (isCommand(message, "hello")) {
                channel.sendMessage("Hello, " + event.getAuthor().getAsMention() + "!").queue();
            }
        }
    }

    //Helper methods
    private boolean isCommand(String message, String command) {
        if(isNullOrEmpty(message, command)) return false;
        if(message.startsWith("!")) return isCommand(message.substring(1), command);
        return message.toLowerCase().startsWith(command.toLowerCase());
    }

    private boolean isNullOrEmpty(String... set) {
        for(String s : set) {
            if(s == null) return true;
            if(s.isEmpty()) return true;
        }
        return false;
    }

    private List<Button> getChannelButtons(String buttonIDPrefix, int start) {
        List<Button> actionRowList = new ArrayList<Button>();
        for(int i = start; i < start+5; i++) {
            actionRowList.add(Button.primary(buttonIDPrefix + i, String.valueOf(i)));
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
}