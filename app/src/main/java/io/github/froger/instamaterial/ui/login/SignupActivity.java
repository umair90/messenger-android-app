package io.github.froger.instamaterial.ui.login;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.github.froger.instamaterial.R;


public class SignupActivity extends AppCompatActivity {
    private static final String TAG = "SignupActivity";
    private static final String REGISTER_URL = "http://ec2-54-191-37-161.us-west-2.compute.amazonaws.com/register";

    private static final String TAG_EMAIL = "email";
    private static final String TAG_PASSWORD = "password";
    private static final String TAG_CONFIRM_PASSWORD = "confirm_password";
    private static final String TAG_GENDER = "gender";
    private static final String TAG_BIRTHDAY = "dob";
    private static final String TAG_PHONE = "phone_no";
    private static final String TAG_NAME = "display_name";

    @Bind(R.id.input_email) EditText _emailText;
    @Bind(R.id.input_password) EditText _passwordText;
    @Bind(R.id.input_confirm_password) EditText _confimPasswordText;
    @Bind(R.id.input_name) EditText _nameText;
    @Bind(R.id.input_phone) EditText _phone_no;
    @Bind(R.id.btn_signup) Button _signupButton;
    @Bind(R.id.link_login) TextView _loginLink;
    @Bind(R.id.radio_sex) RadioGroup radioSexGroup;

    private RadioButton radioSexButton;
    TextView _birthdayText;
    int birthYear,birthMonth,birthDay;
    static final int dialog_ID = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);
        showBirthdayDialog();

        // Getting Phone Number

        TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String telNumber = manager.getLine1Number();
        _phone_no.setText(telNumber);

        final Calendar cal = Calendar.getInstance();
        birthYear = cal.get(Calendar.YEAR);
        birthMonth = cal.get(Calendar.MONTH);
        birthDay = cal.get(Calendar.DAY_OF_MONTH);

        _signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });
        _loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration screen and return to the Login activity
                finish();
            }
        });
    }

    public void signup() {
        Log.d(TAG, "Signup");

        if (!validate()) {
            onSignupFailed();
            return;
        }

       // _signupButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(SignupActivity.this,
                R.style.Base_Theme_AppCompat_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Creating Account...");
        progressDialog.show();

        // TODO: Implement your own signup logic here.

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        makeJsonObjReq();
                        progressDialog.dismiss();
                    }
                }, 3000);
    }


    public void onSignupSuccess() {
//        _signupButton.setEnabled(true);
        setResult(RESULT_OK, null);
//        finish();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    public void onSignupFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();
//        _signupButton.setEnabled(true);
    }


    private void makeJsonObjReq(){

        Map<String, String> jsonParams = new HashMap<String, String>();

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();
        String confirm_password = _confimPasswordText.getText().toString();
        String phone_no = _phone_no.getText().toString();
        String name = _nameText.getText().toString();

        int selectedId = radioSexGroup.getCheckedRadioButtonId(); // get selected radio button from radioGroup
        radioSexButton = (RadioButton) findViewById(selectedId);  // find the radio button by returned id
        //Toast.makeText(getApplicationContext(),radioSexButton.getText(),Toast.LENGTH_LONG).show();

        String birthday = birthYear+"/"+birthMonth+"/"+birthDay;

        jsonParams.put(TAG_EMAIL, email);
        jsonParams.put(TAG_PASSWORD, password);
        jsonParams.put(TAG_CONFIRM_PASSWORD, confirm_password);
        jsonParams.put(TAG_NAME, name);
        jsonParams.put(TAG_PHONE, phone_no);
        jsonParams.put(TAG_BIRTHDAY, birthday);
        jsonParams.put(TAG_GENDER, radioSexButton.getText().toString());

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
            Toast.makeText(SignupActivity.this,""+msg,Toast.LENGTH_LONG).show();
            onSignupSuccess();
        }else if(status.contentEquals("error")){
            JSONArray subArray = jsonObject.getJSONArray("errors");
            String msg = subArray.getJSONObject(0).getString("msg")
                    .toString();
            Toast.makeText(SignupActivity.this,""+msg,Toast.LENGTH_LONG).show();
        }
    }



    public boolean validate() {
        boolean valid = true;

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();
        String confirm_password = _confimPasswordText.getText().toString();
        String birth_day = _birthdayText.getText().toString();
        String phone_no= _phone_no.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }

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

        if (birth_day.isEmpty()){
            _birthdayText.setError("please enter your birthday");
            valid = false;
        } else {
            _birthdayText.setError(null);
        }

        if (phone_no.isEmpty()){
            _phone_no.setError("please enter your phone no.");
            valid = false;
        } else {
            _phone_no.setError(null);
        }


        return valid;
    }

    public void showBirthdayDialog(){
        _birthdayText = (TextView)(findViewById(R.id.input_dob));
        _birthdayText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showDialog(dialog_ID);
                }
            }
        });

    }

    @Override
    public Dialog onCreateDialog(int id){
        if(id == dialog_ID)
            return new DatePickerDialog(this, datePickerListener, birthYear, birthMonth, birthDay);
            return null;
    }

    private DatePickerDialog.OnDateSetListener datePickerListener
            = new DatePickerDialog.OnDateSetListener(){
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            birthYear = year;
            birthMonth = monthOfYear+1;
            birthDay = dayOfMonth;
            _birthdayText.setText(birthYear+"/"+birthMonth+"/"+birthDay);
            _birthdayText.clearFocus();
            Toast.makeText(SignupActivity.this, birthYear + "/" + birthMonth + "/" + birthDay, Toast.LENGTH_SHORT).show();
        }
    };


}