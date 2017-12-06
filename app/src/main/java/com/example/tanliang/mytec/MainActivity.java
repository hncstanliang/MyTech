package com.example.tanliang.mytec;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import com.mipt.mediacenter.utils.cifs.*;
import java.io.*;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_WRITE_STORAGE = 112;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        Button btn=findViewById(R.id.btn);
        btn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //ScanNet scan=new ScanNet();
                //scan.scan5();
                try {
                    UdpGetClientMacAddr dp=new UdpGetClientMacAddr("192.168.0.105");
                    LanNodeInfo inf=dp.getRemoteMacAddr();
                    Log.v("名称：","名称："+inf.name);

                }catch(Exception e)
                {

                }


            }
        });

        Button btn1=(Button)findViewById(R.id.btnCard);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int permission = ActivityCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE);

                if (permission != PackageManager.PERMISSION_GRANTED) {
// We don't have permission so prompt the user
                    ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS_STORAGE,
                            REQUEST_EXTERNAL_STORAGE);
                }else{
                    writeExternalCard();
                    //readExternalCard();
                }
            }
        });

        Button btnWrite=(Button)findViewById(R.id.btnWrite);
        btnWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int permission = ActivityCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE);

                if (permission != PackageManager.PERMISSION_GRANTED) {
// We don't have permission so prompt the user
                    ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS_STORAGE,
                            REQUEST_EXTERNAL_STORAGE);
                }else {
                    boolean res=com.tl.util.SDCardHelper.saveFileToSDCardCustomDir("Hello world", "Tl", "test.txt");
                     res=com.tl.util.SDCardHelper.saveFileToSDCardCustomDir("very good\r\n", "TlDir", "test.txt");
                     com.tl.util.SDCardHelper.readFileTest("TlDir", "test.txt");
                    com.tl.util.SDCardHelper.DeleteFile();
                }
            }
        });
    }

    private void readExternalCard(){
        String sdCard="/mnt/ext_sdcard/tl/123";

        try{
            // File dir=MainActivity.this.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
            File dir=new File(sdCard);
            if(!dir.exists()){
                dir.mkdirs();
            }


            File file=new File(dir.getPath()+"/"+"tlabc.txt");
            if(!file.exists()){

                file.createNewFile();
            }


//                        FileWriter write=new FileWriter(file);
//                        write.write("A test String123");
//                        write.flush();
//                        write.close();

            InputStreamReader inr=new InputStreamReader(new FileInputStream(file));
            BufferedReader rd=new BufferedReader(inr);
            String line=rd.readLine();
            rd.close();
            inr.close();

//                        File dir=new File(sdCard);
//                        if(!dir.exists())
//                        {
//                            dir.mkdir();
//                        }
//
//                        File file=new File(dir.getPath()+"/"+"TestFile.txt");
//                        if(!file.exists()){
//                            file.createNewFile();
//                        }
//                        FileWriter write=new FileWriter(file);
//                        write.write("A test String");
//                        write.close();
        }catch (Exception e)
        {
            Log.d("Card",e.getMessage());
        }
    }

    private  void writeExternalCard(){
        //String sdCard="/mnt/ext_sdcard/Android/data/com.example.tanliang.mytec/files";
        String sdCard="/mnt/ext_sdcard/tl/123";
        try{
            // File dir=MainActivity.this.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
            File dir=new File(sdCard);
            if(!dir.exists()){
                dir.mkdirs();
            }


            File file=new File(dir.getPath()+"/"+"tlabc123.txt");
            if(!file.exists()){

                file.createNewFile();
            }


            FileWriter write=new FileWriter(file);
            write.write("hello A test String123");
            write.flush();
            write.close();

            InputStreamReader inr=new InputStreamReader(new FileInputStream(file));
            BufferedReader rd=new BufferedReader(inr);
            String line=rd.readLine();
            rd.close();
            inr.close();

//                        File dir=new File(sdCard);
//                        if(!dir.exists())
//                        {
//                            dir.mkdir();
//                        }
//
//                        File file=new File(dir.getPath()+"/"+"TestFile.txt");
//                        if(!file.exists()){
//                            file.createNewFile();
//                        }
//                        FileWriter write=new FileWriter(file);
//                        write.write("A test String");
//                        write.close();
        }catch (Exception e)
        {
            Log.d("Card",e.getMessage());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
