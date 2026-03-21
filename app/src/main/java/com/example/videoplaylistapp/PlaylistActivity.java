package com.example.videoplaylistapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.videoplaylistapp.database.AppDatabase;
import com.example.videoplaylistapp.model.PlaylistItem;

import java.util.List;
import java.util.concurrent.Executors;

public class PlaylistActivity extends AppCompatActivity {

    RecyclerView rvPlaylist;
    TextView tvEmpty, tvCount;
    Button btnLogout, btnBack;
    AppDatabase db;
    int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);

        userId = getIntent().getIntExtra("userId", -1);
        db = AppDatabase.getInstance(this);

        rvPlaylist = findViewById(R.id.rvPlaylist);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvCount = findViewById(R.id.tvCount);
        btnLogout = findViewById(R.id.btnLogout);
        btnBack = findViewById(R.id.btnBack);

        rvPlaylist.setLayoutManager(new LinearLayoutManager(this));

        db.playlistDao().getPlaylistForUser(userId).observe(this, items -> {
            if (items == null || items.isEmpty()) {
                tvEmpty.setVisibility(View.VISIBLE);
                rvPlaylist.setVisibility(View.GONE);
                tvCount.setText("0 saved videos");
            } else {
                tvEmpty.setVisibility(View.GONE);
                rvPlaylist.setVisibility(View.VISIBLE);
                rvPlaylist.setAdapter(new PlaylistAdapter(items));

                long youtubeCount = 0, rumbleCount = 0;
                for (PlaylistItem item : items) {
                    if ("YouTube".equals(item.platform)) youtubeCount++;
                    else if ("Rumble".equals(item.platform)) rumbleCount++;
                }
                tvCount.setText(items.size() + " saved videos — "
                        + youtubeCount + " YouTube, " + rumbleCount + " Rumble");
            }
        });

        btnBack.setOnClickListener(v -> finish());

        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.ViewHolder> {

        List<PlaylistItem> items;

        PlaylistAdapter(List<PlaylistItem> items) {
            this.items = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_playlist, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            PlaylistItem item = items.get(position);
            holder.tvTitle.setText(item.title != null ? item.title : item.url);
            holder.tvPlatform.setText(item.platform != null ? item.platform : "");
            holder.tvUrl.setText(item.url);

            // tap row to load video back in player
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(PlaylistActivity.this, HomeActivity.class);
                intent.putExtra("userId", userId);
                intent.putExtra("playUrl", item.url);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            });

            // delete off main thread, LiveData updates the list automatically
            holder.btnDelete.setOnClickListener(v ->
                    Executors.newSingleThreadExecutor().execute(() ->
                            db.playlistDao().delete(item)
                    )
            );
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvPlatform, tvUrl;
            Button btnDelete;

            ViewHolder(View view) {
                super(view);
                tvTitle = view.findViewById(R.id.tvTitle);
                tvPlatform = view.findViewById(R.id.tvPlatform);
                tvUrl = view.findViewById(R.id.tvUrl);
                btnDelete = view.findViewById(R.id.btnDelete);
            }
        }
    }
}