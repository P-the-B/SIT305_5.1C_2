# iStream — Personal Video Playlist App

A personal video player for Android. Save, organise, and play your favourite videos from YouTube and Rumble — all in one place, with your own private account.

## Features

### 🎬 Multi-Platform Video Playback
- Plays **YouTube** and **Rumble** videos directly inside the app — no browser needed
- Supports both full YouTube URLs (`youtube.com/watch?v=`) and short links (`youtu.be/`)
- Smooth in-app playback powered by an embedded WebView player

### 📋 Personal Playlist
- Save any YouTube or Rumble video to your playlist with one tap
- Tap a saved video to load and play it instantly
- Videos are saved with their **title** and **platform** automatically — no manual labelling
- See a live count of your saved videos broken down by platform
- Delete individual items anytime

### 🔐 User Accounts
- Create a personal account with full name, username, and password
- Each user's playlist is completely private — no crossover between accounts
- Secure login with validation; duplicate usernames are blocked at signup

### 🚀 Clean & Responsive UI
- Instant feedback with toast notifications throughout
- Empty state messaging when playlist is empty
- Logout available from both Home and Playlist screens

## Tech Stack

- Java / Android SDK 26+
- Room ORM for local data storage
- LiveData for reactive UI updates
- RecyclerView for playlist display
- WebView for video playback
- Material Design components

## Project Structure

```
app/src/main/java/com/example/videoplaylistapp/
├── LoginActivity.java
├── SignUpActivity.java
├── HomeActivity.java
├── PlaylistActivity.java
├── database/
│   ├── AppDatabase.java
│   ├── UserDao.java
│   └── PlaylistDao.java
└── model/
    ├── User.java
    └── PlaylistItem.java
```

## Build & Run

### Prerequisites
- Android Studio
- Android SDK 26+

### Steps
1. Clone the repository
2. Open in Android Studio
3. Sync Gradle
4. Run on emulator or device

```
./gradlew installDebug
```

---

## Legal

This project was created for educational purposes as part of Deakin University's SIT305 unit. All rights reserved. Reuse, redistribution, or reproduction of any part of this codebase requires explicit written permission from the author.

---
