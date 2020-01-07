package com.bignerdranch.android.beaconlocation;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

public class StartActivity extends AppCompatActivity {

    private Button mSearchBlueButton;
    private Button mLocationButton;
    private Button mReadButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
        setContentView(R.layout.activity_start);
        mSearchBlueButton = (Button)findViewById(R.id.start_search);//寻找货物（蓝牙）
        mLocationButton = (Button)findViewById(R.id.start_location);//室内定位（蓝牙基站 定位移动终端）
        mReadButton=(Button)findViewById(R.id.readfile);//取货记录 寻找到的蓝牙坐标及时间

        mSearchBlueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(v.getContext(),BluetoothList.class);
                startActivity(i);
            }
        });

        mLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(v.getContext(),Location.class);
                startActivity(i);
            }
        });

        mReadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(v.getContext(),ReadFile.class);
                startActivity(i);
            }
        });

    }
}
