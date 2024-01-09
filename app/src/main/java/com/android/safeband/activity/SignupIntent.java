package com.android.safeband.activity;

import android.content.Intent;
import android.os.Bundle;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;

import java.util.HashMap;
import java.util.Map;

public class SignupIntent extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_app);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        // Find your registration input fields and registration button by their IDs
        EditText signupName = findViewById(R.id.username);
        EditText signupPhone = findViewById(R.id.phone);
        EditText signupEmail = findViewById(R.id.userEmail);
        EditText signupPassword = findViewById(R.id.password);
        EditText confirmPassword = findViewById(R.id.confirm_password);

        // Button for registration
        Button signupButton = findViewById(R.id.signupButton);


        // Handle the registration button click event
        signupButton.setOnClickListener(v -> {
            String name = signupName.getText().toString().trim();
            String phone = signupPhone.getText().toString().trim();
            String email = signupEmail.getText().toString().trim();
            String password = signupPassword.getText().toString().trim();
            String confirmPwd = confirmPassword.getText().toString().trim();

            if (!name.isEmpty() && !phone.isEmpty() && !email.isEmpty() && !password.isEmpty() && !confirmPwd.isEmpty()) {
                if (password.equals(confirmPwd)) {
                    // Passwords match, call the method to create a new user
                    registerUser(email, password, name, phone);
                } else {
                    Toast.makeText(SignupIntent.this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(SignupIntent.this, "모든 필드를 채워주세요.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void registerUser(String email, String password, String name, String phone) {
        // Check if the email is already in use
        mAuth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean isNewUser = task.getResult().getSignInMethods().isEmpty();

                        if (isNewUser) {
                            // Email is not in use, proceed with user registration
                            mAuth.createUserWithEmailAndPassword(email, password)
                                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (task.isSuccessful()) {
                                                // Registration success, update UI with the signed-in user's information
                                                FirebaseUser user = mAuth.getCurrentUser();
                                                String uid = user.getUid();
                                                Map<String, Object> users = new HashMap<>();
                                                users.put("name", name);
                                                users.put("phone", phone);
                                                users.put("password", password);
                                                users.put("email", email);

                                                db.collection("users")
                                                        .document(uid)
                                                        .set(users)
                                                        .addOnSuccessListener(aVoid -> {
                                                            Toast.makeText(SignupIntent.this, "회원가입 성공", Toast.LENGTH_SHORT).show();
                                                            Intent intent = new Intent(getApplicationContext(), Login.class);
                                                            startActivity(intent);
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            Toast.makeText(SignupIntent.this, "회원가입 실패", Toast.LENGTH_LONG).show();
                                                        });

                                                // Check if the user is already logged in
                                                if (mAuth.getCurrentUser() != null) {
                                                    // User is already logged in, go to MainActivity
                                                   // startActivity(new Intent(SignupIntent.this, MainActivity.class));
                                                    //finish(); // Close the SignupIntent activity
                                                }
                                            } else {
                                                // If registration fails, display a message to the user.
                                                Toast.makeText(SignupIntent.this, "회원가입 실패", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                        } else {
                            // Email is already in use, show an error message
                            Toast.makeText(SignupIntent.this, "이미 사용 중인 이메일입니다. 다른 이메일을 선택해주세요.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Handle the exception if the email check fails
                        Toast.makeText(SignupIntent.this, "사용불가능한 이메일입니다.", Toast.LENGTH_SHORT).show();
                    }
                });
    }


}




