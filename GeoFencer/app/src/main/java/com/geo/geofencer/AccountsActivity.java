package com.geo.geofencer;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
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


public class AccountsActivity extends AppCompatActivity {

    Button btnChangePassword,btnRemove,btnLogout;
    TextView displayMsg;
    String newPassword,newPassword2;
    AlertDialog alertDialog;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accounts);

        progressDialog=new ProgressDialog(this);
        progressDialog.setCancelable(false);

        btnChangePassword=findViewById(R.id.btnChangePassword);
        btnRemove=findViewById(R.id.btnRemove);
        btnLogout=findViewById(R.id.btnLogout);

        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlert();
            }
        });

        btnRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences=getApplicationContext().getSharedPreferences("com.geo.geofencer.client",Context.MODE_PRIVATE);
                SharedPreferences.Editor editor=sharedPreferences.edit();

                if(sharedPreferences.getString("clientUsername",null)!=null)
                {
                    Toast.makeText(getApplicationContext(),sharedPreferences.getString("clientUsername",null)+" has been removed successfully",Toast.LENGTH_LONG).show();
                    editor.clear();
                    editor.apply();
                    Intent intent=new Intent(AccountsActivity.this,MapsActivity.class);
                    intent.putExtra("isClientRemoved",true);
                    finish();
                    startActivity(intent);
                }
                else
                {
                    Toast.makeText(getApplicationContext(),"Error removing client account",Toast.LENGTH_LONG).show();
                }
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout(false);
        }
        });
    }


    void logout(final boolean isPasswordChanged)
    {
        final SharedPreferences sharedPreferences=getApplicationContext().getSharedPreferences("com.geo.geofencer.user",Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor=sharedPreferences.edit();
        if(sharedPreferences.getString("username",null)!=null)
        {
            progressDialog.setMessage(getString(R.string.logging_out));
            showProgressDialog();
            String url=getString(R.string.ip_logout);

            StringRequest strReq = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    editor.clear();
                    editor.apply();
                    if(!isPasswordChanged)
                        Toast.makeText(getApplicationContext(),"You have been successfully logged out",Toast.LENGTH_LONG).show();
                    hideProgressDialog();
                    finish();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getApplicationContext(),"Connection Failed... Logout Unsuccessful",Toast.LENGTH_LONG).show();
                    hideProgressDialog();
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    // Posting params to register url
                    Map<String, String> params = new HashMap<>();
                    params.put("username",sharedPreferences.getString("username",""));
                    return params;
                }
            };
            // Adding request to request queue
            AppSingleton.getInstance(getApplicationContext()).addToRequestQueue(strReq, "logout");

        }
        else
        {
            Toast.makeText(getApplicationContext(),"Logout Unsuccessful",Toast.LENGTH_LONG).show();
        }
    }

    void showAlert()
    {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(AccountsActivity.this);
        mBuilder.setTitle("Change Password");

         View mView = getLayoutInflater().inflate(R.layout.change_password, null);

         final EditText newPasswordHolder =  mView.findViewById(R.id.newPassword);
         final EditText confirmPasswordHolder = mView.findViewById(R.id.confirmPassword);
         displayMsg = mView.findViewById(R.id.errorMsg);
        Button changePassword =  mView.findViewById(R.id.btnChangePasswordOnClick);

         mBuilder.setView(mView);
         alertDialog = mBuilder.create();

        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                newPassword=newPasswordHolder.getText().toString();
                newPassword2=confirmPasswordHolder.getText().toString();
                if(newPassword.equals(newPassword2))
                  changePassword();
                else
                    displayMsg.setText(R.string.passwordMismatch);
            }
        });

        alertDialog.show();
    }

    private void showProgressDialog()
    {
        if(!progressDialog.isShowing())
        {
            progressDialog.show();
        }
    }

    private void hideProgressDialog()
    {
        if(progressDialog.isShowing())
            progressDialog.dismiss();
    }

    private void changePassword()
    {
        progressDialog.setMessage("Changing your password...");
        showProgressDialog();

        SharedPreferences sharedPreferences=getApplicationContext().getSharedPreferences("com.geo.geofencer.user", Context.MODE_PRIVATE);
        final String username=sharedPreferences.getString("username",null);

        String url = getString(R.string.ip_changePassword);

        StringRequest strReq = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jobj=new JSONObject(response);
                    if(jobj.getInt("code")==0)
                    {
                        displayMsg.setText(jobj.getString("message"));
                    }
                    else if(jobj.getInt("code")==1)
                    {
                        alertDialog.dismiss();
                        Toast.makeText(getApplicationContext(),"Your Password has been changed successfully",Toast.LENGTH_LONG).show();
                        logout(true);
                        finish();
                        Intent intent=new Intent(AccountsActivity.this,LoginActivity.class);
                        startActivity(intent);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                hideProgressDialog();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                displayMsg.setText(error.getMessage());
                hideProgressDialog();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<>();
                params.put("username",username);
                params.put("newPassword", newPassword);
                return params;
            }
        };
        // Adding request to request queue
        AppSingleton.getInstance(getApplicationContext()).addToRequestQueue(strReq, "changePassword");
    }

}
