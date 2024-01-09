package com.android.safeband.activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.safeband.activity.Guardian;
import com.android.safeband.activity.GuardianAdapter;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.android.safebandproject.R;
import com.google.firebase.firestore.Query;

public class emergencyCallActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private GuardianAdapter adapter;
    private List<Guardian> guardians;

    private String addedGuardianName;
    private String addedPhoneNumber;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_call);

        ImageButton ImageButton = findViewById(R.id.backButton);

        recyclerView = findViewById(R.id.recycler_view);
        guardians = new ArrayList<>();

        adapter = new GuardianAdapter(guardians, new GuardianAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Guardian guardian) {
                // 클릭한 Guardian 객체에 대한 처리
                String name = guardian.getName();
                String phone = guardian.getPhoneNumber();
                // 여기에서 필요한 처리를 수행하면 됩니다.
                new AlertDialog.Builder(emergencyCallActivity.this)
                        .setTitle("Guardian 정보")
                        .setMessage("이름: " + name + "\n전화번호: " + phone)
                        .setPositiveButton("긴급연락처로 저장", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                savePhoneNumberToSharedPreferences(phone);

                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                // Handle cancel button click (if needed)
                            }
                        })
                        .show();
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        // 앱 시작 시 Firestore에서 데이터 불러오기
        loadDataFromFirestore("");

        ImageButton.setOnClickListener(v -> {
            // 뒤로 가기 버튼 클릭 시 앱 종료
            finish();
        });

        findViewById(R.id.btn_plus).setOnClickListener(v -> {
            showAddGuardianDialog();
        });

        EditText searchView = findViewById(R.id.search_view);
        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // No implementation needed
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Perform search when text changes
                String searchQuery = charSequence.toString().trim();
                loadDataFromFirestore(searchQuery);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // No implementation needed
            }
        });

    }

    private void savePhoneNumberToSharedPreferences(String phoneNumber) {
        SharedPreferences sharedPreferences = getSharedPreferences("Phone", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("phoneNumber", phoneNumber);
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

                        // Firestore에 데이터 추가
                        Map<String, Object> guardianData = new HashMap<>();
                        guardianData.put("name", addedGuardianName);
                        guardianData.put("phone", addedPhoneNumber);

                        db.collection("guardians")
                                .add(guardianData)
                                .addOnSuccessListener(documentReference -> {
                                    // Firestore에 추가 성공 시 동작
                                    // 예: 성공 메시지 출력 또는 다른 작업 수행
                                })
                                .addOnFailureListener(e -> {
                                    // Firestore에 추가 실패 시 동작
                                    // 예: 실패 메시지 출력 또는 다른 작업 수행
                                });

                        // RecyclerView에 추가
                        Guardian newGuardian = new Guardian(addedGuardianName, addedPhoneNumber);
                        guardians.add(newGuardian);
                        adapter.notifyDataSetChanged();
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

    private void loadDataFromFirestore(String searchQuery) {
        // Firestore에서 데이터 가져오기
        Query query;
        if (TextUtils.isEmpty(searchQuery)) {
            query = db.collection("guardians");
        } else {
            String searchQueryLowerCase = searchQuery.toLowerCase();

            query = db.collection("guardians")
                    .orderBy("name")
                    .startAt(searchQueryLowerCase)
                    .endAt(searchQueryLowerCase + "\uf8ff");
        }

        query.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // 데이터 가져오기 성공
                        guardians.clear(); // 기존 데이터 클리어
                        for (DocumentSnapshot document : task.getResult()) {
                            Map<String, Object> data = document.getData();
                            if (data != null) {
                                String name = (String) data.get("name");
                                String phone = (String) data.get("phone");
                                Guardian guardian = new Guardian(name, phone);
                                if (name.toLowerCase().contains(searchQuery.toLowerCase())) {
                                    // 이름이 검색어를 포함할 경우에만 추가
                                    guardians.add(guardian);
                                }
                            }
                        }
                        adapter.notifyDataSetChanged(); // 어댑터에 변경 사항 알리기
                    } else {
                        // 데이터 가져오기 실패
                        // 실패 시 처리를 여기에 추가할 수 있습니다.
                    }
                });
    }
}