package com.joycity.intern.areyoumafia;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


public class GameRecylcerViewHolder extends RecyclerView.ViewHolder {
    public ImageView profile;
    public TextView contents;

    public GameRecylcerViewHolder(@NonNull View itemView, int who) {
        super(itemView);

        if(who == 1){
            profile = itemView.findViewById(R.id.profile);
            contents = itemView.findViewById(R.id.other);
        }else{
            profile = null;
            contents = itemView.findViewById(R.id.main);
        }
    }
}
