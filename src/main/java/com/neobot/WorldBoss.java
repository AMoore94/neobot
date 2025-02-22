package com.neobot;

import java.util.List;

import lombok.Getter;

@Getter
public enum WorldBoss {
    //TODO: Enums can be optimized ... there is some redundancy across WorldBoss and WorldEvent
    //TODO: Disable unreleased world bosses
    stalkerjiangshi("stalkerjiangshi","Stalker Jiangshi", "Everdusk", 6, List.of(WorldEvent.JIANGSHI, WorldEvent.JIANGSHI_LIGHTNING_START, WorldEvent.JIANGSHI_LIGHTNING_DIED)),
    giganura("giganura","Giganura", "Pondkip Vale", 15, List.of(WorldEvent.GIGANURA, WorldEvent.GIGANURA_LIGHTNING_START, WorldEvent.GIGANURA_LIGHTNING_DIED)),
    wufu("wufu","Top Dog Wu Fu", "Songshu Isle", 18, List.of(WorldEvent.TOP_DOG_WU_FU, WorldEvent.TOP_DOG_WU_FU_LIGHTNING_START, WorldEvent.TOP_DOG_WU_FU_LIGHTNING_DIED)),
    bulbari("bulbari","King Bulbari", "Tomun Range", 25, List.of(WorldEvent.KING_BULBARI, WorldEvent.KING_BULBARI_LIGHTNING_START, WorldEvent.KING_BULBARI_LIGHTNING_DIED)),
    deva("deva","Golden Deva", "Scorching Sands", 30, List.of(WorldEvent.GOLDEN_DEVA, WorldEvent.GOLDEN_DEVA_LIGHTNING_START, WorldEvent.GOLDEN_DEVA_LIGHTNING_DIED)),
    pinchy("pinchy","Pinchy", "Scorcing Sands", 35, List.of(WorldEvent.PINCHY, WorldEvent.PINCHY_LIGHTNING_START, WorldEvent.PINCHY_LIGHTNING_DIED)),
    lycan("lycan","Lycan", "Lycandi Foothills", 38, List.of(WorldEvent.LYCAN, WorldEvent.LYCAN_LIGHTNING_START, WorldEvent.LYCAN_LIGHTNING_DIED)),
    kaari("kaari","King Kaari", "Sapphire Basin", 42, List.of(WorldEvent.KING_KAARI, WorldEvent.KING_KAARI_LIGHTNING_START, WorldEvent.KING_KAARI_LIGHTNING_DIED)),
    profanejiangshi("profanejiangshi","Profane Jiangshi", "Highland Necropolis", 43, List.of(WorldEvent.PROFANE_JIANGSHI, WorldEvent.PROFANE_JIANGSHI_LIGHTNING_START, WorldEvent.PROFANE_JIANGSHI_LIGHTNING_DIED)),
    sajifi("sajifi","Sajifi", "Misty Woods", 44, List.of(WorldEvent.SAJIFI, WorldEvent.SAJIFI_LIGHTNING_START, WorldEvent.SAJIFI_LIGHTNING_DIED));

    private final String commandName;
    private final String displayName;
    private final String area;
    private final int level;
    private final List<WorldEvent> events;

    WorldBoss(String commandName, String displayName, String area, int level, List<WorldEvent> events) {
        this.commandName = commandName;
        this.displayName = displayName;
        this.area = area;
        this.level = level;
        this.events = events;
    }
}
