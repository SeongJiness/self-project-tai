package com.android.safeband.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.safebandproject.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class profileSettingActivity extends AppCompatActivity {

    private EditText name, phone, email, password, confirm_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setting);
        ImageButton ImageButton = findViewById(R.id.backButton);

        Intent receivedIntent = getIntent();

        String user_name = receivedIntent.getStringExtra("name");
        String user_phone = receivedIntent.getStringExtra("phone");
        String user_email = receivedIntent.getStringExtra("email");
        String user_password = receivedIntent.getStringExtra("password");

        name = findViewById(R.id.name);
        phone = findViewById(R.id.phone);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        confirm_password = findViewById(R.id.confirm_password);

        name.setText(user_name);
        phone.setText(user_phone);
        email.setText(user_email);

        email.setEnabled(false);

        password.setText(user_password);
        confirm_password.setText(user_password);

        findViewById(R.id.backButton).setOnClickListener(v -> {
            finish(); // 현재 액티비티 없애기
        });

        findViewById(R.id.btn_plus).setOnClickListener(v -> {
            String newPassword = password.getText().toString().trim();
            String confirmPassword = confirm_password.getText().toString().trim();

            if (newPassword.length() < 6) {
                // 새 비밀번호가 6자리 미만인 경우
                Toast.makeText(profileSettingActivity.this, "비밀번호는 최소 6자 이상이어야 합니다.", Toast.LENGTH_SHORT).show();
            } else if (!newPassword.equals(confirmPassword)) {
                // 비밀번호가 일치하지 않는 경우
                Toast.makeText(profileSettingActivity.this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
            } else {
                // 비밀번호가 일치하면 비밀번호 및 사용자 정보 업데이트를 진행
                updatePasswordAndUserInfo();
                finish();
            }
        });


    }

    private void updatePasswordAndUserInfo () {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String newPasswordStr = password.getText().toString().trim();

        if (newPasswordStr.length() < 6) {
            // 새 비밀번호가 6자리 미만인 경우
            Toast.makeText(profileSettingActivity.this, "비밀번호는 최소 6자 이상이어야 합니다.", Toast.LENGTH_SHORT).show();
            return; // 비밀번호가 6자리 미만이면 업데이트를 진행하지 않고 종료
        }

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