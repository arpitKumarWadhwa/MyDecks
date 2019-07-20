package com.kumar.ak.arpit.mydecks;

public class ListViewItem {
    private Cards card;
    private String classOrNeutral;
    private int noOfClassOrNeutralCards;

    private int type;

    public ListViewItem(Cards card, String classOrNeutral, int noOfClassOrNeutralCards, int type) {
        this.card = card;
        this.classOrNeutral = classOrNeutral;
        this.noOfClassOrNeutralCards = noOfClassOrNeutralCards;
        this.type = type;
    }

    public Cards getCard() {
        return card;
    }

    public void setCard(Cards card) {
        this.card = card;
    }

    public String getClassOrNeutral() {
        return classOrNeutral;
    }

    public void setClassOrNeutral(String classOrNeutral) {
        this.classOrNeutral = classOrNeutral;
    }

    public int getNoOfClassCards() {
        return noOfClassOrNeutralCards;
    }

    public void setNoOfClassCards(int noOfClassOrNeutralCards) {
        this.noOfClassOrNeutralCards = noOfClassOrNeutralCards;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
