package com.example.videoplaylistapp;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.videoplaylistapp.database.AppDatabase;
import com.example.videoplaylistapp.model.PlaylistItem;

import java.util.concurrent.Executors;

public class HomeActivity extends AppCompatActivity {

    EditText etYoutubeUrl;
    WebView webViewPlayer;
    Button btnPlay, btnAddToPlaylist, btnMyPlaylist, btnLogout;
    AppDatabase db;
    int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // grab the userId passed from login — needed to scope the playlist
        userId = getIntent().getIntExtra("userId", -1);

        db = AppDatabase.getInstance(this);

        etYoutubeUrl = findViewById(R.id.etYoutubeUrl);
        webViewPlayer = findViewById(R.id.webViewPlayer);
        btnPlay = findViewById(R.id.btnPlay);
        btnAddToPlaylist = findViewById(R.id.btnAddToPlaylist);
        btnMyPlaylist = findViewById(R.id.btnMyPlaylist);
        btnLogout = findViewById(R.id.btnLogout);

        setupWebView();

        btnPlay.setOnClickListener(v -> {
            String url = etYoutubeUrl.getText().toString().trim();
            playVideo(url);
        });

        btnAddToPlaylist.setOnClickListener(v -> {
            String url = etYoutubeUrl.getText().toString().trim();
            if (url.isEmpty()) {
                Toast.makeText(this, "Enter a URL first", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!isValidYoutubeUrl(url) && !isValidRumbleUrl(url)) {
                Toast.makeText(this, "Invalid YouTube or Rumble URL", Toast.LENGTH_SHORT).show();
                return;
            }
            Executors.newSingleThreadExecutor().execute(() -> {
                // reject duplicate before fetching title
                PlaylistItem existing = db.playlistDao().findByUrl(userId, url);
                if (existing != null) {
                    runOnUiThread(() ->
                            Toast.makeText(this, "Already in playlist", Toast.LENGTH_SHORT).show()
                    );
                    return;
                }
                String title;
                String platform;
                if (isValidYoutubeUrl(url)) {
                    title = fetchYoutubeTitle(url);
                    platform = "YouTube";
                } else {
                    title = fetchRumbleTitle(url);
                    platform = "Rumble";
                }
                saveToPlaylist(url, title, platform);
            });
        });

        btnMyPlaylist.setOnClickListener(v -> {
            Intent intent = new Intent(this, PlaylistActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private void saveToPlaylist(String url, String title, String platform) {
        PlaylistItem item = new PlaylistItem();
        item.userId = userId;
        item.url = url;
        item.title = title;
        item.platform = platform;
        db.playlistDao().insert(item);
        runOnUiThread(() ->
                Toast.makeText(this, "Added: " + title, Toast.LENGTH_SHORT).show()
        );
    }

    private void setupWebView() {
        WebSettings settings = webViewPlayer.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        webViewPlayer.setWebChromeClient(new WebChromeClient());
    }

    private void playVideo(String url) {
        if (url.isEmpty()) {
            Toast.makeText(this, "Enter a URL first", Toast.LENGTH_SHORT).show();
            return;
        }
        if (isValidYoutubeUrl(url)) {
            String videoId = extractYoutubeId(url);
            if (videoId == null) {
                Toast.makeText(this, "Invalid YouTube URL", Toast.LENGTH_SHORT).show();
                return;
            }
            // youtube-nocookie domain bypasses the WebView embed restriction
            String html = "<html><body style='margin:0;padding:0;background:#000;'>"
                    + "<iframe width='100%' height='100%' "
                    + "src='https://www.youtube-nocookie.com/embed/" + videoId + "?autoplay=1' "
                    + "referrerpolicy='strict-origin' "
                    + "frameborder='0' allowfullscreen></iframe>"
                    + "</body></html>";
            webViewPlayer.loadDataWithBaseURL("https://www.youtube-nocookie.com", html, "text/html", "utf-8", null);

        } else if (isValidRumbleUrl(url)) {
            Executors.newSingleThreadExecutor().execute(() -> {
                String embedUrl = fetchRumbleEmbedUrl(url);
                runOnUiThread(() -> {
                    if (embedUrl == null) {
                        Toast.makeText(this, "Could not load Rumble video", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String html = "<html><body style='margin:0;padding:0;background:#000;'>"
                            + "<iframe width='100%' height='100%' "
                            + "src='" + embedUrl + "' "
                            + "frameborder='0' allowfullscreen></iframe>"
                            + "</body></html>";
                    webViewPlayer.loadDataWithBaseURL("https://rumble.com", html, "text/html", "utf-8", null);
                });
            });
        } else {
            Toast.makeText(this, "Invalid URL — YouTube or Rumble only", Toast.LENGTH_SHORT).show();
        }
    }

    // fetches YouTube page HTML and pulls the title tag out
    private String fetchYoutubeTitle(String videoUrl) {
        try {
            java.net.URL url = new java.net.URL(videoUrl);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                if (sb.toString().contains("</title>")) break;
            }
            reader.close();
            String html = sb.toString();
            int start = html.indexOf("<title>") + 7;
            int end = html.indexOf("</title>");
            if (start > 7 && end > start) {
                return html.substring(start, end).replace(" - YouTube", "").trim();
            }
        } catch (Exception e) {
            // fall through to default
        }
        return "YouTube Video";
    }

    // reuses the oEmbed API — title is already in the JSON response
    private String fetchRumbleTitle(String videoUrl) {
        try {
            String apiUrl = "https://wn0.rumble.com/api/Media/oembed.json?url=" + videoUrl;
            java.net.URL url = new java.net.URL(apiUrl);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            reader.close();
            String json = sb.toString();
            String marker = "\"title\":\"";
            int start = json.indexOf(marker) + marker.length();
            int end = json.indexOf("\"", start);
            return json.substring(start, end);
        } catch (Exception e) {
            return "Rumble Video";
        }
    }

    private String fetchRumbleEmbedUrl(String videoUrl) {
        try {
            String apiUrl = "https://wn0.rumble.com/api/Media/oembed.json?url=" + videoUrl;
            java.net.URL url = new java.net.URL(apiUrl);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            reader.close();
            String json = sb.toString();
            String srcMarker = "src=\\\"";
            int start = json.indexOf(srcMarker) + srcMarker.length();
            int end = json.indexOf("\\\"", start);
            return json.substring(start, end);
        } catch (Exception e) {
            return null;
        }
    }

    private String extractYoutubeId(String url) {
        if (url.contains("v=")) {
            String id = url.split("v=")[1];
            int ampIndex = id.indexOf("&");
            return ampIndex != -1 ? id.substring(0, ampIndex) : id;
        } else if (url.contains("youtu.be/")) {
            String id = url.split("youtu.be/")[1];
            int qIndex = id.indexOf("?");
            return qIndex != -1 ? id.substring(0, qIndex) : id;
        }
        return null;
    }

    private boolean isValidYoutubeUrl(String url) {
        return url.contains("youtube.com/watch") || url.contains("youtu.be/");
    }

    private boolean isValidRumbleUrl(String url) {
        return url.contains("rumble.com/");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String url = intent.getStringExtra("playUrl");
        if (url != null) {
            etYoutubeUrl.setText(url);
            playVideo(url);
        }
    }
}