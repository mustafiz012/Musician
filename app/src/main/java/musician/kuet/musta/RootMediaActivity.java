package musician.kuet.musta;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.math.BigDecimal;

public class RootMediaActivity extends AppCompatActivity {

    public MediaCursorAdapter rootMediaCursorAdapter = null;
    ListView rootSongList = null;
    public Cursor gCursor = null;
    public int rootTotalSongs = 0;
    public ActionBar gActionBar = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*gActionBar = getSupportActionBar();
        gActionBar.hide();*/
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        fetchAudioFiles();
    }


    public String getCurrentFileName(int currentPosition) {
        String songName = null;
        Cursor cursor = (Cursor) rootMediaCursorAdapter.getItem(currentPosition);
        songName = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.TITLE));
        return songName;
    }

    public String getCurrentArtistName(int currentPosition) {
        String artistName = null;
        Cursor cursor = (Cursor) rootMediaCursorAdapter.getItem(currentPosition);
        artistName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Artists.ARTIST));
        return artistName;
    }

    public Drawable getCurrentAlbumArt(int currentPosition) {
        String artistName = null;
        Cursor cursor = (Cursor) rootMediaCursorAdapter.getItem(currentPosition);
        Log.i("albumArt: ", cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART)));
        artistName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AlbumColumns.ALBUM_ART));
        //Drawable img = Drawable.createFromPath(coverPath);
        Drawable image = null;
        if (artistName != null)
            image = Drawable.createFromPath(artistName);
        //albumcover.setImageDrawable(img);
        return image;
    }

    public String getCurrentFile(int currentPosition) {
        //songPositionFromList = currentPosition;
        String songData = null;
        Cursor cursor = (Cursor) rootMediaCursorAdapter.getItem(currentPosition);
        songData = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
        return songData;
    }

    //fetching audio files from local storage
    public void fetchAudioFiles() {
        gCursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
        if (null != gCursor) {
            gCursor.moveToFirst();
            rootMediaCursorAdapter = new MediaCursorAdapter(this, R.layout.listitem, gCursor);
            //total songs in local storage
            rootTotalSongs = rootMediaCursorAdapter.getCount();
        }
        //rootSongList.setAdapter(rootMediaCursorAdapter);
    }

    //creating a media cursor adapter which will be publicly accessible
    public class MediaCursorAdapter extends SimpleCursorAdapter {

        public MediaCursorAdapter(Context context, int layout, Cursor c) {
            super(context, layout, c,
                    new String[]{MediaStore.MediaColumns.TITLE, MediaStore.Audio.Artists.ARTIST, MediaStore.Audio.AudioColumns.DURATION},
                    new int[]{R.id.displayname, R.id.title, R.id.duration});
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView title = (TextView) view.findViewById(R.id.title);
            TextView name = (TextView) view.findViewById(R.id.displayname);
            TextView duration = (TextView) view.findViewById(R.id.duration);

            name.setText(cursor.getString(
                    cursor.getColumnIndex(MediaStore.MediaColumns.TITLE)));

            title.setText(cursor.getString(
                    cursor.getColumnIndex(MediaStore.Audio.Artists.ARTIST)));

            long durationInMs = Long.parseLong(cursor.getString(
                    cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DURATION)));

            double durationInMin = ((double) durationInMs / 1000.0) / 60.0;

            durationInMin = new BigDecimal(Double.toString(durationInMin)).setScale(2, BigDecimal.ROUND_UP).doubleValue();

            duration.setText("" + durationInMin);

            view.setTag(cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA)));
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View v = inflater.inflate(R.layout.listitem, parent, false);

            bindView(v, context, cursor);

            return v;
        }
    }
}
