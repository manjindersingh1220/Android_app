package com.example.caipsgcms;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;


import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.scottyab.aescrypt.AESCrypt;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeActivity extends AppCompatActivity  {
    RecyclerView recyclerView;
    private FirebaseAuth mAuth;
    private MessageAdapter messageAdapter;
    private List<Message> messageList;
    EditText editTextMsg;
    public User user;
    private RequestQueue mRequestQue;
    Uri imageUri;
    String to,status;
    RecyclerView recyclerViewTimer;
    NavigationView navigationView;
    DrawerLayout drawerLayout;
    LinearLayoutManager linearLayoutManagerTimer;
    ActionBarDrawerToggle actionBarDrawerToggle;
    private String key = "asdfgh";
    ProgressDialog progressDialog;
    private String myUrl = null;
    StorageTask uploadtask;
    TimerAdapter timerAdapter;
    private List<Timer> timerList;
    StorageReference storageReference;
    private static String mCurrentPhotoPath;
    LinearLayoutManager linearLayoutManager;
    private FirebaseUser firebaseUser;
    ImageView send, attach;
    Toolbar toolbar;
    String type;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
        else if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        else if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        }
        setContentView(R.layout.activity_home);
        editTextMsg = findViewById(R.id.msg_edit);
        send = findViewById(R.id.send_btn);
        attach = findViewById(R.id.attach);
        toolbar = findViewById(R.id.toolbar_home);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view_home);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.open,R.string.close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading");
        progressDialog.setCanceledOnTouchOutside(false);
        recyclerView = findViewById(R.id.recylerview);
        messageList = new ArrayList<>();
        timerList = new ArrayList<>();
        mRequestQue = Volley.newRequestQueue(this);

        timerAdapter = new TimerAdapter(this,timerList);

        recyclerViewTimer = findViewById(R.id.timerRecyclerUser);
        recyclerViewTimer.setAdapter(timerAdapter);
        mAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference(mAuth.getCurrentUser().getUid());
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManagerTimer = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(false);
        linearLayoutManager.setReverseLayout(false);
        linearLayoutManager.setStackFromEnd(false);
        linearLayoutManagerTimer.setStackFromEnd(false);
        recyclerView.setHasFixedSize(true);
        recyclerViewTimer.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerViewTimer.setLayoutManager(linearLayoutManagerTimer);
        recyclerView.setItemViewCacheSize(50);

        progressDialog.show();
        readMessages();
        checkStatus();
        setToken();
        getTimer();

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.profile:
                        Intent intent = new Intent(HomeActivity.this, UserDetails.class);
                        startActivity(intent);
                        break;
                    case R.id.signOut:
                        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(HomeActivity.this);
                        alertDialogBuilder.setMessage("Are you want to Sign Out?");
                        alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                FirebaseDatabase.getInstance().getReference("tokens").child(mAuth.getCurrentUser().getUid()).removeValue();
                                FirebaseAuth.getInstance().signOut();
                                Intent intent4 = new Intent(HomeActivity.this,Login.class);
                                startActivity(intent4);
                                finish();
                            }
                        });
                        alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });
                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                        break;
                    case R.id.aboutUs:
                        Intent intent1 = new Intent(HomeActivity.this,AboutUs.class);
                        startActivity(intent1);
                }
                return false;
            }
        });
        getUserDetails();
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkStatus();
                if(TextUtils.equals(status,"unlocked")){
                    if (!TextUtils.isEmpty(editTextMsg.getText())) {
                        progressDialog.show();
                        try {
                            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                        } catch (Exception e) {
                            // TODO: handle exception
                        }
                        if (imageUri != null) {
                            addMessageWithAttachment();
                        } else {
                            addMessage();
                        }

                    }
                }else {
                    Toast.makeText(HomeActivity.this,"Your are locked by admin",Toast.LENGTH_SHORT).show();
                }

            }
        });
        attach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String list[] = {"Camera", "Gallery", "Select PDF"};
                android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(HomeActivity.this);
                alertDialog.setTitle("Select Attachment")
                        .setItems(list, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 9) {

                                    if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
                                            == PackageManager.PERMISSION_DENIED) {
                                        ActivityCompat.requestPermissions(HomeActivity.this, new String[]{Manifest.permission.CAMERA}, 2);
                                    } else {
                                        //Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                                        //startActivityForResult(intent, 1);
                                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                                        Uri uriForFile = null;
                                        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                                            // Create the File where the photo should go
                                            File photoFile = null;
                                            try {
                                                photoFile = createImageFile();
                                            } catch (IOException ex) {
                                                // Error occurred while creating the File
                                                Log.i("Phototag", "IOException");
                                                Toast.makeText(HomeActivity.this, ex.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                            // Continue only if the File was successfully created
                                            if (photoFile != null) {
                                                Toast.makeText(HomeActivity.this, "photo not null", Toast.LENGTH_SHORT).show();
                                                uriForFile = FileProvider.getUriForFile(HomeActivity.this, "com.example.myapplication.fileprovider", photoFile);
                                                List<ResolveInfo> resInfoList = getBaseContext().getPackageManager().queryIntentActivities(cameraIntent, PackageManager.MATCH_DEFAULT_ONLY);
                                                for (ResolveInfo resolveInfo : resInfoList) {
                                                    String packageName = resolveInfo.activityInfo.packageName;
                                                    getBaseContext().grantUriPermission(packageName, uriForFile, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                                }
                                                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriForFile);
                                                startActivityForResult(cameraIntent, 2);
                                            }
                                        }
                                        if (mCurrentPhotoPath != null) {
                                            imageUri = Uri.parse(mCurrentPhotoPath);
                                        }

                                    }

                                } else if (which == 0) {
                                    if(!checkPermissionREAD_EXTERNAL_STORAGE(HomeActivity.this))
                                    {
                                        checkPermissionREAD_EXTERNAL_STORAGE(HomeActivity.this);
                                    }
                                    else if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
                                            == PackageManager.PERMISSION_DENIED) {
                                        ActivityCompat.requestPermissions(HomeActivity.this, new String[]{Manifest.permission.CAMERA}, 2);
                                    }else if(!checkPermission()){
                                        requestPermission();
                                    }
                                    else{
                                        openCamera();
                                    }

                                } else if (which==1) {
                                    Intent intent = new Intent(Intent.ACTION_PICK);
                                    intent.setType("image/*");
                                    startActivityForResult(intent, 1);
                                }
                                else{
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(HomeActivity.this,
                                            Manifest.permission.READ_EXTERNAL_STORAGE)
                                            != PackageManager.PERMISSION_GRANTED) {
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                                Uri.parse("package:" + getPackageName()));
                                        startActivity(intent);
                                        return;
                                    }else {
                                        Log.d("pdf", "inpdf");
                                        Intent intent = new Intent();
                                        intent.setType("application/pdf");
                                        intent.setAction(Intent.ACTION_GET_CONTENT);
                                        startActivityForResult(intent, 3);
                                    }
                                }
                            }
                        });
                android.app.AlertDialog alertDialog1 = alertDialog.create();
                alertDialog1.show();

            }
        });
    }

    private void checkStatus() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("locked users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(mAuth.getCurrentUser().getUid()).exists()){
                    status = "locked";
                }
                else{
                    status = "unlocked";
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getUserDetails() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users").child(mAuth.getCurrentUser().getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user = snapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else {

            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(HomeActivity.this);
            alertDialogBuilder.setMessage("Are you want to exit?");
            alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    HomeActivity.this.finishAffinity();
                }
            });
            alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

        }
    }

    public void addMessage() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("messages").child(mAuth.getCurrentUser().getUid());
        String msgId = reference.push().getKey();
        //String date = DateFormat.getDateInstance().format(new Date());
        String msg = editTextMsg.getText().toString();
        String encryptedMsg = editTextMsg.getText().toString();
        try{
             encryptedMsg = AESCrypt.encrypt(key,editTextMsg.getText().toString());
        }catch (GeneralSecurityException e){
            Toast.makeText(HomeActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
        }

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.getDefault());
        String time = sdf.format(new Date());
        //String time = Calendar.getInstance().getTime().toString();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("message", encryptedMsg);
        //hashMap.put("date",date);
        hashMap.put("time", time);
        hashMap.put("id", msgId);
        hashMap.put("type","");
        hashMap.put("imageurl", "");
        hashMap.put("publisher", user.getName());
        reference.child(msgId).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(HomeActivity.this, "success", Toast.LENGTH_SHORT).show();
                    editTextMsg.setText("");
                    String publisher = user.getName();
                    String id = user.getId();
                    try{
                        publisher = AESCrypt.decrypt("asdfgh",publisher);
                    }catch (GeneralSecurityException e){
                        Toast.makeText(HomeActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                    sendNotification(publisher, msg, myUrl,id,publisher);
                    imageUri = null;
                    progressDialog.dismiss();
                }
            }
        });
    }

    public void readMessages() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("messages").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();
                //messageAdapter.notifyItemRangeRemoved(0, messageAdapter.getItemCount());
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Message message = dataSnapshot.getValue(Message.class);
                    messageList.add(message);
                }
                //recyclerView.getRecycledViewPool().clear();
                //messageAdapter.notifyDataSetChanged();

                messageAdapter = new MessageAdapter(HomeActivity.this, messageList);
                recyclerView.setAdapter(messageAdapter);
                recyclerView.stopScroll();
                //linearLayoutManager.scrollToPosition(messageAdapter.getItemCount() - 1);
                recyclerView.smoothScrollToPosition(recyclerView.getBottom());
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        //File storageDir = Environment.getExternalStoragePublicDirectory(
        //        Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  // prefix
                ".jpg",         // suffix
                storageDir      // directory
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        Log.d("photopath", mCurrentPhotoPath);

        //Toast.makeText(CommentsActivity.this, mCurrentPhotoPath, Toast.LENGTH_SHORT).show();
        return image;
    }

    public void addMessageWithAttachment() {
        final StorageReference fileReference;
        fileReference = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));

        uploadtask = fileReference.putFile(imageUri);
        uploadtask.continueWithTask(new Continuation() {
            @Override
            public Object then(@NonNull Task task) throws Exception {
                if (!task.isComplete()) {
                    throw task.getException();
                }
                return fileReference.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()) {

                    Uri downloadUri = (Uri) task.getResult();
                    myUrl = downloadUri.toString();
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("messages").child(mAuth.getCurrentUser().getUid());
                    String msgId = reference.push().getKey();
                    //String date = DateFormat.getDateInstance().format(new Date());
                    String msg = editTextMsg.getText().toString();
                    String encryptedMsg = editTextMsg.getText().toString();
                    try {
                        encryptedMsg = AESCrypt.encrypt(key,msg);
                    }catch (GeneralSecurityException e)
                    {
                        Toast.makeText(HomeActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.getDefault());
                    String time = sdf.format(new Date());
                    //String time = Calendar.getInstance().getTime().toString();
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("message", encryptedMsg);
                    //hashMap.put("date",date);
                    hashMap.put("time", time);
                    hashMap.put("id", msgId);
                    hashMap.put("type",type);
                    hashMap.put("imageurl", myUrl);
                    hashMap.put("publisher", user.getName());

                    reference.child(msgId).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                //Toast.makeText(HomeActivity.this,"success",Toast.LENGTH_SHORT).show();
                                editTextMsg.setText("");
                                String publisher = user.getName();
                                try{
                                    publisher = AESCrypt.decrypt("asdfgh",publisher);
                                }catch (GeneralSecurityException e){
                                    Toast.makeText(HomeActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                                }
                                sendNotification(publisher, msg, myUrl,user.getId(),publisher);
                                imageUri = null;
                                progressDialog.dismiss();
                            }
                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(HomeActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            type = "image";
            //imageView.setImageURI(imageUri);
            Toast.makeText(this, "Image selected, Please! type message", Toast.LENGTH_SHORT).show();
        }
        if (requestCode == 2 && resultCode == RESULT_OK) {
            //File f = new File(mCurrentPhotoPath);
            //Log.d("photopath22",mCurrentPhotoPath);

            if (mCurrentPhotoPath != null) {
                imageUri = Uri.parse(mCurrentPhotoPath);
                Toast.makeText(this, "Image selected, Please! type message", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Null not path", Toast.LENGTH_SHORT).show();
            }
            type = "image";
            //imageUri = data.getData();

            //if(mCurrentPhotoPath!=null){
            // imageUri = Uri.parse(mCurrentPhotoPath);
            //}else imageUri = data.getData();

            //Toast.makeText(CommentsActivity.this, mCurrentPhotoPath, Toast.LENGTH_SHORT).show();
        }
        if(requestCode ==3 && resultCode == RESULT_OK){
            imageUri = data.getData();
            type = "pdf";
            Toast.makeText(this, "PDF selected, Please! type message", Toast.LENGTH_SHORT).show();
            Log.d("uriuri",String.valueOf(imageUri));
        }

    }

    void openCamera() {
        ContentValues value = new ContentValues();
        value.put(MediaStore.Images.Media.TITLE, new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()));
        value.put(MediaStore.Images.Media.DESCRIPTION, "From Camera");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, value);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(cameraIntent, 2);
    }

    @Override
    protected void onStart() {
        messageList.clear();
        super.onStart();
    }

    private void sendNotification(String user, String questionText, String imageUrl,String id,String username) {
        //addDummyReq();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("tokens").child("2Wc3jYgqdBTjIC1JDwUH9NMesfx2");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    to = dataSnapshot.getValue(String.class);
                    createNotification(user,questionText,imageUrl,id,username);
                }

                Toast.makeText(HomeActivity.this, to, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }



    public void setToken() {


        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                if (task.isSuccessful()) {
                    DatabaseReference reference;
                    reference = FirebaseDatabase.getInstance().getReference().child("tokens").child(mAuth.getCurrentUser().getUid());
                    final Map hashMap = new HashMap<>();
                    hashMap.put("token", task.getResult().getToken());
                    reference.updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            //Toast.makeText(HomeActivity.this,"Notifications subscribed",Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(HomeActivity.this, "error token", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
    private void createNotification(String user,String questionText,String imageUrl,String id,String username){
        JSONObject jsonObject = new JSONObject();
        String URL = "https://fcm.googleapis.com/fcm/send";
        try {
            jsonObject.put("to", to);
            jsonObject.put("content_avaliable", "true");
            jsonObject.put("priority", "high");
            JSONObject notiobj = new JSONObject();
            notiobj.put("title", user);
            notiobj.put("click_action","OPEN_ACTIVITY_ADMINCHAT");
            notiobj.put("body", questionText);
            JSONObject dataobj = new JSONObject();
            dataobj.put("id",id);
            dataobj.put("username",username);
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
                    Toast.makeText(HomeActivity.this, error.toString(), Toast.LENGTH_SHORT).show();

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
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;

    public boolean checkPermissionREAD_EXTERNAL_STORAGE(
            final Context context) {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        (Activity) context,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    showDialog("External storage", context,
                            Manifest.permission.READ_EXTERNAL_STORAGE);

                } else {
                    ActivityCompat
                            .requestPermissions(
                                    (Activity) context,
                                    new String[] { Manifest.permission.READ_EXTERNAL_STORAGE },
                                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                }
                return false;
            } else {
                return true;
            }

        } else {
            return true;
        }
    }
    public void showDialog(final String msg, final Context context,
                           final String permission) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle("Permission necessary");
        alertBuilder.setMessage(msg + " permission is necessary");
        alertBuilder.setPositiveButton(android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions((Activity) context,
                                new String[] { permission },
                                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                    }
                });
        AlertDialog alert = alertBuilder.create();
        alert.show();
    }

        private boolean checkPermission() {
            int result = ContextCompat.checkSelfPermission(HomeActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (result == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                return false;
            }
        }
    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(HomeActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(HomeActivity.this, "Write External Storage permission allows us to save files. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(HomeActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 11);
        }
    }
    public void setItems(ArrayList<Message> newMessages) {
        //get the current items
        int currentSize = messageAdapter.getItemCount();
        //remove the current items
        messageList.clear();
        //add all the new items
        messageList.addAll(newMessages);
        //tell the recycler view that all the old items are gone
        messageAdapter.notifyItemRangeRemoved(0, currentSize);
        //tell the recycler view how many new items we added
        //notifyItemRangeInserted(0, new);
    }
    public interface MyCallback {
        // Declaration of the template function for the interface
        public void updateMyText(String myString);
    }
    private void getTimer() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("timer").child(mAuth.getCurrentUser().getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                timerList.clear();
                //for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                //  timerList.add(dataSnapshot.getValue(Timer.class));
                //}
                timerList.add(snapshot.getValue(Timer.class));
                timerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}

