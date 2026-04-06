package io.inkwell.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import io.inkwell.data.local.dao.NoteDao
import io.inkwell.data.local.entity.NoteEntity
import io.inkwell.data.local.entity.NoteFtsEntity

@Database(
    entities = [NoteEntity::class, NoteFtsEntity::class],
    version = 6,
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

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE notes ADD COLUMN attachment_uris TEXT DEFAULT NULL")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE notes ADD COLUMN capture_type TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE notes ADD COLUMN list_name TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE notes ADD COLUMN list_items_json TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE notes ADD COLUMN persistent INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE notes ADD COLUMN color TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE notes ADD COLUMN pinned INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE notes ADD COLUMN source_url TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE notes ADD COLUMN shared INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}
