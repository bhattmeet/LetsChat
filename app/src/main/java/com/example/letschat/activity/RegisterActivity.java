package com.example.letschat.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.letschat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText edtEmail,edtPassword;
    private Button btnRegister;
    private TextView txtNewAccount,txtPhoneNumber;
    private Context context;
    private String email,password,errorMessage,currentUser;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        context = this;

        mAuth = FirebaseAuth.getInstance();
        RootRef = FirebaseDatabase.getInstance().getReference();
        setIds();

        btnRegister.setOnClickListener(this);
        txtNewAccount.setOnClickListener(this);
        txtPhoneNumber.setOnClickListener(this);
    }

    private void setIds(){
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnRegister = findViewById(R.id.btnRegister);
        txtNewAccount = findViewById(R.id.txtnewAccount);
        txtPhoneNumber = findViewById(R.id.txtPhoneNumber);
        progressDialog = new ProgressDialog(context);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.txtnewAccount:
                Intent intent = new Intent(RegisterActivity.this,LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
            case R.id.btnRegister:
                CreateNewAccount();
                break;
        }
    }

    private void CreateNewAccount(){
        email = edtEmail.getText().toString().trim();
        password = edtPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)){
            Toast.makeText(context, "Please enter email-id", Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(password)){
            Toast.makeText(context, "Please enter password", Toast.LENGTH_SHORT).show();
        }else {
            progressDialog.setTitle("Creating New Account");
            progressDialog.setMessage("Please wait, while we are creating new account...");
            progressDialog.setCanceledOnTouchOutside(true);
            progressDialog.show();

            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){
                        String deviceToken = FirebaseInstanceId.getInstance().getToken();
                        currentUser = mAuth.getCurrentUser().getUid();
                        RootRef.child("Users").child(currentUser).setValue("");

                        RootRef.child("Users").child(currentUser).child("device_token").setValue(deviceToken);

                        progressDialog.dismiss();
                        Intent intent = new Intent(RegisterActivity.this,HomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                        Toast.makeText(context, "Created Successfully", Toast.LENGTH_SHORT).show();
                    }else {
                        progressDialog.dismiss();
                        errorMessage = task.getException().toString();
                        Toast.makeText(context, "Error:"+errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}