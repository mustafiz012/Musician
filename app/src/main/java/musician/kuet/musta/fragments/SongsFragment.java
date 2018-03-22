package musician.kuet.musta.fragments;


import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import musician.kuet.musta.R;
import musician.kuet.musta.adapters.SongsListAdapter;
import musician.kuet.musta.db.SongLoader;
import musician.kuet.musta.listeners.OnClickListener;
import musician.kuet.musta.models.Song;

/**
 * A simple {@link Fragment} subclass.
 */
public class SongsFragment extends Fragment implements OnClickListener {

    private RecyclerView rvSFSongList;
    private SongsListAdapter mSongsListAdapter;
    private Context mContext;

    public SongsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_songs, container, false);
        mContext = getContext();
        rvSFSongList = rootView.findViewById(R.id.rvSFSongList);
        rvSFSongList.setLayoutManager(new LinearLayoutManager(getActivity()));

        new LoadSongs().execute("");
        return rootView;
    }

    @Override
    public void onClick(Song song) {

    }

    private class LoadSongs extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            if (getActivity() != null) {
                mSongsListAdapter = new SongsListAdapter(mContext, R.layout.rv_item_layout, SongLoader.getAllSongs(mContext), SongsFragment.this);
            }
            return "Executed";
        }

        @Override
        protected void onPostExecute(String s) {
            rvSFSongList.setAdapter(mSongsListAdapter);
            if (getActivity() != null)
                rvSFSongList.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        }
    }

}
