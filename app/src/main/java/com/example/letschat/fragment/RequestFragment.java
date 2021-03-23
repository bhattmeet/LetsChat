package com.example.letschat.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.letschat.R;
import com.example.letschat.model.Contacts;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class RequestFragment extends Fragment {

    private View requestFragmentView;
    private RecyclerView rvChatRequests;
    private String currentUserId,type;
    private View view;

    private DatabaseReference chatRequestReference,userReference,contactReference;
    private FirebaseAuth mAuth;

    public RequestFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        requestFragmentView = inflater.inflate(R.layout.fragment_request, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        chatRequestReference = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        userReference = FirebaseDatabase.getInstance().getReference().child("Users");
        contactReference = FirebaseDatabase.getInstance().getReference().child("Contacts");


        rvChatRequests = requestFragmentView.findViewById(R.id.rvChatRequests);
        rvChatRequests.setLayoutManager(new LinearLayoutManager(getContext()));

        return requestFragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(chatRequestReference.child(currentUserId),Contacts.class).build();

        FirebaseRecyclerAdapter<Contacts,requestViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts,requestViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull requestViewHolder holder, int position, @NonNull Contacts model) {
                holder.btnCancel.setVisibility(View.VISIBLE);
                holder.btnAccept.setVisibility(View.VISIBLE);

                final String userId = getRef(position).getKey();

                DatabaseReference typeReference = getRef(position).child("requestType").getRef();

                typeReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            type = snapshot.getValue().toString();
                            if (type.equals("received")){
                                userReference.child(userId).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.hasChild("image")){
                                            final String userImg = snapshot.child("image").getValue().toString();

                                            Picasso.get().load(userImg).placeholder(R.drawable.ic_baseline_person_24).into(holder.imgUser);
                                        }
                                        final String userName = snapshot.child("username").getValue().toString();
                                        final String userStatus = snapshot.child("status").getValue().toString();

                                        holder.txtUserName.setText(userName);
                                        holder.txtUserStatus.setText("wants to connect with you.");

                                        holder.btnAccept.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                contactReference.child(currentUserId).child(userId).child("Contact").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()){
                                                            contactReference.child(userId).child(currentUserId).child("Contact").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()){
                                                                        chatRequestReference.child(currentUserId).child(userId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if (task.isSuccessful()){
                                                                                    chatRequestReference.child(userId).child(currentUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                            if (task.isSuccessful()){
                                                                                                Toast.makeText(getContext(), "New Contact Saved", Toast.LENGTH_SHORT).show();
                                                                                            }
                                                                                        }
                                                                                    });
                                                                                }
                                                                            }
                                                                        });
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    }
                                                });
                                            }
                                        });

                                        holder.btnCancel.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                chatRequestReference.child(currentUserId).child(userId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()){
                                                            chatRequestReference.child(userId).child(currentUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()){
                                                                        Toast.makeText(getContext(), "Request Cancel", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    }
                                                });
                                            }
                                        });
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                            else if (type.equals("sent")){
                                holder.btnAccept.setText("Request Cancel");
                                holder.btnCancel.setVisibility(View.GONE);

                                userReference.child(userId).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.hasChild("image")){
                                            final String userImg = snapshot.child("image").getValue().toString();

                                            Picasso.get().load(userImg).placeholder(R.drawable.ic_baseline_person_24).into(holder.imgUser);
                                        }
                                        final String userName = snapshot.child("username").getValue().toString();
                                        final String userStatus = snapshot.child("status").getValue().toString();

                                        holder.txtUserName.setText(userName);
                                        holder.txtUserStatus.setText("You have sent a request to "+userName);

                                        holder.btnAccept.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                chatRequestReference.child(currentUserId).child(userId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()){
                                                            chatRequestReference.child(userId).child(currentUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()){
                                                                        Toast.makeText(getContext(), "You have cancelled chat request", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    }
                                                });
                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
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
            public requestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_user,parent,false);
                requestViewHolder holder = new requestViewHolder(view);
                return holder;
            }
        };

        rvChatRequests.setAdapter(adapter);
        adapter.startListening();
    }

    public static class requestViewHolder extends RecyclerView.ViewHolder{

        private TextView txtUserName,txtUserStatus;
        private CircleImageView imgUser;
        private Button btnAccept,btnCancel;

        public requestViewHolder(@NonNull View itemView) {
            super(itemView);

            txtUserName = itemView.findViewById(R.id.txtUserName);
            txtUserStatus = itemView.findViewById(R.id.txtUserStatus);
            imgUser = itemView.findViewById(R.id.imgUser);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnCancel = itemView.findViewById(R.id.btnCancel);
        }
    }
}