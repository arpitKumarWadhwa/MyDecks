package com.kumar.ak.arpit.mydecks;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.net.URL;
import java.util.ArrayList;


public class AlternateDeckListAdapter extends ArrayAdapter<ListViewItem> {

    ArrayList<ListViewItem> res;
    Context context;
    QueryUtils utils;

    public static final int TYPE_NON_CARD = 0;
    public static final int TYPE_CARD = 1;


    public AlternateDeckListAdapter(@NonNull Context context, int layoutRes, ArrayList<ListViewItem> deckList) {
        super(context, layoutRes, deckList);
        res = deckList;
        this.context = context;
        utils = new QueryUtils();
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return res.get(position).getType();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        ListViewItem listViewItem = res.get(position);
        int listViewItemType = getItemViewType(position);

        View listItemView = null;


        // Check if the existing view is being reused, otherwise inflate the view
        listItemView = convertView;
        if (listItemView == null) {
            if (listViewItemType == TYPE_NON_CARD)
                listItemView = ((Activity) context).getLayoutInflater().inflate(R.layout.layout3, parent, false);
            else if (listViewItemType == TYPE_CARD)
                listItemView = ((Activity) context).getLayoutInflater().inflate(R.layout.layout1, parent, false);
        }


        if (listViewItemType == TYPE_CARD) {
            TextView manaCostTextView = listItemView.findViewById(R.id.mana_cost);
            String cost = String.valueOf(res.get(position).getCard().getCost());

            //Log.e("rarity: ", String.valueOf(res.get(position).getRarity()));

            switch (res.get(position).getCard().getRarity()) {
                case 1:
                    manaCostTextView.setBackgroundColor(parent.getResources().getColor(R.color.colorCommon));
                    break;
                case 2:
                    manaCostTextView.setBackgroundColor(parent.getResources().getColor(R.color.colorRare));
                    break;
                case 3:
                    manaCostTextView.setBackgroundColor(parent.getResources().getColor(R.color.colorEpic));
                    break;
                case 4:
                    manaCostTextView.setBackgroundColor(parent.getResources().getColor(R.color.colorLegendary));
                    break;
                default:
                    manaCostTextView.setBackgroundColor(parent.getResources().getColor(R.color.colorCommon));
            }

            //Adjusting the cost to the center of the mana cost box
            if (cost.length() == 1) {
                cost = " " + cost;
            }

            manaCostTextView.setText(cost);

            TextView nameTextView = listItemView.findViewById(R.id.card_name);
            nameTextView.setText(res.get(position).getCard().getName());

            TextView quantityTextView = listItemView.findViewById(R.id.card_quantity);
            quantityTextView.setText(String.valueOf(res.get(position).getCard().getQuantity()));

            final ImageView cardTileImageView = listItemView.findViewById(R.id.card_tile);
            GlideApp.with(context).load(res.get(position).getCard().getCardTileUrl()).into(cardTileImageView);

        } else if (listViewItemType == TYPE_NON_CARD) {
            TextView label = listItemView.findViewById(R.id.class_label);
            int quantity = res.get(position).getNoOfClassCards();
            String name = res.get(position).getClassOrNeutral();

            label.setText(name + " (" + String.valueOf(quantity) + ")");
        }

        return listItemView;
    }

}