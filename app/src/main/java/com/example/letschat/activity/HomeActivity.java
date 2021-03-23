package com.example.letschat.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.example.letschat.R;
import com.example.letschat.adapter.TabAccessorAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;

public class HomeActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ViewPager myViewPager;
    private TabLayout myTabLayout;
    private TabAccessorAdapter myTabAccessorAdapter;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private String currentUserID,groupName;
    private DatabaseReference RootRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        currentUserID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();

        mToolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Let's Chat");

        myViewPager = findViewById(R.id.main_tabs_pager);
        myTabAccessorAdapter = new TabAccessorAdapter(getSupportFragmentManager());
        myViewPager.setAdapter(myTabAccessorAdapter);

        myTabLayout = findViewById(R.id.main_tabs);
        myTabLayout.setupWithViewPager(myViewPager);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (currentUser != null){
            updateStatus("online");
            verifyUser();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (currentUser != null){
            updateStatus("offline");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (currentUser != null){
            updateStatus("offline");
        }
    }

    private void verifyUser(){
        RootRef.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("username").exists()){
//                    Toast.makeText(HomeActivity.this, "Welcome back", Toast.LENGTH_SHORT).show();
                }else {
                    Intent settingintent = new Intent(HomeActivity.this,SettingsActivity.class);
                    settingintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    settingintent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(settingintent);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.home_menu,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()){
            case R.id.logout_menu:
                mAuth.signOut();
                updateStatus("offline");
                Intent intent = new Intent(HomeActivity.this,LoginActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.settings_menu:
                Intent settingintent = new Intent(HomeActivity.this,SettingsActivity.class);
                startActivity(settingintent);
                break;
            case R.id.find_friends_menu:
                Intent findfriendintent = new Intent(HomeActivity.this,FindFriendsActivity.class);
                startActivity(findfriendintent);
                break;
            case R.id.create_new_group:
                createNewGroup();
                break;
        }
        return true;
    }

    private void createNewGroup(){
        AlertDialog.Builder builder =  new AlertDialog.Builder(this,R.style.AlertDialog);
        builder.setTitle("Enter Group Name:");
        final EditText grpName = new EditText(this);
        grpName.setHint("e.g Office");
        builder.setView(grpName);

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                groupName = grpName.getText().toString().trim();
                if (TextUtils.isEmpty(groupName)){
                    Toast.makeText(HomeActivity.this, "Please enter group name", Toast.LENGTH_SHORT).show();
                }else {
                    createGroup(groupName);
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void createGroup(String groupName){
        RootRef.child("Groups").child(groupName).setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(HomeActivity.this, groupName + " group is Created Successfully", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateStatus(String status){
        String saveCurrentTime, saveCurrentDate;

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        HashMap<String,Object> onlineStatus = new HashMap<>();
        onlineStatus.put("time",saveCurrentTime);
        onlineStatus.put("date",saveCurrentDate);
        onlineStatus.put("status",status);


        RootRef.child("Users").child(currentUserID).child("userStatus").updateChildren(onlineStatus);
    }
}