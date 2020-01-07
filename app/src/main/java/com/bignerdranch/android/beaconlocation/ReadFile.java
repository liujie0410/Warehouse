package com.bignerdranch.android.beaconlocation;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;


import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.view.View;
import java.io.File;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;




public class ReadFile  extends AppCompatActivity {
     ListView listView;
    ArrayList<String> fileNames;

    Button scanFileButton;

    protected void onCreate(Bundle savedInstanceState) {
        //回调函数
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filelist);

          listView = findViewById(R.id.FileNameListView);
          scanFileButton = findViewById(R.id.scanFileButton);

        fileNames = new ArrayList<>();

       scanFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanFile();
               ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(ReadFile.this, android.R.layout.simple_list_item_1, fileNames);
               listView.setAdapter(arrayAdapter);
            }
        });

    }


        /**
         * 扫描私有文件夹下的txt文件
         */

    private void scanFile(){

         File file = new File(getExternalFilesDir(null).getAbsolutePath());
         File[] files = file.listFiles();
         fileNames.clear();
       for (File e : files){
            if (e.isFile()&&e.getName().endsWith(".txt")){
                fileNames.add(e.getName());
            }
        }
    }

}
