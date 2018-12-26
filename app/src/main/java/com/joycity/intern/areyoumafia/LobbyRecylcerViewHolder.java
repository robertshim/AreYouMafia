package com.joycity.intern.areyoumafia;

import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

public class LobbyRecylcerViewHolder extends RecyclerView.ViewHolder {
    public CardView room;
    public TextView room_name;
    public TextView room_person;
    public LobbyRecylcerViewHolder(@NonNull View itemView) {
        super(itemView);

        room = itemView.findViewById(R.id.room);
        room_name = itemView.findViewById(R.id.room_name);
        room_person = itemView.findViewById(R.id.room_person);
    }
}
