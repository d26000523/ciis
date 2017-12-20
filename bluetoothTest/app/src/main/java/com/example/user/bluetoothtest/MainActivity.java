package com.example.user.bluetoothtest;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private BluetoothAdapter mBtAdapter;
    private ArrayAdapter<String> aBtAdapter;
    private final BroadcastReceiver mReceiver =new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(MainActivity.this,"???",Toast.LENGTH_SHORT).show();
            if(BluetoothDevice.ACTION_FOUND.equals(intent.getAction())){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                aBtAdapter.add(device.getName() + "\n" + device.getAddress());
                aBtAdapter.notifyDataSetChanged();
            }

        }
    };;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        registerReceiver( mReceiver, new IntentFilter( BluetoothDevice.ACTION_FOUND ) );
        aBtAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1);
        Button buttonact = findViewById(R.id.buttonact);
        ListView listView = findViewById(R.id.listview);
        listView.setAdapter(aBtAdapter);

        buttonact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if( !mBtAdapter.isEnabled() ) {
                    Intent enableIntent = new Intent( BluetoothAdapter.ACTION_REQUEST_ENABLE );
                    startActivityForResult( enableIntent, 0 );
                }
                else{
                    aBtAdapter.clear();
                    mBtAdapter.startDiscovery();
                    //IntentFilter filter = new IntentFilter( BluetoothDevice.ACTION_FOUND );
                    //MainActivity.this.registerReceiver( mReceiver, filter );
                    Toast.makeText(MainActivity.this,"?1",Toast.LENGTH_SHORT).show();
                    //filter = new IntentFilter( BluetoothAdapter.ACTION_DISCOVERY_FINISHED );
                    //MainActivity.this.registerReceiver( mReceiver, filter );
                }
            }
        });
        Button buttonpas = findViewById(R.id.buttonpas);
        buttonpas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!mBtAdapter.isEnabled()){
                    Intent enableIntent = new Intent( BluetoothAdapter.ACTION_REQUEST_ENABLE );
                    startActivityForResult( enableIntent, 0 );
                }
                else{
                    if( mBtAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE ) {
                        Intent discoverableIntent = new Intent( BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE );
                        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120 );
                        startActivity( discoverableIntent ); }
                }
            }

        });
        mBtAdapter.getBondedDevices();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }
}
