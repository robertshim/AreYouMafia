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

public class SignUpActivity extends AppCompatActivity {
    private EditText edit_id;
    private EditText edit_password;
    private TextView btn_ok;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        btn_ok = findViewById(R.id.btn_ok);
        edit_id = findViewById(R.id.edit_id);
        edit_password = findViewById(R.id.edit_password);


        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = edit_id.getText().toString();
                String password = edit_password.getText().toString();
                signUp(id, password);
            }
        });
    }

    private void signUp(String id, String password){
        Call<Map<String, Object>> signUp = ((ApplicationController)getApplicationContext()).getNetworkService().signUp(id, password);

        signUp.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                Log.d("error_home","signup");
                if(response.isSuccessful()){
                    String result = (String)response.body().get("result");
                    if(result.compareTo("success") == 0){
                        String sessionkey = (String)response.body().get("sessionkey");
                        ((ApplicationController)getApplicationContext()).setSessionkey(sessionkey);

                        Intent intent = new Intent(getApplicationContext(), LobbyActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                    }
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Log.d("error_home",t.getMessage());
            }
        });
    }
}
