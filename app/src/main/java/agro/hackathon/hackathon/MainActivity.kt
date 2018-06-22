package agro.hackathon.hackathon

import android.app.Activity
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import android.widget.ImageButton
import android.widget.Toast
import android.speech.RecognizerIntent
import android.content.Intent
import java.util.*
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.graphics.Color
import android.os.Handler
import android.os.Message
import android.view.Gravity
import android.widget.TextView
import android.speech.tts.TextToSpeech


class MainActivity : AppCompatActivity() {

    private val VOICE_RECOGNITION_REQUEST_CODE = 1001

    private var btnSpeak: ImageButton? = null
    private var txtConn: TextView? = null

    private var connectThread: ConnectThread? = null

    private var ttobj: TextToSpeech? = null

    val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                //message.setText(R.string.title_home)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_dashboard -> {
                //message.setText(R.string.title_dashboard)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_notifications -> {
                //message.setText(R.string.title_notifications)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        txtConn = findViewById(R.id.txtConn) as TextView
        btnSpeak = findViewById(R.id.btnSpeak) as ImageButton
        btnSpeak!!.setOnClickListener {
            Toast.makeText(this@MainActivity, "Fale Agora!", Toast.LENGTH_SHORT).show()
            val speakIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            speakIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            speakIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())

            if (speakIntent.resolveActivity(packageManager) != null) {
                startActivityForResult(speakIntent, 10)
            } else {
                Toast.makeText(this, "Your Device Don't Support Speech Input", Toast.LENGTH_SHORT).show()
            }
        }

        ttobj = TextToSpeech(applicationContext, TextToSpeech.OnInitListener {
            if (it != TextToSpeech.ERROR) {
                ttobj!!.setLanguage(Locale.getDefault());
            }
        })

        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "N suporta", Toast.LENGTH_SHORT).show()
        } else {
            if (!mBluetoothAdapter.isEnabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, 99)
            } else {
                connectBT()
            }
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        println(requestCode)
        when (requestCode) {
            10 -> if (resultCode == Activity.RESULT_OK && data != null) {
                val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                sendCommand(result[0])
                //Toast.makeText(this, result[0], Toast.LENGTH_SHORT).show()
            }

            99 -> if (resultCode == Activity.RESULT_OK) {
                connectBT()
            }
        }
    }

    fun sendCommand(cmd: String) {
        connectThread!!.senMsg(cmd)
    }

    fun connectBT() {
        val pairedDevices = mBluetoothAdapter.getBondedDevices()
// If there are paired devices
        if (pairedDevices.size > 0) {
            // Loop through paired devices
            for (device in pairedDevices) {
                println(device.getName() + "--------->" + device.getAddress())
                if (device.name == "HC-06") {
                    connectThread = ConnectThread(device, this)
                    connectThread!!.start()
                    break
                }
                // Add the name and address to an array adapter to show in a ListView
                // mArrayAdapter.add(device.getName() + "\n" + device.getAddress())
            }
        }
    }

    var handler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {

            /* Esse método é invocado na Activity principal
                sempre que a thread de conexão Bluetooth recebe
                uma mensagem.
             */
            val bundle = msg.getData()
            val data = bundle.getByteArray("data")
            val dataString = String(data!!)

            /* Aqui ocorre a decisão de ação, baseada na string
                recebida. Caso a string corresponda à uma das
                mensagens de status de conexão (iniciadas com --),
                atualizamos o status da conexão conforme o código.
             */
            if (dataString == "CONN") {
                txtConn!!.setText("Conectado!")
            }

            if (dataString == "ERRO") {
                Toast.makeText(getApplicationContext(), "TRETA", Toast.LENGTH_SHORT).show()
                ttobj!!.speak("Ocorreu alguma treta!", TextToSpeech.QUEUE_FLUSH, null, null);
            }

            if (dataString == "LIGAR") {
                ttobj!!.speak("Ligando a máquina!", TextToSpeech.QUEUE_FLUSH, null, null);
            }

            if (dataString == "DESLIGAR") {
                ttobj!!.speak("Desligando a máquina!", TextToSpeech.QUEUE_FLUSH, null, null);
            }

            //else if (dataString == "---S")
            //   statusMessage.setText("Conectado :D")
        }

    }


}
