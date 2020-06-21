package com.example.flightmobileapp

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(entities = [Server::class], version = 1, exportSchema = false)
abstract class ServersDataBase : RoomDatabase() {
    abstract val serversDataBaseDao: ServersDataBaseDao

    companion object {
        @Volatile
        private var INSTANCE: ServersDataBase? = null
        fun getInstance(context: Context): ServersDataBase? {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = newDatabase(context)
                }
                INSTANCE = instance
                return instance
            }
        }

        private fun newDatabase(context: Context): ServersDataBase {
            return Room.databaseBuilder(
                context.applicationContext,
                ServersDataBase::class.java,
                "servers_database"
            )
                .fallbackToDestructiveMigration().build()
        }
    }
}
