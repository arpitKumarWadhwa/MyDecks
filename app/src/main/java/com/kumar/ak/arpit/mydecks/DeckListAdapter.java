package com.kumar.ak.arpit.mydecks;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.request.RequestOptions;
import java.net.URL;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class DeckListAdapter extends ArrayAdapter<Cards>{

    ArrayList<Cards> res;
    int lRes;
    Context context;

    public DeckListAdapter(@NonNull Context context, int layoutRes, ArrayList<Cards> deckList) {
        super(context, layoutRes, deckList);
        res = deckList;
        this.context = context;
        lRes = layoutRes;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {


        View listItemView = null;

        if(lRes == R.layout.layout1) {

            // Check if the existing view is being reused, otherwise inflate the view
            listItemView = convertView;
            if(listItemView == null) {
                listItemView = LayoutInflater.from(getContext()).inflate(
                        R.layout.layout1, parent, false);
            }

            TextView manaCostTextView = listItemView.findViewById(R.id.mana_cost);
            String cost = String.valueOf(res.get(position).getCost());

            //Log.e("rarity: ", String.valueOf(res.get(position).getRarity()));

            switch (res.get(position).getRarity()) {
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
            nameTextView.setText(res.get(position).getName());

            TextView quantityTextView = listItemView.findViewById(R.id.card_quantity);
            quantityTextView.setText(String.valueOf(res.get(position).getQuantity()));


            final ImageView cardTileImageView = listItemView.findViewById(R.id.card_tile);
            GlideApp.with(context).load(res.get(position).getCardTileUrl()).into(cardTileImageView);
        }
        else if(lRes == R.layout.layout2){
            // Check if the existing view is being reused, otherwise inflate the view
            listItemView = convertView;
            if(listItemView == null) {
                listItemView = LayoutInflater.from(getContext()).inflate(
                        R.layout.layout2, parent, false);
            }

            QueryUtils utils = new QueryUtils();
            CircleImageView cardThumb = listItemView.findViewById(R.id.card_thumb);
            String cardId = res.get(position).getCardId();
            String thumbUrl = "https://art.hearthstonejson.com/v1/256x/" + cardId + ".jpg";
            URL url = utils.createUrl(thumbUrl);
            GlideApp.with(context).load(url).apply(new RequestOptions().override(140, 100)).into(cardThumb);

            TextView manaCost = listItemView.findViewById(R.id.mana_cost);
            manaCost.setText("("+String.valueOf(res.get(position).getCost())+")");

            TextView cardName = listItemView.findViewById(R.id.card_name);
            cardName.setText(res.get(position).getName());

            TextView cardQuantity = listItemView.findViewById(R.id.quantity);
            cardQuantity.setText("x" + String.valueOf(res.get(position).getQuantity()));

            switch (res.get(position).getRarity()) {
                case 1:
                    manaCost.setTextColor(parent.getResources().getColor(R.color.colorCommon));
                    cardName.setTextColor(parent.getResources().getColor(R.color.colorCommon));
                    cardQuantity.setTextColor(parent.getResources().getColor(R.color.colorCommon));
                    break;
                case 2:
                    manaCost.setTextColor(parent.getResources().getColor(R.color.colorRare));
                    cardName.setTextColor(parent.getResources().getColor(R.color.colorRare));
                    cardQuantity.setTextColor(parent.getResources().getColor(R.color.colorRare));
                    break;
                case 3:
                    manaCost.setTextColor(parent.getResources().getColor(R.color.colorEpic));
                    cardName.setTextColor(parent.getResources().getColor(R.color.colorEpic));
                    cardQuantity.setTextColor(parent.getResources().getColor(R.color.colorEpic));
                    break;
                case 4:
                    manaCost.setTextColor(parent.getResources().getColor(R.color.colorLegendary));
                    cardName.setTextColor(parent.getResources().getColor(R.color.colorLegendary));
                    cardQuantity.setTextColor(parent.getResources().getColor(R.color.colorLegendary));
                    break;
                default:
                    manaCost.setTextColor(parent.getResources().getColor(R.color.colorCommon));
                    cardName.setTextColor(parent.getResources().getColor(R.color.colorCommon));
                    cardQuantity.setTextColor(parent.getResources().getColor(R.color.colorCommon));
            }

        }

        return listItemView;
    }

}

