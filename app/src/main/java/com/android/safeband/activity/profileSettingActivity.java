package com.android.safeband.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.android.safebandproject.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class profileSettingActivity extends AppCompatActivity {

    private EditText name, phone, email, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setting);
        ImageButton ImageButton = findViewById(R.id.backButton);

        Intent receivedIntent = getIntent();

        String user_name = receivedIntent.getStringExtra("name");
        String user_phone = receivedIntent.getStringExtra("phone");
        String user_email = receivedIntent.getStringExtra("email");

        name = findViewById(R.id.name);
        phone = findViewById(R.id.phone);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);

        name.setText(user_name);
        phone.setText(user_phone);
        email.setText(user_email);

        email.setEnabled(false);

        findViewById(R.id.backButton).setOnClickListener(v -> {
            finish(); // 현재 액티비티 없애기
        });

        findViewById(R.id.btn_plus).setOnClickListener(v -> {
            // 비밀번호 업데이트 및 Firestore 정보 갱신
            updatePasswordAndUserInfo();
            finish();
        });
    }

    private void updatePasswordAndUserInfo() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String newPasswordStr = password.getText().toString().trim();

        if (!newPasswordStr.isEmpty()) {
            // Firebase Authentication에서 비밀번호 업데이트
            user.updatePassword(newPasswordStr)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // 비밀번호 업데이트 성공
                            // 여기에서 Firestore에서 사용자 정보를 업데이트할 수 있음
                            updateUserInfoInFirestore();
                        } else {
                            // 비밀번호 업데이트 실패
                            // 에러 처리
                        }
                    });
        } else {
            // 새 비밀번호를 입력하지 않은 경우
            // 에러 처리
        }
    }

    private void updateUserInfoInFirestore() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance().collection("users").document(userId)
                .update(
                        "name", name.getText().toString(),
                        "phone", phone.getText().toString()
                        // 여기에 필요한 다른 정보 업데이트
                )
                .addOnSuccessListener(aVoid -> {
                    // Firestore에서 사용자 정보 업데이트 성공
                    // 성공적으로 처리한 후의 동작 구현
                })
                .addOnFailureListener(e -> {
                    // Firestore에서 사용자 정보 업데이트 실패
                    // 에러 처리
                });
    }
}