package com.example.caipsgcms;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
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
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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
import java.util.concurrent.TimeUnit;

import static android.media.MediaExtractor.MetricsConstants.FORMAT;

public class AdminChat extends AppCompatActivity {
    RecyclerView recyclerView,recyclerViewTimer;
    private FirebaseAuth mAuth;
    private MessageAdapter messageAdapter;
    private List<Message> messageList;
    private List<Timer> timerList;
    EditText editTextMsg;
    public User user;
    private TimerAdapter timerAdapter;
    ProgressDialog progressDialog;
    Uri imageUri;
    String to,status;
    String type="";
    Toolbar toolbar;
    String publisher;
    private String key ="asdfgh";
    private String myUrl = null;
    StorageTask uploadtask;
    StorageReference storageReference;
    private static String mCurrentPhotoPath;
    LinearLayoutManager linearLayoutManager,linearLayoutManagerTimer;
    RequestQueue mRequestQue;
    ImageView send,attach;
    String userid;
    ValueEventListener valueEventListener =null;
    DatabaseReference reference;
    private int lastposition;
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_chat);
        if(getIntent().getExtras()!=null){
            userid = getIntent().getStringExtra("id");
            Log.d("userid",userid);
        }
        else{
            //userid = savedInstanceState.getString("id");
            Log.d("userid",userid+" bundle");
        }

        editTextMsg = findViewById(R.id.admin_msg_edit);
        send = findViewById(R.id.admin_send_btn);
        attach = findViewById(R.id.admin_attach);
        toolbar = findViewById(R.id.toolbar_admin_chat);
        toolbar.setSubtitle(getIntent().getStringExtra("username"));
        toolbar.inflateMenu(R.menu.admin_menu);
        recyclerView = findViewById(R.id.admin_recylerview);
        recyclerViewTimer = findViewById(R.id.recylerViewTimer);
        messageList = new ArrayList<>();
        timerList = new ArrayList<>();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading");
        progressDialog.setCanceledOnTouchOutside(false);

        timerAdapter = new TimerAdapter(this,timerList);
        mRequestQue = Volley.newRequestQueue(this);
        mAuth = FirebaseAuth.getInstance();

        recyclerViewTimer.setAdapter(timerAdapter);
        storageReference = FirebaseStorage.getInstance().getReference(userid);
        //firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManagerTimer = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(false);
        linearLayoutManagerTimer.setReverseLayout(false);
        linearLayoutManager.setStackFromEnd(false);
        linearLayoutManagerTimer.setStackFromEnd(false);
        recyclerView.setHasFixedSize(true);
        recyclerViewTimer.setHasFixedSize(true);
        recyclerView.setSaveEnabled(true);
        recyclerView.setItemViewCacheSize(50);
        //recyclerView.setAnimationCacheEnabled(false);
        //recyclerView.dispatchWindowFocusChanged(true);

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerViewTimer.setLayoutManager(linearLayoutManagerTimer);

        try {
            publisher = AESCrypt.encrypt("asdfgh","admin");
        }catch (GeneralSecurityException e){
            Toast.makeText(AdminChat.this,e.getMessage(),Toast.LENGTH_SHORT).show();
        }
        linearLayoutManager.setSmoothScrollbarEnabled(true);
        progressDialog.show();
        readMessages();
        getTimer(userid);
        getStatus(userid);
        getUserDetails(toolbar);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!TextUtils.isEmpty(editTextMsg.getText()))
                {
                    progressDialog.show();
                    try {
                        InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                    } catch (Exception e) {
                        // TODO: handle exception
                    }
                    if(imageUri!=null){
                        addMessageWithAttachment();
                    }
                    else{
                        addMessage();
                    }

                }
            }
        });
        attach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String list[] = {"Camera","Gallery","Select PDF"};
                android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(AdminChat.this);
                alertDialog.setTitle("Select Attachment")
                        .setItems(list, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(which == 9)
                                {
                                    if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
                                            == PackageManager.PERMISSION_DENIED){
                                        ActivityCompat.requestPermissions(AdminChat.this, new String[] {Manifest.permission.CAMERA}, 2);
                                    }else{
                                        //Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                                        //startActivityForResult(intent, 1);
                                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                        Uri uriForFile=null;
                                        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                                            // Create the File where the photo should go
                                            File photoFile = null;
                                            try {
                                                photoFile = createImageFile();
                                            } catch (IOException ex) {
                                                // Error occurred while creating the File
                                                Log.i("Phototag", "IOException");
                                                Toast.makeText(AdminChat.this,ex.getMessage(),Toast.LENGTH_SHORT).show();
                                            }
                                            // Continue only if the File was successfully created
                                            if (photoFile != null) {
                                                Toast.makeText(AdminChat.this,"photo not null",Toast.LENGTH_SHORT).show();
                                                uriForFile = FileProvider.getUriForFile(AdminChat.this,"com.example.myapplication.fileprovider",photoFile);
                                                List<ResolveInfo> resInfoList = getBaseContext().getPackageManager().queryIntentActivities(cameraIntent, PackageManager.MATCH_DEFAULT_ONLY);
                                                for (ResolveInfo resolveInfo : resInfoList) {
                                                    String packageName = resolveInfo.activityInfo.packageName;
                                                    getBaseContext().grantUriPermission(packageName, uriForFile, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                                }
                                                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriForFile);
                                                startActivityForResult(cameraIntent, 2);
                                            }
                                        }
                                        if(mCurrentPhotoPath!=null){
                                            imageUri = Uri.parse(mCurrentPhotoPath);
                                        }

                                    }

                                }
                                else if(which == 0){
                                    if(!checkPermissionREAD_EXTERNAL_STORAGE(AdminChat.this))
                                    {
                                        checkPermissionREAD_EXTERNAL_STORAGE(AdminChat.this);
                                    }
                                    else if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
                                            == PackageManager.PERMISSION_DENIED) {
                                        ActivityCompat.requestPermissions(AdminChat.this, new String[]{Manifest.permission.CAMERA}, 2);
                                    }else if(!checkPermission()){
                                        requestPermission();
                                    }
                                    else{
                                        openCamera();
                                    }
                                }
                                else if(which == 1)
                                {
                                    Intent intent = new Intent(Intent.ACTION_PICK);
                                    intent.setType("image/*");
                                    startActivityForResult(intent, 1);
                                }
                                else if(which ==2 ){
                                    Log.d("pdf","inpdf");
                                    Intent intent = new Intent();
                                    intent.setType("application/pdf");
                                    intent.setAction(Intent.ACTION_GET_CONTENT);
                                    startActivityForResult(intent, 3);

                                }
                            }
                        });
                android.app.AlertDialog alertDialog1 = alertDialog.create();
                alertDialog1.show();

            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                lastposition =linearLayoutManager.findLastVisibleItemPosition();
            }
        });
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {

                if(item.getItemId()==R.id.timer)
                {
                    //Toast.makeText(AdminChat.this,"tool click",Toast.LENGTH_SHORT).show();
                    // Create an alert builder
                    AlertDialog.Builder builder
                            = new AlertDialog.Builder(AdminChat.this);
                    builder.setTitle("Are you really want to set timer");

                    // set the custom layout
                    final View customLayout
                            = getLayoutInflater()
                            .inflate(
                                    R.layout.customlayout,
                                    null);
                    builder.setView(customLayout);
                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                       EditText editText = customLayout.findViewById(R.id.timePick);
                       //Toast.makeText(AdminChat.this,editText.getText().toString(),Toast.LENGTH_SHORT).show();
                       createCustomFloat(editText.getText().toString());
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                else if(item.getItemId()==R.id.qq){
                    if(TextUtils.equals(status,"locked")){
                        unlockuser(userid);
                    }
                    else
                    {
                        Toast.makeText(AdminChat.this,"Already Unlocked",Toast.LENGTH_SHORT).show();
                    }
                }
                else if(item.getItemId()==R.id.viewProfile)
                {
                    Intent intent = new Intent(AdminChat.this,UserDetails.class);
                    intent.putExtra("userid",userid);
                    startActivity(intent);
                }
                else if(item.getItemId()==R.id.lockUser){
                    AlertDialog.Builder builder
                            = new AlertDialog.Builder(AdminChat.this);
                    builder.setTitle("Are you really lock this user");
                    // set the custom layout
                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(TextUtils.equals(status,"unlocked")){
                                lockUser(userid);
                            }
                            else
                            {
                                Toast.makeText(AdminChat.this,"Already Locked",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                else if(item.getItemId()==R.id.delete){
                    deleteUser(userid);
                }

                else
                {

                }

                return false;
            }
        });
    }

    private void getStatus(String userid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("locked users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(userid).exists()){
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



    private void unlockuser(String userid) {
        FirebaseDatabase.getInstance().getReference("locked users").child(userid).removeValue();
        status = "unlocked";
        setLockTimer(userid,"");
    }

    private void deleteUser(String userid) {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(AdminChat.this);
        alertDialogBuilder.setMessage("Are you want to delete this user?");
        alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                    lockUser(userid);
                    FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
                    StorageReference storageReference = firebaseStorage.getReference(userid);
                    storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(AdminChat.this, "User deleted successfully", Toast.LENGTH_SHORT).show();
                        }
                    });
                FirebaseDatabase.getInstance().getReference().child("messages").child(userid).removeValue();
                FirebaseDatabase.getInstance().getReference().child("users").child(userid).removeValue();
                FirebaseDatabase.getInstance().getReference().child("token").child(userid).removeValue();
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("timer").child(userid);
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("load","1");
                hashMap.put("time","Your account has been deleted");
                reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(AdminChat.this,"User deleted successfully",Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            Toast.makeText(AdminChat.this,"Error while deleting user",Toast.LENGTH_SHORT).show();
                        }
                    }
                });



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

    private void lockUser(String userid) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status","locked");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("locked users");
        reference.child(userid).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    //Toast.makeText(AdminChat.this,"User locked successfully",Toast.LENGTH_SHORT).show();
                    setLockTimer(userid,"Your are locked by admin");
                }
                else
                {
                    Toast.makeText(AdminChat.this,"Error while locking user",Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void setLockTimer(String userid,String msg) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("timer").child(userid);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("time",msg);
        //hashMap.put("time","https://firebasestorage.googleapis.com/v0/b/caips-eccd2.appspot.com/o/new.html?alt=media&token=d9ec62d6-ef7e-4bf3-9e5d-d069506e18fc");
        hashMap.put("load","1");
        reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {   if(TextUtils.equals(msg,"Your are locked by admin"))
                    {
                        Toast.makeText(AdminChat.this,"Locked Successfully",Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(AdminChat.this,"Unlocked Successfully",Toast.LENGTH_SHORT).show();
                    }

                }
                else
                {
                    Toast.makeText(AdminChat.this,"Error while locking",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void getTimer(String userid1) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("timer").child(userid);
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

    private void createCustomFloat(String days) {
        String getdays = days;
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("timer").child(userid);
        int day;
        if(!TextUtils.equals(getdays,"")){
            day = Integer.parseInt(days);
        }
        else{
            day = 0;
        }


        Calendar calendar = Calendar.getInstance();
        Date today = calendar.getTime();

        calendar.add(Calendar.DAY_OF_YEAR, day);
        Date tomorrow = calendar.getTime();
        DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("time","Your CAIPS notes will be avaliable by "+ dateFormat.format(tomorrow));
        //hashMap.put("time","https://firebasestorage.googleapis.com/v0/b/caips-eccd2.appspot.com/o/new.html?alt=media&token=d9ec62d6-ef7e-4bf3-9e5d-d069506e18fc");
        hashMap.put("load","2");
        reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    timedUserEntry(days,today,tomorrow);
                    sendNotification("admin","Admin created a timer","");
                    //Toast.makeText(AdminChat.this,"Count Down started",Toast.LENGTH_SHORT).show();
                }
                else
                {

                }
            }
        });

    }

    private void getUserDetails(Toolbar toolbar1) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users").child(userid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user = snapshot.getValue(User.class);
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminChat.this,error.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode ==1 && resultCode ==RESULT_OK && data!= null)
        {
            imageUri = data.getData();
            type = "image";
            //imageView.setImageURI(imageUri);
            Toast.makeText(this, "Image selected, Please type message", Toast.LENGTH_SHORT).show();
        }
        if(requestCode ==2 && resultCode ==RESULT_OK)
        {
            //File f = new File(mCurrentPhotoPath);
            //Log.d("photopath22",mCurrentPhotoPath);
            if(mCurrentPhotoPath!=null) {
                imageUri = Uri.parse(mCurrentPhotoPath);

                Toast.makeText(this, "Image selected, Please type message", Toast.LENGTH_SHORT).show();
            }
            type = "image";
            //imageUri = data.getData();

            //if(mCurrentPhotoPath!=null){
            // imageUri = Uri.parse(mCurrentPhotoPath);
            //}else imageUri = data.getData();
            Toast.makeText(this, imageUri.toString(), Toast.LENGTH_SHORT).show();
            //Toast.makeText(CommentsActivity.this, mCurrentPhotoPath, Toast.LENGTH_SHORT).show();
        }
        if(requestCode == 3 && resultCode == RESULT_OK){
            imageUri = data.getData();
            type = "pdf";
            Toast.makeText(this, "PDF selected, Please type message", Toast.LENGTH_SHORT).show();
            Log.d("uriuri",String.valueOf(imageUri));
        }
        if(data== null){
            Toast.makeText(AdminChat.this,"null data",Toast.LENGTH_SHORT).show();
        }

    }
    private String getFileExtension(Uri uri)
    {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }
    public void addMessageWithAttachment(){
        final StorageReference fileReference;
        fileReference = storageReference.child(System.currentTimeMillis()+"."+getFileExtension(imageUri));

        uploadtask = fileReference.putFile(imageUri);
        uploadtask.continueWithTask(new Continuation() {
            @Override
            public Object then(@NonNull Task task) throws Exception {
                if(!task.isComplete())
                {
                    throw task.getException();
                }
                return fileReference.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if(task.isSuccessful()){

                    Uri downloadUri = (Uri) task.getResult();
                    myUrl = downloadUri.toString();
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("messages").child(userid);
                    String msgId = reference.push().getKey();
                    //String date = DateFormat.getDateInstance().format(new Date());
                    final String msg = editTextMsg.getText().toString();
                    String encryptedMsg = editTextMsg.getText().toString();
                    try{
                        encryptedMsg = AESCrypt.encrypt(key,encryptedMsg);
                    }catch (GeneralSecurityException e){
                        Toast.makeText(AdminChat.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.getDefault());
                    String time = sdf.format(new Date());
                    //String time = Calendar.getInstance().getTime().toString();
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("message",encryptedMsg);
                    //hashMap.put("date",date);
                    hashMap.put("time",time);
                    hashMap.put("id",msgId);
                    hashMap.put("type",type);
                    hashMap.put("imageurl",myUrl);
                    hashMap.put("publisher",publisher);
                    reference.child(msgId).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                //Toast.makeText(AdminChat.this,"success",Toast.LENGTH_SHORT).show();
                                editTextMsg.setText("");
                                imageUri = null;
                                sendNotification("admin",msg,myUrl);
                                progressDialog.dismiss();
                            }
                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AdminChat.this,e.getMessage(),Toast.LENGTH_SHORT).show();
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
        Log.d("photopath",mCurrentPhotoPath);

        //Toast.makeText(CommentsActivity.this, mCurrentPhotoPath, Toast.LENGTH_SHORT).show();
        return image;
    }
    public void readMessages()
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("messages").child(userid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    Message message = dataSnapshot.getValue(Message.class);
                    messageList.add(message);
                }
                messageAdapter = new MessageAdapter( AdminChat.this, messageList);
                recyclerView.setAdapter(messageAdapter);
                //messageAdapter.notifyDataSetChanged();
                //linearLayoutManager.scrollToPosition(messageAdapter.getItemCount()-1);
                recyclerView.stopScroll();
                //linearLayoutManager.scrollToPosition(messageAdapter.getItemCount() - 1);
                recyclerView.smoothScrollToPosition(recyclerView.getBottom());
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminChat.this,error.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });

    }
    public void addMessage(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("messages").child(userid);
        String msgId = reference.push().getKey();
        //String date = DateFormat.getDateInstance().format(new Date());
        final String msg = editTextMsg.getText().toString();
        String encryptedMsg = editTextMsg.getText().toString();
        try{
            encryptedMsg = AESCrypt.encrypt(key,encryptedMsg);
        }catch (GeneralSecurityException e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.getDefault());
        String time = sdf.format(new Date());
        //String time = Calendar.getInstance().getTime().toString();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("message",encryptedMsg);
        //hashMap.put("date",date);
        hashMap.put("time",time);
        hashMap.put("id",msgId);
        hashMap.put("imageurl","");
        hashMap.put("publisher",publisher);
        reference.child(msgId).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(AdminChat.this,"success",Toast.LENGTH_SHORT).show();
                    editTextMsg.setText("");
                    sendNotification("admin",msg,myUrl);
                    imageUri = null;
                    progressDialog.dismiss();
                }
            }
        });
    }
    @Override
    protected void onStart() {
        //messageList.clear();
        super.onStart();
    }
    private void sendNotification(String user1, String questionText, String imageUrl) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("tokens").child(userid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                    to = dataSnapshot.getValue(String.class);
                    createNotification(user1,questionText,imageUrl);
                }

                Toast.makeText(AdminChat.this, to, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void createNotification(String user1, String questionText, String imageUrl) {
        JSONObject jsonObject = new JSONObject();
        String URL = "https://fcm.googleapis.com/fcm/send";
        try{
            jsonObject.put("to",to);
            jsonObject.put("content_avaliable","true");
            jsonObject.put("priority","high");
            JSONObject notiobj = new JSONObject();
            notiobj.put("title",user1);
            notiobj.put("body", questionText);
            if(TextUtils.equals("",imageUrl)){
                notiobj.put("image",imageUrl);
            }
            jsonObject.put("notification",notiobj);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL, jsonObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            //Toast.makeText(AskAQuestion.this, "Done", Toast.LENGTH_SHORT).show();
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(AdminChat.this, error.toString(), Toast.LENGTH_SHORT).show();

                }
            }
            ){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String,String> header = new HashMap<>();
                    header.put("content-type","application/json");
                    header.put("authorization","key=AAAALCh7jx4:APA91bFcGOTlum5fBWWq3OuWsycX2mJSA7qKUL0b7CgeeFeDMU1XfZtyPrsiMeAwmxUeTzZ1FQcmS6S84yQAxENbsHb_sApKsu9u7WuTZk-OBXwt1pMUtS7JDlVBtL3JS7qCN705GjkE");
                    return header;
                }
            };
            request.setRetryPolicy(new DefaultRetryPolicy(1000,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            mRequestQue.add(request);
        }catch (JSONException e){
            e.printStackTrace();
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
        int result = ContextCompat.checkSelfPermission(AdminChat.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }
    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(AdminChat.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(AdminChat.this, "Write External Storage permission allows us to save files. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(AdminChat.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 11);
        }
    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString("id",userid);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        userid = savedInstanceState.getString("id");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.admin_menu, menu);
        return true;
    }
    public void timedUserEntry(String days, Date today, Date tomorrow){
        HashMap<String, Object> hashMap = new HashMap<>();
        DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
        hashMap.put("timedOn",dateFormat.format(today));
        hashMap.put("timedTill",dateFormat.format(tomorrow));
        hashMap.put("totalDays",days);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("timed users");
        reference.child(userid).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(AdminChat.this,"Timer set successfully",Toast.LENGTH_SHORT).show();

                }
                else
                {
                    Toast.makeText(AdminChat.this,"Error while locking user",Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

}