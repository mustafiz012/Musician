package musician.kuet.musta.activities;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import musician.kuet.musta.R;
import musician.kuet.musta.adapters.SongsListAdapter;
import musician.kuet.musta.db.SongLoader;
import musician.kuet.musta.listeners.OnClickListener;
import musician.kuet.musta.models.Song;

public class SearchActivity extends AppCompatActivity implements OnClickListener, SearchView.OnQueryTextListener {

    private final Executor mSearchExecutor = Executors.newSingleThreadExecutor();
    private RecyclerView searchRecyclerView;
    private AsyncTask mSearchTask = null;

    private SearchView mSearchView;
    private InputMethodManager mImm;
    private String queryString;

    private SongsListAdapter searchAdapter;

    private List<Object> searchResults = Collections.emptyList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mImm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        searchRecyclerView = findViewById(R.id.searchRecyclerView);
        searchRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        searchRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        searchAdapter = new SongsListAdapter(this, R.layout.rv_item_layout, SongLoader.getAllSongs(this), this);
        searchRecyclerView.setAdapter(searchAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_search, menu);
        mSearchView = (SearchView) menu.findItem(R.id.menu_search_library).getActionView();
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setQueryHint(getString(R.string.search_songs));
        mSearchView.setIconifiedByDefault(false);
        mSearchView.setIconified(false);

        menu.findItem(R.id.menu_search_library).
                setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {
                        return false;
                    }

                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        finish();
                        return false;
                    }
                });

        menu.findItem(R.id.menu_search_library).expandActionView();

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        onQueryTextChange(query);
        hideInputManager();
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        searchAdapter.getFilter().filter(newText);
        return true;
    }

    @Override
    public void onClick(Song song) {

    }

    public void hideInputManager() {
        if (mSearchView != null) {
            if (mImm != null) {
                mImm.hideSoftInputFromWindow(mSearchView.getWindowToken(), 0);
            }
            mSearchView.clearFocus();

            //SearchHistory.getInstance(this).addSearchString(queryString);
        }
    }
}
