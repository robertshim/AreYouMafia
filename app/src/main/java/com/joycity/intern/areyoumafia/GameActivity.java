package com.joycity.intern.areyoumafia;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
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
                    items.add(chatInfo);
                    adapter.addItems(items);
                }else if(message.type == MessageType.VOTE){

                }else if(message.type == MessageType.QUIT){
                    joinPerson--;
                    numOfPerson.setText("참가 인원수 : "+String.valueOf(joinPerson));
                    chatInfo.contents = message.text+"님이 퇴장하셨습니다.";
                    chatInfo.who = 2;
                    Log.d("gameLog",message.text);
                    items.add(chatInfo);
                    adapter.addItems(items);
                }else if(message.type == MessageType.JOB){
                    items.clear();
                    adapter.addItems(items);
                    btn_send.setEnabled(false);
                    beforeGame.setVisibility(View.GONE);
                    gameStart.setVisibility(View.VISIBLE);
                    alivePerson = 6;
                    alive_person.setText("살아남은 인원 수 : " + alivePerson);
                    if(message.text.compareTo("마피아") == 0){
                        yourJob.setTextColor(Color.parseColor("#ff0000"));
                    }else if(message.text.compareTo("경찰") == 1){
                        yourJob.setTextColor(Color.parseColor("#00ff00"));
                    }
                    yourJob.setText(message.text);
                }else if(message.type == MessageType.START){
                    Toast.makeText(getApplicationContext(), "채팅을 시작합니다.", Toast.LENGTH_LONG).show();
                    btn_send.setEnabled(true);
                }
            }
        };
        adapter = new GameRecylcerViewAdapter(items);

        chat_list.setAdapter(adapter);
        chat_list.setLayoutManager(new LinearLayoutManager(this));
        writer = ((ApplicationController)getApplicationContext()).getId();
        socketThread = new SocketThread();
        flagConnection = true;
        isConnection = false;
        socketThread.start();
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
                GameMessage data = new GameMessage(5, info.id, writer, writer + "님이 입장하셨습니다.");
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

}

