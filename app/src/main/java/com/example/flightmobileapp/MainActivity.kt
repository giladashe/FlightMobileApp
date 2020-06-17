package com.example.flightsimulatorandroidapp

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.flightmobileapp.R
import com.example.flightmobileapp.Server
import com.example.flightmobileapp.ServersDataBase
import kotlinx.coroutines.*
import java.util.stream.Collectors.toList

class MainActivity : AppCompatActivity() {
    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    // override fun on
    private var db: ServersDataBase? = null
    private var servers: List<Server>? = null
    private lateinit var button1: Button
    private lateinit var button2: Button
    private lateinit var button3: Button
    private lateinit var button4: Button
    private lateinit var button5: Button
    private lateinit var urlText: TextView
    private lateinit var buttonMap: Map<Int, Button>


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
            if (servers != null && servers!!.isNotEmpty()) {
                val size: Int = servers!!.size
                var i = 0
                while (i < size) {
                    buttonMap[i]!!.text = servers!![i].url
                    buttonMap[i]!!.id = servers!![i].serverId.toInt()
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

    }

    suspend fun getServers(): List<Server>? {
        var servers: List<Server>?
        withContext(Dispatchers.IO) {
            // db!!.serversDataBaseDao.insert(Server(url = "https://localhost:5001"))
            //db?.serversDataBaseDao?.nukeTable()
            servers = db!!.serversDataBaseDao.getFirstFive()
        }
        return servers
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
        val text: String = urlText.text.toString()
        if (text != "") {
            val id: Long? = buttonHasText(text)
            uiScope.launch {
                updateDB(id, text)
            }
        }
        finish();
        startActivity(intent);
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

    suspend fun updateDB(id: Long?, newUrl: String) {
        withContext(Dispatchers.IO) {
            if (id != null) {
                db?.serversDataBaseDao?.updateServer(id, System.currentTimeMillis())
            } else {
                db?.serversDataBaseDao?.insert(Server(url = newUrl))
            }
        }
    }
}


