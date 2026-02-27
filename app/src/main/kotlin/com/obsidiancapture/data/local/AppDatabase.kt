package com.obsidiancapture.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.obsidiancapture.data.local.dao.NoteDao
import com.obsidiancapture.data.local.entity.NoteEntity
import com.obsidiancapture.data.local.entity.NoteFtsEntity

@Database(
    entities = [NoteEntity::class, NoteFtsEntity::class],
    version = 3,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE notes ADD COLUMN sync_error TEXT DEFAULT NULL")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_notes_status ON notes(status)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_notes_pending_sync ON notes(pending_sync)")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create FTS4 virtual table
                db.execSQL(
                    """
                    CREATE VIRTUAL TABLE IF NOT EXISTS notes_fts
                    USING fts4(title, body, tags, content=`notes`)
                    """,
                )

                // Populate FTS with existing data
                db.execSQL(
                    """
                    INSERT INTO notes_fts(rowid, title, body, tags)
                    SELECT rowid, title, body, tags FROM notes
                    """,
                )

                // Triggers to keep FTS in sync
                db.execSQL(
                    """
                    CREATE TRIGGER IF NOT EXISTS notes_ai AFTER INSERT ON notes BEGIN
                        INSERT INTO notes_fts(rowid, title, body, tags)
                        VALUES (new.rowid, new.title, new.body, new.tags);
                    END
                    """,
                )

                db.execSQL(
                    """
                    CREATE TRIGGER IF NOT EXISTS notes_ad AFTER DELETE ON notes BEGIN
                        INSERT INTO notes_fts(notes_fts, rowid, title, body, tags)
                        VALUES ('delete', old.rowid, old.title, old.body, old.tags);
                    END
                    """,
                )

                db.execSQL(
                    """
                    CREATE TRIGGER IF NOT EXISTS notes_au AFTER UPDATE ON notes BEGIN
                        INSERT INTO notes_fts(notes_fts, rowid, title, body, tags)
                        VALUES ('delete', old.rowid, old.title, old.body, old.tags);
                        INSERT INTO notes_fts(rowid, title, body, tags)
                        VALUES (new.rowid, new.title, new.body, new.tags);
                    END
                    """,
                )
            }
        }
    }
}
