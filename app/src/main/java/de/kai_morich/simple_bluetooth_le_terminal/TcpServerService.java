package de.kai_morich.simple_bluetooth_le_terminal;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.graphics.Color;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.Build.VERSION;
import android.util.Log;
import androidx.core.app.NotificationCompat.Builder;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

public class TcpServerService extends Service implements SerialListener {
    class TcpServerBinder extends Binder {
        TcpServerService getService() { return TcpServerService.this; }
    }

    private ServerSocket serverSocket;
    private AtomicBoolean working = new AtomicBoolean(true);
    private TcpClientPool tcpool = new TcpClientPool();
    private final IBinder binder = new TcpServerService.TcpServerBinder();

    private Runnable runnable = (Runnable)(new Runnable() {
        public void run() {
            Socket socket = (Socket)null;
            SharedPreferences sharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String sPort = sharedPreferences.getString("listen_port", "3827");
            int port = Integer.parseInt(sPort);

            try {
                serverSocket = new ServerSocket(port);

                while(working.get()) {
                    if (serverSocket != null) {
                        socket = serverSocket.accept();
                        //Log.i(TcpServerService.TAG, "New client: " + socket);
                        TcpClientHandler t = new TcpClientHandler(tcpool, socket);
                        tcpool.add(t);
                        t.start();
                    } else {
                        //Log.e(TcpServerService.TAG, "Couldn't create ServerSocket!");
                    }
                } // while(working.get())
            } catch (IOException e) {
                e.printStackTrace();

                try {
                    if (socket != null) {
                        socket.close();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

        }
    });
    //private static final String TAG;
    private static final int PORT = 9876;

    //public static final TcpServerService.Companion Companion = new TcpServerService.Companion((DefaultConstructorMarker)null);

    public final void write(byte[] ba) throws IOException {
        tcpool.write(ba);
    }

    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void onCreate() {
        this.startMeForeground();
        (new Thread(runnable)).start();
    }

    public void onDestroy() {
        working.set(false);
        try {
            serverSocket.close();
        } catch(IOException e){
            //
        }
    }

    private final void startMeForeground() {
        if (VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String NOTIFICATION_CHANNEL_ID = this.getPackageName();
            String channelName = "Tcp Server Background Service";
            NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, (CharSequence)channelName, NotificationManager.IMPORTANCE_NONE);
            chan.setLightColor(Color.BLUE);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(chan);
            Builder notificationBuilder = new Builder((Context)this, NOTIFICATION_CHANNEL_ID);
            Notification notification = notificationBuilder.setOngoing(true)
                    .setContentTitle((CharSequence)"Tcp Server is running in background")
                    .setPriority(NotificationManager.IMPORTANCE_MIN)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .build();
            startForeground(2, notification);
        } else {
            startForeground(1, new Notification());
        }
    }

    /*
     * SerialListener
     */
    @Override
    public void onSerialConnect() {
    }

    @Override
    public void onSerialConnectError(Exception e) {
        tcpool.drop();
    }

    @Override
    public void onSerialRead(byte[] data) {
        try {
            write(data);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onSerialIoError(Exception e) {
        tcpool.drop();
    }
}
