package musician.kuet.musta.adapters;

import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import musician.kuet.musta.R;
import musician.kuet.musta.listeners.OnClickListener;
import musician.kuet.musta.models.Song;

/**
 * Created by musta on 2/1/18.
 */

public class SongsListAdapter extends RecyclerView.Adapter<SongsListAdapter.SongVH> implements Filterable {

    private static final String TAG = "SongsAdapter";
    private Context mContext;
    private List<Song> mSongList;
    private List<Song> mFilteredSongList;
    private int mLayout;
    private OnClickListener mOnClickListener;

    public SongsListAdapter(Context context, int layout, List<Song> songList, OnClickListener clickListener) {
        this.mContext = context;
        this.mLayout = layout;
        this.mSongList = songList;
        this.mFilteredSongList = songList;
        this.mOnClickListener = clickListener;
    }

    @NonNull
    @Override
    public SongVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(mLayout, parent, false);
        return new SongVH(view);
    }

    @Override
    public void onBindViewHolder(SongVH holder, int position) {
        Song song = mFilteredSongList.get(position);
        holder.songName.setText(song.title);
        holder.artistName.setText(song.artistName);
        holder.songDuration.setText(String.format(Locale.getDefault(), "%.2f", ((float) song.duration / 60000)));

        Long albumId = song.albumId;
        try {
            Uri uri = Uri.parse("content://media/external/audio/albumart");
            Uri albumArt = ContentUris.withAppendedId(uri, albumId);
            Picasso.with(mContext)
                    .load(albumArt) //song album art
                    .placeholder(R.drawable.default_albumart)   //showing before loading art
                    .error(R.drawable.default_albumart) //if song album art unavailable
                    .into(holder.albumArt);
        } catch (IllegalStateException ignored) {
        }
    }

    @Override
    public int getItemCount() {
        return (mFilteredSongList != null ? mFilteredSongList.size() : 0);
    }

    @Override
    public Filter getFilter() {

        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {

                String charString = charSequence.toString();

                if (charString.isEmpty()) {

                    mFilteredSongList = mSongList;
                } else {

                    ArrayList<Song> filteredList = new ArrayList<>();

                    for (Song song : mSongList) {

                        if (song.title.toLowerCase().contains(charString) || song.artistName.toLowerCase().contains(charString)) {
                            filteredList.add(song);
                        }
                    }

                    mFilteredSongList = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = mFilteredSongList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                mFilteredSongList = (List<Song>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public class SongVH extends RecyclerView.ViewHolder implements View.OnClickListener {

        CardView song;
        ImageView albumArt;
        TextView songName;
        TextView artistName;
        TextView songDuration;
        ImageView options;

        SongVH(View view) {
            super(view);
            song = view.findViewById(R.id.cvSongItem);
            albumArt = view.findViewById(R.id.albumArt);
            options = view.findViewById(R.id.songOptions);
            songName = view.findViewById(R.id.songName);
            artistName = view.findViewById(R.id.artistName);
            songDuration = view.findViewById(R.id.songDuration);
            song.setOnClickListener(this);
            options.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.cvSongItem: {
                    mOnClickListener.onClick(mFilteredSongList.get(getAdapterPosition()));
                    break;
                }
                case R.id.songOptions: {
                    break;
                }
            }
        }
    }
}
