package com.joycity.intern.areyoumafia;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class GameActivity extends AppCompatActivity {
    private List<ChatInfo> items;
    private RoomInfo info;
    private RecyclerView chat_list;
    private TextView room_name;
    private TextView numOfPerson;
    private GameRecylcerViewAdapter adapter;
    private ImageView btn_send;
    private EditText input_chat;
    private Handler handler;
    private boolean flagConnection;
    private boolean isConnection;

    private WriteThread wt;
    private ReadThread rt;
    private SocketThread socketThread;
    private Handler writeHandler;
    private boolean flagRead;
    private int joinPerson = 0;
    private String writer;
    private Socket socket;

    private RelativeLayout beforeGame;
    private RelativeLayout gameStart;
    private TextView yourJob;
    private TextView alive_person;
    private int alivePerson = 0;
    private int mSelected;
    private ProgressDialog progressDialog;
    private String yourJobValue;
    private List<String> aliveList;
    private AlertDialog jobDialog;
    private boolean isStart = false;
    private String dead1, dead2;
    private FloatingActionButton btn_ready;
    private boolean isReady = false;
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
        final public static int READY = 17;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        info = getIntent().getParcelableExtra("info");
        if(info ==null){
            Log.d("error_home","error");
        }
        items = new ArrayList<>();
        joinPerson = info.numOfPlayer;
        chat_list = findViewById(R.id.chat_list);
        btn_send = findViewById(R.id.btn_send);
        input_chat = findViewById(R.id.input_chat);
        room_name = findViewById(R.id.room_id);
        numOfPerson = findViewById(R.id.numOfPerson);

        btn_ready = findViewById(R.id.btn_ready);
        btn_ready.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isReady = !isReady;
                if(isReady){
                    btn_ready.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#0000ff")));
                }else{
                    btn_ready.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#ff0000")));
                }
                GameMessage gameMessage = new GameMessage(MessageType.READY,info.id,writer,"");
                Message message = new Message();
                message.obj = gameMessage;
                writeHandler.sendMessage(message);
            }
        });

        room_name.setText(String.valueOf(info.id));
        beforeGame = findViewById(R.id.beforeGame);
        gameStart = findViewById(R.id.gameStart);
        yourJob = findViewById(R.id.your_job);
        alive_person = findViewById(R.id.alive_person);

        gameStart.setVisibility(View.GONE);
        beforeGame.setVisibility(View.VISIBLE);

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GameMessage gameMessage = new GameMessage(MessageType.CHAT,info.id,writer,input_chat.getText().toString());
                Message message = new Message();
                message.obj = gameMessage;
                writeHandler.sendMessage(message);
                input_chat.setText("");
            }
        });

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                GameMessage message = (GameMessage) msg.obj;
                ChatInfo chatInfo = new ChatInfo();
                if(message.type == MessageType.JOIN){
                    joinPerson++;
                    alivePerson = joinPerson;
                    numOfPerson.setText("참가 인원수 : "+String.valueOf(joinPerson));
                    chatInfo.contents = message.text;
                    chatInfo.who = 2;
                    Log.d("gameLog",message.text);
                    items.add(chatInfo);
                    adapter.addItems(items);
                }else if(message.type == MessageType.CHAT){
                    if(writer.compareTo(message.writer) == 0){
                        chatInfo.who = 0;
                    }else{
                        chatInfo.who = 1;
                    }
                    //메세지를 보낸 사람이 누구냐에 따라서 who 값이 바뀐다.
                    chatInfo.contents = message.text;
                    chatInfo.id = message.writer;
                    Log.d("gameLog",chatInfo.contents);
                    items.add(chatInfo);
                    adapter.addItems(items);
                }else if(message.type == MessageType.VOTE){
                    btn_send.setEnabled(false);
                    makeVoteDialog("투표를 시작합니다.",message.type);
                }else if(message.type == MessageType.QUIT){
                    joinPerson = Integer.valueOf(message.text);
                    alivePerson = joinPerson;
                    if(isStart == true){
                        alive_person.setText("살아남은 인원 수 : " + alivePerson);
                    }else{
                        numOfPerson.setText("참가 인원수 : "+String.valueOf(joinPerson));
                    }
                    chatInfo.contents = message.writer+"님이 퇴장하셨습니다.";
                    chatInfo.who = 2;
                    Log.d("gameLog",message.text);
                    items.add(chatInfo);
                    adapter.addItems(items);
                }else if(message.type == MessageType.JOB){
                    isReady = false;
                    btn_ready.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#ff0000")));
                    isStart = true;
                    items.clear();
                    adapter.addItems(items);
                    btn_send.setEnabled(false);
                    beforeGame.setVisibility(View.GONE);
                    gameStart.setVisibility(View.VISIBLE);
                    yourJobValue = message.text;
                    Log.d("gameLog",message.text);
                    if(message.text.compareTo("MAFIA") == 0){
                        yourJob.setTextColor(Color.parseColor("#ff0000"));
                        yourJob.setText("마피아");
                    }else if(message.text.compareTo("POLICE") == 0){
                        yourJob.setTextColor(Color.parseColor("#0000ff"));
                        yourJob.setText("경찰");
                    }else{
                        yourJob.setText("시민");
                    }
                }else if(message.type == MessageType.START){
                    dead1 = "";
                    dead2 = "";
                    aliveList.clear();
                    alive_person.setText("살아남은 인원 수 : " + alivePerson);
                    StringTokenizer stringTokenizer = new StringTokenizer(message.text,",");
                    while(stringTokenizer.hasMoreTokens()){
                        aliveList.add(stringTokenizer.nextToken());
                    }
                    joinPerson = aliveList.size();
                    alivePerson = joinPerson;
                    Log.d("gameLog",message.text);
                    Toast.makeText(getApplicationContext(), "채팅을 시작합니다.", Toast.LENGTH_LONG).show();
                    btn_send.setEnabled(true);
                }else if(message.type == MessageType.ACTION){
                    progressDialog.dismiss();
                    if(yourJobValue.compareTo("MAFIA") == 0){
                        makeVoteDialog("대상을 선택하세요",message.type);
                    }else if(yourJobValue.compareTo("POLICE") == 0){
                        makeVoteDialog("대상을 선택하세요",message.type);
                    }else{
                        makeProgressDialog("활동 완료를 대기 중");
                    }
                }else if(message.type == MessageType.ACTION_RESULT){
                    showJob(message);
                }else if(message.type == MessageType.VOTE_RESULT){
                    progressDialog.dismiss();
                    Intent intent = new Intent(getApplicationContext(),ResultActivity.class);
                    intent.putExtra("message",message);
                    startActivityForResult(intent,1000);
                }else if(message.type == MessageType.GAME_RESULT){
                    Intent intent = new Intent(getApplicationContext(),ResultActivity.class);
                    intent.putExtra("message",message);
                    startActivityForResult(intent,2000);
                    btn_send.setEnabled(true);
                }
            }
        };
        adapter = new GameRecylcerViewAdapter(items);
        aliveList = new ArrayList<>();
        chat_list.setAdapter(adapter);
        chat_list.setLayoutManager(new LinearLayoutManager(this));
        writer = ((ApplicationController)getApplicationContext()).getId();
        socketThread = new SocketThread();
        flagConnection = true;
        isConnection = false;
        socketThread.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 1000){
            Log.d("gameLog",dead1);
            GameMessage gameMessage = new GameMessage(MessageType.VOTE_RESULT,info.id,writer,"");
            Log.d("gameLog","Vote Result "+writer);
            Message message = new Message();
            message.obj = gameMessage;
            writeHandler.sendMessage(message);
            dead1 = data.getStringExtra("dead");
            if( dead1.compareTo(writer) == 0){
                try{
                    socket.close();
                    flagConnection = false;
                    flagRead = false;
                    writeHandler.getLooper().quit();
                    finish();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }else{
            if(resultCode == RESULT_OK){
                isStart = false;
                items.clear();
                adapter.addItems(items);
                beforeGame.setVisibility(View.VISIBLE);
                gameStart.setVisibility(View.GONE);
                numOfPerson.setText("참가 인원수 : "+String.valueOf(joinPerson));
            }
            else{
                dead2 = data.getStringExtra("dead");
                Log.d("gameLog","kill" + dead2);
                if( dead2.compareTo(writer) == 0){
                    try{
                        socket.close();
                        flagConnection = false;
                        flagRead = false;
                        writeHandler.getLooper().quit();
                        finish();
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void showJob(GameMessage message){

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String job;
        if(message.text.compareTo("MAFIA") == 0){
            job = "마피아";
        }else if(message.text.compareTo("POLICE") == 0){
            job = "경찰";
        }else{
            job = "시민";
        }
        builder.setTitle(message.writer).setMessage("직업은 "+ job+" 입니다.")
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        GameMessage gameMessage = new GameMessage(MessageType.ACTION_RESULT,info.id,writer,"");
                        Message message = new Message();
                        message.obj = gameMessage;
                        writeHandler.sendMessage(message);
                        makeProgressDialog("활동 완료를 대기 중");
                    }
                });

        jobDialog = builder.create();
        jobDialog.show();
    }
    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("안내").setMessage("방에서 나가시겠습니까?")
                .setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try{
                            socket.close();
                        }catch (IOException e){

                        }
                        flagConnection = false;
                        flagRead = false;
                        writeHandler.getLooper().quit();
                        finish();
                    }
                }).setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    //연결이 끊겼는지를 확인하는 소켓
    private class SocketThread extends Thread{
        private OutputStream out;
        private InputStream in;
        @Override
        public void run() {
            while(flagConnection){
                try{
                    if(!isConnection){
                        socket = new Socket();
                        SocketAddress remoteAddr = new InetSocketAddress(info.url,info.port);
                        socket.connect(remoteAddr, 10000);
                        Log.d("gameLog","연결 완료");
                        out = socket.getOutputStream();
                        in = socket.getInputStream();

                        if(rt != null){
                            flagRead = false;
                        }
                        if(wt !=null){
                            writeHandler.getLooper().quit();
                        }
                        wt = new WriteThread(out);
                        rt = new ReadThread(in);

                        wt.start();
                        rt.start();

                        isConnection = true;
                        flagRead = true;
                    }else{
                        SystemClock.sleep(10000);
                    }
                }catch (IOException e){
                    e.printStackTrace();
                    SystemClock.sleep(10000);
                }


            }
        }
    }


    //보내기를 누르면 전송시켜주는 스레드
    private class WriteThread extends Thread{
        private OutputStream out;

        public WriteThread(OutputStream out) {
            this.out = out;
        }

        @Override
        public void run() {
            Log.d("gameLog","Write 스레드");

            //최초 1회
            try{
                GameMessage data = new GameMessage(MessageType.JOIN, info.id, writer, writer + "님이 입장하셨습니다.");
                out.write(data.toBytes());
                out.flush();
            }catch (IOException e){
                e.printStackTrace();
            }


            Looper.prepare();
            writeHandler = new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    try{
                        GameMessage data = (GameMessage) msg.obj;
                        out.write(data.toBytes());
                        Log.d("gameLog","write OK");
                        out.flush();
                    }catch (IOException e){
                        e.printStackTrace();
                        isConnection = false;
                        writeHandler.getLooper().quit();
                        flagRead = false;
                    }
                }
            };

            Looper.loop();
        }
    }

    //지속적으로 읽는 스레드
    private class ReadThread extends Thread{
        private InputStream in;

        public ReadThread(InputStream in) {
            this.in = in;
        }

        @Override
        public void run() {
            Log.d("gameLog","Read 스레드");
            GameMessage gameMessage = null;
            byte[] buffer = null;
            while(flagRead){
                buffer = new byte[1024];
                Log.d("gameLog","Read 대기중");
                try{
                    int size = in.read(buffer);
                    if(size >0){
                        gameMessage = new GameMessage(buffer);
                        Message message = new Message();
                        message.obj = gameMessage;
                        handler.sendMessage(message);
                    }else{
                        flagRead = false;
                        isConnection = false;
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    flagRead = false;
                    isConnection = false;
                }
            }
        }
    }


    private void makeVoteDialog(String string, final int type){
        final CharSequence[] charSequences = new CharSequence[aliveList.size() - 1];
        int j = 0;
        for (int i = 0 ; i<aliveList.size(); i++){
            //자신을 뺀 나머지를 생각한다.
            if(aliveList.get(i).compareTo(writer) != 0){
                charSequences[j++] = aliveList.get(i);
                Log.d("gameLog",aliveList.get(i));
                Log.d("gameLog",charSequences[j-1].toString());
            }
        }
        AlertDialog.Builder oDialog = new AlertDialog.Builder(this,
                android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);

        oDialog.setTitle(string)
                .setSingleChoiceItems(charSequences, -1, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        mSelected = which;
                    }
                })
                .setNeutralButton("선택", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        Log.d("gameLog","투표 결과 보내기 전");
                        if(type == MessageType.VOTE){
                            Log.d("gameLog",String.valueOf(mSelected));
                            GameMessage data = new GameMessage(MessageType.VOTE, info.id, writer, charSequences[mSelected].toString());
                            Message message = new Message();
                            message.obj = data;
                            writeHandler.sendMessage(message);
                            Log.d("gameLog","투표 결과 보내기");
                            dialog.dismiss();
                            makeProgressDialog("투표 완료를 대기 중");
                        }
                        else{
                            GameMessage data = new GameMessage(MessageType.ACTION, info.id, writer, charSequences[mSelected].toString());
                            Message message = new Message();
                            message.obj = data;
                            writeHandler.sendMessage(message);
                            dialog.dismiss();
                            if(yourJobValue.compareTo("POLICE") !=0){
                                makeProgressDialog("활동 완료를 대기 중");
                            }
                        }
                    }
                })
                .setCancelable(false)
                .show();
    }

    private void makeProgressDialog(String string){
        progressDialog = new ProgressDialog(this,
                android.R.style.Theme_DeviceDefault_Light_Dialog);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage(string);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }
}

