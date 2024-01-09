package com.android.safeband.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.safebandproject.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {
    private FirebaseAuth mAuth;

    Button signupButton;
    private static final String TAG = "Login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // 사용자가 이미 로그인되어 있으면 MainActivity로 이동
            startActivity(new Intent(Login.this, MainActivity.class));
            finish(); // 로그인 액티비티 종료
        }

        // Find your login input fields and login button by their IDs
        EditText loginEmail = findViewById(R.id.userEmail);
        EditText loginPass = findViewById(R.id.password);
        Button loginBtn = findViewById(R.id.loginButton);

        loginBtn.setOnClickListener(v -> {
            String email = loginEmail.getText().toString().trim();
            String password = loginPass.getText().toString().trim();

            if (!email.isEmpty() && !password.isEmpty()) {
                signIn(email, password);
            } else {
                Toast.makeText(Login.this, "아이디와 비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show();
            }
        });

        // 회원가입 버튼
        signupButton = findViewById(R.id.signup_button);
        // 회원가입 버튼 이벤트 (회원가입 창으로 이동)
        signupButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), SignupIntent.class);
            startActivity(intent);
        });
    }

    private void signIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "로그인성공");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "로그인실패", task.getException());
                            Toast.makeText(Login.this, "로그인실패",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            // User is signed in, navigate to the main activity or perform other actions
            Toast.makeText(Login.this, "인증성공", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish(); // Optional, depends on your app's flow
        } else {
            // User is signed out or authentication failed
            Toast.makeText(Login.this, "인증실패", Toast.LENGTH_SHORT).show();
        }
    }
}