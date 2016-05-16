package io.github.froger.instamaterial.ui.login;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.github.froger.instamaterial.R;
import io.github.froger.instamaterial.ui.activity.MainActivity;


public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;
    private static final String REGISTER_URL = "http://ec2-54-191-37-161.us-west-2.compute.amazonaws.com/login";
    private static final String TAG_EMAIL = "email";
    private static final String TAG_PASSWORD = "password";


    public static final String MyPREFERENCES = "MyPrefs" ;
    SharedPreferences sharedPref;

    @Bind(R.id.input_email) EditText _emailText;
    @Bind(R.id.input_password) EditText _passwordText;
    @Bind(R.id.btn_login) Button _loginButton;
    @Bind(R.id.link_signup) TextView _signupLink;
    @Bind(R.id.link_reset) TextView _resetLink;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        Context context = this;
        sharedPref = context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        _loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
            }
        });

        _signupLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Start the Signup activity
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
            }
        });

        _resetLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Start the Signup activity

                Intent intent_reset = new Intent(getApplicationContext(), ResetPasswordActivity.class);
                startActivity(intent_reset);
            }
        });



    }

    public void login() {
        Log.d(TAG, "Login");
        if (!validate()) {
            onLoginFailed();
            return;
        }

//        _loginButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this,
                R.style.Base_Theme_AppCompat_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();

        // TODO: Implement your own authentication logic here.
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        JsonObjReq();
                        progressDialog.dismiss();
                    }
                }, 3000);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {

                // TODO: Implement successful signup logic here
                // By default we just finish the Activity and log them in automatically
                this.finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        // Disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public void onLoginSuccess(String token) {
//        _loginButton.setEnabled(true);
        //finish();

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("token", token);
        editor.commit();

        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
    }

    private void JsonObjReq(){

        Map<String, String> jsonParams = new HashMap<String, String>();

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        Log.d(TAG,email);
        Log.d(TAG,password);

        jsonParams.put(TAG_EMAIL, email);
        jsonParams.put(TAG_PASSWORD, password);

        JsonObjectRequest myRequest = new JsonObjectRequest(
                Request.Method.POST,
                REGISTER_URL,
                new JSONObject(jsonParams),

                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG,response.toString());
                        try {
                            parseJSON(response);
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(LoginActivity.this,error.toString(),Toast.LENGTH_LONG).show();
                        Log.d(TAG, error.toString());
                    }
                }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(myRequest);
        Log.d(TAG, myRequest.toString());
    }



    public void parseJSON(JSONObject jsonObject) throws JSONException {

//        _loginButton.setEnabled(true);

        String status = jsonObject.getString("status");


        if (status.contentEquals("success")){
            String msg = jsonObject.getString("msg");

            JSONObject user = jsonObject.getJSONObject("user");
            String token = user.getString("token");
            Toast.makeText(LoginActivity.this,""+msg,Toast.LENGTH_LONG).show();
            onLoginSuccess(token);
        }else if(status.contentEquals("error")){
            JSONArray subArray = jsonObject.getJSONArray("errors");
            String msg = subArray.getJSONObject(0).getString("msg")
                    .toString();
            Toast.makeText(LoginActivity.this,""+msg,Toast.LENGTH_LONG).show();
        }
    }


    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();
//        _loginButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        Log.d(TAG,email);
        Log.d(TAG,password);


        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 8) {
            _passwordText.setError("between 4 and 8 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }
        return valid;
    }


}
