package musician.kuet.musta.activities;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;

import musician.kuet.musta.R;
import musician.kuet.musta.adapters.SongsListAdapter;
import musician.kuet.musta.db.SongLoader;
import musician.kuet.musta.listeners.OnClickListener;
import musician.kuet.musta.models.Song;

public class MusicActivity extends AppCompatActivity implements OnClickListener {

    public MediaPlayer player = null;
    private RecyclerView mRecyclerView;
    private SongsListAdapter mSongsListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fabShuffleMusic);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView = findViewById(R.id.rvSongList);
        mRecyclerView.setLayoutManager(layoutManager);
        mSongsListAdapter = new SongsListAdapter(this, R.layout.rv_item_layout, SongLoader.getAllSongs(this), this);
        mRecyclerView.setAdapter(mSongsListAdapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_music, menu);
        MenuItem search = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) search.getActionView();
        searchView.setQueryHint("Search songs...");
        doSearch(searchView);
        return true;
    }

    private void doSearch(SearchView searchView) {

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                mSongsListAdapter.getFilter().filter(newText);
                return true;
            }
        });
    }


    public void playSong(Song song) {

        if (player != null) {
            player.stop();
            player.reset();
        } else {
            player = new MediaPlayer();
        }
        try {
            player.setDataSource(song.data);
            player.prepare();
            player.start();
        } catch (IllegalArgumentException | IllegalStateException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(Song song) {
        playSong(song);
        Toast.makeText(this, "Name: " + song.title, Toast.LENGTH_SHORT).show();
    }
}
