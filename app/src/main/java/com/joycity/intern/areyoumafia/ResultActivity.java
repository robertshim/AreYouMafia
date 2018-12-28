package com.joycity.intern.areyoumafia;

import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

public class ResultActivity extends AppCompatActivity {
    private ConstraintLayout notSame;
    private ConstraintLayout same;
    private ConstraintLayout victory;
    private ConstraintLayout kill;

    private TextView result_id;
    private TextView result_job;
    private TextView result_same;
    private TextView title;
    private TextView victory_id;
    private TextView dead_id;
    private TextView dead_job;

    private ImageView killImage;
    private ImageView jobImage;

    private GameMessage message;

    public class MessageType {
        final public static int CREATE = 1;
        final public static int CREATE_ACK = 2;
        final public static int JOIN_REQ = 3;
        final public static int JOIN_REQ_ACK = 4;
        final public static int JOIN = 5;
        final public static int JOIN_ACK = 6;
        final public static int CHAT = 7;
        final public static int VOTE = 8;
        final public static int POLLING = 9;
        final public static int QUIT = 10;
        final public static int JOB = 11;
        final public static int START = 12;
        final public static int ACTION = 13;
        final public static int ACTION_RESULT = 14;
        final public static int VOTE_RESULT = 15;
        final public static int GAME_RESULT = 16;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_result);

        title = findViewById(R.id.title);
        notSame = findViewById(R.id.notSame);
        same = findViewById(R.id.Same);
        victory = findViewById(R.id.victory);
        kill = findViewById(R.id.kill);

        result_id = findViewById(R.id.id);
        result_job = findViewById(R.id.job);
        result_same = findViewById(R.id.sameNoti);

        victory_id = findViewById(R.id.victory_id);
        dead_id = findViewById(R.id.dead_id);
        dead_job = findViewById(R.id.dead_job);

        killImage = findViewById(R.id.killImage);
        jobImage = findViewById(R.id.jobImage);


        message = getIntent().getParcelableExtra("message");

        if(message.type == MessageType.VOTE_RESULT){
            title.setText("투표 결과");
            if(message.writer.compareTo("same") == 0){
                same.setVisibility(View.VISIBLE);
            }else{
                notSame.setVisibility(View.VISIBLE);
                result_id.setText(message.writer);
                result_job.setText(message.text);
            }
        }else{
            if(message.writer.compareTo("MAFIA") == 0){
                victory.setVisibility(View.VISIBLE);
                victory_id.setText(message.text);
                jobImage.setImageResource(R.drawable.logo);
            }else if(message.writer.compareTo("CIVIL") == 0){
                victory.setVisibility(View.VISIBLE);
                jobImage.setImageResource(R.drawable.person);
                victory_id.setVisibility(View.GONE);
            }else{
                kill.setVisibility(View.VISIBLE);
                dead_id.setText(message.writer);
                dead_job.setText(message.text);
            }

        }
    }
}
