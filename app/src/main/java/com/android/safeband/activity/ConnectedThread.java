package com.android.safeband.activity;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class ConnectedThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;

    private static final String TAG = "ArduinoSensorData";
    private volatile boolean receivingData = false;
    private volatile boolean reconnecting = false;

    // External components to be set from outside
    private Handler handler;
    private StringBuilder receivedDataBuilder;
    private Runnable onDataReceivedCallback;

    private Context context;

    private static boolean isFallDetected = false;

    private float previousXAngle = 0.0f;
    private float previousYAngle = 0.0f;


    public ConnectedThread(BluetoothSocket socket, Context context, Handler handler) {
        mmSocket = socket;
        this.context = context;
        this.handler = handler;

        try {
            mmInStream = socket.getInputStream();
            mmOutStream = socket.getOutputStream();
        } catch (IOException e) {
            throw new RuntimeException("Error getting input/output stream", e);
        }
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public void setOnDataReceivedCallback(Runnable onDataReceivedCallback) {
        this.onDataReceivedCallback = onDataReceivedCallback;
    }

    public void startReceiving() {
        receivingData = true;
        super.start();
    }

    public static void resetFallDetection() {
        isFallDetected = false;
    }

    @Override
    public void run() {
        int readBufferPosition = 0;
        byte[] readBuffer = new byte[1024];
        receivedDataBuilder = new StringBuilder();

        while (receivingData && !Thread.currentThread().isInterrupted()) {
            try {
                int byteAvailable = mmInStream.available();
                if (byteAvailable > 0) {
                    byte[] bytes = new byte[byteAvailable];
                    mmInStream.read(bytes);

                    for (int i = 0; i < byteAvailable; i++) {
                        byte tempByte = bytes[i];
                        if (tempByte == '\n') {
                            byte[] encodedBytes = new byte[readBufferPosition];
                            System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                            final String text = new String(encodedBytes, StandardCharsets.US_ASCII);

                            readBufferPosition = 0;

                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    processReceivedData(text);
                                }
                            });
                        } else {
                            readBuffer[readBufferPosition++] = tempByte;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();

                if (reconnecting) {
                    SystemClock.sleep(2000);
                } else {
                    reconnecting = true;
                    reconnecting = false;
                }
            }

            try {
                SystemClock.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private void processReceivedData(String jsonData) {
        try {
            JSONObject jsonObject = new JSONObject(jsonData);

            // 각도 데이터 추출
            float angleX = (float) jsonObject.getDouble("an_x");
            float angleY = (float) jsonObject.getDouble("an_y");
            float angleZ = (float) jsonObject.getDouble("an_z");

            // 가속도 크기 계산
            float accX = (float) jsonObject.getDouble("acc_x");
            float accY = (float) jsonObject.getDouble("acc_y");
            float accZ = (float) jsonObject.getDouble("acc_z");
            double accelerationMagnitude = Math.sqrt(accX * accX + accY * accY + accZ * accZ);

            // 임계값 설정
            double fallThresholdAcceleration = 2.3;
            double angleMaintainThreshold = 75.0;

            Log.d("가속도", String.valueOf(accelerationMagnitude));
            Log.d("각도 데이터", "angleX=" + angleX + ", angleY=" + angleY + ", angleZ=" + angleZ);

            // 낙상 감지 알고리즘
            if (!isFallDetected &&
                    (accelerationMagnitude > fallThresholdAcceleration &&
                            (Math.abs(angleX) > angleMaintainThreshold || Math.abs(angleY) > angleMaintainThreshold))) {
                // 낙상 감지됨
                Log.d(TAG, "Fall Detected!");
                isFallDetected = true;
                Intent intent = new Intent(context, CountdownTimer.class);
                context.startActivity(intent);
            }

            // 센서 값을 사용하여 필요한 작업 수행

        } catch (JSONException e) {
            Log.e(TAG, "Error parsing JSON data: " + e.getMessage());
        }
    }
    public void write(String input) {
        byte[] bytes = input.getBytes();
        try {
            mmOutStream.write(bytes);
        } catch (IOException e) {
            Log.e(TAG, "Error writing data to OutputStream", e);
        }
    }

    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Error closing BluetoothSocket", e);
        }
    }

}