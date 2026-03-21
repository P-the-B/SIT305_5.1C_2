package com.example.videoplaylistapp.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.videoplaylistapp.model.PlaylistItem;
import com.example.videoplaylistapp.model.User;

// singleton DB instance — only one connection open at a time
@Database(entities = {User.class, PlaylistItem.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;

    public abstract UserDao userDao();
    public abstract PlaylistDao playlistDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "istream_db"
                    )
                    .fallbackToDestructiveMigration() // wipes DB on schema change during dev
                    .build();
        }
        return instance;
    }
}