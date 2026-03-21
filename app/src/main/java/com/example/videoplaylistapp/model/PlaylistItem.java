package com.example.videoplaylistapp.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "playlist_items")
public class PlaylistItem {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public int userId;
    public String url;
    public String title;  // display name shown in playlist
    public String platform; // "YouTube" or "Rumble" — used for the count breakdown
}