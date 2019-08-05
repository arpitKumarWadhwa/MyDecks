package com.kumar.ak.arpit.mydecks;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Comment;

import java.util.ArrayList;

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.MyViewHolder> {

    private ArrayList<String> dataSet;

    Context context;

    public interface OnFolderLongClick{
        void onFolderLongClick(View v, int position);
    }

    OnFolderLongClick onFolderLongClickListener;


    public static class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView folderName;
        private CardView folderContainer;

        public MyViewHolder(View itemView) {
            super(itemView);
            this.folderName = itemView.findViewById(R.id.folder_name);
            this.folderContainer = itemView.findViewById(R.id.folder_container);
        }
    }


    public FolderAdapter(ArrayList<String> data, Context context) {
        this.dataSet = data;
        this.context = context;
    }

    public void setOnFolderLongClickListener(OnFolderLongClick onFolderLongClickListener) {
        this.onFolderLongClickListener = onFolderLongClickListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent,
                                           int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.folder_item, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }


    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int listPosition) {
        String folderNameString = dataSet.get(listPosition);
        holder.folderName.setText(folderNameString);

        holder.folderContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String folderName = dataSet.get(listPosition);
                Intent i = new Intent(context, DeckBoxActivity.class);
                i.putExtra("folderName", folderName);
                context.startActivity(i);
            }
        });

        holder.folderContainer.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onFolderLongClickListener.onFolderLongClick(v, listPosition);
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataSet.size();

    }

}
