package com.ireifejapps.godhelpme;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GodHelpMe extends AppCompatActivity {
    public enum Page {
        main,   // the main page that lists all saved prayers
        edit,   // the edit page to answer questions and edit a prayer
        preview,    // the preview page to see a preview of the prayer you're editing
        view    // the page to view your prayer without being able to edit
    }

    final Context context = this;   // context of entire application
    String[] wallpaper_names = {"heaven", "heaven2", "heaven3", "heaven4", "heaven5", "heaven6", "heaven7", "heaven8"};
    int curr_wallpaper = 0;
    Page curr_page = Page.main;
    ArrayList<Spinner> answer_spinners = new ArrayList<Spinner>();
    Map<String, PrayerTemplate.PrayerPartType> var_to_temp = null;
    Set<String> default_values = null;

    // current prayer information being modified
    Prayer my_current_prayer = new Prayer("", context, false);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_god_help_me);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // string resources
        String adoration[] = getResources().getStringArray(R.array.adoration);
        String confession_offender[] = getResources().getStringArray(R.array.confession_offender);
        String confession_sin[] = getResources().getStringArray(R.array.confession_sin);
        String thanksgiving[] = getResources().getStringArray(R.array.thanksgiving);
        String supplication[] = getResources().getStringArray(R.array.supplication);

        PrayerTemplates.init(adoration, confession_offender, confession_sin, thanksgiving, supplication);
        var_to_temp = PrayerTemplates.get_var_to_temp_map();
        default_values = PrayerTemplates.get_default_values();

        display_saved_prayer_list();

        final FloatingActionButton back = (FloatingActionButton) findViewById(R.id.back);
        final FloatingActionButton save = (FloatingActionButton) findViewById(R.id.save);
        final FloatingActionButton add = (FloatingActionButton) findViewById(R.id.add);
        final FloatingActionButton preview = (FloatingActionButton) findViewById(R.id.preview);
        final FloatingActionButton wallpaper = (FloatingActionButton) findViewById(R.id.wallpaper);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // go back to main
                if (curr_page == Page.edit || curr_page == Page.view) {
                    curr_page = Page.main;

                    add.setVisibility(View.VISIBLE);
                    save.setVisibility(View.GONE);
                    back.setVisibility(View.GONE);
                    preview.setVisibility(View.GONE);

                    TableLayout saved_prayer_list = (TableLayout) findViewById(R.id.saved_prayer_list);
                    TableLayout prayer_questions = (TableLayout) findViewById(R.id.prayer_questions);
                    LinearLayout prayer_parts = (LinearLayout) findViewById(R.id.prayer_parts);

                    saved_prayer_list.setVisibility(View.VISIBLE);
                    prayer_questions.setVisibility(View.GONE);
                    prayer_parts.removeAllViews();

                    display_saved_prayer_list();
                    return;
                }
                // go back to edit
                if (curr_page == Page.preview) {
                    curr_page = Page.edit;

                    add.setVisibility(View.GONE);
                    save.setVisibility(View.GONE);
                    back.setVisibility(View.VISIBLE);
                    preview.setVisibility(View.VISIBLE);

                    TableLayout prayer_questions = (TableLayout) findViewById(R.id.prayer_questions);
                    LinearLayout prayer_parts = (LinearLayout) findViewById(R.id.prayer_parts);

                    prayer_questions.setVisibility(View.VISIBLE);
                    prayer_parts.removeAllViews();

                    return;
                }
            }
        });

        // save prayer
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (my_current_prayer.get_name().equals("")) {
                    Snackbar.make(view, "Oops! This prayer needs a name first.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    return;
                }
                my_current_prayer.save(getApplicationContext());
                Snackbar.make(view, "Prayer \"" + my_current_prayer.get_name() + "\" saved", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });

        // create new prayer
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // clear my current prayer details
                my_current_prayer = new Prayer("", context, false);

                curr_page = Page.edit;
                add.setVisibility(View.GONE);
                save.setVisibility(View.GONE);
                back.setVisibility(View.VISIBLE);
                preview.setVisibility(View.VISIBLE);
                TableLayout my_table = (TableLayout) findViewById(R.id.saved_prayer_list);
                my_table.setVisibility(View.GONE);
                display_questions(null);
            }
        });

        // change wallpaper
        wallpaper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CoordinatorLayout main = (CoordinatorLayout) findViewById(R.id.ireifejhello);
                curr_wallpaper++;
                if (curr_wallpaper >= wallpaper_names.length) curr_wallpaper = 0;
                int drawableResourceId = getResources().getIdentifier(wallpaper_names[curr_wallpaper], "drawable", getPackageName());
                main.setBackgroundResource(drawableResourceId);
            }
        });

        // preview prayer
        preview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                curr_page = Page.preview;
                preview.setVisibility(View.GONE);
                save.setVisibility(View.VISIBLE);
                generate_prayer(false);
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK ) {
            final FloatingActionButton back = (FloatingActionButton) findViewById(R.id.back);
            back.callOnClick();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    public void display_saved_prayer_list() {
        File dir = context.getFilesDir();
        File files[] = dir.listFiles();
        ArrayList<Prayer> prayers = new ArrayList<Prayer>();
        Prayer curr_prayer = null;

        String ret = "";
        for (int i=0; i < files.length; i++) {
            File file = files[i];
            if (file.getName().endsWith(".ghm")) {
                curr_prayer = new Prayer(file.getName(), context, true);
                prayers.add(curr_prayer);
            }
        }

        TableLayout saved_prayer_list = (TableLayout) findViewById(R.id.saved_prayer_list);
        saved_prayer_list.removeAllViews();

        for (int i = 0; i < prayers.size(); i++) {
            curr_prayer = prayers.get(i);
            TableRow row = new TableRow(this);
            TableRow.LayoutParams layout_params = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);

            row.setLayoutParams(layout_params);
            row.setMinimumHeight(200);

            final TextView prayer_name = new TextView(this);
            TextView edit = new TextView(this);
            TextView delete = new TextView(this);

            prayer_name.setBackgroundResource(R.drawable.cell_shape);
            prayer_name.setText(curr_prayer.get_name());
            prayer_name.setTag(curr_prayer.get_file_name());
            prayer_name.setClickable(true);
            prayer_name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    curr_page = Page.view;
                    final FloatingActionButton add = (FloatingActionButton) findViewById(R.id.add);
                    add.setVisibility(View.GONE);
                    my_current_prayer = new Prayer(view.getTag().toString(), context, true);
                    generate_prayer(true);
                }
            });

            edit.setBackgroundResource(android.R.drawable.ic_menu_edit);
            edit.setTag(curr_prayer.get_file_name());
            edit.setClickable(true);
            edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    curr_page = Page.edit;

                    TableLayout saved_prayer_list = (TableLayout) findViewById(R.id.saved_prayer_list);
                    saved_prayer_list.removeAllViews();

                    final FloatingActionButton add = (FloatingActionButton) findViewById(R.id.add);
                    final FloatingActionButton preview = (FloatingActionButton) findViewById(R.id.preview);
                    final FloatingActionButton back = (FloatingActionButton) findViewById(R.id.back);

                    add.setVisibility(View.GONE);
                    preview.setVisibility(View.VISIBLE);
                    back.setVisibility(View.VISIBLE);

                    Prayer prayer_to_load = new Prayer(view.getTag().toString(), context, true);
                    display_questions(prayer_to_load);
                }
            });

            delete.setBackgroundResource(android.R.drawable.ic_delete);
            delete.setTag(curr_prayer.get_file_name());
            delete.setClickable(true);
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final String file_name = String.valueOf(view.getTag());
                    final EditText input = new EditText(context);
                    new AlertDialog.Builder(context)
                            .setMessage("Are you sure you wish to delete '" + file_name + "': this cannot be undone!")
                            .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    Prayer prayer_to_delete = new Prayer(file_name, context, true);
                                    String message = prayer_to_delete.delete(context);
                                    if (message != "") {
                                        Log.i("Error", "error upon delete");
                                        return;
                                    }
                                    display_saved_prayer_list();
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                }
                            }).show();
                }
            });

            row.addView(prayer_name);
            row.addView(edit);
            row.addView(delete);

            saved_prayer_list.addView(row, 0);
        }
    }

    public void display_questions(Prayer prayer_to_load) {
        TableLayout my_table = (TableLayout) findViewById(R.id.prayer_questions);
        my_table.removeAllViews();
        my_table.setVisibility(View.VISIBLE);

        answer_spinners = new ArrayList<Spinner>();
        String answer_choices[];

        // supplication - who are you praying for? what is their relationship to you? what do you ask god to give that person?
        answer_choices = getResources().getStringArray(R.array.gifts);
        get_input("What do you ask God \n to give this person?", Prayer.VariableNames.gift.toString(), answer_choices);

        answer_choices = getResources().getStringArray(R.array.relationship);
        get_input("What is this person's \n relationship to you?", Prayer.VariableNames.relationship.toString(), answer_choices);

        answer_choices = getResources().getStringArray(R.array.help);
        get_input("Who are you praying for?", Prayer.VariableNames.help.toString(), answer_choices);

        // thanksgiving - what/who are you grateful for?
        answer_choices = getResources().getStringArray(R.array.grateful);
        get_input("Who/what are you \n grateful for?", Prayer.VariableNames.appreciate.toString(), answer_choices);

        // confession - what are your sins? who offended you?
        answer_choices = getResources().getStringArray(R.array.sins);
        get_input("What are your sins?", Prayer.VariableNames.sin.toString(), answer_choices);

        answer_choices = getResources().getStringArray(R.array.grateful);
        get_input("Who offended you?", Prayer.VariableNames.offender.toString(), null);

        // adoration - who are you praying to? why do you love him?
        answer_choices = getResources().getStringArray(R.array.love);
        get_input("Why do you love God?", Prayer.VariableNames.love.toString(), answer_choices);

        answer_choices = getResources().getStringArray(R.array.gods);
        get_input("How do you refer to God?", Prayer.VariableNames.god.toString(), answer_choices);

        get_input("What is the name of your prayer?", "prayer_name", null);

        // this is a new prayer, so don't set answers
        if (prayer_to_load == null) return;

        Map<String, String> variables = prayer_to_load.get_variables();
        Boolean exists = false;
        ArrayList<String> list = new ArrayList<String>();
        String prayer_name = prayer_to_load.get_name();

        // you're loading an existing prayer for edit, so set the answers
        for (int i = 0; i < answer_spinners.size(); i++) {
            Spinner spinner = answer_spinners.get(i);
            String key = spinner.getTag().toString();
            String value = variables.get(key);
            if (key.equals("prayer_name")) {
                list.add(getString(R.string.empty_spinner_value));
                list.add(getString(R.string.custom_spinner_value));
                list.add(getString(R.string.default_spinner_value));
                list.add(prayer_name);
                ArrayAdapter<String> adp = new ArrayAdapter<String> (this, android.R.layout.simple_list_item_1, list);
                adp.setDropDownViewResource(android.R.layout.simple_list_item_1);
                spinner.setAdapter(adp);
                spinner.setSelection(list.size() - 1);
                continue;
            }
            if (value == null) continue;
            Adapter adapter = spinner.getAdapter();
            for (int j = 0; j < adapter.getCount(); j++) {
                String curr_answer = adapter.getItem(j).toString();
                list.add(curr_answer);
                if (default_values.contains(value)) {
                    spinner.setSelection(2);
                    exists = true;
                    break;
                }
                if (curr_answer.equals(value)) {
                    spinner.setSelection(j);
                    exists = true;
                    break;
                }
            }
            // add manually and set it
            if (!exists) {
                list.add(value);
                ArrayAdapter<String> adp = new ArrayAdapter<String> (this, android.R.layout.simple_list_item_1, list);
                adp.setDropDownViewResource(android.R.layout.simple_list_item_1);
                spinner.setAdapter(adp);
                spinner.setSelection(list.size() - 1);
            }
            exists = false;
            list = new ArrayList<String>();
        }
    }

    public void get_input(final String question_text, String key, String[] answer_choices) {
        TableLayout table_layout = (TableLayout) findViewById(R.id.prayer_questions);
        TableRow row = new TableRow(this);
        TableRow.LayoutParams layout_params = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
        row.setLayoutParams(layout_params);
        row.setMinimumHeight(200);
        TextView question = new TextView(this);
        question.setBackgroundResource(R.drawable.rounded_corner);
        final Spinner answer = new Spinner(this);
        answer.setBackgroundResource(R.drawable.rounded_corner);
        answer.setTag(key);
        question.setText(question_text);
        final List<String> answers = new ArrayList<String>();

        answers.add(getString(R.string.empty_spinner_value));
        answers.add(getString(R.string.custom_spinner_value));
        answers.add(getString(R.string.default_spinner_value));

        if (answer_choices != null) {
            for (int i = 0; i < answer_choices.length; i++) {
                answers.add(answer_choices[i]);
            }
        }

        answer_spinners.add(answer);

        ArrayAdapter<String> adp = new ArrayAdapter<String> (this, android.R.layout.simple_list_item_1, answers);
        adp.setDropDownViewResource(android.R.layout.simple_list_item_1);
        answer.setAdapter(adp);

        final String message = question_text;
        final String final_key = key;

        answer.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // custom answer selected
                if (position == 1) {
                    // Set an EditText view to get user input
                    final EditText input = new EditText(context);
                    show_keyboard();
                    new AlertDialog.Builder(context)
                            .setMessage(message)
                            .setView(input)
                            .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    Editable editable = input.getText();
                                    answers.add(editable.toString());
                                    answer.setSelection(answers.size() - 1);
                                    if (final_key.equals("prayer_name")) {
                                        my_current_prayer.set_name(editable.toString(), context);
                                    } else {
                                        my_current_prayer.set_variable(final_key, editable.toString(), context);
                                    }
                                    hide_keyboard();
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    hide_keyboard();
                                }
                            }).show();
                } else {
                    // pre-loaded answer selected
                    if (final_key.equals(Prayer.VariableNames.god.toString())) {

                    }
                    if (answer.getSelectedItem().toString().equals(getString(R.string.empty_spinner_value))) {
                        PrayerTemplate.PrayerPartType prayer_part_type = var_to_temp.get(final_key);
                        my_current_prayer.remove_prayer_part(prayer_part_type);
                    }
                    my_current_prayer.set_variable(final_key, answer.getSelectedItem().toString(), context);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // sometimes you need nothing here
            }
        });

        row.addView(question);
        row.addView(answer);
        table_layout.addView(row, 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_god_help_me, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void show_keyboard() {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
    }

    public void hide_keyboard() {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

    // generates the prayer defined in my_current_prayer
    // read_only - cannot refresh the template
    public void generate_prayer(final Boolean read_only) {
        Map<String, String> my_variables = my_current_prayer.get_variables();
        Iterator it = my_variables.entrySet().iterator();

        PrayerPart prayer_part = null;
        String variable_god = my_variables.get(Prayer.VariableNames.god.toString());
        if (variable_god == null) variable_god = getString(R.string.default_spinner_value);

        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            String key = pair.getKey().toString();
            String value = pair.getValue().toString();

            if (key.equals("prayer_name")) continue;
            if (key.equals(Prayer.VariableNames.god.toString())) continue;
            if (key.equals(Prayer.VariableNames.relationship.toString())) continue; // part of supplication (child of help)
            if (key.equals(Prayer.VariableNames.gift.toString())) continue; // part of supplication (child of help)
            if (key.equals(getString(R.string.empty_spinner_value))) continue; // don't add prayer part

            if (value == getString(R.string.default_spinner_value)) {
                prayer_part = new PrayerPart(var_to_temp.get(key), context);
                // deal with God, which is global across all prayer parts
                prayer_part.substitute(Prayer.VariableNames.god, variable_god);
            } else {
                prayer_part = new PrayerPart(var_to_temp.get(key), context);
                // deal with God, which is global across all prayer parts
                prayer_part.substitute(Prayer.VariableNames.god, variable_god);

                // deals with supplication, which is dependant on gift and relationship
                if (key == Prayer.VariableNames.help.toString()) {
                    String gift = my_variables.get(Prayer.VariableNames.gift.toString());
                    String relationship = my_variables.get(Prayer.VariableNames.relationship.toString());
                    prayer_part.substitute(Prayer.VariableNames.gift, gift);
                    prayer_part.substitute(Prayer.VariableNames.relationship, relationship);
                }
                prayer_part.substitute(key, value);
            }

            my_current_prayer.append_to_prayer(prayer_part);
        }

        TableLayout saved_prayer_list_table = (TableLayout) findViewById(R.id.saved_prayer_list);
        saved_prayer_list_table.setVisibility(View.GONE);
        final FloatingActionButton back = (FloatingActionButton) findViewById(R.id.back);
        back.setVisibility(View.VISIBLE);
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.prayer_parts);
        linearLayout.removeAllViews();

        // hide the prayer questions
        TableLayout my_table = (TableLayout) findViewById(R.id.prayer_questions);
        my_table.setVisibility(View.GONE);

        // add prayer name
        TextView text_view_prayer_name = new TextView(this);
        text_view_prayer_name.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        text_view_prayer_name.setText(my_current_prayer.get_name());
        text_view_prayer_name.setBackgroundColor(Color.BLUE);
        text_view_prayer_name.setPadding(20, 150, 20, 20);// in pixels (left, top, right, bottom)
        linearLayout.addView(text_view_prayer_name);

        my_current_prayer.sort();
        for (int i = 0; i < my_current_prayer.my_prayer_parts.size(); i++) {
            PrayerPart current_prayer_part =  my_current_prayer.my_prayer_parts.get(i);
            if (!read_only) {
                // add refresh button with template count label
                TextView template_count = new TextView(this);
                template_count.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
                template_count.setBackgroundResource(R.drawable.rounded_corner);
                template_count.setPadding(20, 20, 20, 20);// in pixels (left, top, right, bottom)
                template_count.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_refresh, 0, 0, 0);
                template_count.setText(String.valueOf(current_prayer_part.current_template_index + 1) + "/" + PrayerTemplates.get_template_size(current_prayer_part.my_prayer_part_type));
                template_count.setTag(String.valueOf(i));
                linearLayout.addView(template_count);

                template_count.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // change prayer part template and update text view
                        int current_prayer_part_index = Integer.parseInt(v.getTag().toString());
                        PrayerPart prayer_part = my_current_prayer.my_prayer_parts.get(current_prayer_part_index);
                        String template_count = prayer_part.change_template(my_current_prayer.get_variables());
                        Spanned heading = (read_only) ? Html.fromHtml("") : Html.fromHtml(prayer_part.my_prayer_part_type.toString() + "<br>");

                        // update display
                        ViewGroup view_group = (ViewGroup) findViewById(R.id.prayer_parts);

                        // update prayer part
                        String prayer_part_text_tag = "refresh" + v.getTag().toString();
                        TextView text_view_prayer_part = (TextView) view_group.findViewWithTag(prayer_part_text_tag);
                        text_view_prayer_part.setText(TextUtils.concat(heading, prayer_part.toHTML()));

                        // update template count
                        String template_count_tag = v.getTag().toString();
                        TextView template_count_text_view = (TextView) view_group.findViewWithTag(template_count_tag);
                        template_count_text_view.setText(template_count);
                    }
                });
            }

            // add prayer part
            Spanned heading = (read_only) ? Html.fromHtml("") : Html.fromHtml(my_current_prayer.my_prayer_parts.get(i).my_prayer_part_type.toString() + "<br>");

            TextView text_view_prayer_part = new TextView(this);
            LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(50, 50, 100, 100); // (left, top, right, bottom)
            text_view_prayer_part.setLayoutParams(layoutParams);
            text_view_prayer_part.setText(TextUtils.concat(heading, my_current_prayer.my_prayer_parts.get(i).toHTML()));
            text_view_prayer_part.setBackgroundResource(R.drawable.rounded_corner);
            text_view_prayer_part.setPadding(20, 20, 20, 20);// in pixels (left, top, right, bottom)
            text_view_prayer_part.setTag("refresh" + String.valueOf(i));
            linearLayout.addView(text_view_prayer_part);
        }

        // add "Amen"
        TextView text_view_amen = new TextView(this);
        LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.LEFT;
        layoutParams.setMargins(50, 100, 100, 300); // (left, top, right, bottom)
        text_view_amen.setLayoutParams(layoutParams);
        text_view_amen.setText("Amen.");
        text_view_amen.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        text_view_amen.setBackgroundResource(R.drawable.rounded_corner);
        linearLayout.addView(text_view_amen);
    }
}
