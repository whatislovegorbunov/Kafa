package com.example.kafa.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ReminderDeliveryDao {
    @Query("SELECT * FROM reminder_delivery WHERE eventId = :eventId")
    suspend fun find(eventId: String): ReminderDeliveryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(delivery: ReminderDeliveryEntity)
}
