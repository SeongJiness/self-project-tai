package com.android.safeband.activity;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.android.safebandproject.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;

    public class MainActivity extends AppCompatActivity implements MapView.CurrentLocationEventListener, MapView.MapViewEventListener {
        private DrawerLayout drawerLayout;
        private View drawerView;

        private MapView mapView;
        private ViewGroup mapViewContainer;

    private Button btn_call, btn_bluetooth, btn_logout;
    private ImageButton btn_profile_setting;
    private Intent data;

    private List<Guardian> guardians;
    private GuardianAdapter adapter;

    Dialog ReCheckDeleteAccount;

    public MainActivity() {
    }
    public static final int REQUEST_CODE = 101;

    TextView name;

    String userName , password, phone, email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 1000;

        // 전화걸기 권한 요청
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE);

        // 권한이 없을 때
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            // 사용자가 권한을 거부한 적이 있을 때
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CALL_PHONE)) {
                Toast.makeText(this,"전화 권한이 필요합니다.", Toast.LENGTH_LONG).show();
            } else {
                // 전화 걸기 권한을 요청한다. 뒤에 상수는 요청을 식별할 때 사용한다.
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, MY_PERMISSIONS_REQUEST_CALL_PHONE);
            }
        }

        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        drawerView = (View)findViewById(R.id.drawer);

        //메뉴를 눌렀을 때
        ImageButton menu_open = (ImageButton)findViewById(R.id.menu_button);
        menu_open.setOnClickListener(v -> drawerLayout.openDrawer(drawerView));
        drawerView.setOnTouchListener((view, motionEvent) -> true);

        name = findViewById(R.id.name);
        fetchUserDataFromFirestore();


        //메뉴 닫기를 눌렀을 때
        ImageButton btn_close = (ImageButton)findViewById(R.id.btn_close);
        btn_close.setOnClickListener(view -> drawerLayout.closeDrawers());

        ReCheckDeleteAccount = new Dialog(this);
        ReCheckDeleteAccount.requestWindowFeature(Window.FEATURE_NO_TITLE);
        ReCheckDeleteAccount.setContentView(R.layout.recheck_delete_account);

        // 회원 탈퇴를 눌렀을 때
        Button btn_out = findViewById(R.id.btn_out);
        btn_out.setOnClickListener(view -> {
            showDialog01();
        });

        // 로그 아웃 버튼을 눌렀을 때
        btn_logout = findViewById(R.id.btn_logout);
        btn_logout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut(); // Firebase 로그아웃
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivityForResult(intent, REQUEST_CODE);
        });
        DrawerLayout.DrawerListener listener = new DrawerLayout.DrawerListener() { ///drawer 오픈됐을 때 작동함
            @Override
            public void onDrawerSlide(@NonNull @org.jetbrains.annotations.NotNull View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull @org.jetbrains.annotations.NotNull View drawerView) {

            }

            @Override
            public void onDrawerClosed(@NonNull @org.jetbrains.annotations.NotNull View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        };

        //블루투스 버튼
        btn_bluetooth = (Button) findViewById(R.id.btn_bluetooth);
        btn_bluetooth.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(getApplicationContext(), bluetooth.class);
                startActivityForResult(intent,REQUEST_CODE);  //intent를 넣어 실행시키게 됩니다.
            }
        });

        // 비상연락망
        btn_call = (Button)findViewById(R.id.btn_call);
        btn_call.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), emergencyCallActivity.class);
            startActivityForResult(intent,REQUEST_CODE);  //intent를 넣어 실행시키게 됩니다.
        });


        //프로필 설정
        btn_profile_setting = (ImageButton)findViewById(R.id.btn_profile_setting);
        btn_profile_setting.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), profileSettingActivity.class);
            intent.putExtra("name", name.getText());
            intent.putExtra("phone", phone);
            intent.putExtra("email", email);
            startActivityForResult(intent,REQUEST_CODE);  //intent를 넣어 실행시키게 됩니다.
        });



        mapView = new MapView(this);
        mapViewContainer = (ViewGroup) findViewById(R.id.map_view);
        mapViewContainer.addView(mapView);
        mapView.setMapViewEventListener(this);
        mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);

        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("키해시는 :", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        // 권한ID를 가져옵니다
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.INTERNET);

        int permission2 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        int permission3 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        // 권한이 열려있는지 확인
        if (permission == PackageManager.PERMISSION_DENIED || permission2 == PackageManager.PERMISSION_DENIED || permission3 == PackageManager.PERMISSION_DENIED) {
            // 마쉬멜로우 이상버전부터 권한을 물어본다
            if (VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // 권한 체크(READ_PHONE_STATE의 requestCode를 1000으로 세팅
                requestPermissions(
                        new String[]{Manifest.permission.INTERNET, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        1000);
            }
            return;
        }

        //지도를 띄우자
        // java code

    }

    // 권한 체크 이후로직
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grandResults) {
        // READ_PHONE_STATE의 권한 체크 결과를 불러온다
        super.onRequestPermissionsResult(requestCode, permissions, grandResults);
        if (requestCode == 1000) {
            boolean check_result = true;

            // 모든 퍼미션을 허용했는지 체크
            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }

            // 권한 체크에 동의를 하지 않으면 안드로이드 종료
            if (check_result == false) {
                finish();
            }
        }
    }

    @Override
    public void onCurrentLocationUpdate(MapView mapView, MapPoint mapPoint, float v) {

    }

    @Override
    public void onCurrentLocationDeviceHeadingUpdate(MapView mapView, float v) {

    }

    @Override
    public void onCurrentLocationUpdateFailed(MapView mapView) {

    }

    @Override
    public void onCurrentLocationUpdateCancelled(MapView mapView) {

    }

    @Override
    public void onMapViewInitialized(MapView mapView) {

    }

    @Override
    public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewZoomLevelChanged(MapView mapView, int i) {

    }

    @Override
    public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDragStarted(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDragEnded(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewMoveFinished(MapView mapView, MapPoint mapPoint) {

    }


        //파이어스토어에서 데이터 가져오기
        private void fetchUserDataFromFirestore() {
            // 현재 사용자의 UID 가져오기
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            // Firestore에서 "users" 컬렉션에 접근하고 사용자 UID를 가진 문서를 가져오기
            FirebaseFirestore.getInstance().collection("users").document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        // 문서가 존재하는지 확인
                        if (documentSnapshot.exists()) {
                            // 문서에서 사용자 이름 가져오기
                            userName = documentSnapshot.getString("name");
                            phone = documentSnapshot.getString("phone");
                            email = documentSnapshot.getString("email");

                            // 가져온 이름을 TextView에 설정
                            name.setText(userName);
                        }
                    })
                    .addOnFailureListener(e -> {
                        // 데이터를 가져오는 데 실패하면 오류 처리
                        Toast.makeText(MainActivity.this, "사용자 데이터를 가져오지 못했습니다: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }


    public void showDialog01() {
        ReCheckDeleteAccount.show();
        Objects.requireNonNull(ReCheckDeleteAccount.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // 아니요 버튼
        Button noBtn = ReCheckDeleteAccount.findViewById(R.id.noButton);
        noBtn.setOnClickListener(view -> {
            ReCheckDeleteAccount.dismiss(); // 닫기
        });

        // 네 버튼
        Button yesBtn = ReCheckDeleteAccount.findViewById(R.id.yesButton);
        yesBtn.setOnClickListener(view -> {
            deleteUserAccount();
            // Firebase Authentication에서 사용자 삭제
            FirebaseAuth.getInstance().getCurrentUser().delete()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Firebase Authentication에서 사용자 삭제 성공
                            // Firestore에서 사용자 데이터 삭제
                            Toast.makeText(MainActivity.this, "회원탈퇴 성공", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getApplicationContext(), Login.class);
                            startActivity(intent);
                        } else {
                            // Firebase Authentication에서 사용자 삭제 실패
                            Toast.makeText(MainActivity.this, "회원탈퇴 실패", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

        private void deleteUserAccount() {
            // Delete user data from "users" collection
            deleteUserDataFromFirestore();

            // Delete guardian data from "guardians" collection
            deleteGuardianDataFromFirestore();

            // Other operations after deletion (if any)
        }

    private void deleteUserDataFromFirestore() {
        // Firestore에서 사용자 데이터 삭제
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance().collection("users").document(userId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Firestore에서 사용자 데이터 삭제 성공


                    // 문서와 해당 문서에 속한 모든 필드를 삭제하는 코드
                })
                .addOnFailureListener(e -> {
                    // Firestore에서 사용자 데이터 삭제 실패
                });
    }

        private void deleteGuardianDataFromFirestore() {
            String currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("guardians")
                    .whereEqualTo("uid", currentUserUid)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            Guardian guardian = documentSnapshot.toObject(Guardian.class);

                            // Firestore에서 가져온 guardian 정보를 사용하여 삭제 등의 작업 수행
                            deleteGuardianDocument(documentSnapshot.getId());

                            // RecyclerView에서 보호자를 제거합니다.
                            if (guardians != null && adapter != null) {
                                guardians.remove(guardian);
                                adapter.notifyDataSetChanged();
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Firestore에서 데이터 조회 실패 시 동작
                        // 예: 실패 메시지 출력 또는 다른 작업 수행
                    });
        }

        // Firestore에서 guardian 문서 삭제
        private void deleteGuardianDocument(String documentId) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("guardians")
                    .document(documentId)
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
    }

