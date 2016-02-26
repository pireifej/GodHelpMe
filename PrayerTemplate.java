package com.ireifejapps.godhelpme;

/**
 * Created by ireifej on 1/27/2016.
 */
public class PrayerTemplate {
    PrayerPartType my_prayer_part_type;
    String my_prayer_text;

    public enum PrayerPartType {
        Adoration, // Give God praise and honor for who he is as Lord over all
        ConfessionSin, // Honestly deal with the sin in your prayer life
        ConfessionOffender, // Honestly deal with the offenders in your prayer life
        Thanksgiving, // Verbalize what you're grateful for in your life and in the world around you.
        Supplication // Pray for the needs of others and yourself.
    }

    PrayerTemplate(PrayerPartType prayer_part_type) {
        my_prayer_part_type = prayer_part_type;
        my_prayer_text = PrayerTemplates.get_template_text(prayer_part_type, 0);
    }
}
