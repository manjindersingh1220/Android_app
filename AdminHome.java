package com.example.caipsgcms;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.internal.NavigationMenu;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminHome extends AppCompatActivity {
    List<User> userList;
    LinearLayoutManager linearLayoutManager;
    RecyclerView recyclerView;
    FirebaseAuth mAuth;
    Toolbar toolbar;
    String loadingType;
    ActionBarDrawerToggle actionBarDrawerToggle;
    NavigationView navigationView;
    ProgressDialog progressDialog;
    UserAdapter userAdapter;
    DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);
        recyclerView = findViewById(R.id.recylerview_users);
        mAuth = FirebaseAuth.getInstance();
        toolbar = findViewById(R.id.toolbar_admin_home);
        drawerLayout = findViewById(R.id.drawer_layout_admin);
        setSupportActionBar(toolbar);
        navigationView = findViewById(R.id.nav_view_admin_home);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.open,R.string.close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        userList = new ArrayList<>();
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(false);
        linearLayoutManager.setStackFromEnd(false);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        userAdapter = new UserAdapter(this, userList);
        recyclerView.setAdapter(userAdapter);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        if(getIntent().getExtras()!=null){
            loadingType = getIntent().getStringExtra("type");
        }
        loadUsers();

        //loadLockedUsers();
        setToken();
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.lockedUsers:

                        progressDialog.show();
                        toolbar.setSubtitle("Locked users");
                        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
                            drawerLayout.closeDrawer(GravityCompat.START);
                        }
                        loadLockedUsers();
                        Toast.makeText(AdminHome.this,"Locked Users",Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.signOutadmin:
                        FirebaseDatabase.getInstance().getReference("tokens").child(mAuth.getCurrentUser().getUid()).removeValue();
                        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(AdminHome.this);
                        alertDialogBuilder.setMessage("Are you want to Sign Out?");
                        alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                FirebaseAuth.getInstance().signOut();
                                Intent intent4 = new Intent(AdminHome.this,Login.class);
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
                    case R.id.allUsers:

                        progressDialog.show();
                        toolbar.setSubtitle("All users");
                        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
                            drawerLayout.closeDrawer(GravityCompat.START);
                        }
                        loadUsers();
                        Toast.makeText(AdminHome.this,"All users",Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.timedUsers:
                        progressDialog.show();
                        toolbar.setSubtitle("Timed users");
                        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
                            drawerLayout.closeDrawer(GravityCompat.START);
                        }
                        loadTimedUsers();
                        Toast.makeText(AdminHome.this,"Timed users",Toast.LENGTH_SHORT).show();
                        break;

                }
                return false;
            }
        });
    }

    private void loadLockedUsers() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child("locked users").exists()){
                    getLockedUsers();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getLockedUsers() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("locked users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){

                    String lockedUsers =  dataSnapshot.getKey();
                    loaduserslocked(lockedUsers);
                    progressDialog.dismiss();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loaduserslocked(String lockedUsers) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        Query query = reference.orderByChild("id").equalTo(lockedUsers);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);
                    userList.add(user);
                }
                userAdapter.notifyDataSetChanged();
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void loadUsers() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);
                    userList.add(user);
                }
                userAdapter.notifyDataSetChanged();
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminHome.this, error.getMessage(), Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(AdminHome.this, "error token", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    public void onBackPressed() {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(AdminHome.this);
                alertDialogBuilder.setMessage("Are you want to exit?");
                alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        AdminHome.this.finishAffinity();
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
    public void loadTimedUsers(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("timed users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){

                    String lockedUsers =  dataSnapshot.getKey();
                    loaduserslocked(lockedUsers);
                    progressDialog.dismiss();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

}