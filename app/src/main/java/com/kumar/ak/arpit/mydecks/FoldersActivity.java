package com.kumar.ak.arpit.mydecks;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.kumar.ak.arpit.mydecks.data.DecksContract;
import com.kumar.ak.arpit.mydecks.data.DecksDbHelper;

import java.util.ArrayList;
import java.util.Arrays;

public class FoldersActivity extends AppCompatActivity {

    RecyclerView foldersRecycler;
    ArrayList<String> folders = new ArrayList<>();
    TextView allDecksTextView, foldersTextView;
    final FolderAdapter folderAdapter = new FolderAdapter(folders, this);
    final DecksDbHelper mDbHelper = new DecksDbHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folders);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setTitle("Folders");
        }

        loadFolders();

        foldersRecycler = findViewById(R.id.folders_recycler);
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        foldersRecycler.setLayoutManager(staggeredGridLayoutManager);
        foldersRecycler.setAdapter(folderAdapter);

        folderAdapter.setOnFolderLongClickListener(new FolderAdapter.OnFolderLongClick() {
            @Override
            public void onFolderLongClick(View v, final int position) {
                //Create the popup menu
                PopupMenu popupMenu = new PopupMenu(FoldersActivity.this, v);
                MenuInflater inflater = popupMenu.getMenuInflater();
                inflater.inflate(R.menu.context_menu_folder, popupMenu.getMenu());
                popupMenu.show();

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.action_delete) {
                            //Get the folder name
                            String folderToBeDeleted = folders.get(position);
                            SQLiteDatabase readDatabase = mDbHelper.getReadableDatabase();

                            Cursor c = readDatabase.query(
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

                                    if (folderNames.contains(folderToBeDeleted)) {
                                        folderNamesList.remove(folderToBeDeleted);

                                        StringBuilder newFolderNameBuilder = new StringBuilder();

                                        //Create the new folders string
                                        for(int i=0; i<folderNamesList.size(); i++){
                                            newFolderNameBuilder.append(folderNamesList.get(i));
                                            newFolderNameBuilder.append("`");
                                        }

                                        String newFolderName = null;
                                        if(folderNamesList.size() > 0 ) {
                                            newFolderName = newFolderNameBuilder.toString();
                                        }

                                        //Get the id of the deck
                                        int id = c.getInt(c.getColumnIndex(DecksContract.DecksEntry._ID));

                                        //Update the database
                                        SQLiteDatabase writeDatabase = mDbHelper.getWritableDatabase();
                                        String selection = DecksContract.DecksEntry._ID + "=?";
                                        String[] selectionArgs = new String[]{String.valueOf(id)};

                                        ContentValues cv = new ContentValues();
                                        cv.put(DecksContract.DecksEntry.FOLDER_NAME, newFolderName);
                                        writeDatabase.update(
                                                DecksContract.DecksEntry.TABLE_NAME,
                                                cv,
                                                selection,
                                                selectionArgs
                                        );
                                        writeDatabase.close();
                                    }
                                }
                            }
                            c.close();
                            readDatabase.close();

                            folders.clear();
                            loadFolders();
                            folderAdapter.notifyDataSetChanged();

                            return true;
                        } else
                            return false;
                    }
                });
            }
        });

        //Hook up all decks and folders button
        allDecksTextView = findViewById(R.id.all_decks);
        foldersTextView = findViewById(R.id.folders);


        allDecksTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (DeckBoxActivity.viewingMode == DeckBoxActivity.VIEW_FOLDERS) {
                    foldersTextView.setBackgroundColor(getResources().getColor(R.color.colorLightBrown));
                    foldersTextView.setTypeface(null, Typeface.NORMAL);
                    allDecksTextView.setBackgroundColor(getResources().getColor(R.color.colorBrown));
                    allDecksTextView.setTypeface(null, Typeface.BOLD);
                    DeckBoxActivity.viewingMode = DeckBoxActivity.VIEW_ALL_DECKS;

                    finish();
                }
            }
        });

        foldersTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (DeckBoxActivity.viewingMode == DeckBoxActivity.VIEW_ALL_DECKS) {
                    allDecksTextView.setBackgroundColor(getResources().getColor(R.color.colorLightBrown));
                    allDecksTextView.setTypeface(null, Typeface.NORMAL);
                    foldersTextView.setBackgroundColor(getResources().getColor(R.color.colorBrown));
                    foldersTextView.setTypeface(null, Typeface.BOLD);
                    DeckBoxActivity.viewingMode = DeckBoxActivity.VIEW_FOLDERS;
                }
            }
        });
    }

    public void loadFolders(){
        //Load all the folders
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
                    if (!folders.contains(folderName)) {
                        Log.e("loadName", folderName);
                        folders.add(folderName);
                    }
                }
            }
        }
        c.close();
        database.close();

    }

    @Override
    protected void onResume() {
        super.onResume();

        folders.clear();
        loadFolders();
        folderAdapter.notifyDataSetChanged();

        DeckBoxActivity.viewingMode = DeckBoxActivity.VIEW_FOLDERS;
        //Set up the All Decks and Folder Tabs
        allDecksTextView.setBackgroundColor(getResources().getColor(R.color.colorLightBrown));
        allDecksTextView.setTypeface(null, Typeface.NORMAL);
        foldersTextView.setBackgroundColor(getResources().getColor(R.color.colorBrown));
        foldersTextView.setTypeface(null, Typeface.BOLD);
    }
}
