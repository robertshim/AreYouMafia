package com.joycity.intern.areyoumafia;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.gson.Gson;
import com.google.gson.JsonArray;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LobbyActivity extends AppCompatActivity {
    private RecyclerView room_list;
    private Button btn_make,btn_fast;
    private List<RoomInfo> items;
    private LobbyRecyclerViewAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        btn_make = findViewById(R.id.make_room);
        btn_fast = findViewById(R.id.fast_start);

        room_list = findViewById(R.id.room_list);

        items = new ArrayList<>();
        adapter = new LobbyRecyclerViewAdapter(items,this);
        room_list.setAdapter(adapter);
        room_list.setLayoutManager(new LinearLayoutManager(this));

        btn_make.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makeRoom();
            }
        });

        btn_fast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestQueue();
            }
        });
        items = new ArrayList<>();
    }



    private void searchRooms(){
        ApplicationController controller = (ApplicationController)getApplicationContext();
        String sessionkey = controller.getSessionkey();
        final Call<Map<String, Object>> getRooms = controller.getNetworkService().getRooms(sessionkey);

        getRooms.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {

                if(response.isSuccessful()){
                    Log.d("error_home","Search Room");
                    Log.d("error_home",response.body().get("body").toString());
                    Gson gson = new Gson();
                    RoomInfo info = null;
                    try{
                    JSONArray jsonArray = new JSONArray(response.body().get("body").toString());
                        for(int i=0; i < jsonArray.length(); i++){
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            int id = jsonObject.getInt("id");
                            String url = jsonObject.getString("url");
                            int port = jsonObject.getInt("port");
                            int numOfPlayer = jsonObject.getInt("numOfPlayer");
                            info = new RoomInfo(id,url,port,numOfPlayer);
                        }
                        items.clear();
                        if(info != null){
                            items.add(info);
                        }
                    }
                    catch (Exception e){
                    }
                    adapter.addItems(items);
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {

            }
        });
    }


    private void makeRoom(){
        ApplicationController controller = (ApplicationController)getApplicationContext();
        String sessionkey = controller.getId();
        Call<Map<String, Object>> postRooms = controller.getNetworkService().postRooms(sessionkey);
        postRooms.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {

                if(response.isSuccessful()){
                    Log.d("error_home","Make Room");
                    try{
                        JSONObject jsonObject = new JSONObject(response.body().get("body").toString());
                        int id = jsonObject.getInt("id");
                        String url = jsonObject.getString("url");
                        int port = jsonObject.getInt("port");
                        int numOfPlayer = jsonObject.getInt("numOfPlayer");
                        RoomInfo info = new RoomInfo(id,url,port,numOfPlayer);
                        Log.d("error_home",response.body().get("body").toString());
                        Intent intent = new Intent(getApplicationContext(), GameActivity.class);
                        intent.putExtra("info",info);
                        startActivity(intent);

                    }catch (Exception e){

                    }
                }

            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {

            }
        });

    }


    private void requestQueue(){
        ApplicationController controller = (ApplicationController)getApplicationContext();
        String sessionkey = controller.getSessionkey();
        Call<Map<String, Object>> requestMatching = controller.getNetworkService().requestMatching(sessionkey);
        requestMatching.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {

            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {

            }
        });
    }

    public void enterTheRoom(int room_id){
        ApplicationController controller = (ApplicationController)getApplicationContext();
        String sessionkey = controller.getId();
        Call<Map<String, Object>> getRoom = controller.getNetworkService().getRoom(sessionkey,room_id);
        getRoom.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if(response.isSuccessful()){
                    Log.d("error_home","Enter Room");
                    //RoomInfo info = (RoomInfo) response.body().get("body");
                    try{
                        JSONObject jsonObject = new JSONObject(response.body().get("body").toString());
                        int id = jsonObject.getInt("id");
                        String url = jsonObject.getString("url");
                        int port = jsonObject.getInt("port");
                        int numOfPlayer = jsonObject.getInt("numOfPlayer");
                        RoomInfo info = new RoomInfo(id,url,port,numOfPlayer);

                        Intent intent = new Intent(getApplicationContext(), GameActivity.class);
                        intent.putExtra("info",info);
                        startActivity(intent);

                    }catch (Exception e){

                    }
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        searchRooms();
    }
}
