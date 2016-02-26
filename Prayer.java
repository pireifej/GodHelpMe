package com.ireifejapps.godhelpme;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by ireifej on 1/26/2016.
 */
public class Prayer {
    ArrayList<PrayerPart> my_prayer_parts = new ArrayList<PrayerPart>();
    private String my_name = "";
    private String my_file_name = "";
    private Map<String, String> my_variables = new HashMap<String, String>();
    private Context my_context = null;

    // variable names
    public enum VariableNames {
        god,    // what do you call God?
        // adoration
        love,   // why do you love God?
        // confession
        offender,   // who sinned against you?
        sin,        // what is your sin?
        // thanksgiving
        appreciate,  // who/what are you thankful for?
        // supplication
        help,    // who are you praying for?
        relationship,   // what is their relationship to you?
        gift,   // what do you ask God to give that person?
    }

    // load: load from a file, meaning that name is the file name
    public Prayer(String name, Context context, Boolean load) {
        my_context = context;
        if (!load) {
            my_name = name;
            return;
        }
        String ret = load(name);
        update_variables();
    }

    public void set_name(String name, Context context) {
        if (!name.equals(context.getString(R.string.empty_spinner_value))) {
            my_name = name;
        }
    }

    public String get_name() {
        return my_name;
    }

    public String get_file_name() {
        return my_file_name;
    }

    public Map<String, String> get_variables() {
        return my_variables;
    }

    public void refresh_variables() {
        Map<String, String> refreshed_variables = new HashMap<String, String>();
        for (int i = 0; i < my_prayer_parts.size(); i++) {
            PrayerPart curr_prayer = my_prayer_parts.get(i);
            Iterator it = my_variables.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                refreshed_variables.put(pair.getKey().toString(), pair.getValue().toString());
            }
        }
        my_variables =  refreshed_variables;
    }

    public void set_variable(String variable, String value, Context context) {
        if (value.equals(context.getString(R.string.empty_spinner_value))) {
            my_variables.remove(variable);
            return;
        }
        my_variables.put(variable, value);
        if (value.equals(context.getString(R.string.default_spinner_value))) {
            return;
        }
        for (int i = 0; i < my_prayer_parts.size(); i++) {
            my_prayer_parts.get(i).substitute(variable, value);
        }
    }

    private String update_variables() {
        for (int i = 0; i < my_prayer_parts.size(); i++) {
            String my_prayer_part_text = my_prayer_parts.get(i).my_prayer_part_text;
            Log.i("prayer part", my_prayer_part_text);
            int begin_index = my_prayer_part_text.indexOf("{{");
            int end_index = my_prayer_part_text.indexOf("}}");

            if (begin_index == -1 || end_index == -1)
                return "Error: update_variables: No variable found!";

            while (begin_index != -1) {
                String variable = my_prayer_part_text.substring(begin_index + 2, end_index);
                String[] key_value_pair = variable.split(",");

                if (key_value_pair.length != 2)
                    return "Error: update_variables: Key/Value pair is bad!";

                String key = key_value_pair[0];
                String value = key_value_pair[1];

                String key_value[] = key.split("=");
                String value_value[] = value.split("=");

                if (key_value.length != 2)
                    return "Error: update_variables: Value of the key is bad!";
                if (value_value.length != 2)
                    return "Error: update_variables: Value of the value is bad!";

                my_variables.put(key_value[1], value_value[1]);

                begin_index = my_prayer_part_text.indexOf("{{", end_index);
                end_index = my_prayer_part_text.indexOf("}}", begin_index);
            }
        }
        return "";
    }

    public void append_to_prayer(PrayerPart new_prayer) {
        my_prayer_parts.add(new_prayer);
    }

    public void remove_prayer_part(PrayerTemplate.PrayerPartType prayer_part_type) {
        for (int i = 0; i < my_prayer_parts.size(); i++) {
            PrayerPart curr_prayer = my_prayer_parts.get(i);
            if (curr_prayer.my_prayer_part_type == prayer_part_type) {
                my_prayer_parts.remove(i);
                return;
            }
        }
    }

    @Override
    public String toString() {
        String prayer_string_output = "";
        for (int i = 0; i < my_prayer_parts.size(); i++) {
            if (my_prayer_parts.get(i) == null) continue;
            prayer_string_output += my_prayer_parts.get(i).toString() + "\n\n";
        }
        return prayer_string_output;
    }

    public String save(Context context) {
        try {
            String file_name = my_name.replaceAll("\\W+", "");
            file_name += ".ghm";
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(file_name, Context.MODE_PRIVATE));
            outputStreamWriter.write(my_name + "%");
            for (int i = 0; i < my_prayer_parts.size(); i++) {
                PrayerPart prayer_part =  my_prayer_parts.get(i);
                outputStreamWriter.write(prayer_part.my_prayer_part_type + "|" + String.valueOf(prayer_part.current_template_index) + "|" + prayer_part.my_prayer_part_text + "%");
            }
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed " + e.toString());
        }
        return "";
    }

    public String delete(Context context) {
        File dir = context.getFilesDir();
        File files[] = dir.listFiles();
        for (int i=0; i < files.length; i++) {
            File file = files[i];
            if (file.getName().equals(my_file_name)) {
                file.delete();
                return "";
            }
        }
        return "Error: Cannot delete file '"  + my_file_name + "' file not found";
    }

    public String load(String file_name) {
        File dir = my_context.getFilesDir();
        File files[] = dir.listFiles();
        my_file_name = file_name;
        String ret = "";

        try {
            InputStream inputStream = my_context.openFileInput(file_name);

            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();

                String[] array = ret.split("%");
                if (array[0] == null) return "Error: loading prayer has no name!";
                my_name = array[0];
                for (int i = 1; i < array.length; i++) {
                    String line = array[i];
                    String[] data = line.split("\\|",-1);
                    if (data.length != 3) return "Error: data has the wrong length!";
                    String prayer_part_type = data[0];
                    int current_template_index = Integer.parseInt(data[1]);
                    String content = data[2];
                    PrayerPart prayer_part = new PrayerPart(PrayerTemplate.PrayerPartType.valueOf(prayer_part_type), my_context);
                    prayer_part.update_my_prayer_part_text(content);
                    prayer_part.current_template_index = current_template_index;
                    my_prayer_parts.add(prayer_part);
                }
            }
        } catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return "";
    }

    public void sort() {
        ArrayList<PrayerPart> tmp_sorted_prayer_parts = new ArrayList<PrayerPart>();

        for (int i = 0; i < my_prayer_parts.size(); i++) {
            PrayerPart guy = my_prayer_parts.get(i);
            String prayer_part_type = guy.my_prayer_part_type.toString();
            if (prayer_part_type.equals(PrayerTemplate.PrayerPartType.Adoration.toString())) {
                tmp_sorted_prayer_parts.add(guy);
                break;
            }
        }
        for (int i = 0; i < my_prayer_parts.size(); i++) {
            PrayerPart guy = my_prayer_parts.get(i);
            String prayer_part_type = guy.my_prayer_part_type.toString();
            if (prayer_part_type.equals(PrayerTemplate.PrayerPartType.ConfessionOffender.toString())) {
                tmp_sorted_prayer_parts.add(guy);
                break;
            }
        }
        for (int i = 0; i < my_prayer_parts.size(); i++) {
            PrayerPart guy = my_prayer_parts.get(i);
            String prayer_part_type = guy.my_prayer_part_type.toString();
            if (prayer_part_type.equals(PrayerTemplate.PrayerPartType.ConfessionSin.toString())) {
                tmp_sorted_prayer_parts.add(guy);
                break;
            }
        }
        for (int i = 0; i < my_prayer_parts.size(); i++) {
            PrayerPart guy = my_prayer_parts.get(i);
            String prayer_part_type = guy.my_prayer_part_type.toString();
            if (prayer_part_type.equals(PrayerTemplate.PrayerPartType.Thanksgiving.toString())) {
                tmp_sorted_prayer_parts.add(guy);
                break;
            }
        }
        for (int i = 0; i < my_prayer_parts.size(); i++) {
            PrayerPart guy = my_prayer_parts.get(i);
            String prayer_part_type = guy.my_prayer_part_type.toString();
            if (prayer_part_type.equals(PrayerTemplate.PrayerPartType.Supplication.toString())) {
                tmp_sorted_prayer_parts.add(guy);
                break;
            }
        }
        my_prayer_parts = tmp_sorted_prayer_parts;
    }
}
