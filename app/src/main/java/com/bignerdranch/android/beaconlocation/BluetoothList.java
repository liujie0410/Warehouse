package com.bignerdranch.android.beaconlocation;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class BluetoothList extends AppCompatActivity  implements AdapterView.OnItemClickListener{

    private final static int SEARCH_CODE = 0x123;
    //BluetoothAdapter对象，判断蓝牙功能是否开启
    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();//获取蓝牙适配器
    private static final String TAG = "MainActivity";
    public static String MAC;
    //蓝牙搜索列表
    private List<BluetoothDevice> mBlueList = new ArrayList<>();
    private ListView lisetView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //回调函数
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetoothlist);
        //搜索蓝牙的列表
        lisetView = (ListView) findViewById(R.id.list_view);
        lisetView.setOnItemClickListener(this);
        init();
    }

    /**
     * 开启手机蓝牙
     */
    private void init() {
        // 判断手机是否支持蓝牙
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "设备不支持蓝牙", Toast.LENGTH_SHORT).show();
            finish();
        }
        // 判断是否打开蓝牙
        if (!mBluetoothAdapter.isEnabled()) {
            //弹出对话框提示用户，点击是后打开
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent,SEARCH_CODE);
        } else {
            // 不做提示，强行打开
            mBluetoothAdapter.enable();
        }
        startDiscovery();//注册异步搜索蓝牙设备的广播
        Log.e(TAG, "startDiscovery: 开启蓝牙");
    }

    /**
     * 注册异步搜索蓝牙设备的广播
     */
    private void startDiscovery() {
        // 找到设备的广播
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        // 注册广播
        registerReceiver(receiver, filter);
        // 搜索完成的广播
        IntentFilter filter1 = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        // 注册广播
        registerReceiver(receiver, filter1);
        Log.e(TAG, "startDiscovery: 注册广播");
        startScanBluth();
    }


    //搜索所有蓝牙的广播接收器
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 收到的广播类型
            String action = intent.getAction();
            // 发现设备的广播
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // 从intent中获取设备
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);//定义蓝牙设备列表
                int RSSI = intent.getExtras().getShort( BluetoothDevice.EXTRA_RSSI);//
                // 是否配对
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    if (!mBlueList.contains(device)) {
                        mBlueList.add(device);//设备列表中没有则添加
                    }
                    //记录蓝牙信息
                    MyAdapter adapter = new MyAdapter(BluetoothList.this, mBlueList);
                    lisetView.setAdapter(adapter);
                }
                // 搜索完成
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                // 关闭进度条
                progressDialog.dismiss();//结束进度对话框
            }
        }
    };

    private ProgressDialog progressDialog;

    /**
     * 搜索蓝牙的方法
     */
    private void startScanBluth() {
        // 判断是否在搜索,如果在搜索，就取消搜索
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        // 开始搜索
        mBluetoothAdapter.startDiscovery();
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
        }
        progressDialog.setMessage("正在搜索，请稍后！");
        progressDialog.show();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (receiver != null) {
            //取消注册,防止内存泄露（onDestroy被回调是否代表Activity被回收，具体看系统，由GC回收，同时广播会注册到系统
            //管理的ams中，即使activity被回收，reciver也不会被回收，所以一定要取消注册），
            unregisterReceiver(receiver);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==SEARCH_CODE){
            startDiscovery();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        BluetoothDevice blueDevice = mBlueList.get(position);
        MAC = blueDevice.getAddress();
        Intent intent = new Intent(this,SearchBlueTooth.class);
        startActivity(intent);
    }
}
