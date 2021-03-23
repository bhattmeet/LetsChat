package com.example.letschat.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.letschat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private Context mContext;
    private EditText edtEmail,edtPassword;
    private TextView txtForgotPassword,txtnewAccount,txtPhoneNumber;
    private Button btnLogin;
    private FirebaseAuth mAuth;
    private String email,password,errorMessage;
    private ProgressDialog progressDialog;
    private DatabaseReference userReference;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mContext = this;

        mAuth = FirebaseAuth.getInstance();
        userReference = FirebaseDatabase.getInstance().getReference().child("Users");
        setIds();

        txtnewAccount.setOnClickListener(this);
        txtForgotPassword.setOnClickListener(this);
        txtPhoneNumber.setOnClickListener(this);
        btnLogin.setOnClickListener(this);
    }

    private void setIds(){
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        txtForgotPassword = findViewById(R.id.txtForgotPassword);
        txtnewAccount = findViewById(R.id.txtnewAccount);
        txtPhoneNumber = findViewById(R.id.txtPhoneNumber);
        btnLogin = findViewById(R.id.btnLogin);
        progressDialog = new ProgressDialog(mContext);
    }

    @Override
    protected void onStart() {
        super.onStart();
//        if (currentUser != null){
//            Intent intent = new Intent(LoginActivity.this,HomeActivity.class);
//            startActivity(intent);
//        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.txtnewAccount:
                Intent intent = new Intent(LoginActivity.this,RegisterActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
            case R.id.txtForgotPassword:
//                Intent intentForgotPassword = new Intent(LoginActivity.this,RegisterActivity.class);
//                startActivity(intentForgotPassword);
                break;
            case R.id.txtPhoneNumber:
                Intent intentPhoneNumber = new Intent(LoginActivity.this,PhoneLoginActivity.class);
                startActivity(intentPhoneNumber);
                break;
            case R.id.btnLogin:
                login();
                break;
        }
    }

    private void login(){
        email = edtEmail.getText().toString().trim();
        password = edtPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)){
            Toast.makeText(mContext, "Please enter email-id", Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(password)){
            Toast.makeText(mContext, "Please enter password", Toast.LENGTH_SHORT).show();
        }else {
            progressDialog.setTitle("Sign In");
            progressDialog.setMessage("Wait, while we login you...");
            progressDialog.setCanceledOnTouchOutside(true);
            progressDialog.show();

            mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){
                        String currentUserId = mAuth.getCurrentUser().getUid();
                        String deviceToken = FirebaseInstanceId.getInstance().getToken();

                        userReference.child(currentUserId).child("device_token").setValue(deviceToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    progressDialog.dismiss();
                                    Intent intent = new Intent(LoginActivity.this,HomeActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                    finish();
                                }
                            }
                        });
                    }else {
                        progressDialog.dismiss();
                        errorMessage = task.getException().toString();
                        Toast.makeText(mContext, "Error:"+errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
