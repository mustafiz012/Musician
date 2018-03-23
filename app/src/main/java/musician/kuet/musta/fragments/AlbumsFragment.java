package musician.kuet.musta.fragments;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import musician.kuet.musta.R;
import musician.kuet.musta.adapters.AlbumListAdapter;
import musician.kuet.musta.db.AlbumLoader;

/**
 * A simple {@link Fragment} subclass.
 */
public class AlbumsFragment extends Fragment {

    private AlbumListAdapter albumListAdapter;
    private RecyclerView rvSFSongList;
    private RecyclerView.ItemDecoration itemDecoration;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_songs, container, false);
        rvSFSongList = rootView.findViewById(R.id.rvSFSongList);
        rvSFSongList.setLayoutManager(new GridLayoutManager(getActivity(), 1));

        if (getActivity() != null)
            new LoadAlbums().execute("");

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    private class LoadAlbums extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            if (getActivity() != null)
                albumListAdapter = new AlbumListAdapter(AlbumLoader.getAllAlbums(getActivity()), getContext());
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            rvSFSongList.setAdapter(albumListAdapter);
            //to add spacing between cards
            if (getActivity() != null) {
                itemDecoration = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL);
                rvSFSongList.addItemDecoration(itemDecoration);
            }

        }
    }

}
