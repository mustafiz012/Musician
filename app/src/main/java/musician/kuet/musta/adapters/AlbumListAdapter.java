package musician.kuet.musta.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import musician.kuet.musta.R;
import musician.kuet.musta.models.Album;

/**
 * Created by musta.
 */

public class AlbumListAdapter extends RecyclerView.Adapter<AlbumListAdapter.ItemHolder> {

    private List<Album> arrayList;
    private Context mContext;

    public AlbumListAdapter(List<Album> arrayList, Context mContext) {
        this.arrayList = arrayList;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_album_list, parent, false);
        return new ItemHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final ItemHolder itemHolder, int position) {

        Album album = arrayList.get(position);
        itemHolder.title.setText(album.title);
        itemHolder.artist.setText(album.artistName);
    }

    @Override
    public int getItemCount() {
        return (arrayList != null ? arrayList.size() : 0);
    }

    public class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView title, artist;
        ImageView albumArt;

        ItemHolder(View view) {
            super(view);
            this.title = view.findViewById(R.id.album_title);
            this.artist = view.findViewById(R.id.album_artist);
            this.albumArt = view.findViewById(R.id.album_art);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

        }
    }
}
