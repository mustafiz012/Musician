package musician.kuet.musta.adapters;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
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

import java.util.Locale;

import musician.kuet.musta.R;

/**
 * Created by musta on 2/1/18.
 */

public class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.SongVH> implements Filterable {

    private static final String TAG = "SongsAdapter";
    private Context mContext;
    private Cursor mCursor;
    private int mLayout;

    public SongsAdapter(Context context, int layout, Cursor cursor) {
        this.mContext = context;
        this.mLayout = layout;
        this.mCursor = cursor;
    }

    @NonNull
    @Override
    public SongVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(mLayout, parent, false);
        return new SongVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongVH holder, int position) {
        mCursor.moveToPosition(position);
        holder.songName.setText(mCursor.getString(mCursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)));
        holder.artistName.setText(mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Artists.ARTIST)));
        holder.songDuration.setText(String.format(Locale.getDefault(), "%.2f", mCursor.getFloat(mCursor.getColumnIndex(MediaStore.Audio.AudioColumns.DURATION)) / 60000));

        Long albumId = mCursor.getLong(mCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
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
        return mCursor.getCount();
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
            song = view.findViewById(R.id.cvSongItem);
            albumArt = view.findViewById(R.id.albumArt);
            songName = view.findViewById(R.id.songName);
            artistName = view.findViewById(R.id.artistName);
            songDuration = view.findViewById(R.id.songDuration);
        }
    }
}
