package com.kumar.ak.arpit.mydecks;

import android.content.Context;
import android.util.Base64;

import com.kumar.ak.arpit.mydecks.data.DecksContract;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeckDecoder {

    private int stringStart; //The starting of a deck string. Expected value is always 0
    private int encodingVersionNumber; //Generally 1
    private int format; // standard = 2, wild = 1
    private int noOfHeroes; // number of heroes in heroes array, always 1
    private int dbfHero; // DBF ID of hero
    private ArrayList<Integer> deck1 = new ArrayList<Integer>(); //Contains the dbfids of 1-quantity cards
    private int deck1Size;
    private ArrayList<Integer> deck2 = new ArrayList<Integer>(); //Contains the dbfids of 2-quantity cards
    private int deck2Size;
    private ArrayList<ArrayList<Integer>> cardsDbfidList = new ArrayList<ArrayList<Integer>>(); //Contains the dbfids of all cards

    //Hearthstone deck string is in a Base64 string with varints(variable integers)
    public int readVarInt(DataInputStream dis) throws RuntimeException, IOException {
        int numRead = 0;
        int result = 0;
        byte read;
        do {
            read = dis.readByte(); //throws IOException
            int value = (read & 0b01111111);
            result |= (value << (7 * numRead));

            numRead++;
            if (numRead > 5) {
                throw new RuntimeException("VarInt is too big");
            }
        } while ((read & 0b10000000) != 0);

        return result;
    }


    //Returns a list of dbfIds of all cards in the deck
    public ArrayList<ArrayList<Integer>> decode(String deckString) throws IOException {
        deckString = prepareDeckString(deckString).trim();
        byte[] dbfidByteArray;

        //Convert the Base64 deck string to an array of bytes
        dbfidByteArray = Base64.decode(deckString, Base64.DEFAULT);

        //Put the byte array inside a data input stream
        InputStream is = null;
        DataInputStream dis = null;

        try {
            // create new byte array input stream
            is = new ByteArrayInputStream(dbfidByteArray);

            // create data input stream
            dis = new DataInputStream(is);

            //Read the header portion of the deck string
            stringStart = readVarInt(dis);
            encodingVersionNumber = readVarInt(dis);
            format = readVarInt(dis);
            noOfHeroes = readVarInt(dis);
            dbfHero = readVarInt(dis);

            //Get the number of 1-quantity
            deck1Size = readVarInt(dis);

            //Get all the 1-quantity cards
            for (int i = 0; i < deck1Size; i++) {
                int tempDbfid = readVarInt(dis); //Temporarily storing the nxet dbfid in the deck string
                deck1.add(tempDbfid); //Adding the card to the list
            }

            //Get the number of 2-quantity cards
            deck2Size = readVarInt(dis);

            //Get all the 2-quantity cards
            for (int i = 0; i < deck2Size; i++) {
                int tempDbfid = readVarInt(dis); //Temporaily storing the nxet dbfid in the deck string
                deck2.add(tempDbfid); //Adding the card to the list
            }

            //Add all cards to the final card list
            cardsDbfidList.add(deck1);
            cardsDbfidList.add(deck2);

        } catch (Exception e) {
            // if any I/O error occurs
            e.printStackTrace();
        } finally {
            // releases any associated system files with this stream
            if (is != null)
                is.close(); //throws IOException
            if (dis != null)
                dis.close(); //throws IOException
        }
        return cardsDbfidList;
    }

    //Removes all deck name(First line and starts with '###') and all comments(Line starting with a single '#')
    public String prepareDeckString(String deckString) {

        Pattern replace = Pattern.compile("#.*(\n)?");

        Matcher regexMatcher = replace.matcher(deckString.trim());

        return regexMatcher.replaceAll("");

    }

    public String getPlayableClass(Context context, int locale, String deckString) {

        deckString = prepareDeckString(deckString).trim();
        byte[] dbfidByteArray;

        //Convert the Base64 deck string to an array of bytes
        dbfidByteArray = Base64.decode(deckString, Base64.DEFAULT);

        //Put the byte array inside a data input stream
        InputStream is = null;
        DataInputStream dis = null;

        try {
            // create new byte array input stream
            is = new ByteArrayInputStream(dbfidByteArray);

            // create data input stream
            dis = new DataInputStream(is);

            //Read the header portion of the deck string
            stringStart = readVarInt(dis);
            encodingVersionNumber = readVarInt(dis);
            format = readVarInt(dis);
            noOfHeroes = readVarInt(dis);
            dbfHero = readVarInt(dis);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Gte the playable class from the hero's dbfid
        Cards heroSkinCard = Cards.getCardByDbfid(context, dbfHero, locale);
        int playableClass = heroSkinCard.getCardClass();

        switch (playableClass) {
/*            case 7:
            case 2828:*/
            case DecksContract.DecksEntry.CARD_CLASS_WARRIOR:
                return DecksContract.DecksEntry.CLASS_WARRIOR;
            /*case 1066:
            case 40183:
            case 53237:*/
                case DecksContract.DecksEntry.CARD_CLASS_SHAMAN:
                return DecksContract.DecksEntry.CLASS_SHAMAN;
           /* case 930:
            case 40195:*/
                case DecksContract.DecksEntry.CARD_CLASS_ROGUE:
                return DecksContract.DecksEntry.CLASS_ROGUE;
         /*   case 671:
            case 2827:
            case 46116:
            case 53187:*/
                case DecksContract.DecksEntry.CARD_CLASS_PALADIN:
                return DecksContract.DecksEntry.CLASS_PALADIN;
          /*  case 31:
            case 2826:*/
                case DecksContract.DecksEntry.CARD_CLASS_HUNTER:
                return DecksContract.DecksEntry.CLASS_HUNTER;
          /*  case 274:
            case 50484:*/
          case DecksContract.DecksEntry.CARD_CLASS_DRUID:
                return DecksContract.DecksEntry.CLASS_DRUID;
            /*case 893:
            case 47817:
            case 51834:*/
                case DecksContract.DecksEntry.CARD_CLASS_WARLOCK:
                return DecksContract.DecksEntry.CLASS_WARLOCK;
       /*     case 637:
            case 2829:
            case 39117:*/
                case DecksContract.DecksEntry.CARD_CLASS_MAGE:
                return DecksContract.DecksEntry.CLASS_MAGE;
            /*case 813:
            case 41887:*/
                case DecksContract.DecksEntry.CARD_CLASS_PRIEST:
                return DecksContract.DecksEntry.CLASS_PRIEST;
            default:
                return DecksContract.DecksEntry.CLASS_UNKNOWN;
        }
    }

    public String getFormat(String deckString) {

        deckString = prepareDeckString(deckString).trim();
        byte[] dbfidByteArray;

        //Convert the Base64 deck string to an array of bytes
        dbfidByteArray = Base64.decode(deckString, Base64.DEFAULT);

        //Put the byte array inside a data input stream
        InputStream is = null;
        DataInputStream dis = null;

        try {
            // create new byte array input stream
            is = new ByteArrayInputStream(dbfidByteArray);

            // create data input stream
            dis = new DataInputStream(is);

            //Read the header portion of the deck string
            stringStart = readVarInt(dis);
            encodingVersionNumber = readVarInt(dis);
            format = readVarInt(dis);
            noOfHeroes = readVarInt(dis);
            dbfHero = readVarInt(dis);
        } catch (Exception e) {
            //e.printStackTrace();
        }

        if (format == DeckListManager.FORMAT_WILD)
            return DecksContract.DecksEntry.FORMAT_WILD;
        else if (format == DeckListManager.FORMAT_STANDARD)
            return DecksContract.DecksEntry.FORMAT_STANDARD;
        else
            return DecksContract.DecksEntry.FORMAT_INVALID;
    }

    public String getDeckName(String deckString) {
        char deck[] = deckString.toCharArray();
        String name = "";

        for (int i = 0; i < 3; i++) {
            if (deck[i] != '#') {
                return null; //Deck Name doesn't exist
            }
        }

        int i = 3;
        while (deck[i] != '\n') {
            name += deck[i];
            i++;
        }

        name = name.trim();

        //Log.e("deck name: ", name);

        return name;
    }
}

