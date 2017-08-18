package musician.kuet.musta;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class MainActivity extends RootMediaActivity implements View.OnClickListener, MediaPlayer.OnCompletionListener,
        AdapterView.OnItemClickListener {
    private final static int READ_EXTERNAL_STORAGE_REQUEST_CODE = 201;
    private final static int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 202;
    private static final int CUSTOM_NOTI_PREVIOUS_SONG_ID = 203;
    private static final int CUSTOM_NOTI_PLAY_PAUSE_ID = 204;
    private static final int CUSTOM_NOTI_NEXT_SONG_ID = 205;
    static boolean isPlugUnplugOccurred = false, shuffleFlag = false, isRepeatOneOn = false, isRepeatOn = false,
            isSeekBarChangedListenerStarted = false, isPhoneCallOccurred = false, isPlayerStartedFirstTimeYet = false;
    public String CUSTOM_NOTI_PREVIOUS_SONG = "android.intent.action.CUSTOM_NOTI_PREVIOUS_SONG";
    public String CUSTOM_NOTI_PLAY_PAUSE = "android.intent.action.CUSTOM_NOTI_PLAY_PAUSE";
    public String CUSTOM_NOTI_NEXT_SONG = "android.intent.action.CUSTOM_NOTI_NEXT_SONG";
    //private static final String TAG = null;
    ListView songList, currentPlayList;
    Button playingSong, playlist_action_bar_back_btn, search_back_btn;
    SearchView searchView;
    FrameLayout playlist_action_bar_search_btn;
    EditText search_song_et;
    int songPositionFromList = -1, totalSongs = 0, previousSongIndex = 0, lastPlayedSong = 0;
    List<Integer> previousSongPositions;
    String currentFile;
    Thread seekBarUpdating;
    long totalDuration = 0;
    long currentPosition = 0;
    SeekBar bar;
    ImageView preSong, nextSong, playPause, thumbnail, playPauseState;
    Button shuffle, repeat, nowPlayingSongs, goToSongList, allowPermission;
    TextView currentSong, currentSongState, currentSongArtistNameState, leftDuration, rightDuration, tvSongsSize,
            currentSongArtistName, current_playlist_action_bar_activity_content;
    String currentItem = "";
    ArrayList<File> songs, songs2;
    int songsSize = 0;
    Random randomPosition = new Random();
    ArrayAdapter<String> adapterForDialog;
    List<Integer> filteredIndexes;
    int filteredIndexPosition;
    //Cursor gCursor = null;
    MenuItem menuSearchItem = null, menuPlaylistItem = null;
    FloatingActionButton homeFab = null;
    //private ActionBar actionBar = null;
    Toolbar actionBar = null;
    LinearLayout showPlayerState = null;
    TelephonyManager telephonyManager = null;
    BroadcastReceiver broadcastReceiver;
    Intent btn_noti_prev_song_btn_intent, btn_noti_play_pause_btn_intent, btn_noti_next_song_btn_intent;
    IntentFilter intentFilterPrevSong = null;
    IntentFilter intentFilterPlayPause = null;
    IntentFilter intentFilterNextSong = null;
    //onBackPresses handling
    boolean doubleBackToExitPressedOnce = false;
    private double startTime = 0;
    private double finalTime = 0;
    private Handler myHandler = new Handler();
    //private MediaCursorAdapter mediaCursorAdapter = null;
    private MediaPlayer player = null;
    PhoneStateListener phoneStateListener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            if (state == TelephonyManager.CALL_STATE_RINGING) {
                //Incoming call: Pause music
                Log.i("CALL_STATE_RINGING ", "called");
                if (player.isPlaying()) {
                    isPhoneCallOccurred = true;
                    player.pause();
                }
            } else if (state == TelephonyManager.CALL_STATE_IDLE) {
                //Not in call: Play music
                Log.i("CALL_STATE_IDLE ", "called");
                if (player != null && !player.isPlaying() && isPhoneCallOccurred) {
                    player.start();
                    isPhoneCallOccurred = false;
                }
            } else if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
                //A call is dialing, active or on hold
                Log.i("CALL_STATE_OFFHOOK ", "called");
                if (player.isPlaying()) {
                    isPhoneCallOccurred = true;
                    player.pause();
                }
            }
            super.onCallStateChanged(state, incomingNumber);
        }
    };
    private IntentFilter headsetUnpluggedIntentFilter = null, headsetPlugUnplugIntentFilter = null;
    private String TAG = null;
    private boolean isPermissionGranted = false;
    private boolean isPermissionRequested = false;
    //Notification
    private NotificationCompat.Builder builder = null;
    private NotificationManager notificationManager = null;
    private RemoteViews remoteViews;
    private RemoteViews smallRemoteViews;
    private Context mContext;
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
    private BroadcastReceiver mNoisyAudioReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            /*if(intent.getAction().equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY)){
                Log.i("Headset ","disconnected");
            }else
            */
            if (intent.getAction().equals(AudioManager.ACTION_HEADSET_PLUG)) {
                int state = intent.getIntExtra("state", -1);
                switch (state) {
                    case 0: {
                        Log.i("state", "Headset is unplugged");
                        if (isPlugUnplugOccurred && player.isPlaying()) {
                            setPlayPauseButtonClickListener();
                            isPlugUnplugOccurred = false;
                        }
                        break;
                    }
                    case 1: {
                        Log.i("state", "Headset is plugged");
                        if (player != null && !isPlugUnplugOccurred) {
                            isPlugUnplugOccurred = true;
                            //setPlayPauseButtonClickListener();
                        }
                        break;
                    }
                    default:
                        Log.i("state", "I dunno");
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TAG = getApplicationContext().getClass().getSimpleName().toString();
        allowPermission = (Button) findViewById(R.id.allow_permission);
        allowPermission.setOnClickListener(this);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE_REQUEST_CODE);
            isPermissionRequested = true;
        } else {
            isPermissionGranted = true;
        }

        if (isPermissionGranted && !isPermissionRequested) {
            findViewById(R.id.fake_layout).setVisibility(View.GONE);
            findViewById(R.id.main_layout).setVisibility(View.VISIBLE);
            setupWizard();
        } else {
            findViewById(R.id.fake_layout).setVisibility(View.VISIBLE);
            findViewById(R.id.main_layout).setVisibility(View.GONE);
        }
    }

    private void setupWizard() {
        actionBar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(actionBar);
        initialization();
        //load player state from sharedPreferences
        loadPlayerStates();
        currentSongState.setText(getCurrentFileName(lastPlayedSong));
        currentSongArtistNameState.setText(getCurrentArtistName(lastPlayedSong));
        //new MediaPlayer
        player = new MediaPlayer();
        player.setOnCompletionListener(this);
        previousSongPositions = new ArrayList<Integer>();
        alreadyFetchedAudioFiles();
        telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        headsetUnpluggedIntentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        headsetPlugUnplugIntentFilter = new IntentFilter(AudioManager.ACTION_HEADSET_PLUG);
        if (isPostLollipop())
            setupNotificationControl();
        else Log.i("Android ", "" + Build.VERSION.SDK_INT);
    }

    private boolean isPostLollipop() {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);
    }

    private void setupNotificationControl() {
        //notification implementation
        mContext = this;
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        remoteViews = new RemoteViews(getPackageName(), R.layout.custom_notification);
        smallRemoteViews = new RemoteViews(getPackageName(), R.layout.custom_notification_small_view);
        //smaller
        smallRemoteViews.setImageViewResource(R.id.noti_previous_song, R.drawable.noti_prev_song);
        smallRemoteViews.setImageViewResource(R.id.noti_play_pause, R.drawable.noti_play_pause);
        smallRemoteViews.setImageViewResource(R.id.noti_next_song, R.drawable.noti_next_song);
        smallRemoteViews.setImageViewResource(R.id.noti_icon, R.mipmap.ic_launcher);
        smallRemoteViews.setTextViewText(R.id.noti_current_song, "" + currentSongState.getText().toString());
        smallRemoteViews.setTextViewText(R.id.noti_current_artist_name, "" + currentSongArtistNameState.getText().toString());
        //bigger
        remoteViews.setImageViewResource(R.id.noti_previous_song, R.drawable.noti_prev_song);
        remoteViews.setImageViewResource(R.id.noti_play_pause, R.drawable.noti_play_pause);
        remoteViews.setImageViewResource(R.id.noti_next_song, R.drawable.noti_next_song);
        remoteViews.setImageViewResource(R.id.noti_icon, R.mipmap.ic_launcher);
        remoteViews.setTextViewText(R.id.noti_current_song, "" + currentSongState.getText().toString());
        remoteViews.setTextViewText(R.id.noti_current_artist_name, "" + currentSongArtistNameState.getText().toString());

        btn_noti_prev_song_btn_intent = new Intent(CUSTOM_NOTI_PREVIOUS_SONG);
        btn_noti_prev_song_btn_intent.putExtra("prev_id", CUSTOM_NOTI_PREVIOUS_SONG_ID);
        sendBroadcast(btn_noti_prev_song_btn_intent);

        btn_noti_play_pause_btn_intent = new Intent(CUSTOM_NOTI_PLAY_PAUSE);
        btn_noti_play_pause_btn_intent.putExtra("play_pause_id", CUSTOM_NOTI_PLAY_PAUSE_ID);
        sendBroadcast(btn_noti_play_pause_btn_intent);

        btn_noti_next_song_btn_intent = new Intent(CUSTOM_NOTI_NEXT_SONG);
        btn_noti_next_song_btn_intent.putExtra("next_id", CUSTOM_NOTI_NEXT_SONG_ID);
        sendBroadcast(btn_noti_next_song_btn_intent);

        PendingIntent pendingIntentForPrevSong = PendingIntent.getBroadcast(mContext, 111, btn_noti_prev_song_btn_intent, 0);
        remoteViews.setOnClickPendingIntent(R.id.noti_previous_song, pendingIntentForPrevSong);
        smallRemoteViews.setOnClickPendingIntent(R.id.noti_previous_song, pendingIntentForPrevSong);
        intentFilterPrevSong = new IntentFilter(CUSTOM_NOTI_PREVIOUS_SONG);

        PendingIntent pendingIntentForPlayPause = PendingIntent.getBroadcast(mContext, 112, btn_noti_play_pause_btn_intent, 0);
        remoteViews.setOnClickPendingIntent(R.id.noti_play_pause, pendingIntentForPlayPause);
        smallRemoteViews.setOnClickPendingIntent(R.id.noti_play_pause, pendingIntentForPlayPause);
        intentFilterPlayPause = new IntentFilter(CUSTOM_NOTI_PLAY_PAUSE);

        PendingIntent pendingIntentForNextSong = PendingIntent.getBroadcast(mContext, 113, btn_noti_next_song_btn_intent, 0);
        remoteViews.setOnClickPendingIntent(R.id.noti_next_song, pendingIntentForNextSong);
        smallRemoteViews.setOnClickPendingIntent(R.id.noti_next_song, pendingIntentForNextSong);
        intentFilterNextSong = new IntentFilter(CUSTOM_NOTI_NEXT_SONG);

        broadcastReceiver = new ButtonClickListenerEvent();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case READ_EXTERNAL_STORAGE_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    findViewById(R.id.fake_layout).setVisibility(View.GONE);
                    findViewById(R.id.main_layout).setVisibility(View.VISIBLE);
                    setupWizard();
                    isPermissionGranted = true;
                } else {
                    isPermissionGranted = false;
                    findViewById(R.id.fake_layout).setVisibility(View.VISIBLE);
                    findViewById(R.id.main_layout).setVisibility(View.GONE);
                }
                break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        //invalidateOptionsMenu();
        menuSearchItem = menu.findItem(R.id.main_action_search);
        menuPlaylistItem = menu.findItem(R.id.main_action_current_playlist);
        if (findViewById(R.id.now_playing_layout).getVisibility() == View.GONE)
            menuPlaylistItem.setVisible(false);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuSearchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filteredIndexes = new ArrayList<Integer>();
                filteredIndexPosition = 0;
                ArrayList<String> currentFilteredList = new ArrayList<String>();
                for (gCursor.moveToFirst(); !gCursor.isAfterLast(); gCursor.moveToNext()) {
                    if (gCursor.getString(gCursor.getColumnIndex(MediaStore.MediaColumns.TITLE)).toLowerCase().contains(query.toLowerCase()) || gCursor.getString(gCursor.getColumnIndex(MediaStore.Audio.Artists.ARTIST)).toLowerCase().contains(query.toLowerCase())) {
                        currentFilteredList.add(gCursor.getString(gCursor.getColumnIndex(MediaStore.MediaColumns.TITLE)));
                        filteredIndexes.add(gCursor.getPosition());
                        filteredIndexPosition++;
                    }
                }
                ArrayAdapter<String> filteredAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, currentFilteredList);
                songList.setAdapter(filteredAdapter);
                songList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Logging.getInstance().I("Filtered", "onItemClicked " + position);
                        Logging.getInstance().I("Filtered", "onItemClicked++ " + filteredIndexes.get(position));
                        songPositionFromList = filteredIndexes.get(position);
                        startPlay(getCurrentFile(songPositionFromList));
                        //updateSongInfoFromDialog(position);
                        Log.i("Search result", "" + position);
                        /*if (findViewById(R.id.now_playing_layout).getVisibility() == View.GONE) {
                            findViewById(R.id.now_playing_layout).setVisibility(View.VISIBLE);
                            //findViewById(R.id.parentLayout).setVisibility(View.GONE);
                            //findViewById(R.id.current_playlist).setVisibility(View.GONE);
                        }*/
                        visibleAnimation(findViewById(R.id.now_playing_layout), findViewById(R.id.home_page_song_list_layout), 700);
                    }
                });
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                Logging.getInstance().I(TAG, "MainActivity Back btn");
                /*findViewById(R.id.now_playing_layout).setVisibility(View.GONE);
                findViewById(R.id.home_page_song_list_layout).setVisibility(View.VISIBLE);
				setActionBarStatus();*/
                visibleAnimation(findViewById(R.id.home_page_song_list_layout), findViewById(R.id.now_playing_layout), 700);
                return true;
            }
            case R.id.main_action_search: {
                return true;
            }
            case R.id.main_action_current_playlist: {
                Intent intent = new Intent(MainActivity.this, CurrentPlaylistActivity.class);
                intent.putExtra("currentPosition", songPositionFromList);
                startActivityForResult(intent, 200);
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (findViewById(R.id.now_playing_layout).getVisibility() == View.VISIBLE) {
            /*findViewById(R.id.now_playing_layout).setVisibility(View.GONE);
            findViewById(R.id.home_page_song_list_layout).setVisibility(View.VISIBLE);
			setActionBarStatus();*/
            visibleAnimation(findViewById(R.id.home_page_song_list_layout), findViewById(R.id.now_playing_layout), 700);
            return;
        }
        if (doubleBackToExitPressedOnce) {
            moveTaskToBack(true);
            //finish();
        }
        if (findViewById(R.id.now_playing_layout).getVisibility() == View.GONE || findViewById(R.id.home_page_song_list_layout).getVisibility() == View.VISIBLE) {
            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
        //super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isPermissionGranted && !isPermissionRequested) {
            songList.setAdapter(rootMediaCursorAdapter);
            /*if (state != null)
                songList.onRestoreInstanceState(state);*/
            if (telephonyManager != null) {
                telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
                Log.i("telephonyManager", "not null onResume");
            }
            if (mNoisyAudioReceiver != null) {
                //registerReceiver(mNoisyAudioReceiver, headsetUnpluggedIntentFilter);
                registerReceiver(mNoisyAudioReceiver, headsetPlugUnplugIntentFilter);
            }
        }
        if (isPostLollipop()) {
            Logging.getInstance().I(TAG, "onResumed");
            registerReceiver(broadcastReceiver, intentFilterPrevSong);
            registerReceiver(broadcastReceiver, intentFilterPlayPause);
            registerReceiver(broadcastReceiver, intentFilterNextSong);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Logging.getInstance().I(TAG, "onRestart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        //state = songList.onSaveInstanceState();
        savePlayerStates("shuffleOn", shuffleFlag);
        savePlayerStates("repeatOn", isRepeatOn);
        savePlayerStates("repeatOneOn", isRepeatOneOn);
        savePlayerIntegerStates("lastPlayedSong", lastPlayedSong);

        if (isPostLollipop()) {
            Logging.getInstance().I(TAG, "onStop");
            Intent notificationIntent = new Intent(mContext, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, notificationIntent, 0);
            PendingIntent pendingIntentSmall = PendingIntent.getActivity(mContext, 0, notificationIntent, 0);
            builder = new NotificationCompat.Builder(mContext);
            builder.setCustomContentView(smallRemoteViews)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntentSmall);

            builder.setAutoCancel(true)
                    .setCustomBigContentView(remoteViews)
                    .setContentIntent(pendingIntent);
            notificationManager.notify(CUSTOM_NOTI_PREVIOUS_SONG_ID, builder.build());
        /*notificationManager.notify(CUSTOM_NOTI_PLAY_PAUSE_ID, builder.build());
        notificationManager.notify(CUSTOM_NOTI_NEXT_SONG_ID, builder.build());*/
        }
    }

    @Override
    protected void onPause() {
        //state = songList.onSaveInstanceState();
        super.onPause();
        if (telephonyManager != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
            Log.i("telephonyManager", "not null onPause");
        }
        if (isPermissionGranted && !isPermissionRequested && mNoisyAudioReceiver != null) {
            registerReceiver(mNoisyAudioReceiver, headsetPlugUnplugIntentFilter);
        }
        Logging.getInstance().I(TAG, "onPause");
        //finish();
    }

    private void visibleAnimation(View visible, View gone, int duration) {
        TranslateAnimation animationView = new TranslateAnimation(0, -gone.getWidth(), 0, 0);
        animationView.setDuration(duration);
        animationView.setFillBefore(true);
        visible.startAnimation(animationView);
        visible.setVisibility(View.VISIBLE);
        gone.setVisibility(View.GONE);
        setActionBarStatus();
    }

    //Parcelable state;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case (200): {
                if (resultCode == Activity.RESULT_OK) {
                    songPositionFromList = data.getIntExtra("resultantPosition", -1);
                    Log.i("CurrentPlaylist", "onActivityResult Clicked" + songPositionFromList);
                    startPlay(getCurrentFile(songPositionFromList));
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("onDestroy", "phone state");
        //state = songList.onSaveInstanceState();
        if (telephonyManager != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
            Log.i("Unregistered", "phone state");
        }
        if (mNoisyAudioReceiver != null)
            unregisterReceiver(mNoisyAudioReceiver);
        if (player.isPlaying()) {
            player.stop();
        }
        player.reset();
        player.release();
        unregisterReceiver(broadcastReceiver);
    }

    private void setPlayPauseButtonClickListener() {
        Log.i("setPlayPauseButton", "ClickListener" + songPositionFromList);
        if (!isPlayerStartedFirstTimeYet || songPositionFromList == -1) {
            songPositionFromList = lastPlayedSong;
            startPlay(getCurrentFile(songPositionFromList));
            if (seekBarUpdating != null && !seekBarUpdating.isAlive())
                setSeekBarUpdating();
        } else {
            try {
                if (player.isPlaying()) {
                    player.pause();
                    setPlayerStates();
                } else {
                    player.start();
                    updateSongInfo();
                    setPlayerStates();
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }

    private void setNextSongButtonClickListener() {
        try {
                    /*player.stop();
                    player.reset();*/
            if (shuffleFlag) {
                songPositionFromList = randomPosition.nextInt((totalSongs - 0) + 0);
            } else {
                songPositionFromList = (songPositionFromList + 1) % totalSongs;
            }
            if (songPositionFromList == -1) {
                Log.i("Next button clicked", "no song selected " + lastPlayedSong);
                startPlay(getCurrentFile(lastPlayedSong));
                //Log.i("seekbarchangedNext+", "" + isSeekBarChangedListenerStarted);
                //setSeekBarChangedListener();
            } else {
                startPlay(getCurrentFile(songPositionFromList));
                Log.i("Next button clicked", "Player already running" + lastPlayedSong);
                //Log.i("seekbarchangedNext-", "" + isSeekBarChangedListenerStarted);
                //setSeekBarChangedListener();
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    private void setPreviousSongButtonClickListener() {
        try {
            Log.i("previous list size", "" + previousSongPositions.size());
            if (previousSongPositions.size() > 0) {
                previousSongIndex--;
                if (previousSongIndex >= 0 && previousSongIndex < totalSongs) {
                    Log.i("Previous button clicked", "available pre " + previousSongPositions.get(previousSongIndex));
                    songPositionFromList = previousSongPositions.get(previousSongIndex);
                    startPlay(getCurrentFile(songPositionFromList));
                    //Log.i("seekbarchangedPre+", "" + isSeekBarChangedListenerStarted);
                    //setSeekBarChangedListener();
                } else {
                    songPositionFromList = previousSongPositions.get(0);
                    startPlay(getCurrentFile(songPositionFromList));
                    Log.i("Previous button clicked", "available pre " + previousSongPositions.get(0));
                    //setSeekBarChangedListener();
                }
            } else {
                songPositionFromList = lastPlayedSong;
                startPlay(getCurrentFile(songPositionFromList));
                Log.i("Previous button clicked", "last played song" + lastPlayedSong);
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
    }

    public void customToast(String text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
    }

    private void updateSongInfo() {
        finalTime = player.getDuration();
        bar.setMax((int) finalTime);
        rightDuration.setText(String.format("%d:%d",
                TimeUnit.MILLISECONDS.toMinutes((long) finalTime),
                TimeUnit.MILLISECONDS.toSeconds((long) finalTime) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) finalTime)))
        );
        bar.setProgress((int) startTime);
        myHandler.postDelayed(UpdateSongTimeT, 100);
    }

    private void setPlayerStates() {
        if (player.isPlaying()) {
            playPause.setImageResource(R.drawable.player_pause_btn);
            playPauseState.setImageResource(R.drawable.ic_action_pause);
            if (remoteViews != null) {
                remoteViews.setImageViewResource(R.id.noti_play_pause, R.drawable.noti_play_pause_pausing);
                smallRemoteViews.setImageViewResource(R.id.noti_play_pause, R.drawable.noti_play_pause_pausing);
                if (notificationManager != null && builder != null)
                    notificationManager.notify(CUSTOM_NOTI_PREVIOUS_SONG_ID, builder.build());
            }
        } else {
            playPause.setImageResource(R.drawable.player_play_btn);
            playPauseState.setImageResource(R.drawable.ic_action_play);
            if (remoteViews != null) {
                remoteViews.setImageViewResource(R.id.noti_play_pause, R.drawable.noti_play_pause);
                smallRemoteViews.setImageViewResource(R.id.noti_play_pause, R.drawable.noti_play_pause);
                if (notificationManager != null && builder != null)
                    notificationManager.notify(CUSTOM_NOTI_PREVIOUS_SONG_ID, builder.build());
            }
        }
    }

    public void initialization() {
        playingSong = (Button) findViewById(R.id.playingSong);
        playlist_action_bar_back_btn = (Button) findViewById(R.id.playlist_action_bar_back_btn);
        current_playlist_action_bar_activity_content = (TextView) findViewById(R.id.current_playlist_action_bar_activity_content);
        playlist_action_bar_search_btn = (FrameLayout) findViewById(R.id.playlist_action_bar_search_btn);
        search_back_btn = (Button) findViewById(R.id.search_back_btn);
        search_song_et = (EditText) findViewById(R.id.search_song_et);
        searchView = (SearchView) findViewById(R.id.searchView);
        /*floatingActionButton = (FloatingActionButton) findViewById(R.id.shuffle_all_songs);
        floatingActionButton.setOnClickListener(this);*/
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
        currentSongState = (TextView) findViewById(R.id.currentSongState);
        currentSongArtistNameState = (TextView) findViewById(R.id.currentSongArtistNameState);
        currentSongArtistName = (TextView) findViewById(R.id.currentSongArtistName);
        leftDuration = (TextView) findViewById(R.id.leftDuration);
        rightDuration = (TextView) findViewById(R.id.rightDuration);
        goToSongList = (Button) findViewById(R.id.songList);
        playPauseState = (ImageView) findViewById(R.id.playPauseState);
        shuffle = (Button) findViewById(R.id.shuffle);
        repeat = (Button) findViewById(R.id.repeat);
        nowPlayingSongs = (Button) findViewById(R.id.imageViewSongList);

        preSong.setOnClickListener(this);
        nextSong.setOnClickListener(this);
//		leftSeek.setOnClickListener(this);
//		rightSeek.setOnClickListener(this);
        playPause.setOnClickListener(this);
        goToSongList.setOnClickListener(this);
        playPauseState.setOnClickListener(this);
        shuffle.setOnClickListener(this);
        repeat.setOnClickListener(this);
        playingSong.setOnClickListener(this);
//		playlist_action_bar_search_btn.setOnClickListener(this);
//		search_back_btn.setOnClickListener(this);
//		playlist_action_bar_back_btn.setOnClickListener(this);
        //player.setOnCompletionListener(this);
        nowPlayingSongs.setOnClickListener(this);
        homeFab = (FloatingActionButton) findViewById(R.id.fab_main);
        homeFab.setOnClickListener(this);
        showPlayerState = (LinearLayout) findViewById(R.id.showPlayerState);
        showPlayerState.setOnClickListener(this);
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

    private void alreadyFetchedAudioFiles() {
        if (isPermissionGranted && null != gCursor) {
            totalSongs = rootMediaCursorAdapter.getCount();
            songList.setAdapter(rootMediaCursorAdapter);
            songList.setOnItemClickListener(this);
        }
        tvSongsSize.setText(totalSongs + " Songs");
        if (seekBarUpdating != null && !seekBarUpdating.isAlive())
            setSeekBarUpdating();
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
                seekBar.getProgress();
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

    public void savePlayerStates(String key, boolean value) {
        SharedPreferences playerStates = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor playerStatesEditor = playerStates.edit();
        playerStatesEditor.putBoolean(key, value);
        playerStatesEditor.commit();
        //Toast.makeText(this, "Player States Saved", Toast.LENGTH_SHORT).show();
    }

    public void savePlayerIntegerStates(String key, int lastPlayedSong) {
        SharedPreferences playerStates = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor playerStatesEditor = playerStates.edit();
        playerStatesEditor.putInt(key, lastPlayedSong);
        playerStatesEditor.commit();
        //Toast.makeText(this, "Player Integer States Saved", Toast.LENGTH_SHORT).show();
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
            shuffle.setBackgroundResource(R.drawable.player_shuffle_on_btn);
        } else {
            shuffleFlag = false;
            shuffle.setBackgroundResource(R.drawable.player_shuffle_off_btn);
        }
        if (repeatOn && !repeatOneOn) {
            isRepeatOn = true;
            isRepeatOneOn = false;
            repeat.setBackgroundResource(R.drawable.player_repeat_on_btn);
        } else if (repeatOneOn && !repeatOn) {
            isRepeatOneOn = true;
            isRepeatOn = false;
            repeat.setBackgroundResource(R.drawable.player_repeat_one_on_btn);
        } else if (!repeatOn && !repeatOneOn) {
            isRepeatOn = false;
            isRepeatOneOn = false;
            repeat.setBackgroundResource(R.drawable.player_repeat_off_btn);
        }
        //Toast.makeText(this, "Player States restored", Toast.LENGTH_SHORT).show();
    }

    private void setActionBarStatus() {
        if (songPositionFromList == -1)
            songPositionFromList = lastPlayedSong;
        currentSongState.setText(getCurrentFileName(songPositionFromList));
        currentSongArtistNameState.setText(getCurrentArtistName(songPositionFromList));
        currentSong.setText(getCurrentFileName(songPositionFromList));
        currentSongArtistName.setText(getCurrentArtistName(songPositionFromList));
        if (smallRemoteViews != null) {
            //notification update
            smallRemoteViews.setTextViewText(R.id.noti_current_song, "" + currentSongState.getText().toString());
            smallRemoteViews.setTextViewText(R.id.noti_current_artist_name, "" + currentSongArtistNameState.getText().toString());
            remoteViews.setTextViewText(R.id.noti_current_song, "" + currentSongState.getText().toString());
            remoteViews.setTextViewText(R.id.noti_current_artist_name, "" + currentSongArtistNameState.getText().toString());
            if (notificationManager != null && builder != null)
                notificationManager.notify(CUSTOM_NOTI_PREVIOUS_SONG_ID, builder.build());
        }
        //albumart setup
        Bitmap bitmap = null;
        bitmap = getCurrentAlbumArt(mContext, songPositionFromList);
        if (bitmap != null) {
            thumbnail.setImageBitmap(bitmap);
            thumbnail.setScaleType(ImageView.ScaleType.FIT_XY);
        } else {
            thumbnail.setImageResource(R.drawable.thumbnail);
            thumbnail.setScaleType(ImageView.ScaleType.FIT_XY);
            //Toast.makeText(mContext, "No albumart found for this song", Toast.LENGTH_SHORT).show();
        }

        if (findViewById(R.id.now_playing_layout).getVisibility() == View.VISIBLE) {
            //homeFab.setVisibility(View.GONE);
            findViewById(R.id.player_layout).setVisibility(View.GONE);
            menuSearchItem.setVisible(false);
            menuPlaylistItem.setVisible(true);
            actionBar.setTitle("" + getCurrentFileName(songPositionFromList));
            actionBar.setSubtitle("" + getCurrentArtistName(songPositionFromList));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            //actionBar.setHomeButtonEnabled(true);

        } else {
            //homeFab.setVisibility(View.VISIBLE);
            findViewById(R.id.player_layout).setVisibility(View.VISIBLE);
            menuSearchItem.setVisible(true);
            menuPlaylistItem.setVisible(false);
            actionBar.setTitle(getString(R.string.app_name));
            actionBar.setSubtitle(null);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            //actionBar.setHomeButtonEnabled(false);
        }
    }

    private void startPlay(String file) {
        Log.i("Selected: ", file);
        Log.i("Selected position: ", "" + songPositionFromList);
        songList.setSelection(songPositionFromList);
        songList.setPressed(true);
        //songList.requestFocus();
        lastPlayedSong = songPositionFromList;
        isPlayerStartedFirstTimeYet = true;
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
        /*if (findViewById(R.id.now_playing_layout).getVisibility() == View.GONE || findViewById(R.id.home_page_song_list_layout).getVisibility() == View.VISIBLE) {
            findViewById(R.id.home_page_song_list_layout).setVisibility(View.GONE);
            findViewById(R.id.now_playing_layout).setVisibility(View.VISIBLE);
        }*/
        bar.setMax(player.getDuration());
        //thumbnail.setImageDrawable(getCurrentAlbumArt(songPositionFromList));
        if (!checkingPreviousSongIndexDuplicity(songPositionFromList)) {
            previousSongPositions.add(songPositionFromList);
            previousSongIndex++;
        }
        setActionBarStatus();
        updateSongInfo();
        setPlayerStates();
        setSeekBarChangedListener();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        int currentSongPosition = songPositionFromList;
        Logging.getInstance().I(TAG, "onCompletion");
        if (shuffleFlag && !isRepeatOneOn) {
            songPositionFromList = randomPosition.nextInt((totalSongs - 0) + 0);
        } else if (shuffleFlag && isRepeatOn) {
            songPositionFromList = randomPosition.nextInt((totalSongs - 0) + 0);
        } else if ((!shuffleFlag && isRepeatOn) || (!shuffleFlag && !isRepeatOn) || isRepeatOn) {
            songPositionFromList = (songPositionFromList + 1) % totalSongs;
        } else if (isRepeatOneOn) {
            songPositionFromList = currentSongPosition;
        }
        Log.i("onCompletion called", " " + songPositionFromList);
        startPlay(getCurrentFile(songPositionFromList));
    }

    @Override
    public void onClick(View v) {
        /*if (songPositionFromList == -1)
            songPositionFromList = lastPlayedSong;*/
        int id = v.getId();
        switch (id) {
            case R.id.allow_permission: {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE_REQUEST_CODE);
                break;
            }
            case R.id.fab_main: {
                if (findViewById(R.id.now_playing_layout).getVisibility() == View.GONE) {
                    /*findViewById(R.id.home_page_song_list_layout).setVisibility(View.GONE);
                    findViewById(R.id.now_playing_layout).setVisibility(View.VISIBLE);
					setActionBarStatus();*/
                    visibleAnimation(findViewById(R.id.now_playing_layout), findViewById(R.id.home_page_song_list_layout), 700);
                } else if (findViewById(R.id.home_page_song_list_layout).getVisibility() == View.GONE) {
                    /*findViewById(R.id.now_playing_layout).setVisibility(View.GONE);
                    findViewById(R.id.home_page_song_list_layout).setVisibility(View.VISIBLE);
					setActionBarStatus();*/
                    visibleAnimation(findViewById(R.id.home_page_song_list_layout), findViewById(R.id.now_playing_layout), 700);
                }
                break;
            }
            case R.id.playPause: {
                setPlayPauseButtonClickListener();
                break;
            }

            //repeating functionality
            case R.id.repeat: {
                if (!isRepeatOn && !isRepeatOneOn) {
                    isRepeatOn = true;
                    isRepeatOneOn = false;
                    Toast.makeText(this, "Repeat On", Toast.LENGTH_SHORT).show();
                    repeat.setBackgroundResource(R.drawable.player_repeat_on_btn);
                } else if (isRepeatOn && !isRepeatOneOn) {
                    isRepeatOneOn = true;
                    isRepeatOn = false;
                    Toast.makeText(this, "RepeatOne On", Toast.LENGTH_SHORT).show();
                    repeat.setBackgroundResource(R.drawable.player_repeat_one_on_btn);
                } else {
                    repeat.setBackgroundResource(R.drawable.player_repeat_off_btn);
                    Toast.makeText(this, "Repeat Off", Toast.LENGTH_SHORT).show();
                    isRepeatOn = false;
                    isRepeatOneOn = false;
                }
                break;
            }

            //shuffling songs sequence
            case R.id.shuffle: {
                if (!shuffleFlag) {
                    shuffle.setBackgroundResource(R.drawable.player_shuffle_on_btn);
                    Toast.makeText(this, "Shuffle On", Toast.LENGTH_SHORT).show();
                    shuffleFlag = true;
                } else {
                    shuffle.setBackgroundResource(R.drawable.player_shuffle_off_btn);
                    Toast.makeText(this, "Shuffle Off", Toast.LENGTH_SHORT).show();
                    shuffleFlag = false;
                }
                break;
            }
            //show songList
            case R.id.imageViewSongList: {
                //song list in a new activity
                Intent intent = new Intent(this, CurrentPlaylistActivity.class);
                startActivityForResult(intent, 200);
                break;
            }
            //back to songList from player
            case R.id.songList: {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (findViewById(R.id.now_playing_layout).getVisibility() == View.VISIBLE) {
                            /*findViewById(R.id.now_playing_layout).setVisibility(View.GONE);
                            findViewById(R.id.home_page_song_list_layout).setVisibility(View.VISIBLE);*/
                            visibleAnimation(findViewById(R.id.home_page_song_list_layout), findViewById(R.id.now_playing_layout), 700);
                        }
                    }
                }, 100);
                break;
            }

            //next song button action
            case R.id.next: {
                setNextSongButtonClickListener();
                break;
            }

            //previous song button action
            case R.id.previous: {
                setPreviousSongButtonClickListener();
                break;
            }
            case R.id.playingSong: {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (findViewById(R.id.now_playing_layout).getVisibility() == View.GONE) {
                            findViewById(R.id.now_playing_layout).setVisibility(View.VISIBLE);
                            //findViewById(R.id.parentLayout).setVisibility(View.GONE);
                        }
                    }
                }, 400);
                break;
            /*case R.id.shuffle_all_songs: {
                //Toast.makeText(this, "Not implemented yet :(", Toast.LENGTH_SHORT).show();
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
			}*/
            }
            case R.id.playlist_action_bar_back_btn: {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (findViewById(R.id.now_playing_layout).getVisibility() == View.GONE) {
                            findViewById(R.id.now_playing_layout).setVisibility(View.VISIBLE);
                            //findViewById(R.id.parentLayout).setVisibility(View.GONE);
                            //findViewById(R.id.current_playlist).setVisibility(View.GONE);
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
            case R.id.showPlayerState: {
                if (findViewById(R.id.now_playing_layout).getVisibility() == View.GONE || findViewById(R.id.home_page_song_list_layout).getVisibility() == View.VISIBLE) {
                    /*findViewById(R.id.home_page_song_list_layout).setVisibility(View.GONE);
                    findViewById(R.id.now_playing_layout).setVisibility(View.VISIBLE);
					setActionBarStatus();*/
                    visibleAnimation(findViewById(R.id.now_playing_layout), findViewById(R.id.home_page_song_list_layout), 700);
                }
                break;
            }
            case R.id.playPauseState: {
                setPlayPauseButtonClickListener();
                break;
            }
            default:
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        songPositionFromList = position;
        Log.i("Main playlist", "item selected" + songPositionFromList);
        if (findViewById(R.id.now_playing_layout).getVisibility() == View.GONE || findViewById(R.id.home_page_song_list_layout).getVisibility() == View.VISIBLE) {
            /*findViewById(R.id.now_playing_layout).setVisibility(View.VISIBLE);
            findViewById(R.id.home_page_song_list_layout).setVisibility(View.GONE);*/
            visibleAnimation(findViewById(R.id.now_playing_layout), findViewById(R.id.home_page_song_list_layout), 700);
        }
        startPlay(getCurrentFile(position));
        //updateSongInfoFromDialog(position);
    }

    public class ButtonClickListenerEvent extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            //notificationManager.cancel(intent.getExtras().getInt("prev_id"));
            /*if (intent.getAction().equals(CUSTOM_NOTI)) {
                Log.i("Notific", "Player not null");
            } else {
                Log.i("Notific", "Player null");
            }*/
            if (intent.getExtras().getInt("prev_id") == CUSTOM_NOTI_PREVIOUS_SONG_ID) {
                Log.i("Notification", "Previous");
                setPreviousSongButtonClickListener();
            } else if (intent.getExtras().getInt("play_pause_id") == CUSTOM_NOTI_PLAY_PAUSE_ID) {
                Log.i("Notification", "PlayPause");
                if (player != null) {
                    //Log.i("Notific", "Player not null");
                    setPlayPauseButtonClickListener();
                } else {
                    Log.i("Notific", "Player null");
                }
            } else if (intent.getExtras().getInt("next_id") == CUSTOM_NOTI_NEXT_SONG_ID) {
                Log.i("Notification", "Next");
                setNextSongButtonClickListener();
            }
        }
    }
}
