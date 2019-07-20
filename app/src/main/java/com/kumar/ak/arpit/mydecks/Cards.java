package com.kumar.ak.arpit.mydecks;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


import com.kumar.ak.arpit.mydecks.data.CardsDbHelper;
import com.kumar.ak.arpit.mydecks.data.DecksContract;

import java.net.URL;
import java.util.ArrayList;

public class Cards {
    public static final int RARITY_UNKNOWN = -1;
    public static final int RARITY_FREE = 0; //FREE AND SET=CORE
    public static final int RARITY_COMMON = 1; //COMMON
    public static final int RARITY_RARE = 2; //RARE
    public static final int RARITY_EPIC = 3; //EPIC
    public static final int RARITY_LEGENDARY = 4; //LEGENDARY

    public static final int TYPE_UNKNOWN = -1;
    public static final int TYPE_HERO = 0; //HERO
    public static final int TYPE_MINION = 1; //MINION
    public static final int TYPE_SPELL = 2; //SPELL
    public static final int TYPE_WEAPON = 3; //WEAPON

    public static final int CARD_CLASS_INVALID = -1;
    public static final int CARD_CLASS_WARRIOR = 0;
    public static final int CARD_CLASS_HUNTER = 1;
    public static final int CARD_CLASS_PALADIN = 2;
    public static final int CARD_CLASS_ROGUE = 3;
    public static final int CARD_CLASS_DRUID = 4;
    public static final int CARD_CLASS_SHAMAN = 5;
    public static final int CARD_CLASS_MAGE = 6;
    public static final int CARD_CLASS_PRIEST = 7;
    public static final int CARD_CLASS_WARLOCK = 8;
    public static final int CARD_CLASS_NEUTRAL = 9;

    public final static int LOCALE_EN_US = 0;
    public final static int LOCALE_KO_KR = 1;
    public final static int LOCALE_ZH_CN = 2;
    public final static int LOCALE_RU_RU = 3;


    private String cardId; //id
    private int dbfid; //For generating deck codes. dbfid
    private String name; //name
    private int cost; //Mana Cost. cost
    private int rarity;//rarity
    private int type; //Type of card i.e hero/minion/spell/weapon. type
    private int quantity; //Quantity of the card in a deck. Does not go in a database.
    private int cardClass; //The class to which the card belongs including neutral
    private String cardText; //text on the card
    private ArrayList<String> mechanics; //Mechanics used by card like Battlecry, Secret, etc
    private URL cardTileUrl; //The card tile image used for generating in-game style decklist(layout1)

    public String getCardId() {
        return cardId;
    }

    public int getDbfid() {
        return dbfid;
    }

    public String getName() {
        return name;
    }

    public int getCost() {
        return cost;
    }

    public int getRarity() {
        return rarity;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getType() {
        return type;
    }

    public URL getCardTileUrl() {
        return cardTileUrl;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setCardTileUrl(URL cardTileUrl) {
        this.cardTileUrl = cardTileUrl;
    }

    public Cards() {
    }

    public int getCardClass() {
        return cardClass;
    }

    public void setCardClass(int cardClass) {
        this.cardClass = cardClass;
    }

    public String getCardText() {
        return cardText;
    }

    public void setCardText(String cardText) {
        this.cardText = cardText;
    }

    public ArrayList<String> getMechanics() {
        return mechanics;
    }

    public void setMechanics(ArrayList<String> mechanics) {
        this.mechanics = mechanics;
    }

    public Cards(String cardId, int dbfid, String name, int cost, int rarity, int type, int quantity) {
        this.cardId = cardId;
        this.dbfid = dbfid;
        this.name = name;
        this.cost = cost;
        this.rarity = rarity;
        this.type = type;
        this.quantity = quantity;
    }

    public static Cards getCardByDbfid(Context context, int dbf, int locale) {
        Cards tempCard = new Cards();
        CardsDbHelper cardsDbHelper = new CardsDbHelper(context);
        SQLiteDatabase db = cardsDbHelper.getReadableDatabase();

        String selection = DecksContract.CardsEntry.COLUMN_CARD_DBFID + "=?";
        String selectionArgs[] = new String[]{String.valueOf(dbf)};

        String cardsTable;

        switch (locale){
            case LOCALE_EN_US:
                cardsTable = DecksContract.CardsEntry.TABLE_NAME;
                break;
            case LOCALE_KO_KR:
                cardsTable = DecksContract.CardsEntry.TABLE_NAME_KO_KR;
                break;
            case LOCALE_ZH_CN:
                cardsTable = DecksContract.CardsEntry.TABLE_NAME_ZH_CN;
                break;
            case LOCALE_RU_RU:
                cardsTable = DecksContract.CardsEntry.TABLE_NAME_RU_RU;
                break;
                default:
                    cardsTable = DecksContract.CardsEntry.TABLE_NAME;
        }

        Cursor c = db.query(
                cardsTable,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        c.moveToFirst();

        try {
            tempCard.cardId = c.getString(c.getColumnIndex(DecksContract.CardsEntry.COLUMN_CARD_ID));
            tempCard.name = c.getString(c.getColumnIndex(DecksContract.CardsEntry.COLUMN_CARD_NAME));
            tempCard.cost = c.getInt(c.getColumnIndex(DecksContract.CardsEntry.COLUMN_CARD_COST));
            tempCard.rarity = c.getInt(c.getColumnIndex(DecksContract.CardsEntry.COLUMN_CARD_RARITY));
            tempCard.type = c.getInt(c.getColumnIndex(DecksContract.CardsEntry.COLUMN_CARD_TYPE));
            tempCard.cardClass = c.getInt(c.getColumnIndex(DecksContract.CardsEntry.COLUMN_CARD_CLASS));
            tempCard.dbfid = dbf;
        } catch (Exception e) {
            //Log.e("Cards: ", e.toString());
        } finally {
            c.close();
            db.close();
        }

        return tempCard;
    }

    //Converts the rarity of a card in String to an int
    public static int tellRarityInInteger(String rarityString) {
        switch (rarityString) {
            case DecksContract.CardsEntry.RARITY_FREE:
                return RARITY_FREE;
            case DecksContract.CardsEntry.RARITY_COMMON:
                return RARITY_COMMON;
            case DecksContract.CardsEntry.RARITY_RARE:
                return RARITY_RARE;
            case DecksContract.CardsEntry.RARITY_EPIC:
                return RARITY_EPIC;
            case DecksContract.CardsEntry.RARITY_LEGENDARY:
                return RARITY_LEGENDARY;
            default:
                return RARITY_UNKNOWN;
        }
    }

    //Converts the type of a card in String to an int
    public static int tellTypeInInteger(String typeString) {
        switch (typeString) {
            case DecksContract.CardsEntry.TYPE_HERO:
                return TYPE_HERO;
            case DecksContract.CardsEntry.TYPE_MINION:
                return TYPE_MINION;
            case DecksContract.CardsEntry.TYPE_SPELL:
                return TYPE_SPELL;
            case DecksContract.CardsEntry.TYPE_WEAPON:
                return TYPE_WEAPON;
            default:
                return TYPE_UNKNOWN;
        }
    }

}

