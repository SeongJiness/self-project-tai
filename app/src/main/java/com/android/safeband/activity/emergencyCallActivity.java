package com.android.safeband.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.android.safebandproject.R;

public class emergencyCallActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_call);
        ImageButton ImageButton = findViewById(R.id.backButton);

        ImageButton.setOnClickListener(v -> {
            Intent intent = new Intent(); // 인텐트 객체 생성하고
            intent.putExtra("name", "suzin"); // 인텐트 객체에 데이터 넣기

            setResult(RESULT_OK, intent); // 응답 보내기
            finish(); // 현재 액티비티 없애기
        });
    }
}