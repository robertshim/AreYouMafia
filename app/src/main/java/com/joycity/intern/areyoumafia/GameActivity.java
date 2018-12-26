package com.joycity.intern.areyoumafia;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        info = getIntent().getParcelableExtra("info");

        items = new ArrayList<>();

        chat_list = findViewById(R.id.chat_list);
        btn_send = findViewById(R.id.btn_send);
        input_chat = findViewById(R.id.input_chat);
        room_name = findViewById(R.id.room_name);
        numOfPerson = findViewById(R.id.numOfPerson);

        room_name.setText(String.valueOf(info.id));
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Message message = new Message();
                message.obj = input_chat.getText().toString();
                writeHandler.handleMessage(message);
                input_chat.setText("");
            }
        });

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                GameMessage message = (GameMessage) msg.obj;
                ChatInfo chatInfo = new ChatInfo();
                if(message.type == GameMessage.MESSAGE_TYPE.JOIN){
                    joinPerson++;
                    numOfPerson.setText("참가 인원수 : "+String.valueOf(joinPerson));
                    chatInfo.contents = message.text.toString();
                    adapter.addItems(items);
                }else if(message.type == GameMessage.MESSAGE_TYPE.CHAT){
                    //메세지를 보낸 사람이 누구냐에 따라서 who 값이 바뀐다.
                    chatInfo.contents = message.text.toString();
                }else if(message.type == GameMessage.MESSAGE_TYPE.VOTE){

                }
            }
        };
        adapter = new GameRecylcerViewAdapter(items);

        chat_list.setAdapter(adapter);
        chat_list.setLayoutManager(new LinearLayoutManager(this));

        socketThread = new SocketThread();
        socketThread.start();
    }




    //연결이 끊겼는지를 확인하는 소켓
    private class SocketThread extends Thread{
        private BufferedOutputStream bout;
        private BufferedInputStream bin;
        @Override
        public void run() {
            while(flagConnection){
                try{
                    if(!isConnection){
                        Socket socket = new Socket();
                        SocketAddress remoteAddr = new InetSocketAddress(info.url,(int)info.port);
                        socket.connect(remoteAddr, 10000);
                        Log.d("gameLog","연결 완료");
                        bout = new BufferedOutputStream(socket.getOutputStream());
                        bin = new BufferedInputStream(socket.getInputStream());

                        if(rt != null){
                            flagRead = false;
                        }
                        if(wt !=null){
                            writeHandler.getLooper().quit();
                        }
                        wt = new WriteThread(bout);
                        rt = new ReadThread(bin);

                        wt.start();
                        rt.start();

                        isConnection = true;
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
        private BufferedOutputStream bout;

        public WriteThread(BufferedOutputStream bout) {
            this.bout = bout;
        }

        @Override
        public void run() {
            Log.d("gameLog","Write 스레드");
            Looper.prepare();
            writeHandler = new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    try{
                        GameMessage gameMessage = new GameMessage();
                        gameMessage.roomId = (int)info.id;
                        gameMessage.type = GameMessage.MESSAGE_TYPE.CHAT;
                        gameMessage.text = ((String)msg.obj).toCharArray();
                        //gameMessage.write;
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        ObjectOutput out = null;
                        out = new ObjectOutputStream(bos);
                        out.writeObject(gameMessage);
                        bout.write(bos.toByteArray());
                        bout.flush();

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
        private BufferedInputStream bin;

        public ReadThread(BufferedInputStream bin) {
            this.bin = bin;
        }

        @Override
        public void run() {
            Log.d("gameLog","Read 스레드");
            GameMessage gameMessage = null;
            byte[] buffer = null;
            while(flagRead){
                buffer = new byte[1024];

                try{
                    int size = bin.read(buffer);
                    if(size >0){
                        Log.d("gameLog","수신 완료");
                        ByteArrayInputStream bis = new ByteArrayInputStream(buffer);
                        ObjectInput in = new ObjectInputStream(bis);
                        Message message = new Message();
                        message.obj = in.readObject();

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

