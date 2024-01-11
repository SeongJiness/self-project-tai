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
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.android.safebandproject.R;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class emergencyCallActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private GuardianAdapter adapter;
    private List<Guardian> guardians;

    private String addedGuardianName;
    private String addedPhoneNumber;

    private FirebaseFirestore db;

    private int selectedGuardianPosition = RecyclerView.NO_POSITION;


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
                        .setNeutralButton("삭제", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 삭제 버튼 클릭 시 동작
                                deleteGuardian(guardian);
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

                        // 현재 로그인한 사용자의 UID 가져오기
                        String currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                        // Firestore에 데이터 추가
                        Map<String, Object> guardianData = new HashMap<>();
                        guardianData.put("name", addedGuardianName);
                        guardianData.put("phone", addedPhoneNumber);
                        guardianData.put("uid", currentUserUid); // 현재 사용자의 UID 저장

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

        // 이름에 대한 쿼리
        Query nameQuery = db.collection("guardians")
                .orderBy("name")
                .startAt(searchQuery.toLowerCase())
                .endAt(searchQuery.toLowerCase() + "\uf8ff");

        // 전화번호에 대한 쿼리
        Query phoneQuery = db.collection("guardians")
                .orderBy("phone")
                .startAt(searchQuery.toLowerCase())
                .endAt(searchQuery.toLowerCase() + "\uf8ff");

        // 이름과 전화번호의 결과를 합치기
        Tasks.whenAllSuccess(nameQuery.get(), phoneQuery.get())
                .addOnSuccessListener(querySnapshots -> {
                    // nameQuery 및 phoneQuery의 결과를 합쳐서 처리
                    // querySnapshots.get(0)은 nameQuery의 결과
                    // querySnapshots.get(1)은 phoneQuery의 결과
                    processQueryResults(querySnapshots);
                })
                .addOnFailureListener(e -> {
                    // 실패 시 처리
                });
    }

    private void processQueryResults(List<Object> querySnapshots) {
        guardians.clear(); // 기존 데이터 클리어

        // nameQuery의 결과 처리
        for (DocumentSnapshot document : ((QuerySnapshot) querySnapshots.get(0)).getDocuments()) {
            Map<String, Object> data = document.getData();
            if (data != null) {
                String name = (String) data.get("name");
                String phone = (String) data.get("phone");
                Guardian guardian = new Guardian(name, phone);
                guardians.add(guardian);
            }
        }

        // phoneQuery의 결과 처리
        for (DocumentSnapshot document : ((QuerySnapshot) querySnapshots.get(1)).getDocuments()) {
            Map<String, Object> data = document.getData();
            if (data != null) {
                String name = (String) data.get("name");
                String phone = (String) data.get("phone");
                Guardian guardian = new Guardian(name, phone);
                if (!containsGuardian(guardians, guardian)) {
                    // 중복을 방지하기 위해 추가
                    guardians.add(guardian);
                }
            }
        }

        adapter.notifyDataSetChanged(); // 어댑터에 변경 사항 알리기
    }

    private boolean containsGuardian(List<Guardian> guardians, Guardian guardian) {
        for (Guardian existingGuardian : guardians) {
            if (existingGuardian.getName().equals(guardian.getName()) &&
                    existingGuardian.getPhoneNumber().equals(guardian.getPhoneNumber())) {
                return true; // 이미 리스트에 존재하는 경우 true 반환
            }
        }
        return false; // 리스트에 존재하지 않는 경우 false 반환
    }

    private void deleteGuardian(Guardian guardian) {
        // 현재 로그인한 사용자의 UID 가져오기
        String currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Firestore에서도 삭제
        db.collection("guardians")
                .whereEqualTo("name", guardian.getName())
                .whereEqualTo("phone", guardian.getPhoneNumber())
                .whereEqualTo("uid", currentUserUid) // 현재 사용자의 UID와 일치하는 데이터만 삭제
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        db.collection("guardians")
                                .document(documentSnapshot.getId())
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    // Firestore에서 삭제 성공 시 동작
                                    // 예: 성공 메시지 출력 또는 다른 작업 수행
                                })
                                .addOnFailureListener(e -> {
                                    // Firestore에서 삭제 실패 시 동작
                                    // 예: 실패 메시지 출력 또는 다른 작업 수행
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    // Firestore에서 데이터 조회 실패 시 동작
                    // 예: 실패 메시지 출력 또는 다른 작업 수행
                });

        // RecyclerView에서 삭제
        guardians.remove(guardian);
        adapter.notifyDataSetChanged();
    }

}