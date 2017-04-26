package musician.kuet.musta;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class CurrentPlaylistActivity extends RootMediaActivity implements AdapterView.OnItemClickListener {
    ListView nowPlayingList = null;
    List<Integer> nowPlayingFilteredIndexes;
    int nowPlayingFilteredIndexPosition = 0;
    MenuItem searchItem = null, menuPlaylistItem = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_playlist);
        nowPlayingList = (ListView) findViewById(R.id.now_playing_playlist);
        nowPlayingList.setAdapter(rootMediaCursorAdapter);
        Bundle bundle = getIntent().getExtras();
        Log.i("Current position ", "" + bundle.getInt("currentPosition"));
        nowPlayingList.setSelection(bundle.getInt("currentPosition"));
        nowPlayingList.setOnItemClickListener(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_current_playlist, menu);
        searchItem = menu.findItem(R.id.action_search);
        menuPlaylistItem = menu.findItem(R.id.action_current_playlist);
        menuPlaylistItem.setVisible(false);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                nowPlayingFilteredIndexes = new ArrayList<Integer>();
                nowPlayingFilteredIndexPosition = 0;
                ArrayList<String> currentFilteredList = new ArrayList<String>();
                for (gCursor.moveToFirst(); !gCursor.isAfterLast(); gCursor.moveToNext()) {
                    if (gCursor.getString(gCursor.getColumnIndex(MediaStore.MediaColumns.TITLE)).toLowerCase().contains(query.toLowerCase()) || gCursor.getString(gCursor.getColumnIndex(MediaStore.Audio.Artists.ARTIST)).toLowerCase().contains(query.toLowerCase())) {
                        currentFilteredList.add(gCursor.getString(gCursor.getColumnIndex(MediaStore.MediaColumns.TITLE)));
                        nowPlayingFilteredIndexes.add(gCursor.getPosition());
                        nowPlayingFilteredIndexPosition++;
                    }
                }
                ArrayAdapter<String> filteredAdapter = new ArrayAdapter<String>(CurrentPlaylistActivity.this, android.R.layout.simple_list_item_1, currentFilteredList);
                nowPlayingList.setAdapter(filteredAdapter);
                nowPlayingList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("resultantPosition", nowPlayingFilteredIndexes.get(position));
                        setResult(Activity.RESULT_OK, resultIntent);
                        finish();
                    }
                });
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.i("Changes: ", "" + newText);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                Log.i("Child home ", "is clicked");
                return false;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("resultantPosition", position);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }
}
