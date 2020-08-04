package com.example.uwcapstone;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.sonicmeter.android.multisonicmeter.Utils;
import com.sonicmeter.android.multisonicmeter.Params;

import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements OnItemSelectedListener {
    private static MainActivity instance;
    private Spinner mySpinner = null;
    private static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    String msg = "";
    Client clientThread = null;
    Server serverThread = null;
    Params params = new Params();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        instance = this;

        if (Build.VERSION.SDK_INT >= 23) {
            checkAndRequestPermissions();
        }

        // initial UI state
        Button startSendBtn = (Button) findViewById(R.id.startSend);
        setStartSendBtnState(true);
        Button stopSendBtn = (Button) findViewById(R.id.stopSend);
        setStopSendBtnState(false);
        Button startReceiveBtn = (Button) findViewById(R.id.startReceiving);
        setStartReceiveBtnState(true);
        Button stopReceiveBtn = (Button) findViewById(R.id.stopReceiving);
        setStopReceiveBtnState(false);
        mySpinner = (Spinner) findViewById(R.id.msgToSend);

        // create a container to hold the values that would integrate to the spinner
        ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(MainActivity.this,
                android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.messages));

        // specify the adapter would have a drop down list
        myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // list in adapter shown in spinner
        mySpinner.setAdapter(myAdapter);

        // initial database


        // initial audiotrack player
        Utils.initPlayer(params.sampleRate, 0);
        Utils.initRecorder(params.sampleRate);

        // register OnItemSelected event
        mySpinner.setOnItemSelectedListener(this);

        startSendBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // start to play sequence
                //Log.d("start", "client thread");
                log("start client thread");
                instance.clientThread = new Client(msg);
                instance.clientThread.start();
            }
        });

        stopSendBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // stop playing sequence
                //Log.d("stop", "client thread");
                log("stop client thread");
                instance.clientThread.stopThread();
                setStopSendBtnState(false);
                setStartSendBtnState(true);
                clientThread = null;
            }
        });

        startReceiveBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // start to listen to sequence
                log("start server thread");
                instance.serverThread = new Server();
                instance.serverThread.start();
            }
        });

        stopReceiveBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // stop listening to sequence
                Log.d("stop", "server thread");
                instance.serverThread.stopThread();
                setStopReceiveBtnState(false);
                setStartReceiveBtnState(true);
                serverThread = null;
            }
        });
    }

    // Spinner OnItemSelectedListener
    @Override
    public void onItemSelected(AdapterView<?> adapter, View view, int position, long id) {
        // get message in spinner
        msg = adapter.getItemAtPosition(position).toString();
    }
    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        msg = "MESSAGE IS NOT SELECTED";
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }

    public static void setStartSendBtnState(final boolean state)
    {
        instance.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Button button_server = (Button) instance.findViewById(R.id.startSend);
                button_server.setEnabled(state);
            }
        });
    }
    public static void setStopSendBtnState(final boolean state)
    {
        instance.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Button button_client = (Button) instance.findViewById(R.id.stopSend);
                button_client.setEnabled(state);
            }
        });
    }

    public static void setStartReceiveBtnState(final boolean state)
    {
        instance.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Button button = (Button) instance.findViewById(R.id.startReceiving);
                button.setEnabled(state);
            }
        });
    }

    public static void setStopReceiveBtnState(final boolean state)
    {
        instance.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Button button = (Button) instance.findViewById(R.id.stopReceiving);
                button.setEnabled(state);
            }
        });
    }

    public static void log(final String text)
    {
        instance.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView logBox = (TextView)instance.findViewById(R.id.textview_log);
                logBox.setMovementMethod(ScrollingMovementMethod.getInstance());
                //Calendar cal = Calendar.getInstance();
                logBox.append("  "+ text + "\n");//cal.get(Calendar.MINUTE)+ ":" +cal.get(Calendar.SECOND)+ ":" + cal.get(Calendar.MILLISECOND) +
            }
        });
    }

    public static void decodedMsg(final String text)
    {
        instance.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView decodedMsgBox = (TextView)instance.findViewById(R.id.decoded_msg);
                //Calendar cal = Calendar.getInstance();
                decodedMsgBox.append("  "+ text + "\n");//cal.get(Calendar.MINUTE)+ ":" +cal.get(Calendar.SECOND)+ ":" + cal.get(Calendar.MILLISECOND) +
            }
        });
    }

    private  boolean checkAndRequestPermissions() {
        int permissionWifi = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE);
        int writepermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permissionRecordAudio = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);


        ArrayList<String> listPermissionsNeeded = new ArrayList<>();

        if (writepermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (permissionWifi != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_WIFI_STATE);
        }
        if (permissionRecordAudio != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.RECORD_AUDIO);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

}
