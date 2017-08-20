package musician.kuet.musta;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by musta on 7/23/17.
 */

public class ComplexListAdapter extends BaseAdapter {
    private TextView artist_name, track_title, track_duration;
    private Context mContext;
    private List<Integer> mFavoriteTrackList;
    private RootMediaActivity.MediaCursorAdapter mRootMediaCursorAdapter;
    private Cursor mCursor;

    public ComplexListAdapter(Context context, List<Integer> favoriteTrackList, RootMediaActivity.MediaCursorAdapter rootMediaCursorAdapter) {
        mContext = context;
        mFavoriteTrackList = favoriteTrackList;
        this.mRootMediaCursorAdapter = rootMediaCursorAdapter;
    }

    @Override
    public int getCount() {
        return mFavoriteTrackList.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.listitem, null);
        }
        artist_name = view.findViewById(R.id.artist_name);
        track_title = view.findViewById(R.id.track_title);
        track_duration = view.findViewById(R.id.duration);
        try {
            mCursor = (Cursor) mRootMediaCursorAdapter.getItem(mFavoriteTrackList.get(position));
            track_title.setText("" + mCursor.getString(mCursor.getColumnIndex(MediaStore.MediaColumns.TITLE)));
            artist_name.setText("" + mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Artists.ARTIST)));

            //duration calculation
            long durationInMs = Long.parseLong(mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.AudioColumns.DURATION)));
            double durationInMin = ((double) durationInMs / 1000.0) / 60.0;
            durationInMin = new BigDecimal(Double.toString(durationInMin)).setScale(2, BigDecimal.ROUND_UP).doubleValue();

            track_duration.setText("" + durationInMin);
        } catch (NullPointerException e) {
        }
        return view;
    }
}
