package com.neobot;

import lombok.Getter;

@Getter
public enum WorldEvent {
    //TODO: Enums can be optimized ... there is some redundancy across WorldBoss and WorldEvent
    //TODO: Add buffering to the display names so that they look nice in Discord
    //TODO: Add lightning zaps and correct displaynames
    JIANGSHI("stalkerjiangshi", "jiangshiDied", "Regular Jiangshi boss died", ", which channel did the boss die in?", "j-", 5),
    JIANGSHI_LIGHTNING_START("stalkerjiangshi", "jiangshiLightningStart", "Jiangshi lightning started", ", which channel did the lightning start in?", "jl-", 2),
    JIANGSHI_LIGHTNING_DIED("stalkerjiangshi", "jiangshiLightningDied", "Jiangshi lightning boss died", ", which channel did the lightning boss die in?", "jlx-", 8),
    GIGANURA("giganura", "giganuraDied", "Giganura boss died", ", which channel did the boss die in?", "g-", 5),
    GIGANURA_LIGHTNING_START("giganura", "giganuraLightningStart", "Giganura lightning started", ", which channel did the lightning start in?", "gl-", 2),
    GIGANURA_LIGHTNING_DIED("giganura", "giganuraLightningDied", "Giganura lightning boss died", ", which channel did the lightning boss die in?", "glx-", 8),
    TOP_DOG_WU_FU("wufu", "wuFuDied", "Top Dog Wu Fu boss died", ", which channel did the boss die in?", "w-", 5),
    TOP_DOG_WU_FU_LIGHTNING_START("wufu", "wuFuLightningStart", "Top Dog Wu Fu lightning started", ", which channel did the lightning start in?", "wl-", 2),
    TOP_DOG_WU_FU_LIGHTNING_DIED("wufu", "wuFuLightningDied", "Top Dog Wu Fu lightning boss died", ", which channel did the lightning boss die in?", "wlx-", 8),
    KING_BULBARI("bulbari", "kingBulbariDied", "King Bulbari boss died", ", which channel did the boss die in?", "kb-", 5),
    KING_BULBARI_LIGHTNING_START("bulbari", "kingBulbariLightningStart", "King Bulbari lightning started", ", which channel did the lightning start in?", "kbl-", 2),
    KING_BULBARI_LIGHTNING_DIED("bulbari", "kingBulbariLightningDied", "King Bulbari lightning boss died", ", which channel did the lightning boss die in?", "kblx-", 8),
    GOLDEN_DEVA("deva", "goldenDevaDied", "Golden Deva boss died", ", which channel did the boss die in?", "gd-", 5),
    GOLDEN_DEVA_LIGHTNING_START("deva", "goldenDevaLightningStart", "Golden Deva lightning started", ", which channel did the lightning start in?", "gdl-", 2),
    GOLDEN_DEVA_LIGHTNING_DIED("deva", "goldenDevaLightningDied", "Golden Deva lightning boss died", ", which channel did the lightning boss die in?", "gdlx-", 8),
    PINCHY("pinchy", "pinchyDied", "Pinchy boss died", ", which channel did the boss die in?", "p-", 5),
    PINCHY_LIGHTNING_START("pinchy", "pinchyLightningStart", "Pinchy lightning started", ", which channel did the lightning start in?", "pl-", 2),
    PINCHY_LIGHTNING_DIED("pinchy", "pinchyLightningDied", "Pinchy lightning boss died", ", which channel did the lightning boss die in?", "plx-", 8),
    LYCAN("lycan", "lycanDied", "Lycan boss died", ", which channel did the boss die in?", "l-", 5),
    LYCAN_LIGHTNING_START("lycan", "lycanLightningStart", "Lycan lightning started", ", which channel did the lightning start in?", "ll-", 2),
    LYCAN_LIGHTNING_DIED("lycan", "lycanLightningDied", "Lycan lightning boss died", ", which channel did the lightning boss die in?", "llx-", 8),
    KING_KAARI("kaari", "kingKaariDied", "King Kaari boss died", ", which channel did the boss die in?", "kk-", 5),
    KING_KAARI_LIGHTNING_START("kaari", "kingKaariLightningStart", "King Kaari lightning started", ", which channel did the lightning start in?", "kkl-", 2),
    KING_KAARI_LIGHTNING_DIED("kaari", "kingKaariLightningDied", "King Kaari lightning boss died", ", which channel did the lightning boss die in?", "kklx-", 8),
    PROFANE_JIANGSHI("profanejiangshi", "profaneJiangshiDied", "Profane Jiangshi boss died", ", which channel did the boss die in?", "pj-", 5),
    PROFANE_JIANGSHI_LIGHTNING_START("profanejiangshi", "profaneJiangshiLightningStart", "Profane Jiangshi lightning started", ", which channel did the lightning start in?", "pjl-", 2),
    PROFANE_JIANGSHI_LIGHTNING_DIED("profanejiangshi", "profaneJiangshiLightningDied", "Profane Jiangshi lightning boss died", ", which channel did the lightning boss die in?", "pjlx-", 8),
    SAJIFI("sajifi", "sajifiDied", "Sajifi boss died", ", which channel did the boss die in?", "s-", 5),
    SAJIFI_LIGHTNING_START("sajifi", "sajifiLightningStart", "Sajifi lightning started", ", which channel did the lightning start in?", "sl-", 2),
    SAJIFI_LIGHTNING_DIED("sajifi", "sajifiLightningDied", "Sajifi lightning boss died", ", which channel did the lightning boss die in?", "slx-", 8);

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
