package com.example.kafa.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [EventEntity::class, EventOccurrenceEntity::class, ReminderDeliveryEntity::class], version = 4, exportSchema = false)
abstract class EventDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
    abstract fun reminderDeliveryDao(): ReminderDeliveryDao

    companion object {
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS reminder_delivery (eventId TEXT NOT NULL PRIMARY KEY, occurrenceId TEXT, sentAt INTEGER NOT NULL, FOREIGN KEY(eventId) REFERENCES events(id) ON UPDATE NO ACTION ON DELETE CASCADE)")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS events (id TEXT NOT NULL PRIMARY KEY, title TEXT NOT NULL, content TEXT NOT NULL, createdAt INTEGER NOT NULL, intervalDays INTEGER)")
                db.execSQL("INSERT INTO events (id, title, content, createdAt, intervalDays) SELECT id, title, content, createdAt, CASE frequency WHEN 'DAILY' THEN 1 WHEN 'WEEKLY' THEN 7 WHEN 'MONTHLY' THEN 30 WHEN 'QUARTERLY' THEN 90 WHEN 'YEARLY' THEN 365 ELSE NULL END FROM notes")
                db.execSQL("CREATE TABLE IF NOT EXISTS event_occurrences (id TEXT NOT NULL PRIMARY KEY, eventId TEXT NOT NULL, occurredAt INTEGER NOT NULL, comment TEXT, FOREIGN KEY(eventId) REFERENCES events(id) ON UPDATE NO ACTION ON DELETE CASCADE)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_event_occurrences_eventId ON event_occurrences (eventId)")
                db.execSQL("INSERT INTO event_occurrences (id, eventId, occurredAt, comment) SELECT id, id, lastDoneAt, NULL FROM notes WHERE lastDoneAt IS NOT NULL")
                db.execSQL("DROP TABLE notes")
            }
        }
    }
}
