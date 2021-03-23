package com.example.letschat.adapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.letschat.activity.HomeActivity;
import com.example.letschat.activity.ImageViewActivity;
import com.example.letschat.R;
import com.example.letschat.model.Messages;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.messageViewHolder> {

    private List<Messages> messagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference userReference;

    public MessageAdapter(List<Messages> messagesList){
        this.messagesList = messagesList;

    }

    public class messageViewHolder extends RecyclerView.ViewHolder{

        private TextView txtReceiverMsg,txtSenderMsg,txtReceiverMsgTime,txtSenderMsgTime;
        private CircleImageView imgReceiverUser;
        private RelativeLayout rlReceiver,rlSender;
        public ImageView imgSenderMsg, imgReceiverMsg;

        public messageViewHolder(@NonNull View itemView) {
            super(itemView);

            txtReceiverMsg = itemView.findViewById(R.id.txtReceiverMsg);
            txtReceiverMsgTime = itemView.findViewById(R.id.txtReceiverMsgTime);
            txtSenderMsg = itemView.findViewById(R.id.txtSenderMsg);
            txtSenderMsgTime = itemView.findViewById(R.id.txtSenderMsgTime);
            rlReceiver = itemView.findViewById(R.id.rlReceiver);
            rlSender = itemView.findViewById(R.id.rlSender);
            imgSenderMsg = itemView.findViewById(R.id.imgSenderMsg);
            imgReceiverMsg = itemView.findViewById(R.id.imgReceiverMsg);
//            imgReceiverUser = itemView.findViewById(R.id.imgUser);
        }
    }

    @NonNull
    @Override
    public messageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_msg,parent,false);
        mAuth = FirebaseAuth.getInstance();
        return new messageViewHolder(view);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(@NonNull messageViewHolder holder, int position) {
        String msgSenderId = mAuth.getCurrentUser().getUid();
        Messages messages = messagesList.get(position);
        String fromUserId = messages.getFrom();
        String fromMsgType = messages.getType();

        userReference = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserId);

        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    if (snapshot.hasChild("image")){
                        String receiverImg = snapshot.child("image").getValue().toString();
//                        Picasso.get().load(receiverImg).placeholder(R.drawable.ic_baseline_person_24).into(holder.imgReceiverUser);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        holder.rlReceiver.setVisibility(View.GONE);
        holder.imgSenderMsg.setVisibility(View.GONE);
        holder.imgReceiverMsg.setVisibility(View.GONE);

        if (fromMsgType.equals("text")){
            if (fromUserId.equals(msgSenderId)){
                holder.txtSenderMsg.setText(messages.getMessage());
                holder.txtSenderMsgTime.setText(messages.getTime());
            }else {
                holder.rlSender.setVisibility(View.GONE);
//                holder.imgReceiverUser.setVisibility(View.GONE);
                holder.rlReceiver.setVisibility(View.VISIBLE);

                holder.txtReceiverMsg.setText(messages.getMessage());
                holder.txtReceiverMsgTime.setText(messages.getTime());
            }
        }else if (fromMsgType.equals("image")){
            if (fromUserId.equals(msgSenderId)){
                holder.imgSenderMsg.setVisibility(View.VISIBLE);
                holder.rlSender.setVisibility(View.GONE);
                Picasso.get().load(messages.getMessage()).placeholder(R.drawable.ic_launcher_foreground).into(holder.imgSenderMsg);
            }else{
                holder.imgReceiverMsg.setVisibility(View.VISIBLE);
                holder.rlReceiver.setVisibility(View.GONE);
                holder.rlSender.setVisibility(View.GONE);
                Picasso.get().load(messages.getMessage()).placeholder(R.drawable.ic_launcher_foreground).into(holder.imgReceiverMsg);
            }
        }else if (fromMsgType.equals("pdf") || fromMsgType.equals("docx")){
            if (fromUserId.equals(msgSenderId)){
                holder.imgSenderMsg.setVisibility(View.VISIBLE);
                holder.rlSender.setVisibility(View.GONE);
                holder.imgSenderMsg.setBackgroundResource(R.drawable.ic_pdf_file);

            }else{
                holder.imgReceiverMsg.setVisibility(View.VISIBLE);
                holder.rlReceiver.setVisibility(View.GONE);
                holder.rlSender.setVisibility(View.GONE);
                holder.imgReceiverMsg.setBackgroundResource(R.drawable.ic_pdf_file);

            }
        }

        if (fromUserId.equals(msgSenderId)){
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (messagesList.get(position).getType().equals("pdf") || messagesList.get(position).getType().equals("docx")){
                        CharSequence options[] = new CharSequence[]{
                                "Download and View",
                                "Delete for me",
                                "Delete for every  one",
                                "Cancel"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete message?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if (i == 0){
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(messagesList.get(position).getMessage()));
                                    holder.itemView.getContext().startActivity(intent);
                                }else if (i == 1){
                                    deleteSentMsg(position, holder);
                                    Intent intent = new Intent(holder.itemView.getContext(), HomeActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }else if (i == 2){
                                    deleteMsgForEveryone(position, holder);
                                    Intent intent = new Intent(holder.itemView.getContext(), HomeActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    }else if (messagesList.get(position).getType().equals("text")){
                        CharSequence options[] = new CharSequence[]{
                                "Delete for me",
                                "Delete for every  one",
                                "Cancel"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete message?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if (i == 0){
                                    deleteSentMsg(position, holder);
                                    Intent intent = new Intent(holder.itemView.getContext(), HomeActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }else if (i == 1){
                                    deleteMsgForEveryone(position, holder);
                                    Intent intent = new Intent(holder.itemView.getContext(), HomeActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    } else if (messagesList.get(position).getType().equals("image")){
                        CharSequence options[] = new CharSequence[]{
                                "View image",
                                "Delete for me",
                                "Delete for every  one",
                                "Cancel"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete message?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if (i == 0){
                                    Intent intent = new Intent(holder.itemView.getContext(), ImageViewActivity.class);
                                    intent.putExtra("url",messagesList.get(position).getMessage());
                                    holder.itemView.getContext().startActivity(intent);
                                }else if (i == 1){
                                    deleteSentMsg(position, holder);
                                    Intent intent = new Intent(holder.itemView.getContext(), HomeActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }else if (i == 2){
                                    deleteMsgForEveryone(position, holder);
                                    Intent intent = new Intent(holder.itemView.getContext(), HomeActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    }
                }
            });
        }
        else{
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (messagesList.get(position).getType().equals("pdf") || messagesList.get(position).getType().equals("docx")){
                        CharSequence options[] = new CharSequence[]{
                                "Download and View",
                                "Delete for me",
                                "Cancel"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete message?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if (i == 0){
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(messagesList.get(position).getMessage()));
                                    holder.itemView.getContext().startActivity(intent);
                                }else if (i == 1){
                                    deleteReceiveMsg(position, holder);
                                    Intent intent = new Intent(holder.itemView.getContext(), HomeActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    }else if (messagesList.get(position).getType().equals("text")){
                        CharSequence options[] = new CharSequence[]{
                                "Delete for me",
                                "Cancel"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete message?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if (i == 0){
                                    deleteReceiveMsg(position, holder);
                                    Intent intent = new Intent(holder.itemView.getContext(), HomeActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    } else if (messagesList.get(position).getType().equals("image")){
                        CharSequence options[] = new CharSequence[]{
                                "View image",
                                "Delete for me",
                                "Cancel"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete message?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if (i == 0){
                                    Intent intent = new Intent(holder.itemView.getContext(), ImageViewActivity.class);
                                    intent.putExtra("url",messagesList.get(position).getMessage());
                                    holder.itemView.getContext().startActivity(intent);
                                }else if (i == 1){
                                    deleteReceiveMsg(position, holder);
                                    Intent intent = new Intent(holder.itemView.getContext(), HomeActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }

    private void deleteSentMsg(final int position,final messageViewHolder holder){
        DatabaseReference rootReference = FirebaseDatabase.getInstance().getReference();
        rootReference.child("Messages")
                .child(messagesList.get(position).getFrom())
                .child(messagesList.get(position).getTo())
                .child(messagesList.get(position).getMessageId())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(holder.itemView.getContext(), "Message deleted successfully.", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(holder.itemView.getContext(), "Error Occurred.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void deleteReceiveMsg(final int position,final messageViewHolder holder){
        DatabaseReference rootReference = FirebaseDatabase.getInstance().getReference();
        rootReference.child("Messages")
                .child(messagesList.get(position).getTo())
                .child(messagesList.get(position).getFrom())
                .child(messagesList.get(position).getMessageId())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(holder.itemView.getContext(), "Message deleted successfully.", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(holder.itemView.getContext(), "Error Occurred.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void deleteMsgForEveryone(final int position,final messageViewHolder holder){
        DatabaseReference rootReference = FirebaseDatabase.getInstance().getReference();
        rootReference.child("Messages")
                .child(messagesList.get(position).getTo())
                .child(messagesList.get(position).getFrom())
                .child(messagesList.get(position).getMessageId())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    rootReference.child("Messages")
                            .child(messagesList.get(position).getFrom())
                            .child(messagesList.get(position).getTo())
                            .child(messagesList.get(position).getMessageId())
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(holder.itemView.getContext(), "Message deleted successfully.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }else{
                    Toast.makeText(holder.itemView.getContext(), "Error Occurred.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
