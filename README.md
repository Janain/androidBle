# androidBle

使用https://github.com/Alex-Jerry/Android-BLE框架。
此demo只实现了扫描，连接，设置通知，发送数据、读取、接收数据。亲测可用。
 
ble总结：

1、所用到的依赖

    `implementation 'cn.com.superLei:blelibrary:2.3.0'
     implementation 'com.orhanobut:logger:1.15'`
     
2、清单文件添加权限
 
    `<uses-feature
                android:name="android.hardware.bluetooth_le"
                android:required="true" />
    <!--使用蓝牙所需要的权限-->
    <uses-permission android:name="android.permission.BLUETOOTH" />
    <!--使用扫描和设置蓝牙的权限（申明这一个权限必须申明上面一个权限）-->
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <!--在 Android 6.0 及以上，还需要打开位置权限。如果应用没有位置权限，蓝牙扫描功能不能使用（其它蓝牙操作例如连接蓝牙设备和写入数据不受影响）-->
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />`
    
3、6.0以上添加动态权限
 
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
     
    @Override
     public void onResume() {
         super.onResume();
         checkGps();
     }`
     
4、初始化ble

5、扫描周边设备

6、连接

7、设置通知和回调

用到的是别人的，可参考

https://juejin.im/post/5ab8b02c6fb9a028b6178207