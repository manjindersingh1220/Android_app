package com.example.caipsgcms;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.signature.ObjectKey;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.hbb20.CountryCodePicker;
import com.scottyab.aescrypt.AESCrypt;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SignUp extends AppCompatActivity {
    Button signUp;
    private FirebaseAuth mAuth;
    TextView login;
    private RequestQueue mRequestQue;
    String onlineuserid;
    CountryCodePicker countryCodePicker;
    Spinner spinner;
    String token="";
    String to;
    String emailText,nameText,postalText,cityText,country,phoneText,passportText,passwordText,pasword2Text,addressText,stateText;
    ProgressDialog progressDialog;
    private DatabaseReference reference;
    EditText email,password,password2,name,phone,state,city,postal,passport,address;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        signUp = findViewById(R.id.signUp);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading");
        mRequestQue = Volley.newRequestQueue(this);
        email = findViewById(R.id.emailAdd);
        countryCodePicker = findViewById(R.id.ccp);
        password = findViewById(R.id.password);
        password2 = findViewById(R.id.password2);
        name = findViewById(R.id.name);
        phone = findViewById(R.id.phone);
        state = findViewById(R.id.state);
        city = findViewById(R.id.city);
        address = findViewById(R.id.address);
        postal = findViewById(R.id.postal);
        passport = findViewById(R.id.passport);
        login = findViewById(R.id.login);
        spinner = findViewById(R.id.country);
        mAuth = FirebaseAuth.getInstance();


        ArrayList<String> countryList=new ArrayList<String>();
        countryList.add("Select a country");
        String[] locales = Locale.getISOCountries();
        for (String countryCode : locales) {
            Locale obj = new Locale("", countryCode);
            countryList.add(obj.getDisplayCountry());
        }
        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, countryList);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);



        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emailText = email.getText().toString();
                nameText = name.getText().toString();
                addressText = address.getText().toString();
                postalText = postal.getText().toString();
                cityText = city.getText().toString();
                stateText = state.getText().toString();
                phoneText = phone.getText().toString();
                passportText = passport.getText().toString();
                passwordText = password.getText().toString();
                pasword2Text = password2.getText().toString();
                country = spinner.getSelectedItem().toString();
                if(TextUtils.isEmpty(emailText)){
                    email.setError("Enter email");
                }else if(TextUtils.isEmpty(nameText)){
                    name.setError("Enter name");
                }else if(TextUtils.isEmpty(phoneText)){
                    phone.setError("Enter phone number");
                }else if(phoneText.length()!=10){
                    phone.setError("Enter a valid phone number");
                }
                else if(!isValidEmail(emailText)){
                    email.setError("Enter a valid email");
                }else if(TextUtils.isEmpty(addressText)){
                    address.setError("Enter address");
                }else if (TextUtils.isEmpty(cityText)){
                    city.setError("Enter city");
                }else if(TextUtils.isEmpty(passportText)){
                    passport.setError("Enter passport number");
                }else if(TextUtils.isEmpty(postalText))
                {
                    postal.setError("Enter postal number");
                }else if(TextUtils.isEmpty(stateText)){
                    state.setError("Enter state");
                }
                else if(TextUtils.isEmpty(passwordText)){
                    password.setError("Enter password");
                }else if (passwordText.length()<8)
                {
                    password.setError("Password is too short");
                }else if(TextUtils.isEmpty(pasword2Text))
                {
                    password2.setError("Re-enter password");
                }else if(!TextUtils.equals(passwordText,pasword2Text)){
                    password2.setError("Password does not match");
                }
                else if (TextUtils.equals(country,"Select a country")){
                    Toast.makeText(SignUp.this,"Select a valid country",Toast.LENGTH_SHORT).show();
                }
                else{
                        progressDialog.show();
                        mAuth.createUserWithEmailAndPassword(emailText,passwordText).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(!task.isSuccessful()){
                                    Toast.makeText(SignUp.this,"Registration failed"+ task.getException().toString(),Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                }else
                                {
                                    String fullContact = countryCodePicker.getFullNumberWithPlus().toString() + phoneText;
                                    try{
                                        String key = "asdfgh";
                                        nameText = AESCrypt.encrypt(key,nameText);
                                        emailText = AESCrypt.encrypt(key,emailText);
                                        fullContact = AESCrypt.encrypt(key,fullContact);
                                        addressText = AESCrypt.encrypt(key,addressText);
                                        stateText = AESCrypt.encrypt(key,stateText);
                                        country = AESCrypt.encrypt(key,country);
                                        cityText = AESCrypt.encrypt(key,cityText);
                                        postalText = AESCrypt.encrypt(key,postalText);
                                    }catch (GeneralSecurityException e){
                                        Toast.makeText(SignUp.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                                    }

                                    onlineuserid= mAuth.getCurrentUser().getUid();
                                    reference = FirebaseDatabase.getInstance().getReference().child("users").child(onlineuserid);
                                    final Map hashMap = new HashMap();
                                    hashMap.put("name", nameText);
                                    hashMap.put("email",emailText);
                                    hashMap.put("phone",fullContact);
                                    hashMap.put("address", addressText);
                                    hashMap.put("state",stateText);
                                    hashMap.put("country",country);
                                    hashMap.put("city",cityText);
                                    hashMap.put("postal",postalText);
                                    hashMap.put("passport",passportText);
                                    hashMap.put("id",onlineuserid);
                                    reference.updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener() {
                                        @Override
                                        public void onComplete(@NonNull Task task) {
                                            if(task.isSuccessful()){
                                                addMessage();
                                                String localuser = null;
                                                try{
                                                    localuser = AESCrypt.decrypt("asdfgh",nameText);
                                                }catch (GeneralSecurityException e){
                                                    
                                                }
                                               
                                                sendNotification(localuser,"A new client has been added","","");
                                                setnulltimer(onlineuserid);
                                            }
                                            else {
                                                Toast.makeText(SignUp.this,task.getException().toString(),Toast.LENGTH_SHORT).show();
                                                progressDialog.dismiss();
                                            }
                                        }
                                    });

                                }
                            }
                        });
                }
            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUp.this,Login.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void setnulltimer(String onlineuserid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("timer").child(onlineuserid);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("time","");
        hashMap.put("load","0");
        reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    //sendNotification("admin","Admin created a count down","");
                    //Toast.makeText(AdminChat.this,"Count Down started",Toast.LENGTH_SHORT).show();
                }
                else
                {

                }
            }
        });
    }

    public static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }
    public void addMessage(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("messages").child(onlineuserid);
        String msgId = reference.push().getKey();
        //String date = DateFormat.getDateInstance().format(new Date());
        String msg = "This msg is automatically created on signup";
        String publisher = "admin";
        try{
            msg = AESCrypt.encrypt("asdfgh",msg);
            publisher = AESCrypt.encrypt("asdfgh",publisher);
        }catch (GeneralSecurityException e){
            Toast.makeText(SignUp.this,e.getMessage(),Toast.LENGTH_SHORT).show();
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        //String time = sdf.format(new Date());
        String time = Calendar.getInstance().getTime().toString();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("message",msg);
        //hashMap.put("date",date);
        hashMap.put("time",time);
        hashMap.put("id",msgId);
        hashMap.put("imageurl","");
        hashMap.put("publisher",publisher);
        reference.child(msgId).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(SignUp.this,"Registered Successfully",Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SignUp.this,Login.class);
                    startActivity(intent);
                    progressDialog.dismiss();
                    finish();
                }
                else {
                    Toast.makeText(SignUp.this,task.getException().toString(),Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }
        });
    }
    private void sendNotification(String user, String questionText, String imageUrl,String id) {
        //addDummyReq();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("tokens").child("2Wc3jYgqdBTjIC1JDwUH9NMesfx2");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    to = dataSnapshot.getValue(String.class);
                    createNotification(user,questionText,imageUrl,id);
                }

                Toast.makeText(SignUp.this, to, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    private void createNotification(String user,String questionText,String imageUrl,String id){
        JSONObject jsonObject = new JSONObject();
        String URL = "https://fcm.googleapis.com/fcm/send";
        try {
            jsonObject.put("to", to);
            jsonObject.put("content_avaliable", "true");
            jsonObject.put("priority", "high");
            JSONObject notiobj = new JSONObject();
            notiobj.put("title", user);
            notiobj.put("click_action","OPEN_ACTIVITY_ADMINHOME");
            notiobj.put("body", questionText);
            JSONObject dataobj = new JSONObject();
            dataobj.put("id",id);
            if (TextUtils.equals("", imageUrl)) {
                notiobj.put("image", imageUrl);
            }
            jsonObject.put("notification", notiobj);
            jsonObject.put("data",dataobj);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL, jsonObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            //Toast.makeText(AskAQuestion.this, "Done", Toast.LENGTH_SHORT).show();
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(SignUp.this, error.toString(), Toast.LENGTH_SHORT).show();

                }
            }
            ) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> header = new HashMap<>();
                    header.put("content-type", "application/json");
                    header.put("authorization", "key=AAAALCh7jx4:APA91bFcGOTlum5fBWWq3OuWsycX2mJSA7qKUL0b7CgeeFeDMU1XfZtyPrsiMeAwmxUeTzZ1FQcmS6S84yQAxENbsHb_sApKsu9u7WuTZk-OBXwt1pMUtS7JDlVBtL3JS7qCN705GjkE");
                    return header;
                }
            };
            request.setRetryPolicy(new DefaultRetryPolicy(1000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            mRequestQue.add(request);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}