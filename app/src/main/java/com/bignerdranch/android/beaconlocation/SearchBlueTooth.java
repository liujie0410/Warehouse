package com.bignerdranch.android.beaconlocation;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class SearchBlueTooth extends AppCompatActivity implements View.OnTouchListener{

    //保存屏幕点击点的位置
    private static int xSearch;
    private static int ySearch;
    private int j = 1;

    private final int A = 50;//1m时信号强度
    private final double n = 2.0;//环境衰减因子
    private int loc_x = 0,loc_y = 0;

    //设置为屏幕上方的提示
    private int x;
    private int y;

    //点击定位的位置
    private static int x_end = 100;
    private static int y_end = 100;

    private String fileName=null;
    private boolean isNewFile = true;

    private double disMac1 = 0,disMac2 = 0,disMac3 = 0;
    private double p0_x=0,p0_y=0 ,p1_x=0 ,p1_y=0,p2_x = 0,p2_y = 0;
    private TextView endRotate;
    private LinearLayout ll;
    private RelativeLayout.LayoutParams mParams;
    private RelativeLayout relativeLayout;
    private RelativeLayout loc_relativeLayout;
    private RelativeLayout.LayoutParams loc_params;
    private ArrayList<ImageView>mImageView = new ArrayList<ImageView>(){};

    private TreeSet<Point> mSet = new TreeSet<Point>(new Comparator<Point>() {
        @Override
        public int compare(Point o1, Point o2) {
            return Integer.valueOf(o1.getRssi()).compareTo(Integer.valueOf(o2.getRssi()));
        }
    });

    private TreeSet<Point> mPointArrayList = new TreeSet<Point>(new Comparator<Point>() {
        @Override
        public int compare(Point o1, Point o2) {
            return Integer.valueOf(o1.getRssi()).compareTo(Integer.valueOf(o2.getRssi()));
        }
    });

    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private static final String TAG = "SearchBlueTooth";
    private Button mScanButton;
    private Button mSearchButton;
    private Button fileButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_page);

        relativeLayout = (RelativeLayout)findViewById(R.id.search_relative);
        mImageView.add(0, new ImageView(SearchBlueTooth.this));

        mImageView.get(0).setImageResource(R.drawable.photo);
        //设置相对布局（控件）的大小
        mParams = new RelativeLayout.LayoutParams(100,100);
        mParams.setMargins(0, 1290, 0, 0);
        mImageView.get(0).setLayoutParams(mParams);
        relativeLayout.addView(mImageView.get(0));
        endRotate = (TextView)findViewById(R.id.end_zuobiao);
        ll = (LinearLayout)findViewById(R.id.touch);
        ll.setOnTouchListener(this);

        mScanButton = (Button)findViewById(R.id.scan);
        mSearchButton = (Button)findViewById(R.id.find);
        fileButton = (Button) findViewById(R.id.writeFile);

        //定位
        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                result();
            }
        });
        //测距
        mScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScanBluth();
            }
        });
        //取货（存入文件）
        fileButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                //写入文件
                if (isNewFile) {
                    fileName = getCurrentTime() + "货号："+"(" + loc_x + "," + loc_y + ")" + ".txt";
                    isNewFile =false;
                }
                try {
                    writeFile(fileName);
                    Toast.makeText(SearchBlueTooth.this,"内容已写入"+fileName,Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        init();
    }
   //获得当前时间作为文件名
    String getCurrentTime(){
        Date date = new Date(System.currentTimeMillis());
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd-HH:mm:ss");
        return simpleDateFormat.format(date);
    }

    void writeFile(String fileName) throws IOException {
        File file = new File(SearchBlueTooth.this.getExternalFilesDir(null),fileName);
        FileWriter fileWriter = new FileWriter(file,!isNewFile);
        fileWriter.write(loc_x + "|" + loc_y + ' ');
        Log.d("writefile","***************____x: "+Integer.toString(loc_x)+"  y: "+Integer.toString(loc_y));
        fileWriter.flush();
        fileWriter.close();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                x= (int) (event.getX()*852/1030-46);
                y= (int) (1700-event.getY()*2880/1920);
                endRotate.setText("当前位置:("+x+","+y+")");
                mImageView.add(j,new ImageView(SearchBlueTooth.this));
                xSearch = (int) event.getX();
                ySearch = (int) event.getY();
                mParams.setMargins(xSearch-50, ySearch-200, 0, 0);
                mImageView.get(j).setLayoutParams(mParams);
                relativeLayout.addView(mImageView.get(j));
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            default:
                break;
        }
        return true;
    }

    /**
     * 判断蓝牙是否开启
     */
    private void init() {
        startDiscovery();
        //Log.e(TAG, "startDiscovery: 开启蓝牙");
    }

      /**
       * 三边定位算法
       */
    public void result(){
         if(!mSet.isEmpty()){
            mSet.pollLast();
         }
        if(!mPointArrayList.isEmpty()){
            mPointArrayList.clear();
        }
        for(int i = 0;i<3;i++){
            if(!mSet.isEmpty()){
                Point t = mSet.pollLast();
                Log.d("MainActivity","_____________________x: "+Integer.toString(t.getX())+"  y: "+Integer.toString(t.getY())+" rssi: "+Integer.toString(t.getRssi()));
                mPointArrayList.add(t);
            }
        }
        if(!mPointArrayList.isEmpty()){
            Point p0 = mPointArrayList.pollLast();
            p0_x=p0.getX();
            p0_y=p0.getY();
            disMac1=calDis(p0.getRssi());
            int x= (int)p0_x;
            int y=(int)p0_y;
            int z=(int)disMac1;
            Log.d("MainActivity","______x: "+Integer.toString(x)+"  y: "+Integer.toString(y)+" rssi: "+Integer.toString(z));
        }
        if(!mPointArrayList.isEmpty()){
            Point p1 = mPointArrayList.pollLast();
            p1_x=p1.getX();
            p1_y=p1.getY();
            disMac2=calDis(p1.getRssi());
            int x= (int)p1_x;
            int y=(int)p1_y;
            int z=(int)disMac2;
            Log.d("MainActivity","______x: "+Integer.toString(x)+"  y: "+Integer.toString(y)+" rssi: "+Integer.toString(z));
        }
        if(!mPointArrayList.isEmpty()){
            Point p2 = mPointArrayList.pollLast();
            p2_x=p2.getX();
            p2_y=p2.getY();
            disMac3=calDis(p2.getRssi());
            int x= (int)p2_x;
            int y=(int)p2_y;
            int z=(int)disMac3;
            Log.d("MainActivity","_______x: "+Integer.toString(x)+"  y: "+Integer.toString(y)+" rssi: "+Integer.toString(z));
        }
        double a=p0_x-p2_x;
        double b=p0_y-p2_y;
        double c= Math.pow(p0_x, 2) - Math.pow(p2_x, 2) + Math.pow(p0_y, 2) - Math.pow(p2_y, 2) + Math.pow(disMac3, 2) - Math.pow(disMac1, 2);
        double d=p1_x-p2_x;
        double e=p1_y-p2_y;
        double f=Math.pow(p1_x, 2) - Math.pow(p2_x, 2) + Math.pow(p1_y, 2) - Math.pow(p2_y, 2) + Math.pow(disMac3, 2) - Math.pow(disMac2, 2);
        loc_x=(int)((b*f-e*c)/(2*b*d-2*a*e));
        loc_y=(int)((a*f-d*c)/(2*a*e-2*b*d))+800;
        //loc_x=317;
       // loc_y= 1337;
        Log.d("MainActivity","________*******________x: "+Integer.toString(loc_x)+"  y: "+Integer.toString(loc_y));
        showPosition(loc_x,loc_y);
        mSet.clear();
    }

    public void showPosition(int x,int y){
        x_end=318;
        y_end=40;
        Toast.makeText(SearchBlueTooth.this,"纵坐标"+y_end,Toast.LENGTH_SHORT).show();
        loc_relativeLayout = (RelativeLayout)findViewById(R.id.loc_relative);
        mImageView.add(1,new ImageView(SearchBlueTooth.this));
        mImageView.get(1).setImageResource(R.drawable.circle);
        loc_params = new RelativeLayout.LayoutParams(60,60);
        loc_params.setMargins(x_end, y_end, 0, 0);
        mImageView.get(1).setLayoutParams(loc_params);
        loc_relativeLayout.addView(mImageView.get(1));
    }


    /**
     * 注册异步搜索蓝牙设备的广播
     */
    private void startDiscovery() {
        // 找到设备的广播
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        // 注册广播
        registerReceiver(searchReceiver, filter);
        // 搜索完成的广播
        IntentFilter filter1 = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        // 注册广播
        registerReceiver(searchReceiver, filter1);
        Log.e(TAG, "startDiscovery: 注册searchReceiver广播");
        startScanBluth();
    }


    private double calDis(int rssi){
        int iRssi = Math.abs(rssi);
        double power = (iRssi-A)/(10*n);
        return Math.pow(10,power);
    }

    //搜索所有蓝牙的广播接收器
    private final BroadcastReceiver searchReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 收到的广播类型
            String action = intent.getAction();
            // 发现设备的广播
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // 从intent中获取设备
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int RSSI = intent.getExtras().getShort( BluetoothDevice.EXTRA_RSSI);

                if (device.getAddress().equals(BluetoothList.MAC)){
                    mBluetoothAdapter.cancelDiscovery();
                    Toast.makeText(context,"扫描成功,RSSI："+Integer.toString(RSSI)+"距离您："+String.format("%.2f",calDis(RSSI))+"米",Toast.LENGTH_SHORT).show();
                    Log.d("MainActivity","+++++++++++++++++ RSSI值为"+Integer.toString(RSSI));
                    Point tmp = new Point(xSearch,ySearch);//___________________________________________________________________x,y要用静态变量
                    tmp.setRssi(RSSI);
                    mSet.add(tmp);
                }

                // 搜索完成
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                // 关闭进度条
                progressDialog.dismiss();
                Log.e(TAG, "SearchBlue onReceive: 搜索完成");
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
        if (searchReceiver != null) {
            //取消注册,防止内存泄露（onDestroy被回调代不代表Activity被回收？：具体回收看系统，由GC回收，同时广播会注册到系统
            //管理的ams中，即使activity被回收，reciver也不会被回收，所以一定要取消注册），
            unregisterReceiver(searchReceiver);
        }
    }

}
