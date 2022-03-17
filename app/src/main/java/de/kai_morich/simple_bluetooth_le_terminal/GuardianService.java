package de.kai_morich.simple_bluetooth_le_terminal;

import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.content.ServiceConnection;
import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;

public class GuardianService extends Service implements ServiceConnection, SerialListener {
    class GuardianBinder extends Binder {
        GuardianService getService() { return GuardianService.this; }
    }

    private enum Connected { False, Pending, True }

    private String deviceAddress;
    private SerialService service;
    private TcpServerService srv_service;
    private final IBinder binder;
    private Connected connected = Connected.False;
    private GuardianListener listener;

    public GuardianService() {
        binder = new GuardianService.GuardianBinder();
    }

    public void setDeviceAddress(String deviceAddress) {
        this.deviceAddress = deviceAddress;
    }

    @Override
    public void onCreate() {
        bindService(new Intent(this, SerialService.class), this, Context.BIND_AUTO_CREATE);
        bindService(new Intent(this, TcpServerService.class), this, Context.BIND_AUTO_CREATE);
    }
    @Override
    public void onDestroy() {
        if (connected != Connected.False)
            disconnect();
        stopService(new Intent(GuardianService.this, SerialService.class));
        stopService(new Intent(GuardianService.this, TcpServerService.class));
        super.onDestroy();
    }

    // called from fragment
    public void onStart() {
        if(service != null) {
            service.attach(this);
            service.attach(srv_service);
        }
        else
            startService(new Intent(this, SerialService.class)); // prevents service destroy on unbind from recreated activity caused by orientation change
    }

    // called from fragment
    public void onDetach() {
        try { unbindService(this); } catch(Exception ignored) {}
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        String csname = name.getClassName();
        if(csname.equals(SerialService.class.getName())) {
            service = ((SerialService.SerialBinder) binder).getService();
            service.attach(this);
            listener.onConnect();
        }
        else
        if(csname.equals(TcpServerService.class.getName())) {
            srv_service = ((TcpServerService.TcpServerBinder) binder).getService();
            service.attach(srv_service);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        String csname = name.getClassName();
        if(csname.equals(SerialService.class.getName())) {
            service = null;
        }
        else
        if(csname.equals(TcpServerService.class.getName())) {
            srv_service = null;
        }
    }

    public void connect() {
        try {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
            status("connecting...");
            connected = Connected.Pending;
            SerialSocket socket = new SerialSocket(getApplicationContext(), device);
            service.connect(socket);
        } catch (Exception e) {
            onSerialConnectError(e);
        }

        startService(new Intent(GuardianService.this, TcpServerService.class));
    }

    public void disconnect() {
        connected = Connected.False;
        stopService(new Intent(GuardianService.this, TcpServerService.class));
        service.disconnect();
    }

    public void reconnect() {
        do {
            try {
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);

                status("connecting...");

                connected = Connected.Pending;
                SerialSocket socket = new SerialSocket(getApplicationContext(), device);
                service.connect(socket);
            } catch (Exception ex) {
                SystemClock.sleep(250);
                continue;
            }

            startService(new Intent(GuardianService.this, TcpServerService.class));
        } while(false);
    }

    public boolean isConnected() {
        return connected != Connected.True;
    }

    public void write(byte[] data) throws IOException {
        service.write(data);
    }

    public void attach(GuardianListener listener) {
        this.listener = listener;
    }

    public void detach() {
        //if(service != null)
        //    service.detach(this);

        listener = null;
    }

    private void status(String str) {
        if(listener != null) {
            listener.onStatus(str);
        }
    }
    /*
     * SerialListener
     */
    @Override
    public void onSerialConnect() {
        status("connected");
        connected = Connected.True;
        if(listener != null) {
            listener.onSerialConnect();
        }
    }

    @Override
    public void onSerialConnectError(Exception e) {
        status("connection failed: " + e.getMessage());
        disconnect();

        if(listener != null) {
            listener.onSerialConnectError(e);
        }

        reconnect();
    }

    @Override
    public void onSerialRead(byte[] data) {
        if(listener != null) {
            listener.onSerialRead(data);
        }
    }
    @Override
    public void onSerialIoError(Exception e) {
        status("connection lost: " + e.getMessage());

        disconnect();

        if(listener != null)
            listener.onSerialConnectError(e);

        reconnect();
    }
}