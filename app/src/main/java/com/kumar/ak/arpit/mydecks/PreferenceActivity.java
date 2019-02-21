package com.kumar.ak.arpit.mydecks;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;

public class PreferenceActivity extends AppCompatActivity {
    int locale;
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;


    RadioGroup languageGroup, themeGroup;
    TextView languageExpand, themeExpand;
    RadioButton checkedLanguage;
    RadioButton checkedTheme;
    ScrollView scrollView;

    boolean isLanguageGroupExpanded = false;
    boolean isThemeGroupExpanded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPref = this.getSharedPreferences("DeckBoxPreferences", Context.MODE_PRIVATE);
        locale = sharedPref.getInt("locale", Cards.LOCALE_EN_US);

        editor = sharedPref.edit();

        ActionBar toolbar = getSupportActionBar();
        toolbar.setTitle(getString(R.string.action_language));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preference);

        languageGroup = findViewById(R.id.language_group);

        switch (locale){
            case Cards.LOCALE_EN_US:
                languageGroup.check(R.id.en_US);
                break;
            case Cards.LOCALE_KO_KR:
                languageGroup.check(R.id.ko_KR);
                break;
            case Cards.LOCALE_RU_RU:
                languageGroup.check(R.id.ru_RU);
                break;
            case Cards.LOCALE_ZH_CN:
                languageGroup.check(R.id.zh_CN);
                break;
                default:
                    languageGroup.check(R.id.en_US);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_preference, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_save_preference){
            int checkedId = languageGroup.getCheckedRadioButtonId();
            switch (checkedId) {
                case R.id.en_US:
                    editor.putInt("locale", Cards.LOCALE_EN_US);
                    break;
                case R.id.ko_KR:
                    editor.putInt("locale", Cards.LOCALE_KO_KR);
                    break;
                case R.id.zh_CN:
                    editor.putInt("locale", Cards.LOCALE_ZH_CN);
                    break;
                case R.id.ru_RU:
                    editor.putInt("locale", Cards.LOCALE_RU_RU);
                    break;
                default:
                    editor.putInt("locale", Cards.LOCALE_EN_US);
            }
            editor.commit();

            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
