package com.example.marco_nuno_maganda.monitoreocalcetinv2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    SharedPreferences prefe;

    MyReceiver myReceiver;
    private BluetoothAdapter myBluetooth = null;
    private Set<BluetoothDevice> pairedDevices;
    public static String EXTRA_ADDRESS = "device_address";
    ListView devicelist;
    MainActivity MA;

//    private ProgressDialog progress;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    String address = null;
    InputStream tmpIn = null;
    OutputStream tmpOut = null;

    TextView Resultados;
    ConnectBT ConexionBluetooth;
    String Lecturas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefe=getSharedPreferences("DatosDispositivoBluetooth", Context.MODE_PRIVATE);
        Lecturas = prefe.getString("Lecturas", "");
        if (Lecturas.equals("")) {
            Toast.makeText(getApplicationContext(), "Vacio", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(getApplicationContext(), Lecturas, Toast.LENGTH_SHORT).show();
        }


        Button btnPaired;
        Button checkCon;

        btnPaired = (Button) findViewById(R.id.button);
        checkCon = findViewById(R.id.button2);
        devicelist = (ListView) findViewById(R.id.listView);
        Resultados = findViewById(R.id.textViewR);
        Resultados.setText("");

        myBluetooth = BluetoothAdapter.getDefaultAdapter();
        if ( myBluetooth==null ) {
            Toast.makeText(getApplicationContext(), "Bluetooth device not available", Toast.LENGTH_LONG).show();
            finish();
        }
        else if ( !myBluetooth.isEnabled() ) {
            Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnBTon, 1);
        }

        btnPaired.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //pairedDevicesList();
                String X1 = ConexionBluetooth.sendSignal("A");
                String X =  ConexionBluetooth.ReceiveData();
                Resultados.append(X+ "\n");
                //Toast.makeText(getApplicationContext(),X,Toast.LENGTH_SHORT).show();
            }
        });

        checkCon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //pairedDevicesList();
                String X1 = ConexionBluetooth.sendSignal("X");
                String X  = ConexionBluetooth.ReceiveData();
                Resultados.append(X+ "\n");
                //Toast.makeText(getApplicationContext(),X,Toast.LENGTH_SHORT).show();
            }
        });
        pairedDevicesList();

    }
    private void pairedDevicesList () {
        pairedDevices = myBluetooth.getBondedDevices();
        ArrayList list = new ArrayList();

        if ( pairedDevices.size() > 0 ) {
            for ( BluetoothDevice bt : pairedDevices ) {
                list.add(bt.getName().toString() + "\n" + bt.getAddress().toString());
            }
        } else {
            Toast.makeText(getApplicationContext(), "No Paired Bluetooth Devices Found.", Toast.LENGTH_LONG).show();
        }

        final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, list);
        devicelist.setAdapter(adapter);
        devicelist.setOnItemClickListener(myListClickListener);
    }

    private AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String info = ((TextView) view).getText().toString();
            address = info.substring(info.length()-17);

            // Pasar todos los parametrso en el constructor
            ConexionBluetooth= new ConnectBT(MainActivity.this, myBluetooth, address, prefe);
            ConexionBluetooth.execute();
        }
    };

    private void Disconnect () {
        if ( btSocket!=null ) {
            try {
                btSocket.close();
            } catch(IOException e) {
                //msg("Error");
            }
        }

        finish();
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            RestartServiceBroadcastReceiver.scheduleJob(getApplicationContext());
        } else {
            ProcessMainClass bck = new ProcessMainClass();
            bck.launchService(getApplicationContext());

        }

        myReceiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Service.MY_ACTION);
        registerReceiver(myReceiver, intentFilter);


    }

    private class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            // TODO Auto-generated method stub

            int datapassed = arg1.getIntExtra("DATAPASSED", 0);


            String X1 = ConexionBluetooth.sendSignal("A");
            String X =  ConexionBluetooth.ReceiveData();
            Log.i("Lectura Sensor", "---> " + X);
            Resultados.append(X+ "\n");


            Toast.makeText(MainActivity.this,
                    "Triggered by Service!\n"
                            + "Data passed: " + String.valueOf(datapassed)
                            + "Lectura obtenida: "+X,
                    Toast.LENGTH_LONG).show();



        }

    }

}