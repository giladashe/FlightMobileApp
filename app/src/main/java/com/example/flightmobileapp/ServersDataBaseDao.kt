package com.example.flightmobileapp

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ServersDataBaseDao {
    // also works auto-magically: @Update, @Delete
    @Insert
    fun insert(server: Server)

    @Query("SELECT distinct * from servers_used ORDER BY time_used DESC LIMIT 5")
    fun getFirstFive(): List<Server>?

    @Query("UPDATE servers_used SET time_used = :time WHERE serverId = :id")
    fun updateServer(id: Long, time: Long)

    @Query("DELETE FROM servers_used")
    fun nukeTable()


}