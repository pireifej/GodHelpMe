package com.ireifejapps.godhelpme;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.ireifejapps.godhelpme.GodHelpMe.*;

/**
 * Created by ireifej on 1/22/2016.
 */
public class PrayerPart  {
    public PrayerTemplate.PrayerPartType my_prayer_part_type;
    String my_prayer_part_text;
    int current_template_index = 0;
    private Boolean is_html = false;
    private Context my_context = null;

    public PrayerPart(PrayerTemplate.PrayerPartType prayer_part_type, Context context) {
        my_prayer_part_type = prayer_part_type;
        my_prayer_part_text = PrayerTemplates.get_template_text(prayer_part_type, current_template_index);
        my_context = context;
    }

    public void update_my_prayer_part_text(String text) {
        my_prayer_part_text = text;
    }

    public String change_template(Map<String, String> variables) {
        // step #1 - get next template
        current_template_index = PrayerTemplates.get_next_template(my_prayer_part_type, current_template_index);
        int template_size = PrayerTemplates.get_template_size(my_prayer_part_type);
        my_prayer_part_text = PrayerTemplates.get_template_text(my_prayer_part_type, current_template_index);

        // step #2 - plug variables into new template
        Iterator it = variables.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            substitute(pair.getKey().toString(), pair.getValue().toString());
        }

        return String.valueOf(current_template_index + 1) + "/" + String.valueOf(template_size);
    }

    public void substitute(String variable_name, String new_value) {
        Prayer.VariableNames[] variable_names = Prayer.VariableNames.values();
        for (int i = 0; i < variable_names.length; i++) {
            if (variable_names[i].toString().equals(variable_name)) {
                substitute(variable_names[i], new_value);
                break;
            }
        }
    }

    public void substitute(Prayer.VariableNames name, String new_value) {
        if (new_value.equals(my_context.getString(R.string.default_spinner_value))) return;

        String new_prayer_part_text = my_prayer_part_text;

        int begin_index = new_prayer_part_text.indexOf("{{key=" + name);
        int end_index = new_prayer_part_text.indexOf("}}", begin_index);

        if (begin_index == -1 || end_index == -1) return;

        while (begin_index != -1) {
            String variable = new_prayer_part_text.substring(begin_index + 2, end_index);
            String[] key_value_pair = variable.split(",");

            if (key_value_pair.length != 2) return;

            String key = key_value_pair[0];
            String value = key_value_pair[1];

            new_prayer_part_text = new_prayer_part_text.substring(0, begin_index) +
                    "{{" + key + ",value=" + new_value + "}}" +
                    new_prayer_part_text.substring(end_index + 2, new_prayer_part_text.length());


            begin_index = new_prayer_part_text.indexOf("{{key=" + name, end_index);
            end_index = new_prayer_part_text.indexOf("}}", begin_index);
        }

        my_prayer_part_text = new_prayer_part_text;
    }

    public Spanned toHTML() {
        is_html = true;
        return Html.fromHtml(this.toString());
    }

    @Override
    public String toString() {
        String new_value = my_prayer_part_text;
        String new_line = is_html ? "<br>" : "";
        String color_beg = is_html ? "<font color='#EE0000'>" : "";
        String color_end = is_html ? "</font>" : "";

        int begin_index = new_value.indexOf("{{");
        int end_index = new_value.indexOf("}}");

        while (begin_index != -1) {
            String variable = new_value.substring(begin_index + 2, end_index);
            String[] key_value_pair = variable.split(",");

            if (key_value_pair.length != 2) return "Error: toString: Key/Value pair is bad!";

            String key = key_value_pair[0];
            String value = key_value_pair[1];

            String value_value[] = value.split("=");

            if (value_value.length != 2) return "Error: toString: Value of the value is bad!";

            String real_value = value_value[1];

            new_value = new_value.substring(0, begin_index) +
                    color_beg + real_value + color_end +
                    new_value.substring(end_index + 2, new_value.length());

            begin_index = new_value.indexOf("{{");
            end_index = new_value.indexOf("}}");
        }

        if (is_html) new_value = new_value.replaceAll("\n", new_line);
        is_html = false;
        return new_value;
    }
}
