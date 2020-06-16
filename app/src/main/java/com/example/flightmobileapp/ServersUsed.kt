package com.example.flightmobileapp

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "servers_used")
data class Server(
    @PrimaryKey(autoGenerate = true)
    var serverId: Long = 0L,
    @ColumnInfo(name = "time_used")
    var time: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "ip")
    var url: String = "127.0.0.1"
)
