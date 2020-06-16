package com.example.flightmobileapp

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ServersDataBaseDao {
    // also works auto-magically: @Update, @Delete
    @Insert
    fun insert(server: Server)

    @Query("SELECT * from servers_used" +
            " ORDER BY time_used DESC LIMIT 5")
    fun getFirstFive(): List<Server>?

    @Update
    fun updateServer(server: Server)


}