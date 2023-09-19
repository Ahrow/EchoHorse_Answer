package no.kristiania.echoworld

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.kristiania.echoworld.ui.theme.EchoWorldTheme
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Request.Builder
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.util.Timer
import java.util.TimerTask
import kotlin.concurrent.schedule
import kotlin.concurrent.timerTask


class MainActivity : ComponentActivity() {
    var client = OkHttpClient()
    var webSocket: WebSocket? = null
    var userInput by mutableStateOf("")
    var serverAnswer by mutableStateOf("")
    var timer : Timer? = null
    var isText by mutableStateOf(false)
    var imageID = 1
    var horse by mutableStateOf(resourceFromHorseID(imageID))

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val wss_url = "ws://192.168.0.109:8765"
        run(wss_url)


        setContent {
            var isButtonClicked by remember { mutableStateOf(false) }

            EchoWorldTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {


                        Spacer(modifier = Modifier.height(16.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateContentSize()
                        ) {
                            TextField(
                                value = userInput,
                                onValueChange = {
                                    userInput = it
                                    isButtonClicked = false
                                },
                                label = {
                                    Text("Enter a message")
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                            )

                            if (isButtonClicked) {
                                Text(
                                    text = serverAnswer,
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .background(color = Color.LightGray)
                                    .fillMaxWidth()
                                    .height(450.dp),
                                contentAlignment = Alignment.Center
                            ) {

                                when {
                                    isText -> {
                                        Text(
                                            text = serverAnswer,
                                            modifier = Modifier.padding(20.dp)
                                        )
                                    }
                                  null != horse -> {
                                      val horsy = if (serverAnswer == "") resourceFromHorseID(1)!! else serverAnswer.toInt()
                                        Image(
                                            painter = painterResource(horsy),
                                            contentDescription = "horse image",
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(20.dp)
                                        )
                                    }
                                    else -> {
                                        // No text or image to display
                                    }
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Button(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(8.dp),
                                    onClick = {
                                        //todo CAN BE IMG implement
                                        sendEverySeconds(userInput,1)
                                    }
                                ) {
                                    Text("REPEAT")
                                }

                                Button(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(8.dp),
                                    onClick = {
                                        cancelTimer()
                                        isText = !isText
                                        serverAnswer = ""
                                    }
                                ) {
                                    Text("IMG or TEXT")
                                }
                            }
                            Row (
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center){

                                Button(
                                    onClick = {
                                        val str = if (isText) userInput else horse.toString()
                                        Log.v("idiots", "hello $str")
                                        webSocket?.send(str)
                                        cancelTimer()
                                        incrementHorseImage()
                                    }
                                ) {
                                    Text("Send msg server")
                                }
                            }

                        }
                    }
                }
            }
        }
    }
    fun resourceFromHorseID(id: Int): Int? {
        when (id) {
            1 -> return R.drawable.horse01
            2 -> return R.drawable.horse02
            3 -> return R.drawable.horse03
            4 -> return R.drawable.horse04
            5 -> return R.drawable.horse05
            6 -> return R.drawable.horse06
            7 -> return R.drawable.horse07
            8 -> return R.drawable.horse08
            9 -> return R.drawable.horse09
            10 -> return R.drawable.horse10
            11 -> return R.drawable.horse11
            12 -> return R.drawable.horse12
            else -> { // Note the block
                return null
            }
        }
    }

    fun cancelTimer() {
        if(timer != null) {
            timer!!.cancel()
            timer = null
        }
    }

    fun sendEverySeconds(data: String, interval: Long) {
        if (timer != null) {
            timer!!.cancel()
        }
        timer = Timer()
    timer!!.scheduleAtFixedRate(timerTask {
            webSocket?.send(data)
            incrementHorseImage()
        }, interval * 1000 ,interval * 1000)
    }

    fun incrementHorseImage() {
        imageID = (imageID % 12) + 1
        horse = resourceFromHorseID(imageID)
    }


    fun run(url: String) {
        val request = Builder().url(url).build()
        val listener = EchoWebSocketListener(this)
        webSocket = OkHttpClient().newWebSocket(request, listener)
    }
}


private class EchoWebSocketListener(private val activity: MainActivity) : WebSocketListener() {
    override fun onOpen(webSocket: WebSocket, response: Response) {
        // Send the userInput to the WebSocket server
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        output( text!!)
    }

    // This will be unused in this assignment, but we'll leave it here
    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        output("Receiving bytes : " + bytes!!.hex())
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        webSocket!!.close(NORMAL_CLOSURE_STATUS, null)
        output("Closing : $code / $reason")
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        output("Error : " + t.message)
    }

    companion object {
        private val NORMAL_CLOSURE_STATUS = 1000
    }

    private fun output(txt: String) {
        Log.v("WSS", txt)
        activity.serverAnswer = txt
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "$name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    EchoWorldTheme {
        Greeting("Echo, World!")
    }
}
