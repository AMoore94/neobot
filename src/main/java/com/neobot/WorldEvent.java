package com.neobot;

import lombok.Getter;

@Getter
public enum WorldEvent {
    //TODO: Enums can be optimized ... there is some redundancy across WorldBoss and WorldEvent
    JIANGSHI("Stalker Jiangshi", "jiangshiDied", "Regular Jiangshi boss died", ", which channel did the boss die in?", "j-", 5),
    JIANGSHI_LIGHTNING_START("Mutated Stalker Jiangshi", "jiangshiLightningStart", "Jiangshi lightning started", ", which channel did the lightning start in?", "jl-", 2),
    JIANGSHI_LIGHTNING_DIED("Stalker Jiangshi", "jiangshiLightningDied", "Jiangshi lightning boss died", ", which channel did the lightning boss die in?", "jlx-", 8),
    GIGANURA("Giganura", "giganuraDied", "Giganura boss died", ", which channel did the boss die in?", "g-", 5),
    GIGANURA_LIGHTNING_START("Mutated Giganura", "giganuraLightningStart", "Giganura lightning started", ", which channel did the lightning start in?", "gl-", 2),
    GIGANURA_LIGHTNING_DIED("Giganura", "giganuraLightningDied", "Giganura lightning boss died", ", which channel did the lightning boss die in?", "glx-", 8),
    TOP_DOG_WU_FU("Top Dog Wu Fu", "wuFuDied", "Top Dog Wu Fu boss died", ", which channel did the boss die in?", "w-", 5),
    TOP_DOG_WU_FU_LIGHTNING_START("Mutated Top Dog Wu Fu", "wuFuLightningStart", "Top Dog Wu Fu lightning started", ", which channel did the lightning start in?", "wl-", 2),
    TOP_DOG_WU_FU_LIGHTNING_DIED("Top Dog Wu Fu", "wuFuLightningDied", "Top Dog Wu Fu lightning boss died", ", which channel did the lightning boss die in?", "wlx-", 8),
    KING_BULBARI("King Bulbari", "kingBulbariDied", "King Bulbari boss died", ", which channel did the boss die in?", "kb-", 5),
    KING_BULBARI_LIGHTNING_START("Mutated King Bulbari", "kingBulbariLightningStart", "King Bulbari lightning started", ", which channel did the lightning start in?", "kbl-", 2),
    KING_BULBARI_LIGHTNING_DIED("King Bulbari", "kingBulbariLightningDied", "King Bulbari lightning boss died", ", which channel did the lightning boss die in?", "kblx-", 8),
    GOLDEN_DEVA("Golden Deva", "goldenDevaDied", "Golden Deva boss died", ", which channel did the boss die in?", "gd-", 5),
    GOLDEN_DEVA_LIGHTNING_START("Mutated Golden Deva", "goldenDevaLightningStart", "Golden Deva lightning started", ", which channel did the lightning start in?", "gdl-", 2),
    GOLDEN_DEVA_LIGHTNING_DIED("Golden Deva", "goldenDevaLightningDied", "Golden Deva lightning boss died", ", which channel did the lightning boss die in?", "gdlx-", 8),
    PINCHY("Pinchy", "pinchyDied", "Pinchy boss died", ", which channel did the boss die in?", "p-", 5),
    PINCHY_LIGHTNING_START("Mutated Pinchy", "pinchyLightningStart", "Pinchy lightning started", ", which channel did the lightning start in?", "pl-", 2),
    PINCHY_LIGHTNING_DIED("Pinchy", "pinchyLightningDied", "Pinchy lightning boss died", ", which channel did the lightning boss die in?", "plx-", 8);
    // LYCAN("Lycan", "lycanDied", "Lycan boss died", ", which channel did the boss die in?", "l-", 5),
    // LYCAN_LIGHTNING_START("Mutated Lycan", "lycanLightningStart", "Lycan lightning started", ", which channel did the lightning start in?", "ll-", 2),
    // LYCAN_LIGHTNING_DIED("Lycan", "lycanLightningDied", "Lycan lightning boss died", ", which channel did the lightning boss die in?", "llx-", 8),
    // KING_KAARI("Kaari", "kingKaariDied", "King Kaari boss died", ", which channel did the boss die in?", "kk-", 5),
    // KING_KAARI_LIGHTNING_START("Mutated Kaari", "kingKaariLightningStart", "King Kaari lightning started", ", which channel did the lightning start in?", "kkl-", 2),
    // KING_KAARI_LIGHTNING_DIED("Kaari", "kingKaariLightningDied", "King Kaari lightning boss died", ", which channel did the lightning boss die in?", "kklx-", 8),
    // PROFANE_JIANGSHI("Profane Jiangshi", "profaneJiangshiDied", "Profane Jiangshi boss died", ", which channel did the boss die in?", "pj-", 5),
    // PROFANE_JIANGSHI_LIGHTNING_START("Mutated Profane Jiangshi", "profaneJiangshiLightningStart", "Profane Jiangshi lightning started", ", which channel did the lightning start in?", "pjl-", 2),
    // PROFANE_JIANGSHI_LIGHTNING_DIED("Profane Jiangshi", "profaneJiangshiLightningDied", "Profane Jiangshi lightning boss died", ", which channel did the lightning boss die in?", "pjlx-", 8),
    // SAJIFI("Sajifi", "sajifiDied", "Sajifi boss died", ", which channel did the boss die in?", "s-", 5),
    // SAJIFI_LIGHTNING_START("Mutated Sajifi", "sajifiLightningStart", "Sajifi lightning started", ", which channel did the lightning start in?", "sl-", 2),
    // SAJIFI_LIGHTNING_DIED("Sajifi", "sajifiLightningDied", "Sajifi lightning boss died", ", which channel did the lightning boss die in?", "slx-", 8);

    private final String displayName;
    private final String buttonId;
    private final String label;
    private final String buttonReplyText;
    private final String buttonPrefix;
    private final int spawnDelayMinutes;

    WorldEvent(String displayName, String buttonId, String label, String buttonReplyText, String buttonPrefix, int spawnDelayMinutes) {
        this.displayName = displayName;
        this.buttonId = buttonId;
        this.label = label;
        this.buttonReplyText = buttonReplyText;
        this.buttonPrefix = buttonPrefix;
        this.spawnDelayMinutes = spawnDelayMinutes;
    }

}
