package com.example.marco_nuno_maganda.monitoreocalcetinv2;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class ConnectBT extends AsyncTask<Void, Void, Void> {
    private boolean ConnectSuccess = true;
    private ProgressDialog progress;
    MainActivity CX;
    SharedPreferences prefe;

    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    String address = null;
    InputStream tmpIn = null;
    OutputStream tmpOut = null;
    private BluetoothAdapter myBluetooth = null;

    public ConnectBT (MainActivity MCX,  BluetoothAdapter myBluetoothX, String maddress, SharedPreferences sh) {
        CX = MCX;
        myBluetooth = myBluetoothX;
        address=maddress;
        prefe=sh;

    }

    @Override
    protected  void onPreExecute () {
        progress = ProgressDialog.show(CX, "Connecting...", "Please Wait!!!");
    }

    @Override
    protected Void doInBackground (Void... devices) {
        try {
            if ( btSocket==null || !isBtConnected ) {
                myBluetooth = BluetoothAdapter.getDefaultAdapter();
                BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);
                btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);
                BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                btSocket.connect();
                tmpOut=btSocket.getOutputStream();
                tmpIn= btSocket.getInputStream();
            }
        } catch (IOException e) {
            ConnectSuccess = false;
        }

        return null;
    }

    @Override
    protected void onPostExecute (Void result) {
        super.onPostExecute(result);

        if (!ConnectSuccess) {
            msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
            //finish();
        } else {
            msg("Connected");
            isBtConnected = true;
        }

        progress.dismiss();
    }

    private void msg (String s) {
        Toast.makeText(CX, s, Toast.LENGTH_LONG).show();
    }

    public String sendSignal ( String number ) {
        byte[] buffer = new byte[256];
        int bytes;
        String ValioBarriga="";

        if ( btSocket != null ) {
            try {
                //btSocket.getOutputStream().write(number.toString().getBytes());
                tmpOut.write(number.toString().getBytes());
                // Insertar un retardo con la finalidad de ver si responde

                //bytes = tmpIn.read(buffer);        	//read bytes from input buffer
                //ValioBarriga = new String(buffer, 0, bytes);

            } catch (IOException e) {
                //msg("Error");
            }
        }
        return (ValioBarriga);
    }


    public String ReceiveData ( ) {
        byte[] buffer = new byte[256];
        int bytes;
        String ValioBarriga="";
        String Final ="";

        if ( btSocket != null ) {
            try {
                //btSocket.getOutputStream().write(number.toString().getBytes());
                //tmpOut.write(number.toString().getBytes());
                // Insertar un retardo con la finalidad de ver si responde

                boolean Incompleto = true;
                Final ="";

                while (Incompleto) {
                    bytes = tmpIn.read(buffer);            //read bytes from input buffer
                    ValioBarriga = new String(buffer, 0, bytes);
                    //Resultados.append(ValioBarriga + "\n");

                    if (ValioBarriga.contains("\n")) {
                        //ValioBarriga="Incompleto - volver a intentar";
                        Incompleto = false;
                    }
                    Final += ValioBarriga;
                }


            } catch (IOException e) {
                //msg("Error");
            }
        }
        //return (ValioBarriga);
        Final = Final.replace("\n","").replace("\r","");
        //Resultados.append(Final + "\n");

        String Lecturas = prefe.getString("Lecturas", "");
        SharedPreferences.Editor myEdit = prefe.edit();
        myEdit.putString("Lecturas", Lecturas+Final+"\n");
        myEdit.commit();

        return (Final);
    }

}
