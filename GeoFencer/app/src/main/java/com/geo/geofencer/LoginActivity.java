package com.geo.geofencer;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.view.View;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;


public class LoginActivity extends AppCompatActivity {

    private String URL_FOR_REGISTRATION;

    ProgressDialog progressDialog;

    EditText usernameHolder,passwordHolder;
    TextView displayMsg;
    Button login;

    String username,password;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        usernameHolder=findViewById(R.id.usernameInput);
        passwordHolder=findViewById(R.id.passwordInput);

        login=findViewById(R.id.login);
        displayMsg=findViewById(R.id.displayMsg);

        displayMsg.setText("");


        login.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view)
            {
                if(checkInput())
                {
                    loginUser(username,password);
                }
                else
                {
                    displayMsg.setText("Please fill up all the fields...");
                }
            }
        });

    }



    private boolean checkInput()
    {

        username=usernameHolder.getText().toString();
        password=passwordHolder.getText().toString();
        if(TextUtils.isEmpty(username) || TextUtils.isEmpty(password))
        {
            return false;
        }
        else
        {
            return true;
        }
    }



    private void loginUser(final String Lusername, final String Lpassword)
    {

        String cancel_req_tag = "login";
        progressDialog.setMessage("Logging in...");
        showDialog();
        URL_FOR_REGISTRATION = getString(R.string.ip_login);
        StringRequest strReq = new StringRequest(Request.Method.POST, URL_FOR_REGISTRATION, new Response.Listener<String>()
        {

            @SuppressLint("ApplySharedPref")
            @Override
            public void onResponse(String response)
            {
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);

                    if(jObj.getInt("code")==0)
                    {
                        displayMsg.setText(jObj.getString("msg"));
                    }
                    else
                    {
                        SharedPreferences sharedPreferences=getApplicationContext().getSharedPreferences("com.geo.geofencer.user", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor=sharedPreferences.edit();
                        editor.putString("username",Lusername);
                        editor.commit();

                        if(getIntent().getBooleanExtra("launchQRActivity",false))
                        {
                            Intent intent = new Intent(LoginActivity.this, QRActivity.class);
                            startActivityFromChild(getParent(), intent, 1);
                            finish();
                        }
                        else
                        {
                            Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                            finish();
                        }

                    }



                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                if(error.getMessage()==null)
                  Toast.makeText(getApplicationContext(),"Connection Failed", Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(getApplicationContext(),error.getMessage(),Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("username", Lusername);
                params.put("password", Lpassword);

                return params;
            }
        };
        // Adding request to request queue
        AppSingleton.getInstance(getApplicationContext()).addToRequestQueue(strReq, cancel_req_tag);
    }



    private void showDialog() {
        if (!progressDialog.isShowing())
            progressDialog.show();
    }

    private void hideDialog() {
        if (progressDialog.isShowing())
            progressDialog.dismiss();
    }

    public void registerUser(View view)
    {
        Intent intent = new Intent(LoginActivity.this, registerActivity.class);
        startActivity(intent);
    }
}
