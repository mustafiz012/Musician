package musician.kuet.musta;

import android.Manifest;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.math.BigDecimal;

public class RootMediaActivity extends AppCompatActivity {

    private final static int READ_EXTERNAL_STORAGE_REQUEST_ROOT_CODE = 201;
    private final static int WRITE_EXTERNAL_STORAGE_REQUEST_ROOT_CODE = 202;
    public MediaCursorAdapter rootMediaCursorAdapter = null;
    public Cursor gCursor = null;
    public int rootTotalSongs = 0;
    public ActionBar gActionBar = null;
    ListView rootSongList = null;
    private boolean isRootPermissionGranted = false;
    private boolean isRootPermissionRequested = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE_REQUEST_ROOT_CODE);
            isRootPermissionRequested = true;
        } else {
            isRootPermissionGranted = true;
        }

        if (isRootPermissionGranted && !isRootPermissionRequested) {
            fetchAudioFiles();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case READ_EXTERNAL_STORAGE_REQUEST_ROOT_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    findViewById(R.id.fake_layout).setVisibility(View.GONE);
                    findViewById(R.id.main_layout).setVisibility(View.VISIBLE);
                    fetchAudioFiles();
                    isRootPermissionGranted = true;
                    Log.i("RootMedia", "Granted");
                } else {
                    isRootPermissionGranted = false;
                    Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
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

    public Bitmap getCurrentAlbumArt(Context context, int currentPosition) {
        Cursor cursor;
        cursor = (Cursor) rootMediaCursorAdapter.getItem(currentPosition);
        Long album_id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
        Log.i("Album ID : ", "" + album_id);
        Bitmap bm = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        try {
            final Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
            Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);
            Log.i("Uri", "" + uri.toString());
            ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
            if (pfd != null) {
                FileDescriptor fd = pfd.getFileDescriptor();
                bm = BitmapFactory.decodeFileDescriptor(fd, null, options);
                pfd = null;
                fd = null;
            }
        } catch (Error ee) {
            ee.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bm;
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
                    new String[]{MediaStore.MediaColumns.TITLE, MediaStore.Audio.Artists.ARTIST, MediaStore.Audio.AudioColumns.DURATION, MediaStore.Audio.Artists.Albums.ALBUM_ID},
                    new int[]{R.id.displayname, R.id.title, R.id.duration, R.id.single_album_art});
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView title = view.findViewById(R.id.title);
            TextView name = view.findViewById(R.id.displayname);
            TextView duration = view.findViewById(R.id.duration);
            ImageView album_art = view.findViewById(R.id.single_album_art);

            //albumart setup
            Bitmap bitmap = null;
            Long album_id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
            //Log.i("Album ID : ", "" + album_id);
            BitmapFactory.Options options = new BitmapFactory.Options();
            try {
                final Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
                //Log.i("Uriiii", "" + sArtworkUri.toString());
                Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);
                Log.i("Uri", "" + uri.toString());
                ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
                Log.i("PDF", "" + pfd.toString());
                if (pfd != null) {
                    FileDescriptor fd = pfd.getFileDescriptor();
                    bitmap = BitmapFactory.decodeFileDescriptor(fd, null, options);
                    pfd = null;
                    fd = null;
                }
            } catch (Error ee) {
                ee.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.default_albumart);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (bitmap != null) {
                album_art.setImageBitmap(bitmap);
                album_art.setScaleType(ImageView.ScaleType.FIT_XY);
            } else {
                album_art.setImageResource(R.drawable.default_albumart);
                album_art.setScaleType(ImageView.ScaleType.FIT_XY);
                //Toast.makeText(mContext, "No albumart found for this song", Toast.LENGTH_SHORT).show();
            }

            //next of albumart setup
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
