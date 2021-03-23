package com.example.letschat.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.letschat.R;
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

public class ContactsFragment extends Fragment {

    private RecyclerView rvContactList;
    private View contactView;
    private String currentUserId,userId,userImg,userName,userStatus;
    private View view;

    private DatabaseReference contactReference,userReference;
    private FirebaseAuth mAuth;

    public ContactsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        contactView = inflater.inflate(R.layout.fragment_contacts, container, false);

        rvContactList = contactView.findViewById(R.id.rvContactList);
        rvContactList.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        contactReference = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserId);
        userReference = FirebaseDatabase.getInstance().getReference().child("Users");

        return contactView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(contactReference,Contacts.class).build();

        FirebaseRecyclerAdapter<Contacts, contactsViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, contactsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull contactsViewHolder holder, int position, @NonNull Contacts model) {
                userId = getRef(position).getKey();

                userReference.child(userId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){

                            if (snapshot.child("userStatus").hasChild("status")){
                                String status = snapshot.child("userStatus").child("status").getValue().toString();
                                String date = snapshot.child("userStatus").child("date").getValue().toString();
                                String time = snapshot.child("userStatus").child("time").getValue().toString();

                                if (status.equals("online")){
                                    holder.imgStatus.setVisibility(View.VISIBLE);
                                }else if (status.equals("offline")){
                                    holder.imgStatus.setVisibility(View.GONE);
                                }
                            }else {
                                holder.imgStatus.setVisibility(View.GONE);
                            }

                            if (snapshot.hasChild("image")){
                                userImg = snapshot.child("image").getValue().toString();
                                userName = snapshot.child("username").getValue().toString();
                                userStatus = snapshot.child("status").getValue().toString();

                                holder.txtUserName.setText(userName);
                                holder.txtUserStatus.setText(userStatus);
                                holder.txtUserStatus.setMaxLines(3);
                                Picasso.get().load(userImg).placeholder(R.drawable.ic_baseline_person_24).into(holder.imgUser);
                            }else {
                                userName = snapshot.child("username").getValue().toString();
                                userStatus = snapshot.child("status").getValue().toString();

                                holder.txtUserName.setText(userName);
                                holder.txtUserStatus.setText(userStatus);
                                holder.txtUserStatus.setMaxLines(3);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @NonNull
            @Override
            public contactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_user,parent,false);
                contactsViewHolder viewHolder = new contactsViewHolder(view);
                return viewHolder;
            }
        };
        rvContactList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class contactsViewHolder extends RecyclerView.ViewHolder{

        private TextView txtUserName, txtUserStatus;
        private CircleImageView imgUser;
        private ImageView imgStatus;

        public contactsViewHolder(@NonNull View itemView) {
            super(itemView);

            txtUserName = itemView.findViewById(R.id.txtUserName);
            txtUserStatus = itemView.findViewById(R.id.txtUserStatus);
            imgUser = itemView.findViewById(R.id.imgUser);
            imgStatus = itemView.findViewById(R.id.imgStatus);
        }
    }
}