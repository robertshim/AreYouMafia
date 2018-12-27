package com.joycity.intern.areyoumafia;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private TextView btn_signUp;
    private TextView btn_ok;
    private EditText edit_id;
    private EditText edit_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edit_id = findViewById(R.id.edit_id);
        edit_password = findViewById(R.id.edit_password);

        btn_signUp = findViewById(R.id.btn_signup);
        btn_ok = findViewById(R.id.btn_ok);
        btn_signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),SignUpActivity.class);
                startActivity(intent);
            }
        });

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String id = edit_id.getText().toString();
                String password = edit_password.getText().toString();
                login(id, password);
            }
        });
    }


    private void login(final String id, String password){
        Call<Map<String, Object>> login = ((ApplicationController)getApplicationContext()).getNetworkService().login(new User(id,password));

        login.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if(response.isSuccessful()){
                    String result = (String)response.body().get("result");
                    if(result.compareTo("success") == 0){
                        String sessionkey = (String)response.body().get("body");
                        Log.d("error_home",sessionkey);
                        ((ApplicationController)getApplicationContext()).setSessionkey(sessionkey);
                        //임시
                        ((ApplicationController)getApplicationContext()).setId(id);
                        //
                        Intent intent = new Intent(getApplicationContext(), LobbyActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                        finish();
                    }
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {

            }
        });
    }
}
