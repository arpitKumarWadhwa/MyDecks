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


import com.kumar.ak.arpit.mydecks.data.DecksContract;

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
                case DecksContract.DecksEntry.CLASS_WARRIOR:
                    imageResource = R.drawable.warrior;
                    break;
                case DecksContract.DecksEntry.CLASS_PALADIN:
                    imageResource = R.drawable.paladin;
                    break;
                case DecksContract.DecksEntry.CLASS_HUNTER:
                    imageResource = R.drawable.hunter;
                    break;
                case DecksContract.DecksEntry.CLASS_DRUID:
                    imageResource = R.drawable.druid;
                    break;
                case DecksContract.DecksEntry.CLASS_ROGUE:
                    imageResource = R.drawable.rogue;
                    break;
                case DecksContract.DecksEntry.CLASS_SHAMAN:
                    imageResource = R.drawable.shaman;
                    break;
                case DecksContract.DecksEntry.CLASS_PRIEST:
                    imageResource = R.drawable.priest;
                    break;
                case DecksContract.DecksEntry.CLASS_MAGE:
                    imageResource = R.drawable.mage;
                    break;
                case DecksContract.DecksEntry.CLASS_WARLOCK:
                    imageResource = R.drawable.warlock;
                    break;
                case DecksContract.DecksEntry.CLASS_DEMON_HUNTER:
                    imageResource = R.drawable.demon_hunter;
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

