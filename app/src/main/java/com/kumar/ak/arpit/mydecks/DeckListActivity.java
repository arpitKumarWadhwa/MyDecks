package com.kumar.ak.arpit.mydecks;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.kumar.ak.arpit.mydecks.data.DecksContract;
import com.kumar.ak.arpit.mydecks.data.DecksDbHelper;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;


public class DeckListActivity extends AppCompatActivity {
    int locale;
    int selectedLayout; //0 for layout1, 1 for alternate layout, 2 for layout 2
    SharedPreferences sharedPref;

    private int deckId; //Id of deck as in database
    String deckCode;
    String deckName;
    String playableClass;
    DeckDecoder deckDecoder = new DeckDecoder();
    QueryUtils httpHelper = new QueryUtils();
    ListView deckListListView;
    DeckListAdapter adapter;

    String folderName = "";

    ArrayList<Cards> deckList = new ArrayList<Cards>();

    //Create Deck List Manager
    DeckListManager dlm = new DeckListManager();
    DecksDbHelper mDbHelper = new DecksDbHelper(this);

    Toolbar toolbar;

    String deletedDeckString;

    TextView toolbarTitle;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                deletedDeckString = data.getStringExtra("deletedDeckString");
                if(!deletedDeckString.equals("returningFromFolder")) {
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("deletedDeckString", deletedDeckString);
                    setResult(Activity.RESULT_OK, returnIntent);
                }

                finish();
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                int comingFromAlternateLayout = sharedPref.getInt("returningFromAlternate", 0);
                if (comingFromAlternateLayout == 1) {
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putInt("returningFromAlternate", 0);
                    editor.commit();
                    finish();
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPref = getSharedPreferences("DeckBoxPreferences", Context.MODE_PRIVATE);
        selectedLayout = sharedPref.getInt("deckLayout", 0);
        locale = sharedPref.getInt("locale", Cards.LOCALE_EN_US);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("returningFromAlternate", 0);
        editor.commit();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deck_list);
        toolbar = (Toolbar) findViewById(R.id.deck_list_toolbar);

        Intent intent = getIntent();
        deckId = intent.getIntExtra("deckId", -1);
        deckCode = intent.getStringExtra("deckString");
        deckName = intent.getStringExtra("deckName");
        playableClass = intent.getStringExtra("playableClass");

        if (intent.hasExtra("folderName")) {
            folderName = intent.getStringExtra("folderName");
        }

        toolbar.setTitle("");
        toolbarTitle = findViewById(R.id.deck_list_title);
        toolbarTitle.setText(deckName);
        setSupportActionBar(toolbar);


        toolbarTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final View v = DeckListActivity.this.getLayoutInflater().inflate(R.layout.edit_deck_name_dialog, null);

                final EditText editText = v.findViewById(R.id.folder_name);
                final String formattedDeckName = deckName.replaceAll("\n", "");
                editText.setText(formattedDeckName);
                editText.setSelection(0, editText.getText().toString().length());

                editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        editText.post(new Runnable() {
                            @Override
                            public void run() {
                                InputMethodManager imm = (InputMethodManager) DeckListActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
                            }
                        });
                    }
                });
                editText.requestFocus();

                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(DeckListActivity.this);
                builder.setView(v);
                builder.setPositiveButton(getString(R.string.dialog_confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String newDeckName = editText.getText().toString();
                        updateName(newDeckName);  //Updates the deck name in the database

                        String formattedNewDeckname = newDeckName;

                        if(formattedNewDeckname.length() > 50){
                            formattedNewDeckname = formattedNewDeckname.substring(0,50);
                            formattedNewDeckname += "...";
                        }
                        String tempName = formattedNewDeckname;
                        if(tempName.length() > 25){
                            formattedNewDeckname = tempName.substring(0,26);
                            formattedNewDeckname += "\n";
                            formattedNewDeckname += tempName.substring(26);
                        }

                        toolbarTitle.setText(formattedNewDeckname);
                    }
                });
                builder.setNegativeButton(getString(R.string.dialog_cancel), null);
                builder.setCancelable(false);

                builder.create();
                builder.show();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setActionBarColor(playableClass);
        }

        dlm.setDeckString(deckCode);
        dlm.setDeckName(deckName);
        dlm.setId(deckId);

        try {
            ArrayList<ArrayList<Integer>> dbfIdList;
            dbfIdList = deckDecoder.decode(deckCode);

            //Find all 1-quantity cards by their dbfIds
            ArrayList<Integer> dbf1 = dbfIdList.get(0);
            //Log.e("1-quantity cards: ", String.valueOf(dbf1.size()));
            for (int i = 0; i < dbf1.size(); i++) {
                Cards tempCard = new Cards();
                tempCard = Cards.getCardByDbfid(DeckListActivity.this, dbf1.get(i), locale);
                tempCard.setQuantity(1);

                String imageUrl = "https://art.hearthstonejson.com/v1/tiles/" + tempCard.getCardId() + ".jpg";
                URL url = httpHelper.createUrl(imageUrl);
                tempCard.setCardTileUrl(url);

                deckList.add(tempCard);
            }

            //Find all 2-quantity cards by their dbfIds
            ArrayList<Integer> dbf2 = dbfIdList.get(1);
            //Log.e("2-quantity cards: ", String.valueOf(dbf2.size()));
            for (int i = 0; i < dbf2.size(); i++) {
                Cards tempCard = new Cards();
                tempCard = Cards.getCardByDbfid(DeckListActivity.this, dbf2.get(i), locale);
                tempCard.setQuantity(2);

                String imageUrl = "https://art.hearthstonejson.com/v1/tiles/" + tempCard.getCardId() + ".jpg";
                URL url = httpHelper.createUrl(imageUrl);
                tempCard.setCardTileUrl(url);

                deckList.add(tempCard);
            }
        } catch (IOException e) {
            //Log.e("Deck Decoding Error: ", e.toString());
        }

        //Sort deck by mana cost
        try {
            deckList = dlm.sortByMana(deckList);
            deckList = dlm.sortLexicographically(deckList);
        } catch (Exception e) {
            Toast.makeText(this, "Malformed Deck", Toast.LENGTH_SHORT).show();
        }

        deckListListView = findViewById(R.id.deck_list);
        if (selectedLayout == 0)
            adapter = new DeckListAdapter(DeckListActivity.this, R.layout.layout1, deckList);
        else if (selectedLayout == 1) {
            Intent i = new Intent(DeckListActivity.this, AlternateDeckListActivity.class);
            i.putExtra("deckId", deckId);
            i.putExtra("deckString", deckCode);
            i.putExtra("deckName", deckName);
            i.putExtra("playableClass", playableClass);
            if(folderName != null && !TextUtils.isEmpty(folderName)){
                i.putExtra("folderName", folderName);
            }
            startActivityForResult(i, 1);
        } else if (selectedLayout == 2)
            adapter = new DeckListAdapter(DeckListActivity.this, R.layout.layout2, deckList);
        deckListListView.setAdapter(adapter);

        deckListListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String localeString;
                switch (locale) {
                    case Cards.LOCALE_EN_US:
                        localeString = "enUS";
                        break;
                    case Cards.LOCALE_KO_KR:
                        localeString = "koKR";
                        break;
                    case Cards.LOCALE_ZH_CN:
                        localeString = "zhCN";
                        break;
                    case Cards.LOCALE_RU_RU:
                        localeString = "ruRU";
                        break;
                    default:
                        localeString = "enUS";
                }
                String cardId = ((Cards) parent.getItemAtPosition(position)).getCardId();
                String cardImageUrl = "https://art.hearthstonejson.com/v1/render/latest/" + localeString + "/256x/" + cardId + ".png";
                Intent i = new Intent(DeckListActivity.this, ImageDialog.class);
                i.putExtra("imageUrl", cardImageUrl);
                startActivityForResult(i, 1);
            }
        });

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_favorites);
        int isFavorite = getFavoriteState();
        switch (isFavorite) {
            case DecksContract.DecksEntry.NOT_FAVORITE:
                item.setTitle(getString(R.string.action_add_to_favorites));
                break;
            case DecksContract.DecksEntry.FAVORITE:
                item.setTitle(getString(R.string.action_remove_from_favorites));
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_deck_list, menu);

        MenuItem deleteItem = menu.findItem(R.id.action_delete);
        if (folderName != null && !TextUtils.isEmpty(folderName)) {
            deleteItem.setTitle("Remove from folder"); //TODO: Put this in strings.xml and translate
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //Delete Deck
        if (id == R.id.action_delete) {
            if(folderName != null && !TextUtils.isEmpty(folderName)){

                //Remove the folder from the deck
                String selection = DecksContract.DecksEntry._ID + "=?";
                String[] selectionArgs = new String[]{String.valueOf(deckId)};

                SQLiteDatabase readDatabase = mDbHelper.getReadableDatabase();

                Cursor c = readDatabase.query(
                        DecksContract.DecksEntry.TABLE_NAME,
                        null,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        null
                );
                c.moveToFirst();

                String folderNamesString = c.getString(c.getColumnIndex(DecksContract.DecksEntry.FOLDER_NAME));
                c.close(); readDatabase.close();
                ArrayList<String> foldersList = new ArrayList<>();
                foldersList.addAll(Arrays.asList(folderNamesString.split("`")));

                if(foldersList.contains(folderName)){
                    foldersList.remove(folderName);
                }

                StringBuilder newFolderStringBuilder = new StringBuilder();
                String newFolderString = null;
                for(int i=0; i<foldersList.size(); i++){
                    newFolderStringBuilder.append(foldersList.get(i));
                    newFolderStringBuilder.append("`");
                }
                if(foldersList.size() > 0){
                    newFolderString = newFolderStringBuilder.toString();
                }

                //Update the database
                SQLiteDatabase writeDatabase = mDbHelper.getWritableDatabase();

                ContentValues cv = new ContentValues();
                cv.put(DecksContract.DecksEntry.FOLDER_NAME, newFolderString);
                writeDatabase.update(
                        DecksContract.DecksEntry.TABLE_NAME,
                        cv,
                        selection,
                        selectionArgs
                );
                writeDatabase.close();

                finish();

            }
            else {
                SQLiteDatabase db = mDbHelper.getWritableDatabase();

                String selection = DecksContract.DecksEntry._ID + "=?";
                String selectionArgs[] = new String[]{String.valueOf(deckId)};

                deletedDeckString = getAnnotatedDeckString(deckCode);

                int r = db.delete(
                        DecksContract.DecksEntry.TABLE_NAME,
                        selection,
                        selectionArgs
                );

                db.close();

                if (selectedLayout == 2) {
                    Intent i = new Intent(DeckListActivity.this, DeckBoxActivity.class);
                    i.putExtra("deletedDeckString", deletedDeckString);
                    startActivity(i);

                    finish();
                }

                Intent returnIntent = new Intent();
                returnIntent.putExtra("deletedDeckString", deletedDeckString);
                setResult(Activity.RESULT_OK, returnIntent);

                finish();
            }
        } else if (id == R.id.action_copy) {

            try {
                ClipboardManager myClipboard;
                myClipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

                ClipData myClip;
                String text = dlm.getDeckString();
                String annotatedDeckString = getAnnotatedDeckString(text);
                myClip = ClipData.newPlainText("text", annotatedDeckString);
                myClipboard.setPrimaryClip(myClip);

                Toast.makeText(this, getString(R.string.deck_copy_success), Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, getString(R.string.deck_copy_failure), Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.action_share_deck) {
            shareDeck();
        } else if (id == R.id.action_switch_layout) {
            selectedLayout = (selectedLayout + 1) % 3;
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt("deckLayout", selectedLayout);
            editor.commit();

            if (selectedLayout == 1) {
                Intent i = new Intent(DeckListActivity.this, AlternateDeckListActivity.class);
                i.putExtra("deckId", deckId);
                i.putExtra("deckString", deckCode);
                i.putExtra("deckName", deckName);
                i.putExtra("playableClass", playableClass);
                if(folderName != null && !TextUtils.isEmpty(folderName)){
                    i.putExtra("folderName", folderName);
                }
                startActivityForResult(i, 1);
            } else if (selectedLayout == 0)
                adapter = new DeckListAdapter(DeckListActivity.this, R.layout.layout1, deckList);
            else if (selectedLayout == 2)
                adapter = new DeckListAdapter(DeckListActivity.this, R.layout.layout2, deckList);
            deckListListView.setAdapter(adapter);
        } else if (id == R.id.action_favorites) {
            int isFavorite = getFavoriteState();
            switch (isFavorite) {
                case DecksContract.DecksEntry.NOT_FAVORITE:
                    addToFavorites();
                    Toast.makeText(this, getString(R.string.added_to_favorites), Toast.LENGTH_SHORT).show();
                    break;
                case DecksContract.DecksEntry.FAVORITE:
                    removeFromFavorites();
                    Toast.makeText(this, getString(R.string.removed_from_favorites), Toast.LENGTH_SHORT).show();
            }
        }
        else if(id == R.id.action_edit_deck_name){
            final View v = DeckListActivity.this.getLayoutInflater().inflate(R.layout.edit_deck_name_dialog, null);

            final EditText editText = v.findViewById(R.id.folder_name);
            String formattedDeckName = deckName.replaceAll("\n", "");
            editText.setText(formattedDeckName);
            editText.setSelection(0, editText.getText().toString().length());

            editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    editText.post(new Runnable() {
                        @Override
                        public void run() {
                            InputMethodManager imm = (InputMethodManager) DeckListActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
                        }
                    });
                }
            });
            editText.requestFocus();

            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(DeckListActivity.this);
            builder.setView(v);
            builder.setPositiveButton(getString(R.string.dialog_confirm), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    String newDeckName = editText.getText().toString();
                    updateName(newDeckName);  //Updates the deck name in the database

                    String formattedNewDeckname = newDeckName;

                    if(formattedNewDeckname.length() > 50){
                        formattedNewDeckname = formattedNewDeckname.substring(0,50);
                        formattedNewDeckname += "...";
                    }
                    String tempName = formattedNewDeckname;
                    if(tempName.length() > 25){
                        formattedNewDeckname = tempName.substring(0,26);
                        formattedNewDeckname += "\n";
                        formattedNewDeckname += tempName.substring(26);
                    }

                    toolbarTitle.setText(formattedNewDeckname);
                }
            });
            builder.setNegativeButton(getString(R.string.dialog_cancel), null);
            builder.setCancelable(false);

            builder.create();
            builder.show();
        }
        return super.onOptionsItemSelected(item);
    }

    public void addToFavorites() {
        DecksDbHelper mDbHelper = new DecksDbHelper(this);
        SQLiteDatabase favs = mDbHelper.getWritableDatabase();

        String selection = DecksContract.DecksEntry._ID + "=?";
        String selectionArgs[] = new String[]{String.valueOf(deckId)};

        ContentValues cv = new ContentValues();
        cv.put(DecksContract.DecksEntry.IS_FAVORITE, 1);

        favs.update(
                DecksContract.DecksEntry.TABLE_NAME,
                cv,
                selection,
                selectionArgs
        );

        favs.close();
    }

    public void removeFromFavorites() {

        DecksDbHelper mDbHelper = new DecksDbHelper(this);
        SQLiteDatabase favs = mDbHelper.getWritableDatabase();

        String selection = DecksContract.DecksEntry._ID + "=?";
        String selectionArgs[] = new String[]{String.valueOf(deckId)};

        ContentValues cv = new ContentValues();
        cv.put(DecksContract.DecksEntry.IS_FAVORITE, 0);

        favs.update(
                DecksContract.DecksEntry.TABLE_NAME,
                cv,
                selection,
                selectionArgs
        );

        favs.close();
    }

    public int getFavoriteState() {
        DecksDbHelper mDbHelper = new DecksDbHelper(this);
        SQLiteDatabase favs = mDbHelper.getReadableDatabase();

        String selection = DecksContract.DecksEntry._ID + "=?";
        String selectionArgs[] = new String[]{String.valueOf(deckId)};
        String columns[] = new String[]{DecksContract.DecksEntry.IS_FAVORITE};

        Cursor c = favs.query(
                DecksContract.DecksEntry.TABLE_NAME,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        c.moveToFirst();

        int isFavorite = c.getInt(c.getColumnIndex(DecksContract.DecksEntry.IS_FAVORITE));
        c.close();
        favs.close();
        return isFavorite;
    }

    public void updateName(String newName) {
        DecksDbHelper mDbHelper = new DecksDbHelper(this);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        String selection = DecksContract.DecksEntry._ID + "=?";
        String selectionArgs[] = new String[]{String.valueOf(deckId)};

        ContentValues cv = new ContentValues();
        cv.put(DecksContract.DecksEntry.COLUMN_DECK_NAME, newName);

        int r = db.update(
                DecksContract.DecksEntry.TABLE_NAME,
                cv,
                selection,
                selectionArgs
        );

        //Log.e("Update: ", String.valueOf(r));
    }

    public String getAnnotatedDeckString(String deckString) {
        StringBuilder builder = new StringBuilder();
        DeckDecoder dd = new DeckDecoder();
        String deckName = dlm.getDeckName();
        deckName = deckName.replaceAll("\n", "");
        String playableClass = dd.getPlayableClass(this, locale, deckString);
        String format = dd.getFormat(deckString);

        //Header
        builder.append("### ");
        builder.append(deckName);
        builder.append("\n");
        builder.append("# ");
        builder.append("Class: ");
        builder.append(playableClass);
        builder.append("\n");
        builder.append("# ");
        builder.append("Format: ");
        builder.append(format);
        builder.append("\n");
        builder.append("#");
        builder.append("\n");

        //Generate the deck list
        for (int i = 0; i < deckList.size(); i++) {
            builder.append("# ");
            builder.append(String.valueOf(deckList.get(i).getQuantity()));
            builder.append("x ");
            builder.append("(");
            builder.append(String.valueOf(deckList.get(i).getCost()));
            builder.append(") ");
            builder.append(deckList.get(i).getName());
            builder.append("\n");
        }
        builder.append("#");
        builder.append("\n");

        //Add the deck string
        builder.append(deckString.trim());
        builder.append("\n");

        builder.append("#");
        builder.append("\n");

        //Add Note
        builder.append("# ");
        builder.append(getString(R.string.use_copied_deck_hint));
        builder.append("\n");

        //Credits
        builder.append("#");
        builder.append("\n");
        builder.append("# ");
        builder.append(getString(R.string.generated_by_deck_box));

        return builder.toString();
    }

    public void shareDeck() {

        String deck = getAnnotatedDeckString(dlm.getDeckString());

        Intent newIntent = new Intent(Intent.ACTION_SEND);
        newIntent.setType("text/plain");
        newIntent.putExtra(Intent.EXTRA_TEXT, deck);

        Intent chooser = Intent.createChooser(newIntent, getString(R.string.action_share));

        if (newIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(chooser);
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void setActionBarColor(String playableClass) {
        switch (playableClass) {

            case DecksContract.DecksEntry.CLASS_WARLOCK:
                toolbar.setBackgroundColor(getColor(R.color.warlock));
                getWindow().setStatusBarColor(getColor(R.color.warlockDark));
                break;
            case DecksContract.DecksEntry.CLASS_PRIEST:
                toolbar.setBackgroundColor(getColor(R.color.priest));
                getWindow().setStatusBarColor(getColor(R.color.priestDark));
                break;
            case DecksContract.DecksEntry.CLASS_MAGE:
                toolbar.setBackgroundColor(getColor(R.color.mage));
                getWindow().setStatusBarColor(getColor(R.color.mageDark));
                break;
            case DecksContract.DecksEntry.CLASS_DRUID:
                toolbar.setBackgroundColor(getColor(R.color.druid));
                getWindow().setStatusBarColor(getColor(R.color.druidDark));
                break;
            case DecksContract.DecksEntry.CLASS_ROGUE:
                toolbar.setBackgroundColor(getColor(R.color.rogue));
                getWindow().setStatusBarColor(getColor(R.color.rogueDark));
                break;
            case DecksContract.DecksEntry.CLASS_SHAMAN:
                toolbar.setBackgroundColor(getColor(R.color.shaman));
                getWindow().setStatusBarColor(getColor(R.color.shamanDark));
                break;
            case DecksContract.DecksEntry.CLASS_WARRIOR:
                toolbar.setBackgroundColor(getColor(R.color.warrior));
                getWindow().setStatusBarColor(getColor(R.color.warriorDark));
                break;
            case DecksContract.DecksEntry.CLASS_HUNTER:
                toolbar.setBackgroundColor(getColor(R.color.hunter));
                getWindow().setStatusBarColor(getColor(R.color.hunterDark));
                break;
            case DecksContract.DecksEntry.CLASS_PALADIN:
                toolbar.setBackgroundColor(getColor(R.color.paladin));
                getWindow().setStatusBarColor(getColor(R.color.paladinDark));
                break;
            case DecksContract.DecksEntry.CLASS_DEMON_HUNTER:
                toolbar.setBackgroundColor(getColor(R.color.demonHunter));
                getWindow().setStatusBarColor(getColor(R.color.darkDemonHunter));
        }
    }

    @Override
    public void onBackPressed() {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("returningFromAlternate", 1);
        editor.commit();
        super.onBackPressed();
    }
}
