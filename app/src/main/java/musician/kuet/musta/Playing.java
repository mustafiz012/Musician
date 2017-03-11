package musician.kuet.musta;

import android.Manifest;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;


public class Playing extends ListActivity implements View.OnClickListener, MediaPlayer.OnCompletionListener {
    static boolean active = false, shuffleFlag = false, isRepeatOneOn = false, isRepeatOn = false, isSeekBarChangedListenerStarted = false;
    private static final String TAG = null;
    ListView songList, currentPlayList;
    Button playingSong, playlist_action_bar_back_btn, search_back_btn;
    FrameLayout playlist_action_bar_search_btn;
    EditText search_song_et;
    int songPositionFromList = -1, totalSongs = 0, previousSongIndex = 0, lastPlayedSong = 0;
    List<Integer> previousSongPositions;
    String currentFile;
    Thread seekBarUpdating;
    long totalDuration = 0;
    long currentPosition = 0;
    FloatingActionButton floatingActionButton;
    SeekBar bar;
    ImageView preSong, nextSong, playPause, thumbnail;
    Button shuffle, repeat, nowPlayingSongs, goToSongList;
    TextView currentSong, leftDuration, rightDuration, tvSongsSize, currentSongArtistName;
    private double startTime = 0;
    private double finalTime = 0;
    private Handler myHandler = new Handler();
    String currentItem = "";
    NowPlaying status = new NowPlaying();
    ArrayList<File> songs, songs2;
    int songsSize = 0;
    Random randomPosition = new Random();
    private MediaCursorAdapter mediaCursorAdapter = null;
    private MediaPlayer player = null;
    ArrayAdapter<String> adapterForDialog;

    private void updatePlaylist() {
        Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
        if (null != cursor) {
            cursor.moveToFirst();
            //listing out the songList in Playing activity
            mediaCursorAdapter = new MediaCursorAdapter(getApplicationContext(), R.layout.song_layout, cursor);
            setListAdapter(mediaCursorAdapter);
            songsSize = cursor.getCount();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        state = songList.onSaveInstanceState();
        player.reset();
        player.release();
    }


    public void initialization() {
        playingSong = (Button) findViewById(R.id.playingSong);
        playlist_action_bar_back_btn = (Button) findViewById(R.id.playlist_action_bar_back_btn);
        playlist_action_bar_search_btn = (FrameLayout) findViewById(R.id.playlist_action_bar_search_btn);
        search_back_btn = (Button) findViewById(R.id.search_back_btn);
        search_song_et = (EditText) findViewById(R.id.search_song_et);
        floatingActionButton = (FloatingActionButton) findViewById(R.id.shuffle_all_songs);
        floatingActionButton.setOnClickListener(this);
        tvSongsSize = (TextView) findViewById(R.id.songsSize);
        songList = (ListView) findViewById(android.R.id.list);
        currentPlayList = (ListView) findViewById(R.id.lv_current_playlist);
        //registerForContextMenu(songList);
        bar = (SeekBar) findViewById(R.id.seekBar);
        preSong = (ImageView) findViewById(R.id.previous);
        nextSong = (ImageView) findViewById(R.id.next);
        thumbnail = (ImageView) findViewById(R.id.thumbnail);
        //thumbnail.setOnClickListener(this);
//		leftSeek = (Button) findViewById(R.id.leftSeeking);
//		rightSeek = (Button) findViewById(R.id.rightSeeking);
        playPause = (ImageView) findViewById(R.id.playPause);
        currentSong = (TextView) findViewById(R.id.currentSong);
        currentSongArtistName = (TextView) findViewById(R.id.currentSongArtistName);
        leftDuration = (TextView) findViewById(R.id.leftDuration);
        rightDuration = (TextView) findViewById(R.id.rightDuration);
        goToSongList = (Button) findViewById(R.id.songList);
        shuffle = (Button) findViewById(R.id.shuffle);
        repeat = (Button) findViewById(R.id.repeat);
        nowPlayingSongs = (Button) findViewById(R.id.imageViewSongList);

        preSong.setOnClickListener(this);
        nextSong.setOnClickListener(this);
//		leftSeek.setOnClickListener(this);
//		rightSeek.setOnClickListener(this);
        playPause.setOnClickListener(this);
        goToSongList.setOnClickListener(this);
        shuffle.setOnClickListener(this);
        repeat.setOnClickListener(this);
        playingSong.setOnClickListener(this);
        playlist_action_bar_search_btn.setOnClickListener(this);
        search_back_btn.setOnClickListener(this);
        playlist_action_bar_back_btn.setOnClickListener(this);
        //player.setOnCompletionListener(this);
        nowPlayingSongs.setOnClickListener(this);
    }

    private boolean checkingExternalStoragePermission() {
        boolean flag = false;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Log.i("if flag", "" + flag);
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Log.i("if if flag", "" + flag);
            } else {
                Log.i("if else flag", "" + flag);
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 200);
                flag = true;
            }
        } else {
            Log.i("else flag", "" + flag);
            flag = true;
        }
        Log.i("flag", "" + flag);
        return flag;
    }

    private void fetchAudioFiles() {
        Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
        if (null != cursor) {
            cursor.moveToFirst();
            mediaCursorAdapter = new MediaCursorAdapter(this, R.layout.listitem, cursor);
            totalSongs = mediaCursorAdapter.getCount();
            setListAdapter(mediaCursorAdapter);
            songList.setAdapter(mediaCursorAdapter);
        }
        tvSongsSize.setText(totalSongs + " Songs");
        if (seekBarUpdating != null && !seekBarUpdating.isAlive())
            setSeekBarUpdating();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playing);
        initialization();
        //load player state from sharedPreferences
        loadPlayerStates();
        //new MediaPlayer
        player = new MediaPlayer();
        player.setOnCompletionListener(this);
        previousSongPositions = new ArrayList<Integer>();
        //fetching all audio files from external and internal sdCards
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            Log.i("if SDK", "" + android.os.Build.VERSION.SDK_INT);
            if (checkingExternalStoragePermission()) {
                //fetchAudioFiles();
            } else {
                //Playing.this.finish();
            }
        } else {
            Log.i("else SDK", "" + android.os.Build.VERSION.SDK_INT);
            fetchAudioFiles();
        }

    }

    private void setSeekBarUpdating() {
        //for updating the current song duration
        seekBarUpdating = new Thread() {
            @Override
            public void run() {
                totalDuration = player.getDuration();
                currentPosition = 0;
                while (currentPosition + 1001 < totalDuration) {
                    try {
                        sleep(1001);
                        currentPosition = player.getCurrentPosition();
                        bar.setProgress((int) currentPosition);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    }

    private void setSeekBarChangedListener() {
        Log.i("seekbarchanged", "" + isSeekBarChangedListenerStarted);
        //isSeekBarChangedListenerStarted = true;
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // leftDuration.setText((String.valueOf(player.getCurrentPosition() / 1000)));
                // rightDuration.setText(String.valueOf(player.getDuration() / 1000));
                //seekBar.getProgress();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                seekBar.setProgress(0);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                player.seekTo(seekBar.getProgress());
            }
        });
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        findViewById(R.id.now_playing_layout).setVisibility(View.VISIBLE);
        findViewById(R.id.parentLayout).setVisibility(View.GONE);
        Log.i("position", "" + position);
        songPositionFromList = position;
        currentItem = (String) v.getTag();
        startPlay(currentItem);
        if (seekBarUpdating != null && !seekBarUpdating.isAlive())
            setSeekBarUpdating();
        //setSeekBarChangedListener();
    }

    public void savePlayerStates(String key, boolean value) {
        SharedPreferences playerStates = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor playerStatesEditor = playerStates.edit();
        playerStatesEditor.putBoolean(key, value);
        playerStatesEditor.commit();
        Toast.makeText(this, "Player States Saved", Toast.LENGTH_SHORT).show();
    }

    public void savePlayerIntegerStates(String key, int lastPlayedSong) {
        SharedPreferences playerStates = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor playerStatesEditor = playerStates.edit();
        playerStatesEditor.putInt(key, lastPlayedSong);
        playerStatesEditor.commit();
        Toast.makeText(this, "Player Integer States Saved", Toast.LENGTH_SHORT).show();
    }

    public void loadPlayerStates() {
        SharedPreferences playerStates = PreferenceManager.getDefaultSharedPreferences(this);
        boolean shuffleOn = playerStates.getBoolean("shuffleOn", false);
        boolean repeatOn = playerStates.getBoolean("repeatOn", false);
        boolean repeatOneOn = playerStates.getBoolean("repeatOneOn", false);
        int lastSong = playerStates.getInt("lastPlayedSong", 0);
        lastPlayedSong = lastSong;
        if (shuffleOn) {
            shuffleFlag = true;
            shuffle.setBackgroundResource(R.mipmap.shuffle);
        } else {
            shuffleFlag = false;
            shuffle.setBackgroundResource(R.mipmap.shuffle_off);
        }
        if (repeatOn && !repeatOneOn) {
            isRepeatOn = true;
            isRepeatOneOn = false;
            repeat.setBackgroundResource(R.mipmap.repeat);
        } else if (repeatOneOn && !repeatOn) {
            isRepeatOneOn = true;
            isRepeatOn = false;
            repeat.setBackgroundResource(R.mipmap.repeat_one);
        } else if (!repeatOn && !repeatOneOn) {
            isRepeatOn = false;
            isRepeatOneOn = false;
            repeat.setBackgroundResource(R.mipmap.repeat_off);
        }
        Toast.makeText(this, "Player States restored", Toast.LENGTH_SHORT).show();
    }

    private String getCurrentFileName(int currentPosition) {
        String songName = null;
        Cursor cursor = (Cursor) mediaCursorAdapter.getItem(currentPosition);
        songName = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.TITLE));
        return songName;
    }

    private String getCurrentArtistName(int currentPosition) {
        String artistName = null;
        Cursor cursor = (Cursor) mediaCursorAdapter.getItem(currentPosition);
        artistName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Artists.ARTIST));
        return artistName;
    }

    private Drawable getCurrentAlbumArt(int currentPosition) {
        String artistName = null;
        Cursor cursor = (Cursor) mediaCursorAdapter.getItem(currentPosition);
        Log.i("albumArt: ", cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART)));
        artistName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AlbumColumns.ALBUM_ART));
        //Drawable img = Drawable.createFromPath(coverPath);
        Drawable image = null;
        if (artistName != null)
            image = Drawable.createFromPath(artistName);
        //albumcover.setImageDrawable(img);
        return image;
    }

    private String getCurrentFile(int currentPosition) {
        songPositionFromList = currentPosition;
        String songData = null;
        Cursor cursor = (Cursor) mediaCursorAdapter.getItem(currentPosition);
        songData = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
        return songData;
    }

    private void startPlay(String file) {
        Log.i("Selected: ", file);
        Log.i("Selected position: ", "" + songPositionFromList);
        songList.setSelection(songPositionFromList);
        songList.setPressed(true);
        //songList.requestFocus();
        lastPlayedSong = songPositionFromList;
        bar.setProgress(0);
        player.stop();
        player.reset();
        try {
            player.setDataSource(file);
            player.prepare();
            player.start();
            /*if (!seekBarUpdating.isAlive())
                seekBarUpdating.start();*/
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        bar.setMax(player.getDuration());
        playPause.setImageResource(R.drawable.btn_pause);
        currentSong.setText(getCurrentFileName(songPositionFromList));
        currentSongArtistName.setText(getCurrentArtistName(songPositionFromList));
        //thumbnail.setImageDrawable(getCurrentAlbumArt(songPositionFromList));
        if (!checkingPreviousSongIndexDuplicity(songPositionFromList)) {
            previousSongPositions.add(songPositionFromList);
            previousSongIndex++;
        }
        updateSongInfo(songPositionFromList);
        setSeekBarChangedListener();
    }

    //checking redundancy on previous song indexing
    private boolean checkingPreviousSongIndexDuplicity(int currentPosition) {
        boolean flag = false;
        for (Integer i : previousSongPositions) {
            if (i == currentPosition) {
                flag = true;
                break;
            }
        }
        return flag;
    }

    //onBackPresses handling
    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (findViewById(R.id.current_playlist).getVisibility() == View.VISIBLE) {
            if (findViewById(R.id.search_layout).getVisibility() == View.VISIBLE) {
                findViewById(R.id.search_layout).setVisibility(View.GONE);
                findViewById(R.id.playlist_actionbar_layout).setVisibility(View.VISIBLE);
                return;
            }
            findViewById(R.id.current_playlist).setVisibility(View.GONE);
            findViewById(R.id.parentLayout).setVisibility(View.GONE);
            findViewById(R.id.now_playing_layout).setVisibility(View.VISIBLE);
            return;
        } else if (findViewById(R.id.now_playing_layout).getVisibility() == View.VISIBLE) {
            findViewById(R.id.now_playing_layout).setVisibility(View.GONE);
            findViewById(R.id.current_playlist).setVisibility(View.GONE);
            findViewById(R.id.parentLayout).setVisibility(View.VISIBLE);
            return;
        }
        if (doubleBackToExitPressedOnce) {
            moveTaskToBack(true);
        }
        if (findViewById(R.id.current_playlist).getVisibility() == View.GONE && findViewById(R.id.now_playing_layout).getVisibility() == View.GONE) {
            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();
        }
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }


    @Override
    protected void onResume() {
        super.onResume();
        songList.setAdapter(mediaCursorAdapter);
        if (state != null)
            songList.onRestoreInstanceState(state);
        Log.i("Playing", "onResumed");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i("Playing", "onRestart");
    }

    public void customToast(String text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
    }

    //reading all music files from sdCard using ArrayList<>
    public ArrayList<File> updateSongList(File root) {
        ArrayList<File> arrayList = new ArrayList<File>();
        File[] files = root.listFiles();  //all files from root directory //file array
        for (File singleFile : files) {
            if (singleFile.isDirectory() && !singleFile.isHidden()) {
                arrayList.addAll(updateSongList(singleFile));
            } else {
                //picking up only .mp3 and .wav format files
                if (singleFile.getName().endsWith(".mp3") || singleFile.getName().endsWith(".wav")) {
                    arrayList.add(singleFile);
                }
            }
        }
        return arrayList;
    }

    Parcelable state;

    @Override
    protected void onPause() {
        state = songList.onSaveInstanceState();
        super.onPause();
        Log.i("Playing:", "onPause");
        //finish();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_playing, menu);
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == android.R.id.list) {
            getMenuInflater().inflate(R.menu.menu_song_properties, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.share:
                customToast("Sharing functionality will be implemented soon :P");
                return true;
            case R.id.set_as:
                customToast("SET AS functionality will be implemented soon :P");
                return true;
            case R.id.add_to_playlist:
                customToast("Playlist will be implemented soon :P");
                return true;
            case R.id.add_quick_list:
                customToast("Quick list will be implemented soon :P");
                return true;
            case R.id.deletion: {

                customToast("Deletion will be implemented soon :P");
            }
            return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_exit:
                this.finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
        state = songList.onSaveInstanceState();
        savePlayerStates("shuffleOn", shuffleFlag);
        savePlayerStates("repeatOn", isRepeatOn);
        savePlayerStates("repeatOneOn", isRepeatOneOn);
        savePlayerIntegerStates("lastPlayedSong", lastPlayedSong);
        Log.i("Playing ", "onStop");
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        int currentSongPosition = songPositionFromList;
        Log.i("NowPlaying", "onCompletion");
        //Playing next song automatically
        mp.stop();
        mp.reset();
        //if (isDialogAOk == true)
        //updateSongInfoFromDialog(position);
        //Log.i("dialog index:", "" + position);

        if (shuffleFlag && !isRepeatOneOn) {
            songPositionFromList = randomPosition.nextInt((totalSongs - 0) + 0);
        } else if (shuffleFlag && isRepeatOn) {
            songPositionFromList = randomPosition.nextInt((totalSongs - 0) + 0);
        } else if ((!shuffleFlag && isRepeatOn) || (!shuffleFlag && !isRepeatOn) || isRepeatOn) {
            songPositionFromList = (songPositionFromList + 1) % totalSongs;
        } else if (isRepeatOneOn) {
            songPositionFromList = currentSongPosition;
        }
        startPlay(getCurrentFile(songPositionFromList));
    }

    private class MediaCursorAdapter extends SimpleCursorAdapter {

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

    private Runnable UpdateSongTimeT = new Runnable() {
        @Override
        public void run() {
            try {
                startTime = player.getCurrentPosition();
                leftDuration.setText(String.format("%d:%d",
                        TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                        TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                        toMinutes((long) startTime)))
                );
                bar.setProgress((int) startTime);
                myHandler.postDelayed(this, 100);
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
    };

    private void updateSongInfo(int currentPosition) {
        playPause.setImageResource(R.drawable.btn_pause);
        finalTime = player.getDuration();
        bar.setMax((int) finalTime);
        //bar.setProgress(0);
        //currentSong.setText("" + songs.get(thisOne).getName().replace(".mp3", "").replace(".MP3", "").replace(".wav", "").replace(".WAV", "").replace("_", " "));
        //setting player button
        if (!player.isPlaying()) {
            playPause.setImageResource(R.drawable.btn_play);
        }
        rightDuration.setText(String.format("%d:%d",
                TimeUnit.MILLISECONDS.toMinutes((long) finalTime),
                TimeUnit.MILLISECONDS.toSeconds((long) finalTime) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) finalTime)))
        );
        bar.setProgress((int) startTime);
        myHandler.postDelayed(UpdateSongTimeT, 100);
        /*if (active) {
            openActivityNotification(getApplicationContext());
		}*/
    }

    //updating position from song list dialog
    private void updateSongInfoFromDialog(int positionFromDialog) {
        currentSong.setText(getCurrentFileName(positionFromDialog));
        currentSongArtistName.setText(getCurrentArtistName(positionFromDialog));

    }

    // Filter method
    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        /*if (charText.length() == 0) {

        }
        else
        {
            for (WorldPopulation wp : arraylist)
            {
                if (wp.getCountry().toLowerCase(Locale.getDefault()).contains(charText))
                {
                    worldpopulationlist.add(wp);
                }
            }
        }
        notifyDataSetChanged();*/
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.playPause: {
                if (songPositionFromList == -1) {
                    songPositionFromList = lastPlayedSong;
                    startPlay(getCurrentFile(songPositionFromList));
                    if (seekBarUpdating != null && !seekBarUpdating.isAlive())
                        setSeekBarUpdating();
                    Log.i("seekbarchanged+-", "" + isSeekBarChangedListenerStarted);
                    //setSeekBarChangedListener();
                } else {
                    try {
                        if (player.isPlaying()) {
                            Log.i("position here if", "" + songPositionFromList);
                            player.pause();
                            playPause.setImageResource(R.drawable.btn_play);
                        } else {
                            Log.i("position here", "" + songPositionFromList);
                            player.start();
                            playPause.setImageResource(R.drawable.btn_pause);
                            updateSongInfo(songPositionFromList);
                        }
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    }
                }
                break;
            }

            //repeating functionality
            case R.id.repeat: {
                if (!isRepeatOn && !isRepeatOneOn) {
                    isRepeatOn = true;
                    isRepeatOneOn = false;
                    Toast.makeText(this, "Repeat On", Toast.LENGTH_SHORT).show();
                    repeat.setBackgroundResource(R.mipmap.repeat);
                } else if (isRepeatOn && !isRepeatOneOn) {
                    isRepeatOneOn = true;
                    isRepeatOn = false;
                    Toast.makeText(this, "RepeatOne On", Toast.LENGTH_SHORT).show();
                    repeat.setBackgroundResource(R.mipmap.repeat_one);
                } else {
                    repeat.setBackgroundResource(R.mipmap.repeat_off);
                    Toast.makeText(this, "Repeat Off", Toast.LENGTH_SHORT).show();
                    isRepeatOn = false;
                    isRepeatOneOn = false;
                }
                break;
            }

            //shuffling songs sequence
            case R.id.shuffle: {
                if (!shuffleFlag) {
                    shuffle.setBackgroundResource(R.mipmap.shuffle);
                    Toast.makeText(this, "Shuffle On", Toast.LENGTH_SHORT).show();
                    shuffleFlag = true;
                } else {
                    shuffle.setBackgroundResource(R.mipmap.shuffle_off);
                    Toast.makeText(this, "Shuffle Off", Toast.LENGTH_SHORT).show();
                    shuffleFlag = false;
                }
                break;
            }
            //show songList
            case R.id.imageViewSongList: {
                /*//Toast.makeText(NowPlaying.this, "Under Construction", Toast.LENGTH_SHORT).show();
                //final Dialog dialog = new Dialog(this, android.R.style.Theme_DeviceDefault_Dialog);
                final Dialog dialog = new Dialog(this);
                dialog.setTitle("Your List");
                dialog.setContentView(R.layout.songs_dialog);

                //songList from the NowPlaying activity
                ListView listView = (ListView) dialog.findViewById(R.id.songsDialog);
                listView.setAdapter(mediaCursorAdapter);
                listView.setSelection(songPositionFromList);
                listView.requestFocus();
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        startPlay(getCurrentFile(position));
                        updateSongInfoFromDialog(position);
                        dialog.dismiss();
                    }
                });
                dialog.show();*/
                findViewById(R.id.now_playing_layout).setVisibility(View.GONE);
                findViewById(R.id.current_playlist).setVisibility(View.VISIBLE);
                currentPlayList.setAdapter(mediaCursorAdapter);
                currentPlayList.setSelection(songPositionFromList);
                currentPlayList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        startPlay(getCurrentFile(position));
                        updateSongInfoFromDialog(position);
                        if (findViewById(R.id.now_playing_layout).getVisibility() == View.GONE) {
                            if (findViewById(R.id.search_layout).getVisibility() == View.VISIBLE) {
                                findViewById(R.id.search_layout).setVisibility(View.GONE);
                                findViewById(R.id.playlist_actionbar_layout).setVisibility(View.VISIBLE);
                            }
                            findViewById(R.id.now_playing_layout).setVisibility(View.VISIBLE);
                            findViewById(R.id.parentLayout).setVisibility(View.GONE);
                            findViewById(R.id.current_playlist).setVisibility(View.GONE);
                        }
                    }
                });
                break;
            }
            //back to songList from player
            case R.id.songList: {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (findViewById(R.id.now_playing_layout).getVisibility() == View.VISIBLE) {
                            findViewById(R.id.now_playing_layout).setVisibility(View.GONE);
                            findViewById(R.id.parentLayout).setVisibility(View.VISIBLE);
                        }
                    }
                }, 100);
                break;
            }

            //next song button action
            case R.id.next: {
                try {
                    /*player.stop();
                    player.reset();*/
                    if (shuffleFlag) {
                        songPositionFromList = randomPosition.nextInt((totalSongs - 0) + 0);
                    } else {
                        songPositionFromList = (songPositionFromList + 1) % totalSongs;
                    }
                    /*uri = Uri.parse(mediaCursorAdapter.getCursor().getString(songPositionFromList));
                    try {
                        player.setDataSource(getApplicationContext(), uri);
                        player.prepare();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //Log.i("songPositionFromList ", "" + songPositionFromList);
                    player.start();
                    updateSongInfo(songPositionFromList);
                    playPause.setImageResource(R.drawable.btn_pause);*/
                    if (songPositionFromList == -1) {
                        startPlay(getCurrentFile(lastPlayedSong));
                        Log.i("seekbarchangedNext+", "" + isSeekBarChangedListenerStarted);
                        //setSeekBarChangedListener();
                    } else {
                        startPlay(getCurrentFile(songPositionFromList));
                        Log.i("seekbarchangedNext-", "" + isSeekBarChangedListenerStarted);
                        //setSeekBarChangedListener();
                    }
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
                break;
            }

            //previous song button action
            case R.id.previous: {
                try {
                    /*player.stop();
                    player.reset();*/
                    //Log.i("Previous ", "" + songPositionFromList);
                    /*if (songPositionFromList - 1 < 0) {
                        if (shuffleFlag) {
							songPositionFromList = randomPosition.nextInt((totalSongs - 0) + 0);
						} else {
							songPositionFromList = totalSongs - 1;
						}
					} else {
						if (shuffleFlag)
							songPositionFromList = randomPosition.nextInt((totalSongs - 0) + 0);
						else
							songPositionFromList--;
					}

					Log.i("PreviousFile ", songPositionFromList + "");*/
                    if (previousSongPositions.size() > 0) {
                        previousSongIndex--;
                        if (previousSongIndex >= 0 && previousSongIndex < totalSongs) {
                            startPlay(getCurrentFile(previousSongPositions.get(previousSongIndex)));
                            Log.i("seekbarchangedPre+", "" + isSeekBarChangedListenerStarted);
                            //setSeekBarChangedListener();
                        } else {
                            startPlay(getCurrentFile(previousSongPositions.get(0)));
                            Log.i("seekbarchangedPre-", "" + isSeekBarChangedListenerStarted);
                            //setSeekBarChangedListener();
                        }
                    } else {
                        startPlay(getCurrentFile(lastPlayedSong));
                    }
                    //uri = Uri.parse(songList.getItemAtPosition(songPositionFromList).toString());
                    //uri = Uri.parse(songs.get(songPositionFromList).toString());
                    /*try {
                        //player.setDataSource(getApplicationContext(), uri);
                        player.setDataSource(currentFile);
                        player.prepare();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    playPause.setImageResource(R.drawable.btn_pause);
                    //Log.i("songPositionFromList ", "" + songPositionFromList);-
                    player.start();
                    updateSongInfo(songPositionFromList);*/
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
                break;
            }
            case R.id.playingSong: {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (findViewById(R.id.now_playing_layout).getVisibility() == View.GONE) {
                            findViewById(R.id.now_playing_layout).setVisibility(View.VISIBLE);
                            findViewById(R.id.parentLayout).setVisibility(View.GONE);
                        }
                    }
                }, 400);
                break;
            }
            case R.id.shuffle_all_songs: {
                Toast.makeText(this, "Not implemented yet :(", Toast.LENGTH_SHORT).show();
                break;
            }
            case R.id.playlist_action_bar_back_btn: {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (findViewById(R.id.now_playing_layout).getVisibility() == View.GONE) {
                            findViewById(R.id.now_playing_layout).setVisibility(View.VISIBLE);
                            findViewById(R.id.parentLayout).setVisibility(View.GONE);
                            findViewById(R.id.current_playlist).setVisibility(View.GONE);
                        }
                    }
                }, 300);
                break;
            }
            case R.id.playlist_action_bar_search_btn: {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.playlist_actionbar_layout).setVisibility(View.GONE);
                    }
                }, 300);

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        search_song_et.setText("");
                        findViewById(R.id.search_layout).setVisibility(View.VISIBLE);
                        search_song_et.requestFocus();
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(search_song_et, InputMethodManager.SHOW_IMPLICIT);
                    }
                }, 500);
                //searching songs
                search_song_et.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        Playing.this.mediaCursorAdapter.getFilter().filter(s);
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        //String searchString = search_song_et.getText().toString().toLowerCase(Locale.getDefault());

                    }
                });
                break;
            }
            case R.id.search_back_btn: {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.search_layout).setVisibility(View.GONE);
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(search_song_et.getWindowToken(), 0);
                    }
                }, 300);

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.playlist_actionbar_layout).setVisibility(View.VISIBLE);
                    }
                }, 500);
                break;
            }
            default:
        }
    }
}
