package musician.kuet.musta.activities;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import musician.kuet.musta.R;
import musician.kuet.musta.models.SongsAdapter;

public class MusicActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private SongsAdapter mSongsAdapter;

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

        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        String[] projection = {MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.DATA, MediaStore.Audio.Artists.ARTIST,
                MediaStore.Audio.AudioColumns.DURATION};
        Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            mSongsAdapter = new SongsAdapter(this, R.layout.rv_item_layout, cursor);
            mRecyclerView.setAdapter(mSongsAdapter);
        }
    }

}
