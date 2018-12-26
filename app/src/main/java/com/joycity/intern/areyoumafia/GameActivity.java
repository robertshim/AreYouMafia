package com.joycity.intern.areyoumafia;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
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

        room_name.setText(info.id);
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Message message = new Message();
                message.obj = input_chat.getText().toString();
                writeHandler.handleMessage(message);
                input_chat.setText("");
            }
        });

        handler = new Handler();
        adapter = new GameRecylcerViewAdapter(items);

        chat_list.setAdapter(adapter);
        chat_list.setLayoutManager(new LinearLayoutManager(this));
    }


    private class SocketThread extends Thread{
        private BufferedOutputStream bout;
        private BufferedInputStream bin;
        @Override
        public void run() {
            while(flagConnection){
                try{
                    if(!isConnection){
                        Socket socket = new Socket();
                        SocketAddress remoteAddr = new InetSocketAddress("",0);
                        socket.connect(remoteAddr, 10000);

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


    private class WriteThread extends Thread{
        private BufferedOutputStream bout;

        public WriteThread(BufferedOutputStream bout) {
            this.bout = bout;
        }

        @Override
        public void run() {
            Looper.prepare();
            writeHandler = new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    try{
                        final String contents = (String)msg.obj;
                        bout.write(contents.getBytes());
                        bout.flush();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                ChatInfo info = new ChatInfo();
                                info.who = 0;
                                info.contents = contents;
                                items.add(info);
                                adapter.addItems(items);
                            }
                        });
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


    private class ReadThread extends Thread{
        private BufferedInputStream bin;

        public ReadThread(BufferedInputStream bin) {
            this.bin = bin;
        }

        @Override
        public void run() {
            byte[] buffer = null;
            while(flagRead){
                buffer = new byte[1024];

                try{
                    String message = null;
                    int size = bin.read(buffer);

                    if(size >0){
                        message = new String(buffer, 0, size, "utf-8");
                        if(!message.equals("")){
                            handler.post(new Runnable() {
                                @Override
                                public void run() {

                                }
                            });
                        }
                    }else{
                        flagRead = false;
                        isConnection = false;
                    }
                }catch (IOException e){
                    e.printStackTrace();
                    flagRead = false;
                    isConnection = false;
                }
            }
        }
    }

}

