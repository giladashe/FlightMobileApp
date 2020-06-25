package com.example.flightmobileapp


import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.AlarmClock.EXTRA_MESSAGE
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import kotlinx.coroutines.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit


class MainActivity : AppCompatActivity() {

    //todo put screenshot code in other activity


    companion object {
        private const val maxSize = 5
        private const val httpStr: String = "http://"
    }

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
    private lateinit var buttonList: List<Button>
    private var biggestId: Long = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        button1 = findViewById(R.id.url1)
        button2 = findViewById(R.id.url2)
        button3 = findViewById(R.id.url3)
        button4 = findViewById(R.id.url4)
        button5 = findViewById(R.id.url5)
        urlText = findViewById(R.id.inputURL)
        buttonList = listOf(button1, button2, button3, button4, button5)
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
            var id: Long
            for ((i, server) in servers!!.withIndex()) {
                buttonList[i].text = server.url
                id = server.serverId
                buttonList[i].id = id.toInt()
                if (id > biggestId) {
                    biggestId = id
                }
            }

/*

            val size: Int = servers!!.size
            var i = 0
            while (i < size) {
                buttonList[i].text = servers!![i].url
                id = servers!![i].serverId
                buttonList[i].id = id.toInt()
                if (id > biggestId) {
                    biggestId = id
                }
                i++
            }
*/
        }
    }

    private suspend fun getServers(): MutableList<Server>? {
        var servers: List<Server>?
        withContext(Dispatchers.IO) {
            //db?.serversDataBaseDao?.nukeTable()
            servers = db!!.serversDataBaseDao.getFirstFive()
        }
        return servers?.toMutableList()
    }


    fun urlButtonClicked(view: View) {
        var b: Button? = null
        for (button in buttonList) {
            if (view.id == button.id) {
                b = button
                break
            }
        }
        if (b != null && b.text.isNotEmpty()) {
            urlText.text = b.text
        }
    }


    fun connectClicked(view: View) {
        val url: String = urlText.text.toString()
        if (url.isNotEmpty()) {
            addServerToList(url)
            updateButtons()
            checkIfCanConnect(url)
        }
    }

    private fun addServerToList(url: String) {
        val id: Long? = buttonHasText(url)
        var size: Int = servers!!.size
        if (id != null) {
            updateServersList(id)
        } else {
            biggestId += 1
            servers?.add(0, Server(serverId = biggestId, url = url))
            size++
            //todo make variable max size
            if (size > maxSize) {
                servers?.removeAt(maxSize)
            }
        }
    }


    private fun updateServersList(id: Long?) {
        if (id == null)
            return

        for (server in servers!!) {
            if (server.serverId == id) {
                val removed: Boolean? = servers?.remove(server)
                if (servers != null && removed!!) {
                    server.time = System.currentTimeMillis()
                    servers?.add(0, server)
                }
                break
            }
        }
    }


    private fun checkIfCanConnect(url: String) {
        try {
            var formattedUrl:String = url
            if(!url.startsWith(httpStr)){
                formattedUrl = "$httpStr$url"
            }
            val retrofit = Retrofit.Builder().baseUrl(formattedUrl).build();

            //Defining the api for sending by the request
            val api = retrofit.create(Api::class.java)

            //Sending the request
            api.getScreenShot().enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.message() == "OK") {
                        println("Successfully got screenshot")
                        /*val imageStream = response.body()?.byteStream()
                        val theImage = BitmapFactory.decodeStream(imageStream)
                        runOnUiThread {
                            //todo change to the id of imageview
                            // X.setImageBitmap(theImage)
                        }*/
                        launchGame(formattedUrl)
                        //println(response.body()?.string())
                    } else {
                        //todo write "Error connecting"
                        println("Failed to get screenshot - on response")

                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    //todo write "Error connecting"
                    val text = "Failed to get screenshot - invalid url"
                    val duration = Toast.LENGTH_SHORT

                    val toast = Toast.makeText(applicationContext, text, duration)
                    toast.setGravity(Gravity.BOTTOM or Gravity.START, 100, 250)
                    toast.show()
                    println("Failed to get screenshot - on failure")
                }
            })
        } catch (e: Exception) {
            //todo write "Error connecting"
            val text = "Error connecting"
            val duration = Toast.LENGTH_SHORT

            val toast = Toast.makeText(applicationContext, text, duration)
            toast.setGravity(Gravity.BOTTOM or Gravity.START, 100, 250)
            toast.show()
            //println("Failed to get screenshot - invalid url")
        }
    }


    private fun launchGame(url: String) {
        val intent = Intent(this, gameScreen::class.java)
        intent.putExtra(EXTRA_MESSAGE, url)
        startActivity(intent)
    }


    private fun buttonHasText(text: String): Long? {
        for ((i, button) in buttonList.withIndex()) {
            if (text == button.text) {
                return servers?.get(i)?.serverId
            }
        }
        return null
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
                for (server in servers!!) {
                    val serverInDB: Server? = db?.serversDataBaseDao?.getServer(server.serverId)
                    insertToDB(serverInDB, server.url, server.time)
                }
            }
        }
    }


}


