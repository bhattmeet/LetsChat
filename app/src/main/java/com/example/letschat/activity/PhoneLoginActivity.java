package com.example.letschat.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.letschat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity{

    private EditText edtPhoneNumber,edtCode;
    private Button btnLogin,btnVerify;
    private String phoneNumber,code,errorMessage;
    private ProgressDialog progressDialog;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBacks;
    private String mverificationId;
    private PhoneAuthProvider.ForceResendingToken mforceResendingToken;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        mAuth = FirebaseAuth.getInstance();

        setIds();

        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phoneNumber = edtPhoneNumber.getText().toString();

                if (TextUtils.isEmpty(phoneNumber)) {
                    Toast.makeText(PhoneLoginActivity.this, "Please enter phone number", Toast.LENGTH_SHORT).show();
                } else {
                    progressDialog.setTitle("Phone Verification");
                    progressDialog.setMessage("We authenticate your phone number");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();
                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phoneNumber,            //Phone Number to verify
                            60,                  //Timeout duration
                            TimeUnit.SECONDS,       //Unit of timeout
                            PhoneLoginActivity.this,           //Activity for callback binding
                            mCallBacks              //OnVerificationStateChangedCallBacks
                    );
                    Log.e("test", "IN:");
                }
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                code = edtCode.getText().toString();

                if (TextUtils.isEmpty(code)){
                    Toast.makeText(PhoneLoginActivity.this, "Please enter code", Toast.LENGTH_SHORT).show();
                }else {
                    progressDialog.setTitle("Code Verification");
                    progressDialog.setMessage("We check your verification code");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mverificationId,code);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });

        mCallBacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                Log.e("test","OnVrificationCompleted: "+phoneAuthCredential);
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Log.e("test","OnVrificationFailed: "+e);
                progressDialog.dismiss();
                Toast.makeText(PhoneLoginActivity.this, "Invalid phone number, Please enter phone number with country code", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
//                super.onCodeSent(verificationId, forceResendingToken);

                Log.e("test","VID: "+verificationId);
                Log.e("test","forceToken: "+forceResendingToken);
                mverificationId = verificationId;
                mforceResendingToken = forceResendingToken;

                progressDialog.dismiss();
                Toast.makeText(PhoneLoginActivity.this, "Code has sent", Toast.LENGTH_SHORT).show();

                edtCode.setVisibility(View.VISIBLE);
                btnLogin.setVisibility(View.VISIBLE);
            }
        };

    }

    private void setIds(){
        edtPhoneNumber = findViewById(R.id.edtPhoneNumber);
        edtCode = findViewById(R.id.edtCode);
        btnLogin = findViewById(R.id.btnLogin);
        btnVerify = findViewById(R.id.btnVerify);
        progressDialog = new ProgressDialog(this);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential){
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    progressDialog.dismiss();
                    Log.e("test","Done");
                    Toast.makeText(PhoneLoginActivity.this, "Login Successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(PhoneLoginActivity.this,HomeActivity.class);
                    startActivity(intent);
                    finish();
                }else {
                    progressDialog.dismiss();
                    errorMessage = task.getException().toString();
                    Log.e("test","error:"+errorMessage);
                    Toast.makeText(PhoneLoginActivity.this, "Error: "+errorMessage, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}