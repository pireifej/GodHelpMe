package com.ireifejapps.godhelpme;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by ireifej on 1/27/2016.
 */
public final class PrayerTemplates {
    // prayer part array
    private static ArrayList<String> adoration_prayer_parts = new ArrayList<String>();
    private static ArrayList<String> confession_sin_prayer_parts = new ArrayList<String>();
    private static ArrayList<String> confession_offender_prayer_parts = new ArrayList<String>();
    private static ArrayList<String> thanksgiving_prayer_parts = new ArrayList<String>();
    private static ArrayList<String> supplication_prayer_parts = new ArrayList<String>();

    // variable name to prayer template mapping
    private static Map<String, PrayerTemplate.PrayerPartType> var_to_temp = new HashMap<String, PrayerTemplate.PrayerPartType>();

    // default variable values
    private static Set<String> default_values = new HashSet<>();

    public static void init(String adoration[], String confession_offender[], String confession_sin[], String thanksgiving[], String supplication[]) {
        for (int i = 0; i < adoration.length; i++) {
            adoration_prayer_parts.add(adoration[i]);
            collect_default_values(adoration[i]);
        }
        for (int i = 0; i < confession_offender.length; i++) {
            confession_offender_prayer_parts.add(confession_offender[i]);
            collect_default_values(confession_offender[i]);
        }
        for (int i = 0; i < confession_sin.length; i++) {
            confession_sin_prayer_parts.add(confession_sin[i]);
            collect_default_values(confession_sin[i]);
        }
        for (int i = 0; i < thanksgiving.length; i++) {
            thanksgiving_prayer_parts.add(thanksgiving[i]);
            collect_default_values(thanksgiving[i]);
        }
        for (int i = 0; i < supplication.length; i++) {
            supplication_prayer_parts.add(supplication[i]);
            collect_default_values(supplication[i]);
        }

        Log.i("default values", default_values.toString());

        var_to_temp.put(Prayer.VariableNames.god.toString(), PrayerTemplate.PrayerPartType.Adoration);
        var_to_temp.put(Prayer.VariableNames.love.toString(), PrayerTemplate.PrayerPartType.Adoration);
        var_to_temp.put(Prayer.VariableNames.offender.toString(), PrayerTemplate.PrayerPartType.ConfessionOffender);
        var_to_temp.put(Prayer.VariableNames.sin.toString(), PrayerTemplate.PrayerPartType.ConfessionSin);
        var_to_temp.put(Prayer.VariableNames.appreciate.toString(), PrayerTemplate.PrayerPartType.Thanksgiving);
        var_to_temp.put(Prayer.VariableNames.help.toString(), PrayerTemplate.PrayerPartType.Supplication);
        var_to_temp.put(Prayer.VariableNames.relationship.toString(), PrayerTemplate.PrayerPartType.Supplication);
        var_to_temp.put(Prayer.VariableNames.gift.toString(), PrayerTemplate.PrayerPartType.Supplication);
    }

    private static void collect_default_values(String text) {
            String my_prayer_part_text = text;
            int begin_index = my_prayer_part_text.indexOf("{{");
            int end_index = my_prayer_part_text.indexOf("}}");

            if (begin_index == -1 || end_index == -1) return;

            while (begin_index != -1) {
                String variable = my_prayer_part_text.substring(begin_index + 2, end_index);
                String[] key_value_pair = variable.split(",");

                if (key_value_pair.length != 2) return;

                String key = key_value_pair[0];
                String value = key_value_pair[1];

                String key_value[] = key.split("=");
                String value_value[] = value.split("=");

                if (key_value.length != 2) return;
                if (value_value.length != 2) return;

                default_values.add(value_value[1]);

                begin_index = my_prayer_part_text.indexOf("{{", end_index);
                end_index = my_prayer_part_text.indexOf("}}", begin_index);
            }
    }

    public static Set<String> get_default_values() {
        return default_values;
    }

    public static Map<String, PrayerTemplate.PrayerPartType> get_var_to_temp_map() {
        return var_to_temp;
    }

    public static String get_template_text(PrayerTemplate.PrayerPartType prayer_part_type, int index) {
        String prayer_template_text = "";
        if (prayer_part_type == PrayerTemplate.PrayerPartType.Adoration) {
            if (index >= adoration_prayer_parts.size()) return prayer_template_text;
            prayer_template_text = adoration_prayer_parts.get(index);
        }
        if (prayer_part_type == PrayerTemplate.PrayerPartType.ConfessionOffender) {
            if (index >= confession_offender_prayer_parts.size()) return prayer_template_text;
            prayer_template_text = confession_offender_prayer_parts.get(index);
        }
        if (prayer_part_type == PrayerTemplate.PrayerPartType.ConfessionSin) {
            if (index >= confession_sin_prayer_parts.size()) return prayer_template_text;
            prayer_template_text = confession_sin_prayer_parts.get(index);
        }
        if (prayer_part_type == PrayerTemplate.PrayerPartType.Thanksgiving) {
            if (index >= thanksgiving_prayer_parts.size()) return prayer_template_text;
            prayer_template_text = thanksgiving_prayer_parts.get(index);
        }
        if (prayer_part_type == PrayerTemplate.PrayerPartType.Supplication) {
            if (index >= supplication_prayer_parts.size()) return prayer_template_text;
            prayer_template_text = supplication_prayer_parts.get(index);
        }
        return prayer_template_text;
    }

    public static int get_next_template(PrayerTemplate.PrayerPartType prayer_part_type, int index) {
        index++;
        if (prayer_part_type == PrayerTemplate.PrayerPartType.Adoration) {
            if (index >= adoration_prayer_parts.size()) return 0;
        }
        if (prayer_part_type == PrayerTemplate.PrayerPartType.ConfessionSin) {
            if (index >= confession_sin_prayer_parts.size()) return 0;
        }
        if (prayer_part_type == PrayerTemplate.PrayerPartType.ConfessionOffender) {
            if (index >= confession_offender_prayer_parts.size()) return 0;
        }
        if (prayer_part_type == PrayerTemplate.PrayerPartType.Thanksgiving) {
            if (index >= thanksgiving_prayer_parts.size()) return 0;
        }
        if (prayer_part_type == PrayerTemplate.PrayerPartType.Supplication) {
            if (index >= supplication_prayer_parts.size()) return 0;
        }
        return index;
    }

    public static int get_template_size(PrayerTemplate.PrayerPartType prayer_part_type) {
        int size = 0;
        if (prayer_part_type == PrayerTemplate.PrayerPartType.Adoration) {
            size = adoration_prayer_parts.size();
        }
        if (prayer_part_type == PrayerTemplate.PrayerPartType.ConfessionSin) {
            size = confession_sin_prayer_parts.size();
        }
        if (prayer_part_type == PrayerTemplate.PrayerPartType.ConfessionOffender) {
            size = confession_offender_prayer_parts.size();
        }
        if (prayer_part_type == PrayerTemplate.PrayerPartType.Thanksgiving) {
            size = thanksgiving_prayer_parts.size();
        }
        if (prayer_part_type == PrayerTemplate.PrayerPartType.Supplication) {
            size = supplication_prayer_parts.size();
        }
        return size;
    }
}
