package com.geo.geofencer;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.util.HashMap;
import android.os.Bundle;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Map;

public class registerActivity extends AppCompatActivity {

    private  String URL_FOR_REGISTRATION;


    String name,username,password,password2;


    ProgressDialog progressDialog;
    Button register;
    EditText nameHolder,usernameHolder,passwordHolder,retypePasswordHolder;
    TextView displayMsg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        register=findViewById(R.id.Register);

        nameHolder= findViewById(R.id.nameInput);
        usernameHolder=findViewById(R.id.usernameInput);
        passwordHolder=findViewById(R.id.passwordInput);
        retypePasswordHolder=findViewById(R.id.retypePassword);
        displayMsg=findViewById(R.id.displayMsg);

         displayMsg.setText("");



        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(CheckEditTextIsEmptyOrNot())
                {
                    if(retypePasswordHolder.getText().toString().equals(passwordHolder.getText().toString()))
                        registerUser(name,username,password);
                    else
                        displayMsg.setText(R.string.passwordMismatch);
                }
                else
                {

                    displayMsg.setText(R.string.blankFields);

                }
            }
        });
    }



    public boolean CheckEditTextIsEmptyOrNot()
    {
        name=nameHolder.getText().toString();
        username=usernameHolder.getText().toString();
        password=passwordHolder.getText().toString();
        password2=retypePasswordHolder.getText().toString();


        if(TextUtils.isEmpty(name) || TextUtils.isEmpty(username) || TextUtils.isEmpty(password)||TextUtils.isEmpty(password2))
          return false;
        else
            return true ;
    }



    private void registerUser(final String Lname, final String Lusername,final String Lpassword) {

        String cancel_req_tag = "register";
        progressDialog.setMessage("Creating your account...");
        showDialog();
        URL_FOR_REGISTRATION = getString(R.string.ip_register);
        StringRequest strReq = new StringRequest(Request.Method.POST, URL_FOR_REGISTRATION, new Response.Listener<String>()
        {

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
                    else {
                        String resultMsg = jObj.getString("msg");
                        Toast.makeText(getApplicationContext(), resultMsg, Toast.LENGTH_SHORT).show();
                        finish();
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
                params.put("name", Lname);
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


}




