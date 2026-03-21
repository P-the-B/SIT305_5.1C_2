package com.example.videoplaylistapp.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.videoplaylistapp.model.PlaylistItem;

import java.util.List;

@Dao
public interface PlaylistDao {

    // save a URL to this user's playlist
    @Insert
    void insert(PlaylistItem item);

    // delete a specific playlist item
    @Delete
    void delete(PlaylistItem item);

    // fetch only this user's saved URLs, newest first
    @Query("SELECT * FROM playlist_items WHERE userId = :userId ORDER BY id DESC")
    LiveData<List<PlaylistItem>> getPlaylistForUser(int userId);

    // check if this URL already exists for this user
    @Query("SELECT * FROM playlist_items WHERE userId = :userId AND url = :url LIMIT 1")
    PlaylistItem findByUrl(int userId, String url);
}