package com.android.safeband.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.safebandproject.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class bluetooth extends AppCompatActivity {
    BluetoothAdapter btAdapter;


    TextView textStatus;
    Button btnSearch, btnBluetoothOn, btnBluetoothOff;
    ListView listView;

    ArrayAdapter<String> btArrayAdapter;
    ArrayList<String> deviceAddressArray;

    BluetoothSocket btSocket;
    ConnectedThread connectedThread;

    private final static int REQUEST_ENABLE_BT = 1;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // 블루투스 SPP 프로파일 UUID

    private static final String TAG = "ArduinoSensorData";

    private static final String PREFS_NAME = "BluetoothPrefs";
    private static final String PREF_BT_ADDRESS = "BluetoothAddress";
    private static final String PREF_BT_NAME = "BluetoothDeviceName";
    private boolean isReconnecting = false;



    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        BluetoothSocket socket = null;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                socket = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
            } else {
                socket = device.createRfcommSocketToServiceRecord(MY_UUID);
            }
        } catch (IOException e) {
            Log.e(TAG, "Error creating Bluetooth socket: " + e.getMessage());
            throw e;
        }
        return socket;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        ImageButton ImageButton = findViewById(R.id.backButton);
        ImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 // 현재 액티비티 없애기
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });



        // Get permission
        String[] permissionList = {
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.BLUETOOTH_ADMIN // 스캔 권한 요청을 위해 추가
        };

        ActivityCompat.requestPermissions(this, permissionList, 1);

        // Enable bluetooth
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) {
            // Device does not support Bluetooth
            Log.d("MainActivity", "Device does not support Bluetooth.");
            finish(); // Close the app
            return;
        }

        if (!btAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        // 뷰 ID값 찾아오기
        textStatus = findViewById(R.id.text_status);
        btnBluetoothOn = findViewById(R.id.btnBluetoothOn);
        btnBluetoothOff = findViewById(R.id.btnBluetoothOff);
        btnSearch = findViewById(R.id.btn_search);
        listView = findViewById(R.id.listview);

        //블루투스 ON 버튼 리스너
        btnBluetoothOn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                bluetoothOn();
            }
        });

        //블루투스 OFF 버튼 리스너
        btnBluetoothOff.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                bluetoothOff();
            }
        });

        // Initialize ListView
        btArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        deviceAddressArray = new ArrayList<>();
        listView.setAdapter(btArrayAdapter);

        // Register the ACTION_FOUND receiver.
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);

        listView.setOnItemClickListener(new myOnItemClickListener());

        String savedBluetoothAddress = getSavedBluetoothAddress();

        if (savedBluetoothAddress != null && !savedBluetoothAddress.isEmpty()) {
            autoConnectToBluetooth(savedBluetoothAddress);
        }


    }

    void bluetoothOn() {
        if (btAdapter == null) {
            Toast.makeText(getApplicationContext(), "블루투스를 지원하지 않는 기기입니다.", Toast.LENGTH_LONG).show();
        } else {
            if (btAdapter.isEnabled()) {
                Toast.makeText(getApplicationContext(), "블루투스가 이미 활성화 되어 있습니다.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "블루투스가 활성화 되어 있지 않습니다.", Toast.LENGTH_LONG).show();
                Intent intentBluetoothEnable = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intentBluetoothEnable, REQUEST_ENABLE_BT);
            }
        }
    }

    void bluetoothOff() {
        if (btAdapter != null) {
            if (btAdapter.isEnabled()) {
                // 사용자에게 Bluetooth를 비활성화할 것인지 묻는 대화상자를 띄우지 않고
                // 토스트 메시지를 표시한 후, 일정 시간 후에 시스템 설정으로 이동합니다.
                Toast.makeText(getApplicationContext(), "Bluetooth를 비활성화하려면 시스템 설정에서 꺼야합니다...", Toast.LENGTH_SHORT).show();

                // 일정 시간(예: 2초) 후에 시스템 설정으로 이동
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // 시스템 설정으로 이동
                        Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
                        startActivity(intent);
                    }
                }, 1000); // 2000 밀리초 = 2초
            } else {
                Log.d("Bluetooth", "Bluetooth 상태: 이미 비활성화됨");
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Unregister the ACTION_FOUND receiver.
        unregisterReceiver(receiver);

        if (connectedThread != null) {
            connectedThread.resetFallDetection();
        }

        saveBluetoothAddress();
    }

    // 디바이스 검색 버튼 클릭 핸들러
    public void onClickButtonSearch(View view){
        // Check if the device is already discovering
        if(btAdapter.isDiscovering()){
            btAdapter.cancelDiscovery();
        } else {
            if (btAdapter.isEnabled()) {
                btAdapter.startDiscovery();
                btArrayAdapter.clear();
                if (deviceAddressArray != null && !deviceAddressArray.isEmpty()) {
                    deviceAddressArray.clear();
                }
                IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                registerReceiver(receiver, filter);
            } else {
                Toast.makeText(getApplicationContext(), "블루투스가 꺼져있습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                String deviceAddress = device.getAddress();
                String deviceName = device.getName();

                if (deviceName != null && !deviceAddressArray.contains(deviceAddress)) {
                    btArrayAdapter.add(deviceName);
                    deviceAddressArray.add(deviceAddress);
                    btArrayAdapter.notifyDataSetChanged();
                }
            }
        }
    };

    public class myOnItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Toast.makeText(getApplicationContext(), btArrayAdapter.getItem(position), Toast.LENGTH_SHORT).show();

            textStatus.setText("try...");

            final String name = btArrayAdapter.getItem(position); // get name
            final String address = deviceAddressArray.get(position); // get address
            boolean flag = true;

            BluetoothDevice device = btAdapter.getRemoteDevice(address);

            // create & connect socket
            try {
                btSocket = createBluetoothSocket(device);
                btSocket.connect();
            } catch (IOException e) {
                flag = false;
                textStatus.setText("연결실패!");
                e.printStackTrace();
            }

            if (flag) {
                textStatus.setText(name +  "에 연결됨");
                saveBluetoothAddress();
                Handler handler = new Handler(Looper.getMainLooper());  // 이 부분에 원하는 Looper를 사용하세요.
                connectedThread = new ConnectedThread(btSocket, bluetooth.this, handler);
                receiveDataFromArduino(); // 연결 수립 전에 데이터 수신 시작
            }
        }
    }
    private void receiveDataFromArduino() {
        if (connectedThread != null) {
            connectedThread.startReceiving();
        } else {
            // If the connection is not established, create a new connection and start receiving data
            if (btAdapter.isDiscovering()) {
                btAdapter.cancelDiscovery();
            }

            if (deviceAddressArray.size() > 0) {
                textStatus.setText("시도중");

                final String name = btArrayAdapter.getItem(0);
                final String address = deviceAddressArray.get(0);
                boolean flag = true;

                BluetoothDevice device = btAdapter.getRemoteDevice(address);

                // create & connect socket
                try {
                    btSocket = createBluetoothSocket(device);
                    btSocket.connect();
                } catch (IOException e) {
                    flag = false;
                    textStatus.setText("연결실패!");
                    e.printStackTrace();
                }

                if (flag) {
                    textStatus.setText(name + "에 연결됨");
                    Handler handler = new Handler(Looper.getMainLooper());  // 이 부분에 원하는 Looper를 사용하세요.
                    connectedThread = new ConnectedThread(btSocket, bluetooth.this, handler);
                    connectedThread.startReceiving();
                }
            } else {
                // No devices in the list, handle this case accordingly (e.g., show a message or take any action)
            }
        }
    }

    private void autoConnectToBluetooth(String bluetoothAddress) {
        Log.d(TAG, "Bluetooth 자동 연결: " + bluetoothAddress);



        if (btAdapter != null && !btAdapter.isEnabled()) {
            // Bluetooth가 비활성화되어 있으면 활성화합니다.
            Log.d(TAG, "Bluetooth 비활성화, 활성화 중...");
            bluetoothOn(); // 블루투스를 활성화하는 코드를 주석에서 제거
        }

        // 이미 연결된 상태를 확인
        if (btSocket != null && btSocket.isConnected()) {
            textStatus.setText("이미 연결되어 있습니다.");
            Log.d(TAG, "이미 연결됨");
            return;
        }

        // 이전에 연결된 Bluetooth 주소 가져오기
        String savedBluetoothAddress = getSavedBluetoothAddress();

        // 기존에 연결된 장치가 있을 때만 자동 연결 시도
        if (savedBluetoothAddress != null) {
            // 새로운 Bluetooth 소켓 생성 및 연결 시도
            BluetoothDevice device = btAdapter.getRemoteDevice(savedBluetoothAddress);
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // SPP UUID
            try {
                btSocket = device.createRfcommSocketToServiceRecord(uuid);
                btSocket.connect(); // 연결 시도
                saveBluetoothAddress(); // 연결이 성공하면 주소를 저장
                SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                textStatus.setText(prefs.getString(PREF_BT_NAME,"") + "에 연결됨");
                Handler handler = new Handler(Looper.getMainLooper());
                connectedThread = new ConnectedThread(btSocket, bluetooth.this, handler);
                connectedThread.startReceiving();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "Bluetooth 연결 실패");
                textStatus.setText("연결 실패!");
            }
        } else {
            Log.d(TAG, "기존에 연결된 Bluetooth 장치가 없습니다.");
        }
    }

    private void saveBluetoothAddress() {
        if (btSocket != null && btSocket.isConnected()) {
            String connectedDeviceAddress = btSocket.getRemoteDevice().getAddress();
            String connectedDeviceName = btSocket.getRemoteDevice().getName(); // 디바이스 이름 가져오기
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(PREF_BT_ADDRESS, connectedDeviceAddress);
            editor.putString(PREF_BT_NAME, connectedDeviceName); // 디바이스 이름도 저장
            editor.apply();
        }
    }

    private String getSavedBluetoothAddress() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if(prefs.getString(PREF_BT_ADDRESS, "") != null) {
            return prefs.getString(PREF_BT_ADDRESS, "");
        } else {
            return null;
        }
    }
}