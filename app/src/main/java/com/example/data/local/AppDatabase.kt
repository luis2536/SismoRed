package com.example.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.data.local.dao.ChatDao
import com.example.data.local.dao.ResqReportDao
import com.example.data.local.dao.UserDao
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.ResqReportEntity
import com.example.data.local.entity.UserEntity

@Database(entities = [ResqReportEntity::class, ChatMessageEntity::class, UserEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun resqReportDao(): ResqReportDao
    abstract fun chatDao(): ChatDao
    abstract fun userDao(): UserDao
}
