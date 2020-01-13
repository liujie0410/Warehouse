package com.bignerdranch.android.beaconlocation;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class Location extends AppCompatActivity {

    private final static int SEARCH_CODE = 0x123;
    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private String MAC1 = "68:6A:AC:2C:68:23";//LIUJIE
    private String MAC2 = "6D:97:AF:96:61:18";//WANGRUI
    private String MAC3 = "65:C3:EF:CC:CB:13";//YANGQI
    //private String MAC1 = "5C:03:39:BB:36:49";
    //private String MAC2 = "A0:86:C6:1E:52:2E";
    //private String MAC3 = "73:F1:94:03:22:B6";
    private int i = 0;

    private final double p0_x = 600,p0_y = 2340,p1_x = 720,p1_y = 1560,p2_x = 300,p2_y = 1980;
    private int loc_x = 0,loc_y = 0;

    private RelativeLayout.LayoutParams params;
    private RelativeLayout relativeLayout;
    private ArrayList<ImageView> imageView = new ArrayList<ImageView>(){};
    private TextView mTextView;

    private boolean getMac1 = false,getMac2 = false,getMac3 = false;
    private double disMac1 = 0,disMac2 = 0,disMac3 = 0;
    private final int A = 45;//1m时信号强度
    private final double n = 3.5;//环境衰减因子

    Handler handler=new Handler();
    Runnable runnable=new Runnable() {
        @Override
        public void run() {
            startScanBluth();
            refreshUI();
            handler.postDelayed(this, 3000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        init();
        relativeLayout = findViewById(R.id.relative);
        //获取全局变量
        mTextView = (TextView)findViewById(R.id.rotate);
        //定义相对布局，设置imageview控件
        handler.postDelayed(runnable, 3000);//每两秒执行一次runnable.

    }

    public void refreshUI(){
        imageView.add(i,new ImageView(Location.this));
        imageView.get(i).setImageResource(R.drawable.photo);//使用图片标记人所在位置
        //设置相对布局（控件）的大小
        params = new RelativeLayout.LayoutParams(100,100);
        //设置布局控件的位置
        params.setMargins(loc_x,1290-loc_y/2, 0, 0);
        //设置image的布局以及将imageview组件添加到relative布局中
        imageView.get(i).setLayoutParams(params);
        relativeLayout.addView(imageView.get(i));
        //改变文本的xy值
        Log.e("Locationxy",Integer.toString(loc_x)+"  "+Integer.toString(loc_y));
        i++;
    }

    //判断蓝牙是否开启
    private void init() {
        // 判断手机是否支持蓝牙
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "设备不支持蓝牙", Toast.LENGTH_SHORT).show();
            finish();
        }
        // 判断是否打开蓝牙
        if (!mBluetoothAdapter.isEnabled()) {
            //弹出对话框提示用户是后打开
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent,SEARCH_CODE);
        } else {
            // 不做提示，强行打开
            mBluetoothAdapter.enable();
        }
        startDiscovery();
        Log.e("Location", "startDiscovery: 开启蓝牙");
    }

    /**
     * 注册异步搜索蓝牙设备的广播
     */
    private void startDiscovery() {
        // 找到设备的广播
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        // 注册广播
        registerReceiver(locationReceiver, filter);
        // 搜索完成的广播
        IntentFilter filter1 = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        // 注册广播
        registerReceiver(locationReceiver, filter1);
        Log.e("Location", "startDiscovery: 注册广播");
        startScanBluth();
    }

    //利用RSSI计算距离：d = 10^((abs(RSSI) - A) / (10 * n))
    private double calDis(int rssi){
        int iRssi = Math.abs(rssi);
        double power = (iRssi-A)/(10*n);
        return Math.pow(10,power);
    }

    private void findLocation(){
        double a=p0_x-p2_x;
        double b=p0_y-p2_y;
        double c= Math.pow(p0_x, 2) - Math.pow(p2_x, 2) + Math.pow(p0_y, 2) - Math.pow(p2_y, 2) + Math.pow(disMac3, 2) - Math.pow(disMac1, 2);
        double d=p1_x-p2_x;
        double e=p1_y-p2_y;
        double f=Math.pow(p1_x, 2) - Math.pow(p2_x, 2) + Math.pow(p1_y, 2) - Math.pow(p2_y, 2) + Math.pow(disMac3, 2) - Math.pow(disMac2, 2);
        loc_x=(int)((b*f-e*c)/(2*b*d-2*a*e));
        loc_y=(int)((a*f-d*c)/(2*a*e-2*b*d));
    }
    /**
     * 广播接收器
     */
    private final BroadcastReceiver locationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 收到的广播类型
            String action = intent.getAction();
            // 发现设备的广播
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // 从intent中获取设备
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int RSSI = intent.getExtras().getShort( BluetoothDevice.EXTRA_RSSI);
                Log.e("Location",device.getAddress());
                if(device.getAddress().equals(MAC1)){
                    getMac1 = true;
                    disMac1 = calDis(RSSI);
                    Toast.makeText(context,"iBeacon1距您"+String.format("%.2f",disMac1)+"米",Toast.LENGTH_SHORT).show();
                    Log.d("Location","************111"+Double.toString(disMac1));
                }else if(device.getAddress().equals(MAC2)){
                    getMac2 = true;
                    disMac2 = calDis(RSSI);
                    Toast.makeText(context,"iBeacon2距您"+String.format("%.2f",disMac2)+"米",Toast.LENGTH_SHORT).show();
                    Log.d("Location","************222"+Double.toString(disMac1));
                }else if (device.getAddress().equals(MAC3)){
                    getMac3 = true;
                    disMac3 = calDis(RSSI);
                    Toast.makeText(context,"iBeacon3距您"+String.format("%.2f",disMac3)+"米",Toast.LENGTH_SHORT).show();
                    Log.d("Location","************333"+Double.toString(disMac1));
                }

                // 搜索完成
            } else if ((getMac1&&getMac2&&getMac3)||BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                // 关闭进度条
                getMac1 = false;
                getMac2 = false;
                getMac3 = false;

                //显示位置
                Log.d("Location","位置1 2 3 = "+Double.toString(disMac1)+" "+Double.toString(disMac2)+" "+Double.toString(disMac3));
                findLocation();
                progressDialog.dismiss();
                Log.e("Location", "onReceive: 搜索完成");
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
        handler.removeCallbacks(runnable);
        if (locationReceiver != null) {
            //取消注册,防止内存泄露（onDestroy被回调代不代表Activity被回收？：具体回收看系统，由GC回收，同时广播会注册到系统
            //管理的ams中，即使activity被回收，reciver也不会被回收，所以一定要取消注册），
            unregisterReceiver(locationReceiver);
        }
    }
}
