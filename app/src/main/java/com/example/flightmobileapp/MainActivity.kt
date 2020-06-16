package com.example.flightsimulatorandroidapp

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.flightmobileapp.R
import com.example.flightmobileapp.Server
import com.example.flightmobileapp.ServersDataBase
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        db = ServersDataBase.getInstance(context = this)
        /*uiScope.launch {
            servers = getServers()
        }*/
        println("wow")
    }

    override fun onStart() {
        super.onStart()
    }

    suspend fun getServers(): List<Server>? {
        var servers : List<Server>? = null
        withContext(Dispatchers.IO) {
            db!!.serversDataBaseDao.insert(Server())
            servers = db!!.serversDataBaseDao.getFirstFive()
        }
        return servers
    }
    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
   // override fun on
    private var db: ServersDataBase? = null
    private var servers: List<Server>? = null
    private lateinit var button1: Button
    private lateinit var button2: Button
    private lateinit var button3: Button
    private lateinit var button4: Button
    private lateinit var button5: Button

    private fun doneClicked(view: View) {
        button1 = view.findViewById<Button>(R.id.url1)

    }
}


