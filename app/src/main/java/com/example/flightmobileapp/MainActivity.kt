package com.example.flightmobileapp


import android.content.Context
import android.content.Intent
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

    // We use the toastMessage here and in the game screen
    companion object {
        private const val MAX_SIZE_URLS = 5
        private const val HTTP_STR: String = "http://"

        //toast message to the screen
        fun toastMessage(context: Context, messageText: String) {
            val duration = Toast.LENGTH_SHORT
            val toast = Toast.makeText(context, messageText, duration)
            //todo where????
            toast.setGravity(Gravity.BOTTOM or Gravity.START, 100, 250)
            toast.show()
        }
    }

    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    // buttons, db and servers
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

    //creates the ui elements and put them in variables
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
        //get servers from db and update the buttons
        uiScope.launch {
            servers = getServers()
            updateButtons()
        }
    }

    // Put urls of servers on the buttons.
    private fun updateButtons() {
        if (servers != null && servers!!.isNotEmpty()) {
            var id: Long
            for ((i, server) in servers!!.withIndex()) {
                buttonList[i].text = server.url
                id = server.serverId
                buttonList[i].id = id.toInt()
                //maintain biggest id to give unique id for user's input server
                if (id > biggestId) {
                    biggestId = id
                }
            }
        }
    }

    // Get 5 servers from DB that are most recently used.
    private suspend fun getServers(): MutableList<Server>? {
        var servers: List<Server>?
        withContext(Dispatchers.IO) {
            //db?.serversDataBaseDao?.nukeTable()
            servers = db!!.serversDataBaseDao.getFirstFive()
        }
        return servers?.toMutableList()
    }

    // If url in button is not empty it copies it to the url text box at the bottom.
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

    // Connect button was pressed - if there is url in the input box it updates the servers
    // list, updates the buttons and tries to connect.
    fun connectClicked(view: View) {
        val url: String = urlText.text.toString()
        if (url.isNotEmpty()) {
            addServerToList(url)
            updateButtons()
            checkIfCanConnect(url)
        }
    }

    // Add new server to servers list with unique id (biggest id +1).
    private fun addServerToList(url: String) {
        val id: Long? = buttonHasText(url)
        var size: Int = servers!!.size
        if (id != null) {
            updateServersList(id)
        } else {
            biggestId += 1
            servers?.add(0, Server(serverId = biggestId, url = url))
            size++
            if (size > MAX_SIZE_URLS) {
                servers?.removeAt(MAX_SIZE_URLS)
            }
        }
    }

    // Update servers list - put the server we are trying to connect to at the beginning.
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

    // Tries to get screenshot from server - if succeed go to game screen
    private fun checkIfCanConnect(url: String) {
        try {
            // Adds http prefix if it's not url's prefix
            var formattedUrl: String = url
            if (!url.startsWith(HTTP_STR)) {
                formattedUrl = "$HTTP_STR$url"
            }
                // Tries to build retrofit with url - if didn't succeed toasts "Error connecting".
            val retrofit = Retrofit.Builder().baseUrl(formattedUrl).build();
            // Defining the api for sending by the request
            val api:Api = retrofit.create(Api::class.java)
            // The actual check
            checkConnectionAsync(api,formattedUrl)

        } catch (e: Exception) {
            toastMessage(applicationContext,"Error connecting to server")
        }
    }

    private fun checkConnectionAsync(api:Api,formattedUrl: String) {
        CoroutineScope(Dispatchers.IO).launch {
            // Sending the request
            api.getScreenShot().enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    // We got a good response - a screenshot so we go to game screen
                    if (response.message() == "OK") {
                        launchGame(formattedUrl)
                    } else {
                        //we got a bad response - we don't connect
                        toastMessage(applicationContext,"Error connecting to server")
                    }
                }
                // We didn't get any response - we don't connect
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    toastMessage(applicationContext,"Error connecting to server")
                }
            })
        }
    }

    // Launch the game screen and pass the url to it.
    private fun launchGame(url: String) {
        val intent = Intent(this, GameScreen::class.java)
        intent.putExtra(EXTRA_MESSAGE, url)
        startActivity(intent)
    }

    // Returns id of the button if it has the text on it - otherwise returns null.
    private fun buttonHasText(text: String): Long? {
        for ((i, button) in buttonList.withIndex()) {
            if (text == button.text) {
                return servers?.get(i)?.serverId
            }
        }
        return null
    }

    // When the app stops we update the DB with our servers list.
    override fun onStop() {
        super.onStop()
        uiScope.launch {
            updateDB()
        }
    }

    // Insert server to DB - update if it was there already.
    private suspend fun insertToDB(server: Server?, newUrl: String, theTime: Long) {
        withContext(Dispatchers.IO) {
            if (server != null) {
                db?.serversDataBaseDao?.updateServer(id = server.serverId, time = theTime)
            } else {
                db?.serversDataBaseDao?.insert(Server(time = theTime, url = newUrl))
            }
        }
    }

    // Update the DB with all the servers in the list.
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


