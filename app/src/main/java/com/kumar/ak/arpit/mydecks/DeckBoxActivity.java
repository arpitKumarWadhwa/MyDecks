package com.kumar.ak.arpit.mydecks;

import android.app.Activity;
import android.app.SearchManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.kumar.ak.arpit.mydecks.data.DecksContract;
import com.kumar.ak.arpit.mydecks.data.DecksDbHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

//This activity load a eck every time a change is made in the list of decks.
//The decks are filtered out using a filterIconPointer which keeps cycling
//between the values 0-2 depending on user filter choice.
public class DeckBoxActivity extends AppCompatActivity {

    int locale;
    SharedPreferences sharedPref;
    Toolbar toolbar;

    ClipboardManager myClipboard;
    String deckString;
    DecksDbHelper mDbHelper = new DecksDbHelper(this);
    static ArrayList<DeckListManager> decks; //Manages all the decks stored by user
    ArrayList<DeckListManager> filteredDecks; //Filtered decks based on user's search query
    ListView deckBoxListView; //The ListView which displays all the decks saved by user
    DecksAdapter adapter;
    DeckDecoder deckDecoder = new DeckDecoder();
    SearchView searchView;
    TextView allDecks, folders;

    public static final int VIEW_ALL_DECKS = 0;
    public static final int VIEW_FOLDERS = 1;

    public static int viewingMode = VIEW_ALL_DECKS;

    DeckListManager deletedDeck = new DeckListManager();
    ArrayList<DeckListManager> allDeletedDecks = new ArrayList<DeckListManager>();

    //A variable selectedDeckPosition is used to remember which deck the user
    //was on when going into the DeckListActivity. This information is used to
    //scroll back to that deck when returning back to this activity.
    //This is handled in onResume().
    int selectedDeckPosition = 0;

    String searchString;

    //These are the icons which allow the user to filter the decks based
    //on format. The filterIconPointer is used to cycle through the filterIcons[].
    int[] filterIcons = new int[]{R.drawable.all_icon, R.drawable.standard_icon, R.drawable.wild_icon, R.drawable.favorite};
    static int filterIconPointer;

    ArrayList<Integer> favDecks = new ArrayList<>();

    String folderName = "";

    //Handles the increment and cycling of filterIconPointer
    void incrementFilterIconPointer() {
        filterIconPointer = (filterIconPointer + 1) % 4;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                String deletedDeckString = data.getStringExtra("deletedDeckString");
                deletedDeck.setDeckString(deletedDeckString);
                deletedDeck.setDeckName(deckDecoder.getDeckName(deletedDeckString));
                Snackbar mySnackbar = Snackbar.make(findViewById(R.id.activity_deck_box),
                        R.string.snack_bar_delete, Snackbar.LENGTH_LONG);
                mySnackbar.setAction(R.string.snack_bar_undo, new MyUndoListener());
                mySnackbar.show();
            }
            if (resultCode == Activity.RESULT_CANCELED) {

            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Copy Database from asset folder to the installed package
        String appDataPath = this.getApplicationInfo().dataDir;

        File dbFolder = new File(appDataPath + "/databases");//Make sure the /databases folder exists
        dbFolder.mkdir();//This can be called multiple times.

        File dbFilePath = new File(appDataPath + "/databases/deckbox.db");

        try {
            InputStream inputStream = this.getAssets().open("deckbox.db");
            OutputStream outputStream = new FileOutputStream(dbFilePath);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {

        }

        sharedPref = getSharedPreferences("DeckBoxPreferences", Context.MODE_PRIVATE);
        int deckLayout = sharedPref.getInt("deckLayout", 0);
        locale = sharedPref.getInt("locale", Cards.LOCALE_EN_US);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deck_box);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        allDecks = findViewById(R.id.all_decks);
        folders = findViewById(R.id.folders);
        allDecks.setVisibility(View.VISIBLE);
        folders.setVisibility(View.VISIBLE);

        //Check if all decks need to be displayed or only the decks in a folder needs to be displayed
        Intent i = getIntent();
        if (i.hasExtra("folderName")) {
            folderName = i.getStringExtra("folderName");
            if (folderName.length() > 20) {
                toolbar.setTitle(folderName.substring(0, 20) + "..");
            } else {
                toolbar.setTitle(folderName);
            }
            //Set up the All Decks and Folder Tabs
            viewingMode = VIEW_FOLDERS;
            allDecks.setVisibility(View.GONE);
            folders.setVisibility(View.GONE);
        }

        String deletedDeckString = getIntent().getStringExtra("deletedDeckString");
        if (deletedDeckString != null) {
            deletedDeck.setDeckString(deletedDeckString);
            deletedDeck.setDeckName(deckDecoder.getDeckName(deletedDeckString));
            Snackbar mySnackbar = Snackbar.make(findViewById(R.id.activity_deck_box),
                    R.string.snack_bar_delete, Snackbar.LENGTH_LONG);
            mySnackbar.setAction(R.string.snack_bar_undo, new MyUndoListener());
            mySnackbar.show();
        }

        myClipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);


        decks = loadDecks(filterIconPointer); //Load all saved decks

        deckBoxListView = findViewById(R.id.deck_box);
        adapter = new DecksAdapter(this, decks);
        deckBoxListView.setAdapter(adapter);
        deckBoxListView.setEmptyView(findViewById(R.id.empty_deck_box));

        if (selectedDeckPosition >= deckBoxListView.getCount()) {
            selectedDeckPosition = deckBoxListView.getCount() - 1;
        }
        deckBoxListView.setSelection(selectedDeckPosition);

        registerForContextMenu(deckBoxListView);

        deckBoxListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DeckListManager deck = (DeckListManager) parent.getItemAtPosition(position);
                String deckString = deck.getDeckString();

                Intent i = new Intent(getBaseContext(), DeckListActivity.class);
                i.putExtra("deckId", deck.getId());
                i.putExtra("deckString", deckString);
                i.putExtra("deckName", deck.getDeckName());
                i.putExtra("playableClass", deck.getPlayableClass());
                //Save the selected deck position in the list view so that it can be scrolled back to on return
                selectedDeckPosition = position;
                //Save the Search Query so that it can be restored on return
                searchString = searchView.getQuery().toString();
                startActivityForResult(i, 1);
            }
        });

        //Get the deck string from the clipboard
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    ClipData data = myClipboard.getPrimaryClip();
                    ClipData.Item item = data.getItemAt(0);
                    deckString = item.getText().toString();

                    if (doesDeckAlreadyExist(deckString)) {
                        deckString = deckDecoder.prepareDeckString(deckString);
                        AlertDialog.Builder builder = new AlertDialog.Builder(DeckBoxActivity.this);
                        builder.setMessage(getString(R.string.deck_already_exists));
                        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                DeckListManager newDeck = insertDeck(deckString);
                                if (newDeck != null) {
                                    adapter.add(newDeck);
                                    adapter.notifyDataSetChanged();
                                    deckBoxListView.setSelection(deckBoxListView.getCount() - 1);
                                }
                            }
                        });
                        builder.setNegativeButton(getString(R.string.no), null);

                        builder.setCancelable(false);

                        builder.create().show();
                    } else {

                        DeckListManager newDeck = insertDeck(deckString);

                        if (newDeck != null) {
                            adapter.add(newDeck);
                            adapter.notifyDataSetChanged();
                            deckBoxListView.setSelection(deckBoxListView.getCount() - 1);
                        }
                    }
                } catch (Exception e) {

                }
            }
        });

        //Hook up all decks and folders button

        allDecks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (viewingMode == VIEW_FOLDERS) {
                    folders.setBackgroundColor(getResources().getColor(R.color.colorLightBrown));
                    folders.setTypeface(null, Typeface.NORMAL);
                    allDecks.setBackgroundColor(getResources().getColor(R.color.colorBrown));
                    allDecks.setTypeface(null, Typeface.BOLD);
                    viewingMode = VIEW_ALL_DECKS;
                }
            }
        });

        folders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (viewingMode == VIEW_ALL_DECKS) {
                    allDecks.setBackgroundColor(getResources().getColor(R.color.colorLightBrown));
                    allDecks.setTypeface(null, Typeface.NORMAL);
                    folders.setBackgroundColor(getResources().getColor(R.color.colorBrown));
                    folders.setTypeface(null, Typeface.BOLD);
                    viewingMode = VIEW_FOLDERS;

                    Intent i = new Intent(DeckBoxActivity.this, FoldersActivity.class);
                    startActivity(i);
                }
            }
        });
    }

    //Deck Searching is also implemented here
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_deck_box, menu);
        final MenuItem filterItem = menu.findItem(R.id.action_filter_by_format);
        filterItem.setIcon(filterIcons[filterIconPointer]);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setQueryHint(getString(R.string.search_hint));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterItem.setIcon(filterIcons[filterIconPointer]);
                if (newText.isEmpty()) {
                    adapter.clear();
                    decks = loadDecks(filterIconPointer);
                    adapter.addAll(decks);
                    adapter.notifyDataSetChanged();

                    return true;
                } else {
                    searchString = newText;
                    decks = loadDecks(filterIconPointer);
                    handleSearch(newText);
                    adapter.clear();
                    adapter.addAll(filteredDecks);
                    adapter.notifyDataSetChanged();
                    decks = loadDecks(filterIconPointer);
                    return true;
                }
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //Delete All Decks
        if (id == R.id.action_delete_all) {

            DecksDbHelper mDbHelper = new DecksDbHelper(this);
            SQLiteDatabase db = mDbHelper.getWritableDatabase();

            for (int i = 0; i < decks.size(); i++) {
                allDeletedDecks.add(decks.get(i));
                allDeletedDecks.get(i).setDeckName(decks.get(i).getDeckName());
                allDeletedDecks.get(i).setFavorite(decks.get(i).isFavorite());
            }

            db.delete(
                    DecksContract.DecksEntry.TABLE_NAME,
                    null,
                    null
            );

            db.close();

            adapter.clear();
            adapter.notifyDataSetChanged();

            Snackbar mySnackbar = Snackbar.make(findViewById(R.id.activity_deck_box),
                    R.string.snack_bar_delete_all, Snackbar.LENGTH_LONG);
            mySnackbar.setAction(R.string.snack_bar_undo, new MyUndoAllListener());
            mySnackbar.show();

        } else if (id == R.id.action_share_all_decks) {
            shareAllDecks();
        } else if (id == R.id.action_import_all_decks) {
            importAllDecksFromClipboard();
        } else if (id == R.id.action_filter_by_format) {

            incrementFilterIconPointer();
            item.setIcon(filterIcons[filterIconPointer]);

            adapter.clear();
            decks = loadDecks(filterIconPointer);
            adapter.addAll(decks);
            adapter.notifyDataSetChanged();

            if (searchString != null && !searchString.equals("")) {
                handleSearch(searchString);
                adapter.clear();
                adapter.addAll(filteredDecks);
                adapter.notifyDataSetChanged();
                decks = loadDecks(filterIconPointer);
            }

            searchString = null; //Reset the search

        } else if (id == R.id.action_language) {
            Intent i = new Intent(DeckBoxActivity.this, PreferenceActivity.class);
            startActivity(i);
        } else if (id == R.id.action_about) {
            Intent i = new Intent(DeckBoxActivity.this, About.class);
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu_deck_box, menu);

        MenuItem item = menu.findItem(R.id.action_favorites);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        final int position = info.position;
        boolean isFavorite = adapter.getItem(position).isFavorite();
        if (isFavorite)
            item.setTitle(getString(R.string.action_remove_from_favorites));
        else
            item.setTitle(getString(R.string.action_add_to_favorites));

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_edit_deck_name) {
            //Determine which item was long clicked
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            final int position = info.position;

            final View v = this.getLayoutInflater().inflate(R.layout.edit_deck_name_dialog, null);

            final EditText editText = v.findViewById(R.id.folder_name);
            editText.setText(adapter.getItem(position).getDeckName());
            editText.setSelection(0, editText.getText().toString().length());

            editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    editText.post(new Runnable() {
                        @Override
                        public void run() {
                            InputMethodManager imm = (InputMethodManager) DeckBoxActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
                        }
                    });
                }
            });
            editText.requestFocus();

            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
            builder.setView(v);
            builder.setPositiveButton(getString(R.string.dialog_confirm), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    String newDeckName = editText.getText().toString();
                    updateName(newDeckName, adapter.getItem(position).getId());  //Updates the deck name in the database

                    adapter.clear();
                    decks = loadDecks(filterIconPointer);
                    adapter.addAll(decks);
                    adapter.notifyDataSetChanged();
                }
            });
            builder.setNegativeButton(getString(R.string.dialog_cancel), null);
            builder.setCancelable(false);

            builder.create();
            builder.show();
        } else if (id == R.id.action_copy) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            final int position = info.position;

            DeckListManager dlm = adapter.getItem(position);
            try {
                ClipboardManager myClipboard;
                myClipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

                ClipData myClip;
                String annotatedDeckString = getAnnotatedDeckString(dlm);
                myClip = ClipData.newPlainText("text", annotatedDeckString);
                myClipboard.setPrimaryClip(myClip);

                Toast.makeText(this, getString(R.string.deck_copy_success), Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, getString(R.string.deck_copy_failure), Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.action_delete) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            final int position = info.position;

            DecksDbHelper mDbHelper = new DecksDbHelper(this);
            SQLiteDatabase db = mDbHelper.getWritableDatabase();

            String selection = DecksContract.DecksEntry._ID + "=?";
            String selectionArgs[] = new String[]{String.valueOf(adapter.getItem(position).getId())};

            deletedDeck = adapter.getItem(position);

            int r = db.delete(
                    DecksContract.DecksEntry.TABLE_NAME,
                    selection,
                    selectionArgs
            );

            //Log.e("Deleted: ", String.valueOf(r));

            db.close();
            adapter.clear();
            decks = loadDecks(filterIconPointer);
            adapter.addAll(decks);
            adapter.notifyDataSetChanged();

            Snackbar mySnackbar = Snackbar.make(findViewById(R.id.activity_deck_box),
                    R.string.snack_bar_delete, Snackbar.LENGTH_LONG);
            mySnackbar.setAction(R.string.snack_bar_undo, new MyUndoListener());
            mySnackbar.show();
        } else if (id == R.id.action_share_deck) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            final int position = info.position;

            DeckListManager dlm = adapter.getItem(position);
            shareDeck(dlm);
        } else if (id == R.id.action_details) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            final int position = info.position;

            DeckListManager selectedDeck = adapter.getItem(position);

            resetDeckStats(selectedDeck);

            try {
                ArrayList<ArrayList<Integer>> dbfIdList;

                dbfIdList = deckDecoder.decode(selectedDeck.getDeckString());

                //Find all 1-quantity cards by their dbfIds
                ArrayList<Integer> dbf1 = dbfIdList.get(0);
                //Log.e("1-quantity cards: ", String.valueOf(dbf1.size()));
                for (int i = 0; i < dbf1.size(); i++) {
                    Cards tempCard = new Cards();
                    tempCard = Cards.getCardByDbfid(DeckBoxActivity.this, dbf1.get(i), locale);
                    prepareDeckStats(tempCard, selectedDeck);
                }

                //Find all 2-quantity cards by their dbfIds
                ArrayList<Integer> dbf2 = dbfIdList.get(1);
                //Log.e("2-quantity cards: ", String.valueOf(dbf2.size()));
                for (int i = 0; i < dbf2.size(); i++) {
                    Cards tempCard = new Cards();
                    tempCard = Cards.getCardByDbfid(DeckBoxActivity.this, dbf2.get(i), locale);
                    prepareDeckStats(tempCard, selectedDeck);
                    prepareDeckStats(tempCard, selectedDeck);
                }
                dbf1.clear();
                dbf2.clear();
            } catch (Exception e) {
                //Log.e("Deck Decoding Error: ", e.toString());
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View v = this.getLayoutInflater().inflate(R.layout.deck_details_dialog, null);

            Toolbar dialogToolbar = v.findViewById(R.id.toolbar2);
            dialogToolbar.setTitle(selectedDeck.getDeckName());

            TextView noOfHeroes = v.findViewById(R.id.no_of_heroes);
            TextView noOfMinions = v.findViewById(R.id.no_of_minions);
            TextView noOfSpells = v.findViewById(R.id.no_of_spells);
            TextView noOfWeapons = v.findViewById(R.id.no_of_weapons);
            TextView craftingCost = v.findViewById(R.id.crafting_cost);

            GraphView graph = v.findViewById(R.id.graph);

            noOfHeroes.setText(String.valueOf(selectedDeck.getNoOfHeroes()));
            noOfMinions.setText(String.valueOf(selectedDeck.getNoOfMinions()));
            noOfSpells.setText(String.valueOf(selectedDeck.getNoOfSpells()));
            noOfWeapons.setText(String.valueOf(selectedDeck.getNoOfWeapons()));
            craftingCost.setText(String.valueOf(selectedDeck.getDeckCost()));

            String[] MANA_COST = {"0", "1", "2", "3", "4", "5", "6", "7", " "};

            BarGraphSeries<DataPoint> series = new BarGraphSeries<>(new DataPoint[]{
                    new DataPoint(0, selectedDeck.getNoOf0CostCards()),
                    new DataPoint(1, selectedDeck.getNoOf1CostCards()),
                    new DataPoint(2, selectedDeck.getNoOf2CostCards()),
                    new DataPoint(3, selectedDeck.getNoOf3CostCards()),
                    new DataPoint(4, selectedDeck.getNoOf4CostCards()),
                    new DataPoint(5, selectedDeck.getNoOf5CostCards()),
                    new DataPoint(6, selectedDeck.getNoOf6CostCards()),
                    new DataPoint(7, selectedDeck.getNoOf7CostCards()),
            });
            graph.addSeries(series);

            series.setSpacing(5);

            // draw values on top
            series.setDrawValuesOnTop(true);
            series.setValuesOnTopColor(Color.BLACK);

            double xInterval = 1.0;
            graph.getViewport().setXAxisBoundsManual(true);
            if (series instanceof BarGraphSeries) {
                // Shunt the viewport, per v3.1.3 to show the full width of the first and last bars.
                graph.getViewport().setMinX(series.getLowestValueX() - (xInterval / 2.0));
                graph.getViewport().setMaxX(series.getHighestValueX() + (xInterval / 2.0));
            } else {
                graph.getViewport().setMinX(series.getLowestValueX());
                graph.getViewport().setMaxX(series.getHighestValueX());
            }

            StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(graph);
            staticLabelsFormatter.setHorizontalLabels(MANA_COST);

            GridLabelRenderer glr = graph.getGridLabelRenderer();
            glr.setGridStyle(GridLabelRenderer.GridStyle.NONE);
            glr.setVerticalLabelsVisible(false);


            builder.setView(v);

            builder.create().show();
        } else if (id == R.id.action_favorites) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            final int position = info.position;

            if (adapter.getItem(position).isFavorite()) {
                removeFromFavorites(adapter.getItem(position).getId());
                Toast.makeText(this, getString(R.string.removed_from_favorites), Toast.LENGTH_SHORT).show();
                adapter.clear();
                decks = loadDecks(filterIconPointer);
                adapter.addAll(decks);
                adapter.notifyDataSetChanged();
                deckBoxListView.setSelection(position);
            } else {
                addToFavorites(adapter.getItem(position).getId());
                Toast.makeText(this, getString(R.string.added_to_favorites), Toast.LENGTH_SHORT).show();
                adapter.clear();
                decks = loadDecks(filterIconPointer);
                adapter.addAll(decks);
                adapter.notifyDataSetChanged();
                deckBoxListView.setSelection(position);
            }
        } else if (id == R.id.convert_to_standard) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            final int position = info.position;
            DeckListManager selectedDeck = adapter.getItem(position);

            SQLiteDatabase db = mDbHelper.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(DecksContract.DecksEntry.COLUMN_DECK_FORMAT, DecksContract.DecksEntry.FORMAT_STANDARD);

            String selection = DecksContract.DecksEntry._ID + " = ?";
            String[] selectionArgs = {Integer.toString(selectedDeck.getId())};

            db.update(DecksContract.DecksEntry.TABLE_NAME,
                    cv,
                    selection,
                    selectionArgs);

            adapter.clear();
            decks = loadDecks(filterIconPointer);
            adapter.addAll(decks);
            adapter.notifyDataSetChanged();
            deckBoxListView.setSelection(position);

            db.close();
        } else if (id == R.id.action_add_to_folder) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            final int position = info.position;
            View v = this.getLayoutInflater().inflate(R.layout.add_to_folder_dialog, null);
            TextView cancelButton = v.findViewById(R.id.cancel_button);
            TextView okButton = v.findViewById(R.id.ok_button);
            final EditText folderNameEditText = v.findViewById(R.id.folder_name);
            ListView existingFoldersListView = v.findViewById(R.id.existing_folders);

            final ArrayList<String> existingFolders = new ArrayList<>();

            //Get all the existing folders from the database
            SQLiteDatabase database = mDbHelper.getReadableDatabase();
            Cursor c = database.query(
                    DecksContract.DecksEntry.TABLE_NAME,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );
            while (c.moveToNext()) {
                String folderNames = c.getString(c.getColumnIndex(DecksContract.DecksEntry.FOLDER_NAME));
                if (folderNames != null) {
                    ArrayList<String> folderNamesList = new ArrayList<>();
                    folderNamesList.addAll(Arrays.asList(folderNames.split("`")));

                    for (int i = 0; i < folderNamesList.size(); i++) {
                        String folderName = folderNamesList.get(i);
                        if (!existingFolders.contains(folderName)) {
                            existingFolders.add(folderName);
                        }
                    }
                }
            }
            c.close();
            database.close();

            ArrayAdapter<String> existingFoldersAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, existingFolders);

            Log.e("size", String.valueOf(existingFolders.size()));
            //set the height of the list view
            if (existingFolders.size() > 0) {
                int totalHeight = 0;
                int desiredWidth = View.MeasureSpec.makeMeasureSpec(existingFoldersListView.getWidth(), View.MeasureSpec.AT_MOST);
                for (int i = 0; i < existingFoldersAdapter.getCount(); i++) {
                    View listItem = existingFoldersAdapter.getView(i, null, existingFoldersListView);
                    listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
                    totalHeight += listItem.getMeasuredHeight();
                    if (i == 3) {
                        break;
                    }
                }

                ViewGroup.LayoutParams params = existingFoldersListView.getLayoutParams();
                params.height = totalHeight / 2 + existingFoldersListView.getDividerHeight() * (existingFoldersAdapter.getCount());
                existingFoldersListView.setLayoutParams(params);
                existingFoldersListView.requestLayout();
            }

            existingFoldersListView.setAdapter(existingFoldersAdapter);


            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(v);
            final AlertDialog dialog = builder.create();
            dialog.show();

            okButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String folderName = folderNameEditText.getText().toString().trim();

                    if (!TextUtils.isEmpty(folderName) && !folderName.contains("`")) {

                        String selection = DecksContract.DecksEntry._ID + "=?";
                        String[] selectionArgs = new String[]{String.valueOf(adapter.getItem(position).getId())};

                        //Get all the folder in which this deck is saved
                        SQLiteDatabase database = mDbHelper.getReadableDatabase();
                        Cursor c = database.query(
                                DecksContract.DecksEntry.TABLE_NAME,
                                null,
                                selection,
                                selectionArgs,
                                null,
                                null,
                                null
                        );
                        c.moveToFirst();
                        String existingFolderNames = c.getString(c.getColumnIndex(DecksContract.DecksEntry.FOLDER_NAME));
                        c.close();
                        database.close();


                        Log.e("new", folderName);
                        //Save the deck in the folder
                        database = mDbHelper.getWritableDatabase();

                        ContentValues cv = new ContentValues();
                        folderName += "`"; //Delimiter to separate different folders
                        if(existingFolderNames != null) {
                            Log.e("existsing: ",  existingFolderNames);
                            existingFolderNames += folderName;
                        }
                        else{
                            existingFolderNames = folderName;
                        }
                        cv.put(DecksContract.DecksEntry.FOLDER_NAME, existingFolderNames);

                        database.update(
                                DecksContract.DecksEntry.TABLE_NAME,
                                cv,
                                selection,
                                selectionArgs
                        );

                        database.close();
                    } else {
                        //TODO: Show a Toast
                    }
                    dialog.hide();
                }
            });

            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.hide();
                }
            });

            existingFoldersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    String folderName = existingFolders.get(i);
                    folderNameEditText.setText(folderName);
                }
            });

        }
        return super.onContextItemSelected(item);
    }

    //Inserts the deck in the database and returns the same deck back as a DeckListManager object.
    public DeckListManager insertDeck(String deckString) {

        String deckName = deckDecoder.getDeckName(deckString);

        deckString = deckDecoder.prepareDeckString(deckString).trim();
        mDbHelper = new DecksDbHelper(this);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        String format = deckDecoder.getFormat(deckString);
        String playableClass = deckDecoder.getPlayableClass(this, locale, deckString);


        if (deckName == null) {
            deckName = playableClass + " " + getString(R.string.null_deck_name); //eg. If class is Rogue, and string doesn't contain deck name then deck name will be "Rogue Deck"
        }


        if (format == "Invalid Format") {
            return null;
        }

        DeckListManager deck = new DeckListManager(deckString);
        deck.setPlayableClass(playableClass);
        deck.setFormat(format);
        deck.setDeckName(deckName);

        ContentValues cv = new ContentValues();
        cv.put(DecksContract.DecksEntry.COLUMN_DECK_STRING, deckString);
        cv.put(DecksContract.DecksEntry.COLUMN_DECK_FORMAT, format);
        cv.put(DecksContract.DecksEntry.COLUMN_DECK_CLASS, playableClass);
        cv.put(DecksContract.DecksEntry.COLUMN_DECK_NAME, deckName);

        long result = db.insert(DecksContract.DecksEntry.TABLE_NAME, null, cv);

        if (result == -1) {
            Log.e("DeckBoxActivity: ", "Deck Insertion Error");
        }

        db.close();

        db = mDbHelper.getReadableDatabase();

        Cursor c = db.query(
                DecksContract.DecksEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null
        );

        c.moveToLast();
        deck.setId(c.getInt(c.getColumnIndex(DecksContract.DecksEntry._ID)));

        c.close();
        db.close();

        return deck;
    }

    //Loads all the decks from the database based on the user's format prefernce
    public ArrayList<DeckListManager> loadDecks(int filterIconPointer) {
        ArrayList<DeckListManager> decks = new ArrayList<DeckListManager>();

        mDbHelper = new DecksDbHelper(this);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String format = "";

        switch (filterIconPointer) {
            case 0: //All Decks
                format = "All";
                break;
            case 1: //Standard Format
                format = DecksContract.DecksEntry.FORMAT_STANDARD;
                break;
            case 2: //Wild Format
                format = DecksContract.DecksEntry.FORMAT_WILD;
                break;
            case 3: //favorite Decks
                format = "Favorites";
        }

        String selection = DecksContract.DecksEntry._ID + "=?";
        String selectionArgs[] = null;

        if (format.equals("All")) {
            if (!TextUtils.isEmpty(folderName)) {
                //Get the column ids of all decks which are stored in the selected folder
                SQLiteDatabase folderDatabase = mDbHelper.getReadableDatabase();
                Cursor c = folderDatabase.query(
                        DecksContract.DecksEntry.TABLE_NAME,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                );

                ArrayList<String> selectionArgsList = new ArrayList<>();
                while (c.moveToNext()) {
                    String foldersListString = c.getString(c.getColumnIndex(DecksContract.DecksEntry.FOLDER_NAME));
                    ArrayList<String> foldersList = new ArrayList<>();
                    if(foldersListString != null) {
                        foldersList.addAll(Arrays.asList(foldersListString.split("`")));
                    }
                    if (foldersList.contains(folderName)) {
                        int id = c.getInt(c.getColumnIndex(DecksContract.DecksEntry._ID));
                        selection = selection + " OR " + DecksContract.DecksEntry._ID + "=?";
                        selectionArgsList.add(String.valueOf(id));
                    }
                }
                if (selectionArgsList.size() > 0) {
                    selectionArgs = selectionArgsList.toArray(new String[0]);
                }
                c.close();
            } else {
                selectionArgs = null;
                selection = null;
            }
        } else if (format.equals("Favorites")) {
            if (!TextUtils.isEmpty(folderName)) {
                //Get the column ids of all decks which are stored in the selected folder
                SQLiteDatabase folderDatabase = mDbHelper.getReadableDatabase();
                Cursor c = folderDatabase.query(
                        DecksContract.DecksEntry.TABLE_NAME,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                );

                ArrayList<String> selectionArgsList = new ArrayList<>();
                while (c.moveToNext()) {
                    String foldersListString = c.getString(c.getColumnIndex(DecksContract.DecksEntry.FOLDER_NAME));
                    ArrayList<String> foldersList = new ArrayList<>();
                    if(foldersListString != null) {
                        foldersList.addAll(Arrays.asList(foldersListString.split("`")));
                    }
                    int isFav = c.getInt(c.getColumnIndex(DecksContract.DecksEntry.IS_FAVORITE));
                    if (foldersList.contains(folderName) && isFav == DecksContract.DecksEntry.FAVORITE) {
                        int id = c.getInt(c.getColumnIndex(DecksContract.DecksEntry._ID));
                        selection = selection + " OR " + DecksContract.DecksEntry._ID + "=?";
                        selectionArgsList.add(String.valueOf(id));
                    }
                }
                if (selectionArgsList.size() > 0) {
                    selectionArgs = selectionArgsList.toArray(new String[0]);
                }
                c.close();
            } else {
                selection = DecksContract.DecksEntry.IS_FAVORITE + "=?";
                selectionArgs = new String[]{String.valueOf(DecksContract.DecksEntry.FAVORITE)};
            }
        } else {
            if (!TextUtils.isEmpty(folderName)) {
                //Get the column ids of all decks which are stored in the selected folder
                SQLiteDatabase folderDatabase = mDbHelper.getReadableDatabase();
                Cursor c = folderDatabase.query(
                        DecksContract.DecksEntry.TABLE_NAME,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                );

                ArrayList<String> selectionArgsList = new ArrayList<>();
                while (c.moveToNext()) {
                    String foldersListString = c.getString(c.getColumnIndex(DecksContract.DecksEntry.FOLDER_NAME));
                    String queriedFormat = c.getString(c.getColumnIndex(DecksContract.DecksEntry.COLUMN_DECK_FORMAT));
                    ArrayList<String> foldersList = new ArrayList<>();
                    if(foldersListString != null) {
                        foldersList.addAll(Arrays.asList(foldersListString.split("`")));
                    }
                    if (foldersList.contains(folderName) && queriedFormat.equals(format)) {
                        int id = c.getInt(c.getColumnIndex(DecksContract.DecksEntry._ID));
                        selection = selection + " OR " + DecksContract.DecksEntry._ID + "=?";
                        selectionArgsList.add(String.valueOf(id));
                    }
                }
                if (selectionArgsList.size() > 0) {
                    selectionArgs = selectionArgsList.toArray(new String[0]);
                }
                c.close();
            } else {
                selection = DecksContract.DecksEntry.COLUMN_DECK_FORMAT + "=?";
                selectionArgs = new String[]{format};
            }
        }

        Cursor c = db.query(
                DecksContract.DecksEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        if (c.getCount() == 0)
            return decks;
        else {
            while (c.moveToNext()) {
                DeckListManager deck = new DeckListManager(c.getString(c.getColumnIndex(DecksContract.DecksEntry.COLUMN_DECK_STRING)));
                deck.setFormat(c.getString(c.getColumnIndex(DecksContract.DecksEntry.COLUMN_DECK_FORMAT)));
                deck.setPlayableClass(c.getString(c.getColumnIndex(DecksContract.DecksEntry.COLUMN_DECK_CLASS)));
                deck.setId(c.getInt(c.getColumnIndex(DecksContract.DecksEntry._ID)));
                deck.setDeckName(c.getString(c.getColumnIndex(DecksContract.DecksEntry.COLUMN_DECK_NAME)));
                int fav = c.getInt(c.getColumnIndex(DecksContract.DecksEntry.IS_FAVORITE));

                switch (fav) {
                    case 0:
                        deck.setFavorite(false);
                        break;
                    case 1:
                        deck.setFavorite(true);
                }

                decks.add(deck);
            }

            c.close();
            db.close();

            return decks;
        }
    }

    //Shares all decks as a String to whatsapp/email, etc
    //Can be accessed through Options Menu
    public void shareAllDecks() {
        StringBuilder share = new StringBuilder();

        for (int i = 0; i < decks.size(); i++) {
            share.append("### "); //Indicates the line where Deck Name is written
            share.append(decks.get(i).getDeckName());
            share.append("\n"); //New Line
            share.append(decks.get(i).getDeckString());
            share.append("\n\n"); //2 New Lines
        }

        String allDecks = share.toString().trim();

        Intent newIntent = new Intent(Intent.ACTION_SEND);
        newIntent.setType("text/plain");
        newIntent.putExtra(Intent.EXTRA_TEXT, allDecks);

        Intent chooser = Intent.createChooser(newIntent, getString(R.string.action_share_all_decks));

        if (newIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(chooser);
        }

    }

    //Shares a single deck to whatsapp/email, etc.
    //Can be accessed through Context Menu
    public void shareDeck(DeckListManager dlm) {


        String deck = getAnnotatedDeckString(dlm);

        Intent newIntent = new Intent(Intent.ACTION_SEND);
        newIntent.setType("text/plain");
        newIntent.putExtra(Intent.EXTRA_TEXT, deck);

        Intent chooser = Intent.createChooser(newIntent, getString(R.string.action_share));

        if (newIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(chooser);
        }

    }

    private void importAllDecksFromClipboard() {
        DeckDecoder dd = new DeckDecoder();
        try {
            ClipData data = myClipboard.getPrimaryClip();
            ClipData.Item item = data.getItemAt(0);
            String clipboardData = item.getText().toString();

            if (clipboardData == "") {
                throw new Exception();
            }

            ArrayList<String> deckStrings = new ArrayList<String>(Arrays.asList(clipboardData.split("\n\n")));

            for (int i = 0; i < deckStrings.size(); i++) {
                DeckListManager newDeck = insertDeck(deckStrings.get(i).trim());
            }

            adapter.clear();
            decks = loadDecks(filterIconPointer);
            adapter.addAll(decks);
            adapter.notifyDataSetChanged();

        } catch (Exception e) {
            Toast.makeText(this, "No decks found in clipboard", Toast.LENGTH_SHORT).show();
        }
    }

    public void updateName(String newName, int deckId) {
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

    //Annotates the deck string with the full deck list
    public String getAnnotatedDeckString(DeckListManager dlm) {
        String deckString = dlm.getDeckString();
        deckString = deckDecoder.prepareDeckString(deckString);
        StringBuilder builder = new StringBuilder();
        DeckDecoder dd = new DeckDecoder();
        String deckName = dlm.getDeckName();
        String playableClass = dd.getPlayableClass(this, locale, deckString);
        String format = dd.getFormat(deckString);

        ArrayList<Cards> deckList = new ArrayList<Cards>();
        QueryUtils httpHelper = new QueryUtils();

        try {
            ArrayList<ArrayList<Integer>> dbfIdList;
            dbfIdList = deckDecoder.decode(deckString);

            //Find all 1-quantity cards by their dbfIds
            ArrayList<Integer> dbf1 = dbfIdList.get(0);
            //Log.e("1-quantity cards: ", String.valueOf(dbf1.size()));
            for (int i = 0; i < dbf1.size(); i++) {
                Cards tempCard = new Cards();
                tempCard = Cards.getCardByDbfid(this, dbf1.get(i), locale);
                tempCard.setQuantity(1);

                String imageUrl = "https://art.hearthstonejson.com/v1/tiles/" + tempCard.getCardId() + ".jpg";
                URL url = httpHelper.createUrl(imageUrl);
                tempCard.setCardTileUrl(url);

                deckList.add(tempCard);
            }
            dbf1.clear();

            //Find all 2-quantity cards by their dbfIds
            ArrayList<Integer> dbf2 = dbfIdList.get(1);
            //Log.e("2-quantity cards: ", String.valueOf(dbf2.size()));
            for (int i = 0; i < dbf2.size(); i++) {
                Cards tempCard = new Cards();
                tempCard = Cards.getCardByDbfid(this, dbf2.get(i), locale);
                tempCard.setQuantity(2);

                String imageUrl = "https://art.hearthstonejson.com/v1/tiles/" + tempCard.getCardId() + ".jpg";
                URL url = httpHelper.createUrl(imageUrl);
                tempCard.setCardTileUrl(url);

                deckList.add(tempCard);
            }
            dbf2.clear();
        } catch (IOException e) {
            //Log.e("Deck Decoding Error: ", e.toString());
        }

        //Sort deck by mana cost
        deckList = dlm.sortByMana(deckList);
        deckList = dlm.sortLexicographically(deckList);

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

    public void prepareDeckStats(Cards card, DeckListManager deck) {
        //Get card type and cost
        int cost = card.getCost();
        int type = card.getType();
        int rarity = card.getRarity();

        //Increment the no. of cards in each category in the deck
        switch (type) {
            case Cards.TYPE_HERO:
                deck.incrementNoOfHeroes();
                break;
            case Cards.TYPE_MINION:
                deck.incrementNoOfMinions();
                break;
            case Cards.TYPE_SPELL:
                deck.incrementNoOfSpells();
                break;
            case Cards.TYPE_WEAPON:
                deck.incrementNoOfWeapons();
                ;
                break;
        }

        //Increment the no. of cards in each mana cost category. Will be used to generate mana curve
        incrementDeckManaCategory(cost, deck);

        //Increment deck cost for the passed card
        calculateDeckCraftingCost(rarity, deck);
    }

    public void resetDeckStats(DeckListManager deck) {
        deck.setNoOf0CostCards(0);
        deck.setNoOf1CostCards(0);
        deck.setNoOf2CostCards(0);
        deck.setNoOf3CostCards(0);
        deck.setNoOf4CostCards(0);
        deck.setNoOf5CostCards(0);
        deck.setNoOf6CostCards(0);
        deck.setNoOf7CostCards(0);
        deck.setNoOfHeroes(0);
        deck.setNoOfMinions(0);
        deck.setNoOfSpells(0);
        deck.setNoOfWeapons(0);
        deck.setDeckCost(0);
    }

    public void incrementDeckManaCategory(int cost, DeckListManager deck) {
        switch (cost) {
            case 0:
                deck.incrementNoOf0CostCards();
                break;
            case 1:
                deck.incrementNoOf1CostCards();
                break;
            case 2:
                deck.incrementNoOf2CostCards();
                break;
            case 3:
                deck.incrementNoOf3CostCards();
                break;
            case 4:
                deck.incrementNoOf4CostCards();
                break;
            case 5:
                deck.incrementNoOf5CostCards();
                break;
            case 6:
                deck.incrementNoOf6CostCards();
                break;
            default:
                deck.incrementNoOf7CostCards();
        }
    }

    public void calculateDeckCraftingCost(int rarity, DeckListManager deck) {
        switch (rarity) {
            case Cards.RARITY_COMMON:
                deck.deckCost += 40;
                break;
            case Cards.RARITY_RARE:
                deck.deckCost += 100;
                break;
            case Cards.RARITY_EPIC:
                deck.deckCost += 400;
                break;
            case Cards.RARITY_LEGENDARY:
                deck.deckCost += 1600;
        }
    }


    //This is the function that actually searches the database for all the
    //stored decks based on the format preference set by user.
    //NOTE: Enter "class:<classname>" in the search bar to filter the decks
    //based on class. eg. "class: Druid" will return all Druid decks.
    //This function ignores the case. i.e Druid is same as DRuiD.
    private void handleSearch(String query) {
        filteredDecks = new ArrayList<DeckListManager>();

        query = query.toLowerCase();
        query = query.trim();

        for (int i = 0; i < decks.size(); i++) {
            if (decks.get(i).getDeckName().toLowerCase().contains(query)) {
                filteredDecks.add(decks.get(i));
            }
        }


        if (query.length() > 6) {
            if (query.substring(0, 6).compareTo("class:") == 0) {
                String classSearch = query.substring(6);
                classSearch = classSearch.trim();
                for (int i = 0; i < decks.size(); i++) {
                    if (decks.get(i).getPlayableClass().toLowerCase().equalsIgnoreCase(classSearch)) {
                        filteredDecks.add(decks.get(i));
                    }
                }
            }
        }
    }

    public class MyUndoListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {


            if (deletedDeck.getDeckName() != null) {
                boolean isFavorite = deletedDeck.isFavorite();
                String deletedDeckString = getAnnotatedDeckString(deletedDeck);
                deletedDeck = insertDeck(deletedDeckString);
                deletedDeck.setFavorite(isFavorite);
                addToFavorites(deletedDeck.getId());
                adapter.add(deletedDeck);
                adapter.notifyDataSetChanged();
                deckBoxListView.setSelection(deckBoxListView.getCount() - 1);
            }
        }
    }

    public class MyUndoAllListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            StringBuilder share = new StringBuilder();

            for (int i = 0; i < allDeletedDecks.size(); i++) {
                share.append("### "); //Indicates the line where Deck Name is written
                share.append(allDeletedDecks.get(i).getDeckName());
                share.append("\n"); //New Line
                share.append(allDeletedDecks.get(i).getDeckString());
                share.append("\n\n"); //2 New Lines

            }

            String allDeletedDecksString = share.toString().trim();


            ArrayList<String> deckStrings = new ArrayList<String>(Arrays.asList(allDeletedDecksString.split("\n\n")));

            for (int i = 0; i < deckStrings.size(); i++) {
                DeckListManager newDeck = insertDeck(deckStrings.get(i).trim());
            }

            allDeletedDecks.clear();

            adapter.clear();
            decks.clear();
            decks = loadDecks(filterIconPointer);
            adapter.addAll(decks);
            adapter.notifyDataSetChanged();
        }
    }

    public boolean doesDeckAlreadyExist(String text) {
        String textToCheck = deckDecoder.prepareDeckString(text).trim();

        mDbHelper = new DecksDbHelper(this);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String selection = DecksContract.DecksEntry.COLUMN_DECK_STRING + "=?";
        String selectionArgs[] = new String[]{textToCheck};

        Cursor c = db.query(
                DecksContract.DecksEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        if (c.getCount() == 0) {
            c.close();
            return false;
        } else {
            c.close();
            return true;
        }
    }

    public void addToFavorites(int deckId) {
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

    public void removeFromFavorites(int deckId) {

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

    @Override
    protected void onResume() {
        viewingMode = VIEW_ALL_DECKS;

        //Set up the All Decks and Folder Tabs
        folders.setBackgroundColor(getResources().getColor(R.color.colorLightBrown));
        folders.setTypeface(null, Typeface.NORMAL);
        allDecks.setBackgroundColor(getResources().getColor(R.color.colorBrown));
        allDecks.setTypeface(null, Typeface.BOLD);

        if(folderName != null && !TextUtils.isEmpty(folderName)){
            viewingMode = VIEW_FOLDERS;
            //Set up the All Decks and Folder Tabs
            allDecks.setBackgroundColor(getResources().getColor(R.color.colorLightBrown));
            allDecks.setTypeface(null, Typeface.NORMAL);
            folders.setBackgroundColor(getResources().getColor(R.color.colorBrown));
            folders.setTypeface(null, Typeface.BOLD);
        }


        if (searchString != null && !searchString.equals("")) {
            searchView.requestFocus();
            searchView.setQuery(searchString, false);
            handleSearch(searchString);
            adapter.clear();
            adapter.addAll(filteredDecks);
            adapter.notifyDataSetChanged();
            decks = loadDecks(filterIconPointer);
        } else {
            //Close the search bar if open
            toolbar.collapseActionView();

            adapter.clear();

            decks = loadDecks(filterIconPointer); //Load all saved decks

            deckBoxListView = findViewById(R.id.deck_box);
            adapter = new DecksAdapter(this, decks);
            deckBoxListView.setAdapter(adapter);
        }

        if (selectedDeckPosition >= deckBoxListView.getCount()) {
            selectedDeckPosition = deckBoxListView.getCount() - 1;
        }
        deckBoxListView.setSelection(selectedDeckPosition);


        SharedPreferences sharedPref = getSharedPreferences("DeckBoxPreferences", Context.MODE_PRIVATE);
        locale = sharedPref.getInt("locale", Cards.LOCALE_EN_US);


        super.onResume();

    }

}
