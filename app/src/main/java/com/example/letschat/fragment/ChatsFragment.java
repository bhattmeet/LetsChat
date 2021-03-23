package com.example.letschat.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.letschat.R;
import com.example.letschat.activity.ChatActivity;
import com.example.letschat.model.Contacts;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatsFragment extends Fragment {

    private View chatView;
    private RecyclerView rvChats;
    private String currentUserId;

    private DatabaseReference chatReference,userReference,groupReference ;
    private FirebaseAuth mAuth;

    public ChatsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        chatView =  inflater.inflate(R.layout.fragment_chats, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        chatReference = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserId);
        userReference = FirebaseDatabase.getInstance().getReference().child("Users");
        groupReference = FirebaseDatabase.getInstance().getReference().child("Groups");

        rvChats = chatView.findViewById(R.id.rvChats);
        rvChats.setLayoutManager(new LinearLayoutManager(getContext()));

        return chatView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(chatReference,Contacts.class).build();

        FirebaseRecyclerAdapter<Contacts,chatViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, chatViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull chatViewHolder holder, int position, @NonNull Contacts model) {
                final String userId = getRef(position).getKey();

                userReference.child(userId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            if (snapshot.hasChild("image")){
                                final String userImg = snapshot.child("image").getValue().toString();
                                Picasso.get().load(userImg).placeholder(R.drawable.ic_baseline_person_24).into(holder.imgUser);
                            }

                            final String userName = snapshot.child("username").getValue().toString();
                            final String userStatus = snapshot.child("status").getValue().toString();
                            final String userImg = snapshot.child("image").getValue().toString();

                            holder.txtUserName.setText(userName);

                            if (snapshot.child("userStatus").hasChild("status")){
                                String status = snapshot.child("userStatus").child("status").getValue().toString();
                                String date = snapshot.child("userStatus").child("date").getValue().toString();
                                String time = snapshot.child("userStatus").child("time").getValue().toString();

                                if (status.equals("online")){
                                    holder.txtUserStatus.setText("Online");
                                    holder.txtUserStatus.setTextSize(14);
                                }else if (status.equals("offline")){
                                    holder.txtUserStatus.setText("Last Seen: " + date + " " +time);
                                    holder.txtUserStatus.setTextSize(14);
                                }
                            }else {
                                holder.txtUserStatus.setText("offline");
                                holder.txtUserStatus.setTextSize(14);
                            }


                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                    chatIntent.putExtra("userId",userId);
                                    chatIntent.putExtra("userName",userName);
                                    chatIntent.putExtra("userImg",userImg);
                                    startActivity(chatIntent);
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @NonNull
            @Override
            public chatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_user,parent,false);

                return new chatViewHolder(view);
            }
        };

        rvChats.setAdapter(adapter);
        adapter.startListening();
    }

    private static class chatViewHolder extends RecyclerView.ViewHolder{

        private CircleImageView imgUser;
        private TextView txtUserName,txtUserStatus;

        public chatViewHolder(@NonNull View itemView) {
            super(itemView);

            imgUser = itemView.findViewById(R.id.imgUser);
            txtUserName = itemView.findViewById(R.id.txtUserName);
            txtUserStatus = itemView.findViewById(R.id.txtUserStatus);
        }
    }
}