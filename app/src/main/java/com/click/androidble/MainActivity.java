package com.click.androidble;

import android.Manifest;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.UUID;

import cn.com.heaton.blelibrary.ble.Ble;
import cn.com.heaton.blelibrary.ble.BleDevice;
import cn.com.heaton.blelibrary.ble.callback.BleConnCallback;
import cn.com.heaton.blelibrary.ble.callback.BleNotiftCallback;
import cn.com.heaton.blelibrary.ble.callback.BleScanCallback;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private String TAG = "sss";
    private static final int REQUEST_CODE_ACCESS_COARSE_LOCATION = 1;
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private Ble<BleDevice> mBle;
    ListView mListView;
    Button scanBt;
    TextView displayTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        initArgs();
        mListView = findViewById(R.id.listView);
        scanBt = findViewById(R.id.bt_scan);
        displayTv = findViewById(R.id.tv_ble);
        mLeDeviceListAdapter = new LeDeviceListAdapter(this);
        mListView.setAdapter(mLeDeviceListAdapter);
        scanBt.setOnClickListener(this);
        mListView.setOnItemClickListener(this);
        initBle();
    }

    private void initArgs() {

    }

    //初始化蓝牙
    private void initBle() {
        mBle = Ble.getInstance();
        Ble.Options options = new Ble.Options();
        options.logBleExceptions = true;//设置是否输出打印蓝牙日志
        options.throwBleException = true;//设置是否抛出蓝牙异常
        options.autoConnect = false;//设置是否自动连接
        options.scanPeriod = 12 * 1000;//设置扫描时长
        options.connectTimeout = 10 * 1000;//设置连接超时时长
        options.uuid_service = UUID.fromString("0000FFE0-0000-1000-8000-00805F9B34FB");//设置主服务的uuid
        options.uuid_write_cha = UUID.fromString("d44bc439-abfd-45a2-b575-925416129600");//设置可写特征的uuid
        mBle.init(getApplicationContext(), options);
    }

    @Override
    public void onClick(View v) {
        mBle.startScan(scanCallback);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(this, "行点击连接", Toast.LENGTH_SHORT).show();
        final BleDevice device = mLeDeviceListAdapter.getDevice(position);
        if (device == null) return;
        if (mBle.isScanning()) {
            mBle.stopScan();
        }
        if (device.isConnected()) {
            mBle.disconnect(device);
        } else if (!device.isConnectting()) {
//            mBle.connect(device, connectCallback); //也可以
            mBle.connect(device, connectCallback);//新添加通过mac地址进行连接
        }
    }

    private BleScanCallback<BleDevice> scanCallback = new BleScanCallback<BleDevice>() {
        @Override
        public void onLeScan(final BleDevice device, int rssi, byte[] scanRecord) {
            Toast.makeText(MainActivity.this, "ssss", Toast.LENGTH_SHORT).show();
            synchronized (mBle.getLocker()) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mLeDeviceListAdapter.addDevice(device);
                        mLeDeviceListAdapter.notifyDataSetChanged();
                    }
                });
            }
        }
    };

    //连接的回调
    private BleConnCallback<BleDevice> connectCallback = new BleConnCallback<BleDevice>() {

        @Override
        public void onConnectionChanged(BleDevice device) {
            if (device.isConnected()) {
                setNotify(device);
            }
            Log.e(TAG, "onConnectionChanged: " + device.isConnected());
            mLeDeviceListAdapter.notifyDataSetChanged();
        }

        @Override
        public void onConnectException(BleDevice device, int errorCode) {
            super.onConnectException(device, errorCode);
            Toast.makeText(MainActivity.this, "连接异常，异常状态码:" + errorCode, Toast.LENGTH_SHORT).show();
        }
    };
    private final byte[] hex = "0123456789ABCDEF".getBytes();

    // 从字节数组到十六进制字符串转换
    private String Bytes2HexString(byte[] b) {
        byte[] buff = new byte[2 * b.length];
        for (int i = 0; i < b.length; i++) {
            buff[2 * i] = hex[(b[i] >> 4) & 0x0f];
            buff[2 * i + 1] = hex[b[i] & 0x0f];
        }
        return new String(buff);
    }

    private void setNotify(BleDevice device) {
        /*连接成功后，设置通知*/
        mBle.startNotify(device, new BleNotiftCallback<BleDevice>() {
            @Override
            public void onChanged(BleDevice device, BluetoothGattCharacteristic characteristic) {
                Log.e(TAG, "onChanged: " + Arrays.toString(characteristic.getValue()));
                byte[] bb = characteristic.getValue();
                displayTv.append(Bytes2HexString(bb));
            }

            @Override
            public void onReady(BleDevice device) {
                Log.e(TAG, "onReady: ");
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt) {
                Log.e(TAG, "onServicesDiscovered is success ");
            }

            @Override
            public void onNotifySuccess(BluetoothGatt gatt) {
                Log.e(TAG, "onNotifySuccess is success ");
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        checkGps();
    }

    /**
     * 开启位置权限
     */
    private void checkGps() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_CODE_ACCESS_COARSE_LOCATION);
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_ACCESS_COARSE_LOCATION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mBle.startScan(scanCallback);
                Toast.makeText(getApplicationContext(), "位置权限已开启", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "未开启位置权限", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


}
