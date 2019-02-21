package com.kumar.ak.arpit.mydecks.data;

import android.provider.BaseColumns;

public final class DecksContract {

    public static final class CardsEntry implements BaseColumns {

        //Database Columns
        public final static String TABLE_NAME = "cards";
        public final static String TABLE_NAME_KO_KR = "cards_koKR";
        public final static String TABLE_NAME_ZH_CN = "cards_zhCN";
        public final static String TABLE_NAME_RU_RU = "cards_ruRU";

        /**
         * Unique ID number for the card (only for use in the database table).
         * <p>
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /*
        dbfid for a card
        *
        * TYPE: INTEGER
        */
        public final static String COLUMN_CARD_DBFID = "dbfid";

        /*
        card Id of the card
        *
        * TYPE: String
        */
        public final static String COLUMN_CARD_ID = "cardId";

        /*
        name of the card
        *
        * TYPE: STRING
        */
        public final static String COLUMN_CARD_NAME = "name";

        /*
        Mana Cost of the card
        *
        * TYPE: INTEGER
        */
        public final static String COLUMN_CARD_COST = "manaCost";


        /*
        Rarity of the card
        *
        * TYPE: INTEGER
        */
        public final static String COLUMN_CARD_RARITY = "rarity";

        /*Card Type
         *
         * TYPE: STRING
         */
        public final static String COLUMN_CARD_TYPE = "type";

        /*
        Card Class
        *
        * TYPE: INTEGER
         */
        public final static String COLUMN_CARD_CLASS = "cardClass";

        //Constant Values
        public final static String RARITY_FREE = "FREE";
        public final static String RARITY_COMMON = "COMMON";
        public final static String RARITY_RARE = "RARE";
        public final static String RARITY_EPIC = "EPIC";
        public final static String RARITY_LEGENDARY = "LEGENDARY";

        public final static String TYPE_HERO = "HERO";
        public final static String TYPE_MINION = "MINION";
        public final static String TYPE_SPELL = "SPELL";
        public final static String TYPE_WEAPON = "WEAPON";
    }

    public static final class DecksEntry{
        //Database Columns
        public final static String TABLE_NAME = "decks";

        /**
         * Unique ID number for the card (only for use in the database table).
         * <p>
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /*Deck String
         *
         * TYPE: STRING
         */
        public final static String COLUMN_DECK_STRING = "deckString";

        /*Deck Format
         *
         * TYPE: STRING
         */
        public final static String COLUMN_DECK_FORMAT = "format";

        /*Deck Class
         *
         * TYPE: STRING
         */
        public final static String COLUMN_DECK_CLASS = "class";

        /*Deck Class
         *
         * TYPE: STRING
         */
        public final static String COLUMN_DECK_NAME = "deckName";

        /*Number of 0 cost cards
        *
        * TYPE: INTEGER
         */
        public final static String COLUMN_NO_OF_0_COST_CARDS = "noOf0CostCards";

        /*Number of 1 cost cards
         *
         * TYPE: INTEGER
         */
        public final static String COLUMN_NO_OF_1_COST_CARDS = "noOf1CostCards";

        /*Number of 2 cost cards
         *
         * TYPE: INTEGER
         */
        public final static String COLUMN_NO_OF_2_COST_CARDS = "noOf2CostCards";

        /*Number of 3 cost cards
         *
         * TYPE: INTEGER
         */
        public final static String COLUMN_NO_OF_3_COST_CARDS = "noOf3CostCards";

        /*Number of 4 cost cards
         *
         * TYPE: INTEGER
         */
        public final static String COLUMN_NO_OF_4_COST_CARDS = "noOf4CostCards";

        /*Number of 5 cost cards
         *
         * TYPE: INTEGER
         */
        public final static String COLUMN_NO_OF_5_COST_CARDS = "noOf5CostCards";

        /*Number of 6 cost cards
         *
         * TYPE: INTEGER
         */
        public final static String COLUMN_NO_OF_6_COST_CARDS = "noOf6CostCards";

        /*Number of 7 cost cards
         *
         * TYPE: INTEGER
         */
        public final static String COLUMN_NO_OF_7_COST_CARDS = "noOf7CostCards";

        /*Number of Minions
        *
        * TYPE: INTEGER
         */
        public final static String COLUMN_NO_OF_MINIONS = "noOfMinions";

        /*Number of Spells
         *
         * TYPE: INTEGER
         */
        public final static String COLUMN_NO_OF_SPELLS = "noOfSpells";

        /*Number of Weapaons
         *
         * TYPE: INTEGER
         */
        public final static String COLUMN_NO_OF_WEAPONS = "noOfWepaons";

        /*Number of Heroes
         *
         * TYPE: INTEGER
         */
        public final static String COLUMN_NO_OF_HEROES = "noOfHeroes";

        /*Cost of the Deck
        *
        * TYPE: INTEGER
         */
        public final static String COLUMN_DECK_COST = "deckCost";

        /**
         * Flag indicating whether the Deck is Favorite or not
         * 0 if not favorite, 1 if favorite
         *
         * TYPE: INTEGER
         */
        public final static String IS_FAVORITE = "isFavorite";

        public final static int FAVORITE = 1;
        public final static int NOT_FAVORITE = 0;

        //Constant Values
        public static final String FORMAT_WILD = "Wild";
        public static final String FORMAT_STANDARD = "Standard";
        public static final String FORMAT_INVALID = "Invalid Format";

        public static final String CLASS_WARRIOR = "Warrior";
        public static final String CLASS_SHAMAN = "Shaman";
        public static final String CLASS_ROGUE = "Rogue";
        public static final String CLASS_PALADIN = "Paladin";
        public static final String CLASS_HUNTER = "Hunter";
        public static final String CLASS_DRUID = "Druid";
        public static final String CLASS_WARLOCK = "Warlock";
        public static final String CLASS_MAGE = "Mage";
        public static final String CLASS_PRIEST = "Priest";
        public static final String CLASS_UNKNOWN = "Unknown Class";

    }

}

