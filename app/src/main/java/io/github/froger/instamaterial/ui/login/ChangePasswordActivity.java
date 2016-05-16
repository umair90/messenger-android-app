package io.github.froger.instamaterial.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

/**
 * Created by umair on 4/9/16.
 */
public class ChangePasswordActivity extends AppCompatActivity {

    private static final String TAG = "ChangePasswordActivity";
    private static final String REGISTER_URL = "http://ec2-54-191-37-161.us-west-2.compute.amazonaws.com/api/reset-password/change";


    private static final String TAG_EMAIL = "email";
    private static final String TAG_PASSWORD = "new_password";
    private static final String TAG_CONFIRM_PASSWORD = "confirm_new_password";
    private static final String TAG_CODE = "code";

    private String reset_email;


    @Bind(R.id.input_password) EditText _passwordText;
    @Bind(R.id.input_confirm_password) EditText _confimPasswordText;
    @Bind(R.id.input_code) EditText _codeText;
    @Bind(R.id.btn_reset_password) Button _resetButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.password_change);
        ButterKnife.bind(this);

        Intent myIntent = getIntent(); // gets the previously created intent
        reset_email = myIntent.getStringExtra("email");

        _resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword();
            }
        });

    }

    public void resetPassword() {

        if (!validate()) {
            //onSignupFailed();
            return;
        }
        makeJsonObjReq();
    }

    private void makeJsonObjReq(){

        Map<String, String> jsonParams = new HashMap<String, String>();

        String password = _passwordText.getText().toString();
        String confirm_password = _confimPasswordText.getText().toString();
        String code = _codeText.getText().toString();


        jsonParams.put(TAG_EMAIL, reset_email);
        jsonParams.put(TAG_CODE, code);
        jsonParams.put(TAG_PASSWORD, password);
        jsonParams.put(TAG_CONFIRM_PASSWORD, confirm_password);

        JsonObjectRequest myRequest = new JsonObjectRequest(
                Request.Method.POST,
                REGISTER_URL,
                new JSONObject(jsonParams),

                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        //Toast.makeText(SignupActivity.this,response.toString(),Toast.LENGTH_LONG).show();
                        Log.d("Response",response.toString());
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
                        //Toast.makeText(SignupActivity.this,error.toString(),Toast.LENGTH_LONG).show();
                        Log.d("Error", error.toString());
                    }
                }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
//                headers.put("User-agent", "My useragent");
                return headers;
            }
        };
//      MyApplication.getInstance().addToRequestQueue(myRequest, "tag");
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(myRequest);
        Log.d("Request", myRequest.toString());

    }

    public void parseJSON(JSONObject jsonObject) throws JSONException {

//        _loginButton.setEnabled(true);

        String status = jsonObject.getString("status");

        if (status.contentEquals("success")){
            String msg = jsonObject.getString("msg");
            Toast.makeText(ChangePasswordActivity.this, "" + msg, Toast.LENGTH_LONG).show();
            onSignupSuccess();
        }else if(status.contentEquals("error")){
            JSONArray subArray = jsonObject.getJSONArray("errors");
            String msg = subArray.getJSONObject(0).getString("msg")
                    .toString();
            Toast.makeText(ChangePasswordActivity.this,""+msg,Toast.LENGTH_LONG).show();
        }
    }

    public void onSignupSuccess(){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    public boolean validate() {
        boolean valid = true;

        String password = _passwordText.getText().toString();
        String confirm_password = _confimPasswordText.getText().toString();
        String code = _codeText.getText().toString();


        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        if (confirm_password.isEmpty() || confirm_password.length() < 4 || password.length() > 10) {
            _confimPasswordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            _confimPasswordText.setError(null);
        }

        if (!confirm_password.equals(password)) {
            _confimPasswordText.setError("passwords doesn't match");
            valid = false;
        } else {
            _confimPasswordText.setError(null);
        }

        if (code.isEmpty()){
            _codeText.setError("please enter your birthday");
            valid = false;
        } else {
            _codeText.setError(null);
        }
        return valid;
    }


}
