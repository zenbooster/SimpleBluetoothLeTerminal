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
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;

public class GuardianService extends Service implements SerialListener {
    private static final String TAG = "GuardianService";

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
        Log.v(TAG, "ctor begin");
        binder = new GuardianService.GuardianBinder();
        Log.v(TAG, "ctor end");
    }

    public void setDeviceAddress(String deviceAddress) {
        this.deviceAddress = deviceAddress;
    }

    @Override
    public void onCreate() {
        Log.v(TAG, "onCreate begin");
        bindService(new Intent(this, SerialService.class), serialConnection, Context.BIND_AUTO_CREATE);
        Log.v(TAG, "onCreate end");
    }
    @Override
    public void onDestroy() {
        Log.v(TAG, "onDestroy begin");
        if (connected != Connected.False)
            disconnect();
        stopService(new Intent(GuardianService.this, SerialService.class));
        stopService(new Intent(GuardianService.this, TcpServerService.class));
        super.onDestroy();
        Log.v(TAG, "onDestroy end");
    }

    // called from fragment
    public void onStart() {
        Log.v(TAG, "onStart begin");
        if(service != null) {
            service.attach(this);
            service.attach(srv_service);
        }
        else
            startService(new Intent(this, SerialService.class)); // prevents service destroy on unbind from recreated activity caused by orientation change
        Log.v(TAG, "onStart end");
    }

    // called from fragment
    public void onDetach() {
        Log.v(TAG, "onDetach begin");
        try { unbindService(tcpServerConnection); } catch(Exception ignored) {}
        try { unbindService(serialConnection); } catch(Exception ignored) {}
        Log.v(TAG, "onDetach end");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.v(TAG, "onBind");
        return binder;
    }

    private ServiceConnection serialConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            Log.v(TAG, "serialConnection onServiceConnected begin");
            service = ((SerialService.SerialBinder) binder).getService();
            service.attach(GuardianService.this);
            listener.onConnect();
            Log.v(TAG, "serialConnection onServiceConnected end");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.v(TAG, "serialConnection onServiceDisconnected begin");
            service = null;
            Log.v(TAG, "serialConnection onServiceDisconnected end");
        }
    };

    private ServiceConnection tcpServerConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            Log.v(TAG, "tcpServerConnection onServiceConnected begin");
            srv_service = ((TcpServerService.TcpServerBinder) binder).getService();
            service.attach(srv_service);
            Log.v(TAG, "tcpServerConnection onServiceConnected end");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.v(TAG, "tcpServerConnection onServiceDisconnected begin");
            srv_service = null;
            Log.v(TAG, "tcpServerConnection onServiceDisconnected end");
        }
    };

    public void connect() {
        Log.v(TAG, "connect begin");
        try {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
            status("connecting...");
            connected = Connected.Pending;
            SerialSocket socket = new SerialSocket(getApplicationContext(), device);
            service.connect(socket);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            onSerialConnectError(e);
        }

        Log.v(TAG, "connect end");
    }

    public void disconnect() {
        Log.v(TAG, "disconnect begin");
        connected = Connected.False;
        unbindService(tcpServerConnection);
        stopService(new Intent(GuardianService.this, TcpServerService.class));
        service.disconnect();
        Log.v(TAG, "disconnect end");
    }

    public boolean isConnected() {
        return connected != Connected.True;
    }

    public void write(byte[] data) throws IOException {
        Log.v(TAG, "reconnect begin");
        service.write(data);
        Log.v(TAG, "reconnect end");
    }

    public void attach(GuardianListener listener) {
        Log.v(TAG, "attach begin");
        this.listener = listener;
        Log.v(TAG, "attach end");
    }

    public void detach() {
        Log.v(TAG, "detach begin");
        listener = null;
        Log.v(TAG, "detach end");
    }

    private void status(String str) {
        Log.i(TAG, str);
        if(listener != null) {
            listener.onStatus(str);
        }
    }
    /*
     * SerialListener
     */
    @Override
    public void onSerialConnect() {
        Log.v(TAG, "onSerialConnect begin");
        startService(new Intent(GuardianService.this, TcpServerService.class));
        bindService(new Intent(this, TcpServerService.class), tcpServerConnection, Context.BIND_AUTO_CREATE);
        status("connected");
        connected = Connected.True;
        if(listener != null) {
            listener.onSerialConnect();
        }
        Log.v(TAG, "onSerialConnect end");
    }

    public void reconnect() {
        disconnect();
        // Даже если BLE устройство выключено, исключения тут не будет. Но позже возникнет ошибка, которая опять приведёт нас сюда...
        connect();
    }

    @Override
    public void onSerialConnectError(Exception e) {
        Log.v(TAG, "onSerialConnectError begin");
        status("connection failed: " + e.getMessage());
        reconnect();
        Log.v(TAG, "onSerialConnectError end");
    }

    @Override
    public void onSerialRead(byte[] data) {
        Log.v(TAG, "onSerialRead begin");
        if(listener != null) {
            listener.onSerialRead(data);
        }
        Log.v(TAG, "onSerialRead end");
    }
    @Override
    public void onSerialIoError(Exception e) {
        Log.v(TAG, "onSerialIoError begin");
        status("connection lost: " + e.getMessage());
        reconnect();
        Log.v(TAG, "onSerialIoError end");
    }
}