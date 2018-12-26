package com.joycity.intern.areyoumafia;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;
import java.util.Map;

public class GameRecylcerViewAdapter extends RecyclerView.Adapter<GameRecylcerViewHolder> {
    private List<ChatInfo> items;

    public GameRecylcerViewAdapter(List<ChatInfo> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public GameRecylcerViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view;
        if(i == 0){
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_chat_main,viewGroup,false);
        }
        else if(i == 1){
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_chat_other,viewGroup,false);
        }else{
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_chat_join,viewGroup,false);
        }
        return new GameRecylcerViewHolder(view, i);
    }

    @Override
    public void onBindViewHolder(@NonNull GameRecylcerViewHolder gameRecylcerViewHolder, int i) {
        if(items.get(i).who == 0){
            gameRecylcerViewHolder.contents.setText(items.get(i).contents);
        }else if(items.get(i).who == 1){
            gameRecylcerViewHolder.profile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });

            gameRecylcerViewHolder.contents.setText(items.get(i).contents);
        }else{
            gameRecylcerViewHolder.contents.setText(items.get(i).contents);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).who;
    }

    public void addItems(List<ChatInfo> items){
        this.items = items;
    }

}
