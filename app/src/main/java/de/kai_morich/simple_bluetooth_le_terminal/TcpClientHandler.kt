package de.kai_morich.simple_bluetooth_le_terminal

import android.content.Context
import android.util.Log
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import android.os.Build
import android.os.Vibrator
import android.os.VibrationEffect

class TcpClientHandler(private val dataInputStream: DataInputStream, private val dataOutputStream: DataOutputStream) : Thread() {
    override fun run() {
        while (true) {
            try {
                if(dataInputStream.available() > 0){
                    //Log.i(TAG, "Received: " + dataInputStream.readUTF())
                    //dataOutputStream.writeUTF("Hello Client")
                    //sleep(2000L)

                    var b = dataInputStream.readByte()
                    var t = 'v'.toByte()
                    if (b == t)
                    {
                        val vibrator = ctx.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                        val canVibrate: Boolean = vibrator.hasVibrator()
                        val milliseconds = 1000L

                        if (canVibrate) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                // API 26
                                vibrator.vibrate(
                                    VibrationEffect.createOneShot(
                                        milliseconds,
                                        VibrationEffect.DEFAULT_AMPLITUDE
                                    )
                                )
                            } else {
                                // This method was deprecated in API level 26
                                vibrator.vibrate(milliseconds)
                            }
                        }
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                try {
                    dataInputStream.close()
                    dataOutputStream.close()
                } catch (ex: IOException) {
                    ex.printStackTrace()
                }
            } catch (e: InterruptedException) {
                e.printStackTrace()
                try {
                    dataInputStream.close()
                    dataOutputStream.close()
                } catch (ex: IOException) {
                    ex.printStackTrace()
                }
            }
        }
    }

    companion object {
        private val TAG = TcpClientHandler::class.java.simpleName
        @JvmStatic lateinit var ctx: Context
    }

}