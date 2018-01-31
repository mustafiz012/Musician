package musician.kuet.musta.models;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import musician.kuet.musta.R;

/**
 * Created by musta on 2/1/18.
 */

public class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.SongVH> implements Filterable {

    @Override
    public SongVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_item_layout, parent, false);
        return new SongVH(view);
    }

    @Override
    public void onBindViewHolder(SongVH holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    @Override
    public Filter getFilter() {
        return null;
    }

    public class SongVH extends RecyclerView.ViewHolder {

        CardView song;
        ImageView albumArt;
        TextView songName;
        TextView artistName;
        TextView songDuration;

        public SongVH(View view) {
            super(view);
            song = view.findViewById(R.id.cvRecyclerItem);
            albumArt = view.findViewById(R.id.albumArt);
            songName = view.findViewById(R.id.songName);
            artistName = view.findViewById(R.id.artistName);
            songDuration = view.findViewById(R.id.songDuration);
        }
    }
}
