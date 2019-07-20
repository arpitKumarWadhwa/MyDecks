package com.kumar.ak.arpit.mydecks;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

public class DeckListContainer extends AppCompatActivity {
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deck_list_container);
        toolbar = (Toolbar) findViewById(R.id.deck_list_toolbar);
    }
}
