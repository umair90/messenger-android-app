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
public class ResetPasswordActivity extends AppCompatActivity {

    private static final String TAG = "ResetPasswordActivity";
    private static final String REGISTER_URL = "http://ec2-54-191-37-161.us-west-2.compute.amazonaws.com/api/reset-password";

    private static final String TAG_EMAIL = "email";

    @Bind(R.id.input_email) EditText _emailText;
    @Bind(R.id.btn_continue) Button _continueButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.password_reset);
        ButterKnife.bind(this);

        _continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEmail();
            }
        });

    }

    public void sendEmail() {

        if (!validate()) {
            //onSignupFailed();
            return;
        }


        makeJsonObjReq();
    }

    private void makeJsonObjReq(){

        Map<String, String> jsonParams = new HashMap<String, String>();

        String email = _emailText.getText().toString();

        jsonParams.put(TAG_EMAIL, email);

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
            Toast.makeText(ResetPasswordActivity.this, "" + msg, Toast.LENGTH_LONG).show();
            onSignupSuccess();
        }else if(status.contentEquals("error")){
            JSONArray subArray = jsonObject.getJSONArray("errors");
            String msg = subArray.getJSONObject(0).getString("msg")
                    .toString();
            Toast.makeText(ResetPasswordActivity.this,""+msg,Toast.LENGTH_LONG).show();
        }
    }

    public void onSignupSuccess(){
        String email = _emailText.getText().toString();
        Intent intent = new Intent(this, ChangePasswordActivity.class);
        intent.putExtra("email",email);
        startActivity(intent);
    }

    public boolean validate() {
        boolean valid = true;

        String email = _emailText.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }
        return valid;
    }

}
