package com.android.safeband.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.safebandproject.R;
import androidx.appcompat.app.AlertDialog;

public class emergencyCallActivity extends AppCompatActivity {
    TextView nameTextView, phoneNumberTextView;

    // 추가된 부분
    private String addedGuardianName;
    private String addedPhoneNumber;

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_call);
        ImageButton ImageButton = findViewById(R.id.backButton);

        preferences = getSharedPreferences("PARENT", Context.MODE_PRIVATE);
        // 텍스트뷰 초기화
        nameTextView = findViewById(R.id.u_name);
        phoneNumberTextView = findViewById(R.id.u_phone);

        loadSavedData();



        ImageButton.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.putExtra("name", "suzin");
            setResult(RESULT_OK, intent);
            finish();
        });

        findViewById(R.id.btn_plus).setOnClickListener(v -> {
            showAddGuardianDialog();
        });
    }

    private void loadSavedData() {
        // SharedPreferences에서 이름과 전화번호 불러오기
        String savedName = preferences.getString("name", "");
        String savedPhoneNumber = preferences.getString("phone", "");

        // 불러온 데이터를 텍스트뷰에 설정
        nameTextView.setText(savedName);
        phoneNumberTextView.setText(savedPhoneNumber);
    }

    private void saveData(String name, String phone) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("name", name);
        editor.putString("phone", phone);
        editor.apply();
    }

    private void showAddGuardianDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_add_guardian, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView)
                .setTitle("보호자 정보 추가")
                .setPositiveButton("추가", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText guardianNameEditText = dialogView.findViewById(R.id.name);
                        EditText phoneNumberEditText = dialogView.findViewById(R.id.phone);

                        // 값을 변수에 저장
                        addedGuardianName = guardianNameEditText.getText().toString();
                        addedPhoneNumber = phoneNumberEditText.getText().toString();

                        saveData(addedGuardianName, addedPhoneNumber);

                        // 이름과 전화번호를 텍스트뷰에 설정
                        nameTextView.setText(addedGuardianName);
                        phoneNumberTextView.setText(addedPhoneNumber);

                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // 추가된 메서드
    public String getAddedGuardianName() {
        return addedGuardianName;
    }

    public String getAddedPhoneNumber() {
        return addedPhoneNumber;
    }
}