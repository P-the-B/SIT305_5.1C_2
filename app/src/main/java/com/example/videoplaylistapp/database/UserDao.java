package com.example.videoplaylistapp.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.videoplaylistapp.model.User;

@Dao
public interface UserDao {

    // insert a new user at signup
    @Insert
    void insert(User user);

    // fetch user by username+password for login validation
    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    User login(String username, String password);

    // check if username already taken before signup
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    User findByUsername(String username);
}