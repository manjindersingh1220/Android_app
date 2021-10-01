package com.example.caipsgcms;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static com.example.caipsgcms.SignUp.isValidEmail;

public class Login extends AppCompatActivity {
    EditText email,password;
    Button signIn;
    TextView signUp,forgotPassword;
    ProgressDialog progressDialog;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        email = findViewById(R.id.email_signIn);
        password = findViewById(R.id.password_login);
        signIn = findViewById(R.id.signin);
        signUp = findViewById(R.id.signup);
        forgotPassword = findViewById(R.id.forgorPassword);
        mAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading");
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = mAuth.getCurrentUser();
                if(user!=null){
                    Intent intent = new Intent(Login.this,HomeActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        };
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailText = email.getText().toString();
                if(TextUtils.isEmpty(emailText))
                {
                    email.setError("Enter email");
                }else if(!isValidEmail(emailText)) {
                    email.setError("Enter a valid email");
                }
                else{
                    progressDialog.setMessage("Loading");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();
                    mAuth.sendPasswordResetEmail(emailText)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        //Log.d(TAG, "Email sent.");
                                        Toast.makeText(Login.this, "Password reset link sent to "+email, Toast.LENGTH_SHORT).show();
                                        progressDialog.dismiss();
                                    }
                                    else
                                    {
                                        Toast.makeText(Login.this, "Error while reset password", Toast.LENGTH_SHORT).show();
                                        progressDialog.dismiss();
                                    }
                                }
                            });
                }
            }
        });
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this,SignUp.class);
                startActivity(intent);
                finish();
            }
        });
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String emailText = email.getText().toString();
                String passwordText = password.getText().toString();
                if(TextUtils.isEmpty(emailText))
                {
                    email.setError("Enter email");
                }else if(!isValidEmail(emailText)){
                    email.setError("Enter a valid email");
                }else if(TextUtils.isEmpty(passwordText)){
                    password.setError("Enter password");
                }else{
                    progressDialog.show();
                        mAuth.signInWithEmailAndPassword(emailText,passwordText).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){
                                    progressDialog.dismiss();
                                    Toast.makeText(Login.this,"Login successfully",Toast.LENGTH_SHORT).show();
                                    if(TextUtils.equals(emailText,"admin@email.com"))
                                    {
                                        Intent intent = new Intent(Login.this,AdminHome.class);
                                        startActivity(intent);
                                        finish();
                                    }else{
                                        Intent intent = new Intent(Login.this,HomeActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                }
                                else
                                {
                                    progressDialog.dismiss();
                                    Toast.makeText(Login.this,task.getException().toString(),Toast.LENGTH_SHORT).show();

                                }

                            }
                        });
                }
            }
        });
        password.setOnTouchListener(new View.OnTouchListener() {
            int Count = 0;
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                final int DRAWABLE_RIGHT = 2;
                if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if(motionEvent.getRawX() >= (password.getRight() - password.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        Drawable leftIcon = getApplicationContext().getResources().getDrawable(R.drawable.ic_baseline_lock_24);
                        Drawable rightShow = getApplicationContext().getResources().getDrawable(R.drawable.ic_baseline_visibility_24);
                        Drawable rightHide = getApplicationContext().getResources().getDrawable(R.drawable.ic_baseline_visibility_off_24);
                        if(Count%2==0)
                        {
                            password.setCompoundDrawablesWithIntrinsicBounds(leftIcon,null,rightShow,null);
                            password.setTransformationMethod(new PasswordTransformationMethod());
                            Count++;
                        }
                        else
                        {
                            password.setCompoundDrawablesWithIntrinsicBounds(leftIcon,null,rightHide,null);
                            password.setTransformationMethod(null);
                            Count++;
                        }

                        return true;
                    }
                }
                return false;
            }
        });
    }
    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(authStateListener);
    }
    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(authStateListener);
    }
}