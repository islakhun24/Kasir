package com.example.kasir;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class Login extends AppCompatActivity{

    Button login;
    private EditText etEmail, etPassword;
    private ProgressBar loading;
    private static String URL_LOGIN = "https://geprekrame.doktersoftware.my.id/loginkasir.php";
    SharedPrefManager sharedPrefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        login = findViewById(R.id.btnLogin);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPass);
        loading = findViewById(R.id.loading);


       /* sharedPrefManager = new SharedPrefManager(this);

        if (sharedPrefManager.getSPSudahLogin()){
            startActivity(new Intent(Login.this, Home.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
            finish();
        }*/


        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String mEmail = etEmail.getText().toString().trim();
                String mPass = etPassword.getText().toString().trim();

                if (!mEmail.isEmpty() || !mPass.isEmpty()){
                    Login(mEmail, mPass);
                } else{
                    etEmail.setError("Please insert email");
                    etPassword.setError("Please insert password");
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(this,"press home to exit !",Toast.LENGTH_SHORT).show();
    }

    private void Login(final String etEmailS,final String etPasswordS) {

        loading.setVisibility(View.VISIBLE);
        login.setVisibility(View.GONE);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_LOGIN,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e("response",response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String success = jsonObject.getString("success");
                            JSONArray jsonArray = jsonObject.getJSONArray("login");

                            if(success.equals("1")){
                                for (int i =0; i < jsonArray.length(); i ++){
                                    JSONObject object = jsonArray.getJSONObject(i);

                                    String name = object.getString("name").trim();
                                    String email = object.getString("email").trim();

                                   /* Toast.makeText(Login.this,"Success Login. \n Your Name : "+name+
                                            " \n Your Email : "+email,Toast.LENGTH_SHORT).show();*/

                                   /* sharedPrefManager.saveSPBoolean(SharedPrefManager.SP_SUDAH_LOGIN, true);*/

                                    Intent intent = new Intent(Login.this, Home.class);
                                    intent.putExtra("email",email);
                                    startActivity(intent);

                                    loading.setVisibility(View.GONE);
                                    login.setVisibility(View.VISIBLE);
                                }
                            }else{
                                Toast.makeText(Login.this,"Email or Password is Wrong !",Toast.LENGTH_SHORT).show();
                                loading.setVisibility(View.GONE);
                                login.setVisibility(View.VISIBLE);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(Login.this,"Email or Password is Wrong !",Toast.LENGTH_SHORT).show();
                            loading.setVisibility(View.GONE);
                            login.setVisibility(View.VISIBLE);
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(Login.this,"Email or Password is Wrong !",Toast.LENGTH_SHORT).show();
                        loading.setVisibility(View.GONE);
                        login.setVisibility(View.VISIBLE);

                    }
                })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put("email", etEmailS);
                params.put("password",etPasswordS);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}
