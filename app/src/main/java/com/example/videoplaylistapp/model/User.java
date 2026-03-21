package com.example.videoplaylistapp.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

// Room entity — maps to the "users" table
@Entity(tableName = "users")
public class User {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String fullName;

    // username must be unique — enforced at signup logic level
    public String username;

    public String password;
}