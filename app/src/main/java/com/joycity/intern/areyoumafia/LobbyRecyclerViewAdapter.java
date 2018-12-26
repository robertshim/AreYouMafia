package com.joycity.intern.areyoumafia;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public class LobbyRecyclerViewAdapter extends RecyclerView.Adapter<LobbyRecylcerViewHolder> {
    private List<RoomInfo> items;
    private LobbyActivity activity;
    public LobbyRecyclerViewAdapter(List<RoomInfo> items, LobbyActivity activity) {
        this.activity = activity;
        this.items = items;
    }

    @NonNull
    @Override
    public LobbyRecylcerViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_room,viewGroup,false);
        return new LobbyRecylcerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final LobbyRecylcerViewHolder lobbyRecylcerViewHolder, int i) {
        lobbyRecylcerViewHolder.room.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.enterTheRoom((int)items.get(lobbyRecylcerViewHolder.getAdapterPosition()).id);
            }
        });

        lobbyRecylcerViewHolder.room_name.setText(String.valueOf(items.get(i).id));
        lobbyRecylcerViewHolder.room_person.setText(items.get(i).numOfPlayer + " / 6");
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void addItems(List<RoomInfo> items){
        this.items = items;
        notifyDataSetChanged();
    }
}
