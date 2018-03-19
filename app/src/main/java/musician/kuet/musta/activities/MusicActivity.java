package musician.kuet.musta.activities;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import musician.kuet.musta.R;
import musician.kuet.musta.adapters.SongsListAdapter;
import musician.kuet.musta.db.SongLoader;
import musician.kuet.musta.listeners.OnClickListener;
import musician.kuet.musta.models.Song;

public class MusicActivity extends AppCompatActivity implements OnClickListener {

    private static final String TAG = "MusicActivity";

    public MediaPlayer player = null;
    @BindView(R.id.llBottomSheetPlayer)
    LinearLayout layoutBottomSheet;
    @BindView(R.id.fmFullScreenPlayer)
    FrameLayout fullBottomSheet;
    @BindView(R.id.tempBottomNav)
    LinearLayout bottomSheetController;
    @BindView(R.id.playPauseFloating)
    FloatingActionButton fabPlayPause;
    BottomSheetBehavior bottomSheetBehavior, fullBottomSheetBehavior;
    private RecyclerView mRecyclerView;
    private SongsListAdapter mSongsListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);
        ButterKnife.bind(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fullBottomSheetBehavior = BottomSheetBehavior.from(fullBottomSheet);
        fullBottomSheetBehavior.setHideable(false);
        fullBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        bottomSheetBehavior = BottomSheetBehavior.from(layoutBottomSheet);
        bottomSheetBehavior.setHideable(false);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN: {
                        break;
                    }
                    case BottomSheetBehavior.STATE_EXPANDED: {
                        bottomSheetController.setVisibility(View.INVISIBLE);
                        setPlayerState();
                        Log.i(TAG, "onStateChanged: expanded");
                        break;
                    }
                    case BottomSheetBehavior.STATE_COLLAPSED: {
                        bottomSheetController.setVisibility(View.VISIBLE);
                        Log.i(TAG, "onStateChanged: collapsed");
                        break;
                    }
                    case BottomSheetBehavior.STATE_DRAGGING: {
                        Log.i(TAG, "onStateChanged: dragging");
                        break;
                    }
                    case BottomSheetBehavior.STATE_SETTLING: {
                        Log.i(TAG, "onStateChanged: settling");
                        break;
                    }
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                Log.i(TAG, "onSlide: " + slideOffset);
            }
        });

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

        bottomSheetController.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fullBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });

    }

    private void setPlayerState() {
        if (player != null && player.isPlaying()) {
            fabPlayPause.setImageResource(R.drawable.ic_action_pause);
        } else {
            fabPlayPause.setImageResource(R.drawable.ic_action_play);
        }
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
