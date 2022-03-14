package de.kai_morich.simple_bluetooth_le_terminal;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.Build.VERSION;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import android.util.Base64;

public class TcpClientHandler extends Thread {
    private TcpClientPool tcpool;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    public static Context ctx;
    public LinkedList<byte[]> writeQueue = new LinkedList<byte[]>();

    private void done(Exception e) {
        e.printStackTrace();

        try {
            dataInputStream.close();
            dataOutputStream.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void write(byte[] ba) throws IOException {
        writeQueue.add(ba);
    }

    @Override
    public void run() {
        try {
            while(true) {
                if (dataInputStream.available() > 0) {
                    if (this.dataInputStream.readByte() != 'v') {
                        continue;
                    }

                    Vibrator vibrator = (Vibrator) ctx.getSystemService(Context.VIBRATOR_SERVICE);
                    boolean canVibrate = vibrator.hasVibrator();
                    if (!canVibrate) {
                        continue;
                    }

                    long milliseconds = 250L;

                    if (VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {
                        vibrator.vibrate(milliseconds);
                    }
                } // if (dataInputStream.available() > 0)

                if(writeQueue.size() > 0)
                {
                    byte [] ba = writeQueue.remove();
                    dataOutputStream.write(String.format("%02X", ba.length).getBytes());
                    dataOutputStream.write(Base64.encode(ba, Base64.NO_WRAP));
                }
            } // while(true)
        } catch (IOException ex) {
            this.done(ex);
        }
        tcpool.remove(this);
    }

    public TcpClientHandler(TcpClientPool tcpClientPool, DataInputStream dataInputStream, DataOutputStream dataOutputStream) {
        super();
        this.tcpool = tcpClientPool;
        this.dataInputStream = dataInputStream;
        this.dataOutputStream = dataOutputStream;
    }

    public static Context getCtx() {
        return ctx;
    }

    public static  void setCtx(Context ctx) {
        TcpClientHandler.ctx = ctx;
    }
}
