package de.kai_morich.simple_bluetooth_le_terminal

import android.content.Context
import android.util.Log
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import android.os.Build
import android.os.Vibrator
import android.os.VibrationEffect
import java.lang.Exception
import java.lang.ThreadDeath

class TcpClientHandler(private val ThreadsHashSet: java.util.HashSet<Thread>, private val dataInputStream: DataInputStream, private val dataOutputStream: DataOutputStream) : Thread() {
    private val ths: java.util.HashSet<Thread> = ThreadsHashSet

    private fun done(e: Exception) {
        e.printStackTrace()
        try {
            dataInputStream.close()
            dataOutputStream.close()
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }

    override fun run() {
        while (true) {
            try {
                if(dataInputStream.available() > 0){
                    //Log.i(TAG, "Received: " + dataInputStream.readUTF())
                    //dataOutputStream.writeUTF("Hello Client")
                    //sleep(2000L)

                    var b = dataInputStream.readByte()
                    //var t = 'v'.toByte()
                    var t = 'v'.code.toByte()
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
                done(e)
                break
            } catch (e: InterruptedException) {
                /*e.printStackTrace()
                try {
                    dataInputStream.close()
                    dataOutputStream.close()
                } catch (ex: IOException) {
                    ex.printStackTrace()
                }
                */
                done(e)
                break
            }/* catch(e: ThreadDeath) {
                /*try {
                    dataInputStream.close()
                    dataOutputStream.close()
                } catch (ex: IOException) {
                    ex.printStackTrace()
                }*/
                done(e)
                break
            }*/
        }
        ths.remove(this)
    }

    /*override fun destroy() {
        ths.remove(this)
        super.destroy()
    }*/

    companion object {
        private val TAG = TcpClientHandler::class.java.simpleName
        @JvmStatic lateinit var ctx: Context
    }

}