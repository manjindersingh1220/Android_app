package com.example.caipsgcms;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.scottyab.aescrypt.AESCrypt;

import java.security.GeneralSecurityException;

public class UserDetails extends AppCompatActivity {
    TextView name,email,phone,address,city,country,postal,state,passport;
    FirebaseAuth mAuth;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);
        name = findViewById(R.id.show_name);
        email = findViewById(R.id.show_email);
        phone = findViewById(R.id.show_phone);
        address = findViewById(R.id.show_address);
        city = findViewById(R.id.show_city);
        country = findViewById(R.id.show_country);
        postal = findViewById(R.id.show_postal);
        state = findViewById(R.id.show_state);
        passport = findViewById(R.id.show_passport);
        mAuth = FirebaseAuth.getInstance();
        if(getIntent().getExtras()!=null){
            reference = FirebaseDatabase.getInstance().getReference("users").child(getIntent().getStringExtra("userid"));
        }else {
            reference = FirebaseDatabase.getInstance().getReference("users").child(mAuth.getCurrentUser().getUid());
        }reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                try{
                    String key = "asdfgh";
                name.setText(AESCrypt.decrypt(key,user.getName()));
                email.setText(AESCrypt.decrypt(key,user.getEmail()));
                phone.setText(AESCrypt.decrypt(key,user.getPhone()));
                address.setText(AESCrypt.decrypt(key,user.getAddress()));
                city.setText(AESCrypt.decrypt(key,user.getCity()));
                country.setText(AESCrypt.decrypt(key,user.getCountry()));
                postal.setText(AESCrypt.decrypt(key,user.getPostal()));
                state.setText(AESCrypt.decrypt(key,user.getState()));
                passport.setText(AESCrypt.decrypt(key,user.getPassport()));
                }catch (GeneralSecurityException e){
                    Toast.makeText(UserDetails.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d("exception",e.getMessage());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserDetails.this,error.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }
}