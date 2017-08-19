package musician.kuet.musta;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.List;

public class PlaylistActivity extends RootMediaActivity implements AdapterView.OnItemClickListener {
    private List<Integer> mFavoriteList;
    private ListView lv_favorite_song_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        lv_favorite_song_list = (ListView) findViewById(R.id.lv_favorite_song_list);
        mFavoriteList = getIntent().getIntegerArrayListExtra("favlist");
        ComplexListAdapter complexListAdapter = new ComplexListAdapter(this, mFavoriteList, setMediaCursorAdapter());
        lv_favorite_song_list.setAdapter(complexListAdapter);
        lv_favorite_song_list.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("resultantPosition", mFavoriteList.get(position));
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }
}
