package com.example.letschat.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.letschat.R;
import com.example.letschat.model.Contacts;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class FindFriendsActivity extends AppCompatActivity implements View.OnClickListener{

    private View mToolbar;
    private TextView txtName;
    private ImageView imgBack;
    private RecyclerView rvFriends;
    private DatabaseReference databaseReference;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");

        setIds();
        txtName.setText(getString(R.string.find_friends));

        imgBack.setOnClickListener(this);

    }

    private void setIds(){
        mToolbar = findViewById(R.id.toolbar);
        txtName = findViewById(R.id.txtName);
        imgBack = findViewById(R.id.imgBack);
        rvFriends = findViewById(R.id.rvFirends);
        rvFriends.setLayoutManager(new LinearLayoutManager(FindFriendsActivity.this));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.imgBack:
                onBackPressed();
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(databaseReference,Contacts.class).build();

        FirebaseRecyclerAdapter<Contacts,findFriendViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, findFriendViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull findFriendViewHolder holder, int position, @NonNull Contacts model) {
                holder.txtUserName.setText(model.getUsername());
                holder.txtUserStatus.setText(model.getStatus());
                Picasso.get().load(model.getImage()).into(holder.imgUser);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        userId = getRef(position).getKey();

                        Intent profileIntent = new Intent(FindFriendsActivity.this,ProfileActivity.class);
                        profileIntent.putExtra("userId",userId);
                        profileIntent.putExtra("userName",model.getUsername());
                        startActivity(profileIntent);

                    }
                });
            }

            @NonNull
            @Override
            public findFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_user,parent,false);
                findFriendViewHolder viewHolder = new findFriendViewHolder(view);
                return viewHolder;
            }
        };
        rvFriends.setAdapter(adapter);
        adapter.startListening();
    }

    public static class findFriendViewHolder extends RecyclerView.ViewHolder{

        private TextView txtUserName,txtUserStatus;
        private ImageView imgUser,imgStatus;

        public findFriendViewHolder(@NonNull View itemView) {
            super(itemView);

            txtUserName = itemView.findViewById(R.id.txtUserName);
            txtUserStatus = itemView.findViewById(R.id.txtUserStatus);
            imgUser = itemView.findViewById(R.id.imgUser);
            imgStatus = itemView.findViewById(R.id.imgStatus);
        }
    }
}