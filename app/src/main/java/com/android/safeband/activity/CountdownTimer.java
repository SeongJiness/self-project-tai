package com.android.safeband.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.safebandproject.R;

public class CountdownTimer extends AppCompatActivity {
    static final int MAXIMUM_SECONDS = 30000;
    private TextView timerTextView;
    private Button endButton;
    private MediaPlayer mediaPlayer;

    private CountDownTimer countDownTimer;
    long secondsRemaining;
    // 권한 요청을 식별 하기 위해 쓰이는 변수
    final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_countdown_timer);

        timerTextView = findViewById(R.id.timerTextView);
        endButton = findViewById(R.id.endButton);
        mediaPlayer = MediaPlayer.create(this, R.raw.sound); // 사운드 변수


        // 사운드 시작
        startSound();

        endButton.setOnClickListener(v -> {
            if (countDownTimer != null) {
                countDownTimer.cancel();
            }

            // 남은 시간 변수 초기화
            secondsRemaining = MAXIMUM_SECONDS;

            // 타이머 텍스트 초기화 (예: 다시 30초로 설정)
            timerTextView.setText(String.valueOf(MAXIMUM_SECONDS / 1000));

            mediaPlayer.setLooping(false);
            mediaPlayer.pause();

            // 현재 액티비티를 종료 후 이전 화면으로 돌아간다.
            finish();
        });
    }

    private void startSound() {
        // AudioManager 인스턴스를 얻습니다.
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        // 볼륨을 최대로 설정합니다.
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0);

        //  사운드 무한 반복 설정
        mediaPlayer.setLooping(true);
        mediaPlayer.start();    // 사운드 플레이 시작

        // 30초 후 무한 재생 취소
        timerTextView.postDelayed(() -> {
            mediaPlayer.setLooping(false); // 무한 반복 해제
            mediaPlayer.pause(); // 사운드 재생 중지
        }, MAXIMUM_SECONDS);

        // 타이머 시작
        startTimer();
    }

    private void startTimer() {
        // new CountDownTimer(타이머 총 시간, 간격)
        countDownTimer = new CountDownTimer(MAXIMUM_SECONDS, 1000) {
            // onTick() :  간격마다 호출됨
            @Override
            public void onTick(long millisUntilFinished) {
                // 남은 초 구하기
                secondsRemaining = millisUntilFinished / 1000;
                timerTextView.setText(String.valueOf(secondsRemaining));
            }

            // 타이머 종료 이벤트
            @Override
            public void onFinish() {
                timerTextView.setText("0");

                // 타이머 0이 되면 전화를 건다
                makeCall();
            }
        }.start();
    }

    // Activity가 호출하는 마지막 메소드(마지막에 실행하는 함수)
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.release();
    }

    private void makeCall() {
        String phoneNumber = "01099348459"; // 여기에 전화번호를 입력하세요

        // 현재 전화 걸기 권한의 상태
        int permissionCheck = ContextCompat.checkSelfPermission(this,android.Manifest.permission.CALL_PHONE);

        // 권한을 허용 받았을 때
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            // 전화를 건다.
            startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber)));
        }
        // 권한을 허용 받지 못했을 때
        else {
            Toast.makeText(this,"권한 승인이 필요합니다",Toast.LENGTH_LONG).show();

            // 사용자가 명시적으로 권한을 거부했을 때
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.CALL_PHONE)) {
                Toast.makeText(this,"전화 권한이 필요합니다.",Toast.LENGTH_LONG).show();
            } else {
                // 전화 걸기 권한을 다시 요청한다. 뒤에 상수는 권한 요청 식별할 때 쓰입니다.
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CALL_PHONE}, MY_PERMISSIONS_REQUEST_CALL_PHONE);
            }
        }

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CALL_PHONE: {
                // 사용자가 권한을 허용했을 경우
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    makeCall(); // 권한을 얻었으니 전화를 건다.
                } else {
                    Toast.makeText(this, "전화 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }
}