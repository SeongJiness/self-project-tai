package com.android.safeband.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.android.safebandproject.R;

public class inquiryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inquiry);
        ImageButton ImageButton = findViewById(R.id.backButton);

        ImageButton.setOnClickListener(v -> {
            Intent intent = new Intent(); // 인텐트 객체 생성하고
            intent.putExtra("name", "suzin"); // 인텐트 객체에 데이터 넣기

            setResult(RESULT_OK, intent); // 응답 보내기
            finish(); // 현재 액티비티 없애기
        });

        Button email = (Button) findViewById(R.id.btn_email);

        email.setOnClickListener(view -> {
            Intent email1 = new Intent(Intent.ACTION_SEND);
            email1.setType("plain/text");
            String[] address = {"jmh67686@gmail.com"};
            email1.putExtra(Intent.EXTRA_EMAIL, address);
            email1.putExtra(Intent.EXTRA_SUBJECT, "");
            email1.putExtra(Intent.EXTRA_TEXT, "문의하실 내용을 적어주세요.");
            startActivity(email1);
        });

        //id로 버튼 찾아서 정의
        Button button = (Button)findViewById(R.id.btn_call);

        //버튼 클릭 했을 때 이벤트
        button.setOnClickListener(view -> {
            //전화 어플에 있는 Activity 정보를 넣어 Intent 정의
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:0536509250"));
            startActivity(intent);
        });
    }
}