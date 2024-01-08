package com.android.safeband.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.EditText;

import com.android.safebandproject.R;

public class SignupIntent extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_app);
        // 회원가입 화면 이름
        EditText signupName = findViewById(R.id.username);
        // 회원가입 화면 아이디
        EditText signupId = findViewById(R.id.userid);
        // 회원가입 화면 비밀번호
        EditText signupPassword = findViewById(R.id.password);
    }
}