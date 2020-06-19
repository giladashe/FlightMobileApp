package com.example.flightsimulatorandroidapp


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.provider.AlarmClock.EXTRA_MESSAGE
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.example.flightmobileapp.*
import kotlinx.coroutines.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit


class MainActivity : AppCompatActivity() {
    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    // override fun on
    private var db: ServersDataBase? = null
    private var servers: MutableList<Server>? = null
    private lateinit var button1: Button
    private lateinit var button2: Button
    private lateinit var button3: Button
    private lateinit var button4: Button
    private lateinit var button5: Button
    private lateinit var urlText: TextView
    private lateinit var buttonMap: Map<Int, Button>
    private var biggestId: Long = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        button1 = findViewById<Button>(R.id.url1)
        button2 = findViewById<Button>(R.id.url2)
        button3 = findViewById<Button>(R.id.url3)
        button4 = findViewById<Button>(R.id.url4)
        button5 = findViewById<Button>(R.id.url5)
        urlText = findViewById<TextView>(R.id.inputURL)
        buttonMap = mapOf(0 to button1, 1 to button2, 2 to button3, 3 to button4, 4 to button5)
        db = ServersDataBase.getInstance(context = this)
    }

    override fun onStart() {
        super.onStart()
        uiScope.launch {
            servers = getServers()
            updateButtons()
        }

    }

    private fun updateButtons() {
        if (servers != null && servers!!.isNotEmpty()) {
            val size: Int = servers!!.size
            var i = 0
            var id: Long
            while (i < size) {
                buttonMap[i]!!.text = servers!![i].url
                id = servers!![i].serverId
                buttonMap[i]!!.id = id.toInt()
                if (id > biggestId) {
                    biggestId = id
                }
                i++
            }
            if (size != 5) {
                while (i < 5) {
                    buttonMap[i]!!.text = "Empty slot"
                    i++
                }
            }
        }
    }

    private suspend fun getServers(): MutableList<Server>? {
        var servers: List<Server>?
        withContext(Dispatchers.IO) {
            // db!!.serversDataBaseDao.insert(Server(url = "https://localhost:5001"))
            //db?.serversDataBaseDao?.nukeTable()
            servers = db!!.serversDataBaseDao.getFirstFive()
        }
        return servers?.toMutableList()
    }


    fun urlButtonClicked(view: View) {
        when (view.id) {
            button1.id -> if (button1.text != "Empty slot") {
                urlText.text = button1.text
            }
            button2.id -> if (button2.text != "Empty slot") {
                urlText.text = button2.text
            }
            button3.id -> if (button3.text != "Empty slot") {
                urlText.text = button3.text
            }
            button4.id -> if (button4.text != "Empty slot") {
                urlText.text = button4.text
            }
            button5.id -> if (button5.text != "Empty slot") {
                urlText.text = button5.text
            }
        }
    }


    // suspend is not allowed.... because it doesn't override
    fun connectClicked(view: View) {
        val url: String = urlText.text.toString()
        if (url != "") {
            val id: Long? = buttonHasText(url)
            var size: Int = servers!!.size
            var i = 0
            if (id != null) {
                while (i < size) {
                    if (servers!![i].serverId == id) {
                        val server: Server? = servers?.removeAt(i)
                        if (servers != null && server != null) {
                            server.time = System.currentTimeMillis()
                            servers?.add(0, server)
                        }
                        break
                    }
                    i++
                }
            } else {
                biggestId += 1
                servers?.add(0, Server(serverId = biggestId, url = url))
                size++
                if (size > 5) {
                    servers?.removeAt(5)
                }
            }
            updateButtons()
            val retrofit = Retrofit.Builder()
                .baseUrl(url) //todo: change that will enable dynamic ip and port
                .build();

            //Defining the api for sending by the request
            val api = retrofit.create(Api::class.java)

            //Sending the request
            val body = api.getScreenShot().enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.message() == "OK") {
                        println("Successfully got screenshot")
                        launchGame(url)
                        //println(response.body()?.string())
                    } else {
                        println("Failed to get screenshot")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    println("Failed to get screenshot")
                }
            })
        }

    }

    private fun launchGame(url:String) {
        val intent = Intent(this, gameScreen::class.java)
        intent.putExtra(EXTRA_MESSAGE, url)
        startActivity(intent)
    }



    private fun buttonHasText(text: String): Long? {
        return when (text) {
            button1.text -> servers?.get(0)?.serverId
            button2.text -> servers?.get(1)?.serverId
            button3.text -> servers?.get(2)?.serverId
            button4.text -> servers?.get(3)?.serverId
            button5.text -> servers?.get(4)?.serverId
            else -> null
        }
    }


    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
    }

    override fun onStop() {
        super.onStop()
        uiScope.launch {
            updateDB()
        }
    }


    private suspend fun insertToDB(server: Server?, newUrl: String, theTime: Long) {
        withContext(Dispatchers.IO) {
            if (server != null) {
                db?.serversDataBaseDao?.updateServer(id = server.serverId, time = theTime)
            } else {
                db?.serversDataBaseDao?.insert(Server(time = theTime, url = newUrl))
            }
        }
    }

    private suspend fun updateDB() {
        withContext(Dispatchers.IO) {
            if (servers != null && servers!!.isNotEmpty()) {
                val size: Int = servers!!.size
                var i = 0
                while (i < size) {
                    val server: Server? = db?.serversDataBaseDao?.getServer(servers!![i].serverId)
                    insertToDB(server, servers!![i].url, servers!![i].time)
                    i++
                }
            }
        }
    }
}


