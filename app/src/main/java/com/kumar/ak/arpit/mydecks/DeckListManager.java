package com.kumar.ak.arpit.mydecks;

import java.util.ArrayList;
import java.util.Collections;

public class DeckListManager {

    public static final int FORMAT_WILD = 1;
    public static final int FORMAT_STANDARD = 2;

    private int noOf1QuantityCards;
    private int noOf2QuantityCards;
    private int noOf0CostCards;
    private int noOf1CostCards;
    private int noOf2CostCards;
    private int noOf3CostCards;
    private int noOf4CostCards;
    private int noOf5CostCards;
    private int noOf6CostCards;
    private int noOf7CostCards; //Contains all cards that cost 7 or more mana
    private int noOfMinions;
    private int noOfSpells;
    private int noOfWeapons;
    private int noOfHeroes;
    public int deckCost;
    private String deckName;
    private String playableClass;
    private String format;
    private String deckString;
    private int id; //The id with which it is stored in database
    private boolean isFavorite;

    //Sort the deck list in ascending order by mana cost
    public ArrayList<Cards> sortByMana(ArrayList<Cards> deck) {
        if (deck.size() == 0)
            return null;
        else
            for (int i = 0; i < deck.size(); i++) {
                for (int j = 0; j < deck.size() - i - 1; j++) {
                    if (deck.get(j).getCost() > deck.get(j + 1).getCost()) {
                        //Swap
                        Collections.swap(deck, j, j + 1);
                    }
                }
            }
        return deck;
    }

    //Sort the same mana cards in ascending order lexicographically
    public ArrayList<Cards> sortLexicographically(ArrayList<Cards> deck) {
        if (deck.size() == 0)
            return null;
        else
            for (int i = 0; i < deck.size(); i++) {
                for (int j = 0; j < deck.size() - i - 1; j++) {
                    if (deck.get(j).getCost() == deck.get(j + 1).getCost()) {
                        if(deck.get(j).getName().compareToIgnoreCase(deck.get(j+1).getName()) > 0) {
                            //Swap
                            Collections.swap(deck, j, j + 1);
                        }
                    }
                }
            }
        return deck;
    }

    public DeckListManager(String deckString) {
        this.deckString = deckString;
        initialize();
    }

    public DeckListManager(){
        initialize();
    }

    public void initialize(){
        noOf0CostCards = 0;
        noOf1CostCards = 0;
        noOf2CostCards = 0;
        noOf3CostCards = 0;
        noOf4CostCards = 0;
        noOf5CostCards = 0;
        noOf6CostCards = 0;
        noOf7CostCards = 0;
        noOfMinions = 0;
        noOfSpells = 0;
        noOfWeapons = 0;
        noOfHeroes = 0;
        deckCost = 0;
        isFavorite = false;
    }

    public String getDeckString() {
        return deckString;
    }

    public String getPlayableClass() {
        return playableClass;
    }

    public void setPlayableClass(String playableClass) {
        this.playableClass = playableClass;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDeckName() {
        return deckName;
    }

    public void setDeckName(String deckName) {
        this.deckName = deckName;
    }

    public void setDeckString(String deckString) {
        this.deckString = deckString;
    }

    public int getNoOf0CostCards() {
        return noOf0CostCards;
    }

    public void incrementNoOf0CostCards() {
        this.noOf0CostCards++;
    }

    public int getNoOf1CostCards() {
        return noOf1CostCards;
    }

    public void incrementNoOf1CostCards() {
        this.noOf1CostCards++;
    }

    public int getNoOf2CostCards() {
        return noOf2CostCards;
    }

    public void incrementNoOf2CostCards() {
        this.noOf2CostCards++;
    }

    public int getNoOf3CostCards() {
        return noOf3CostCards;
    }

    public void incrementNoOf3CostCards() {
        this.noOf3CostCards++;
    }

    public int getNoOf4CostCards() {
        return noOf4CostCards;
    }

    public void incrementNoOf4CostCards() {
        this.noOf4CostCards++;
    }

    public int getNoOf5CostCards() {
        return noOf5CostCards;
    }

    public void incrementNoOf5CostCards() {
        this.noOf5CostCards++;
    }

    public int getNoOf6CostCards() {
        return noOf6CostCards;
    }

    public void incrementNoOf6CostCards() {
        this.noOf6CostCards++;
    }

    public int getNoOf7CostCards() {
        return noOf7CostCards;
    }

    public void incrementNoOf7CostCards() {
        this.noOf7CostCards++;
    }

    public int getNoOfMinions() {
        return noOfMinions;
    }

    public void incrementNoOfMinions() {
        this.noOfMinions++;
    }

    public int getNoOfSpells() {
        return noOfSpells;
    }

    public void incrementNoOfSpells() {
        this.noOfSpells++;
    }

    public int getNoOfWeapons() {
        return noOfWeapons;
    }

    public void incrementNoOfWeapons() {
        this.noOfWeapons++;
    }

    public int getNoOfHeroes() {
        return noOfHeroes;
    }

    public void incrementNoOfHeroes() {
        this.noOfHeroes++;
    }

    public int getDeckCost() {
        return deckCost;
    }

    public void setNoOf0CostCards(int noOf0CostCards) {
        this.noOf0CostCards = noOf0CostCards;
    }

    public void setNoOf1CostCards(int noOf1CostCards) {
        this.noOf1CostCards = noOf1CostCards;
    }

    public void setNoOf2CostCards(int noOf2CostCards) {
        this.noOf2CostCards = noOf2CostCards;
    }

    public void setNoOf3CostCards(int noOf3CostCards) {
        this.noOf3CostCards = noOf3CostCards;
    }

    public void setNoOf4CostCards(int noOf4CostCards) {
        this.noOf4CostCards = noOf4CostCards;
    }

    public void setNoOf5CostCards(int noOf5CostCards) {
        this.noOf5CostCards = noOf5CostCards;
    }

    public void setNoOf6CostCards(int noOf6CostCards) {
        this.noOf6CostCards = noOf6CostCards;
    }

    public void setNoOf7CostCards(int noOf7CostCards) {
        this.noOf7CostCards = noOf7CostCards;
    }

    public void setNoOfMinions(int noOfMinions) {
        this.noOfMinions = noOfMinions;
    }

    public void setNoOfSpells(int noOfSpells) {
        this.noOfSpells = noOfSpells;
    }

    public void setNoOfWeapons(int noOfWeapons) {
        this.noOfWeapons = noOfWeapons;
    }

    public void setNoOfHeroes(int noOfHeroes) {
        this.noOfHeroes = noOfHeroes;
    }

    public void setDeckCost(int deckCost) {
        this.deckCost = deckCost;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }
}

