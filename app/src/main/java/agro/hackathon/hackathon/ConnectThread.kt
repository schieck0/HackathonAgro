package agro.hackathon.hackathon

import android.bluetooth.BluetoothSocket
import android.bluetooth.BluetoothDevice
import java.io.IOException
import java.util.*
import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothAdapter
import android.os.Bundle
import android.os.Message
import java.io.InputStream
import java.io.OutputStream
import java.lang.Exception


class ConnectThread(private val mmDevice: BluetoothDevice, private val main: MainActivity) : Thread() {
    private val mmSocket: BluetoothSocket?
    val myUUID = "00001101-0000-1000-8000-00805F9B34FB"
    var output: OutputStream? = null
    var input: InputStream? = null

    init {
        // Use a temporary object that is later assigned to mmSocket,
        // because mmSocket is final
        var socket: BluetoothSocket? = null

        // Get a BluetoothSocket to connect with the given BluetoothDevice
        try {
            // MY_UUID is the app's UUID string, also used by the server code
            socket = mmDevice.createRfcommSocketToServiceRecord(UUID.fromString(myUUID))
            //BluetoothAdapter.getDefaultAdapter().cancelDiscovery();

        } catch (e: IOException) {
            e.printStackTrace()
        }

        mmSocket = socket
    }

    override fun run() {
        // Cancel discovery because it will slow down the connection
        //mBluetoothAdapter.cancelDiscovery()

        try {
            // Connect the device through the socket. This will block
            // until it succeeds or throws an exception
            println("Conectando...")
            mmSocket!!.connect()
            output = mmSocket.outputStream;
            input = mmSocket.inputStream
            Thread() {
                run() {
                    var erro = false
                    while (mmSocket!!.isConnected) {
                        var bytes = input!!.read()
                        val readMessage = input!!.read()
                        println(readMessage)
                        if (readMessage==0 && !erro) {
                            println("ERR")
                            erro = true;
                            toMainActivity("ERRO")
                        }else if (readMessage!=0) {
                            erro = false
                        }
                    }
                }
            }.start()
            toMainActivity("CONN")
            println("FEITOOOOOOOOOOOOOOOOOOOOO")
        } catch (connectException: IOException) {
            // Unable to connect; close the socket and get out
            try {
                mmSocket!!.close()
            } catch (closeException: IOException) {
            }

            return
        }

        // Do work to manage the connection (in a separate thread)
        //manageConnectedSocket(mmSocket)
    }

    /** Will cancel an in-progress connection, and close the socket  */
    fun cancel() {
        try {
            mmSocket!!.close()
        } catch (e: IOException) {
        }

    }

    private fun toMainActivity(data: String) {

        val message = Message()
        val bundle = Bundle()
        bundle.putByteArray("data", data.toByteArray())
        message.setData(bundle)
        main.handler.sendMessage(message)
    }

    fun senMsg(msg: String) {
        if (output != null) {
            try {
                println("->$msg")
                if (msg.contains("desligar")) {
                    output!!.write("0".toByteArray())
                    toMainActivity("DESLIGAR")
                } else if (msg.contains("ligar")) {
                    output!!.write("1".toByteArray())
                    toMainActivity("LIGAR")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


}