package com.kumar.ak.arpit.mydecks;

import android.content.Context;;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import java.util.ArrayList;


public class DecksAdapter extends ArrayAdapter<DeckListManager> {

    ArrayList<DeckListManager> res;

    public DecksAdapter(@NonNull Context context, @NonNull ArrayList<DeckListManager> decks) {
        super(context, R.layout.deck_box_item_layout, decks);
        res = decks;
    }


    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // Check if the existing view is being reused, otherwise inflate the view
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.deck_box_item_layout, parent, false);
        }

        try {
            ImageView classThumb = listItemView.findViewById(R.id.card_thumb);
            int imageResource;

            switch (res.get(position).getPlayableClass()) {
                case "Warrior":
                    imageResource = R.drawable.warrior;
                    break;
                case "Paladin":
                    imageResource = R.drawable.paladin;
                    break;
                case "Hunter":
                    imageResource = R.drawable.hunter;
                    break;
                case "Druid":
                    imageResource = R.drawable.druid;
                    break;
                case "Rogue":
                    imageResource = R.drawable.rogue;
                    break;
                case "Shaman":
                    imageResource = R.drawable.shaman;
                    break;
                case "Priest":
                    imageResource = R.drawable.priest;
                    break;
                case "Mage":
                    imageResource = R.drawable.mage;
                    break;
                case "Warlock":
                    imageResource = R.drawable.warlock;
                    break;
                default:
                    imageResource = R.drawable.ic_launcher_background;
            }

            classThumb.setImageResource(imageResource);

            TextView deckName = listItemView.findViewById(R.id.deck_name);
            deckName.setText(res.get(position).getDeckName());

            TextView format = listItemView.findViewById(R.id.format);
            format.setText(res.get(position).getFormat());

            final ImageView favoriteImage = listItemView.findViewById(R.id.favorite_image);
            if (res.get(position).isFavorite()) {
                favoriteImage.setImageResource(R.drawable.favorite_true);
                favoriteImage.setVisibility(View.VISIBLE);
            }
            else {
                favoriteImage.setImageResource(R.drawable.favorite_true);
                favoriteImage.setVisibility(View.GONE);
            }

        } catch (Exception e) {
            //e.printStackTrace();
        }

        return listItemView;
    }
}

