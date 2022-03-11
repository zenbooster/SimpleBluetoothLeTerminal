package de.kai_morich.simple_bluetooth_le_terminal;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.Build.VERSION;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import kotlin.Metadata;
import kotlin.jvm.JvmStatic;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

public class TcpClientHandler extends Thread {
    private TcpClientPool tcpool;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    public static Context ctx;

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
        //dataOutputStream.write(ba);
    }

    @Override
    public void run() {
        try {
            while(true) {
                if (dataInputStream.available() == 0) {
                    continue;
                }

                if (this.dataInputStream.readByte() != 'v') {
                    continue;
                }

                Vibrator vibrator = (Vibrator)ctx.getSystemService(Context.VIBRATOR_SERVICE);
                boolean canVibrate = vibrator.hasVibrator();
                if (!canVibrate) {
                    continue;
                }

                long milliseconds = 250L;

                if (VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE));
                }
                else {
                    vibrator.vibrate(milliseconds);
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
