package com.duvitech.mybluetooth;

import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity implements DongleListener {

    BTConnectService mBTService;
    boolean mBound = false;

    private static final int REQUEST_ENABLE_BT = 1;
    boolean bCheckForDongleOrTag = false;
    MainActivity myInstance = null;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BTConnectService.BTConnectBinder binder = (BTConnectService.BTConnectBinder) service;

            mBTService = binder.getService();
            mBound = true;


            mBTService.addListener(myInstance);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound =false;
        }
    };

    @Override
    protected void onStop(){
        super.onStop();
        if(mBound){
            unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        myInstance = this;
        if (btAdapter == null) {
            // Device does not support Bluetooth
            bCheckForDongleOrTag = false;
        }
        else
        {
            bCheckForDongleOrTag = true;
            if(!btAdapter.isEnabled()){

                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                this.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

                // broadcast receiver maybe
            }
            else {
                // start bluetooth service
                Intent i = new Intent(getBaseContext(), BTConnectService.class);
                startService(i);
                // bind service here
                bindService(i, mConnection, Context.BIND_AUTO_CREATE);


            }
        }

        Button btnGetAddress = (Button)findViewById(R.id.btnGetAddr);
        btnGetAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mBound) {
                    TextView t = (TextView) findViewById(R.id.tvBTDongleAddr);
                    t.setText(mBTService.getBTDongleAddress());
                }
            }
        });

        Button btnGetTemperature = (Button)findViewById(R.id.btnGetTemp);
        btnGetTemperature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mBound){

                    TextView t = (TextView) findViewById(R.id.textView);
                    t.setText(mBTService.sendReceive("0105\r","41 05"));
                }
            }
        });

        Button btnSendInit = (Button)findViewById(R.id.btnSendInit);
        btnSendInit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mBound){

                    TextView t = (TextView) findViewById(R.id.textView);
                    t.setText(mBTService.sendReceive("AT Z\r","ELM327"));
                }
            }
        });

        Button btnFuelLevel = (Button)findViewById(R.id.btnFuelLevel);
        btnFuelLevel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(mBound){
                    TextView t = (TextView) findViewById(R.id.textView);
                    t.setText(mBTService.sendReceive("012F\r","41 2F"));
                }
            }
        });

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if(resultCode == RESULT_OK){
                Intent i = new Intent(getBaseContext(), BTConnectService.class);
                startService(i);

                bindService(i, mConnection, Context.BIND_AUTO_CREATE);

            }
            if (resultCode == RESULT_CANCELED) {
                //Write your code if there's no result
                Toast.makeText(this, "Must enable bluetooth for tracking application to work properly", Toast.LENGTH_LONG).show();
            }
        }
    }//onActivityResult

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

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mBound){
            unbindService(mConnection);
            mBound = false;
        }
        stopService(new Intent(getBaseContext(), BTConnectService.class));
    }

    @Override
    public void foundDongle(String address) {
        TextView t = (TextView) findViewById(R.id.tvBTDongleAddr);
        t.setText(address);
    }

    @Override
    public void updateData(VehicleData data) {

    }
}
