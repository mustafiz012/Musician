package musician.kuet.musta;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;


public class Playing extends RootMediaActivity implements View.OnClickListener, MediaPlayer.OnCompletionListener, AdapterView.OnItemClickListener {
	static boolean active = false, shuffleFlag = false, isRepeatOneOn = false, isRepeatOn = false, isSeekBarChangedListenerStarted = false;
	private static final String TAG = null;
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
	Button shuffle, repeat, nowPlayingSongs, goToSongList;
	TextView currentSong, currentSongState, currentSongArtistNameState, leftDuration, rightDuration, tvSongsSize, currentSongArtistName, current_playlist_action_bar_activity_content;
	private double startTime = 0;
	private double finalTime = 0;
	private Handler myHandler = new Handler();
	String currentItem = "";
	NowPlaying status = new NowPlaying();
	ArrayList<File> songs, songs2;
	int songsSize = 0;
	Random randomPosition = new Random();
	//private MediaCursorAdapter mediaCursorAdapter = null;
	private MediaPlayer player = null;
	ArrayAdapter<String> adapterForDialog;
	List<Integer> filteredIndexes;
	int filteredIndexPosition;
	//Cursor gCursor = null;
	MenuItem menuSearchItem = null, menuPlaylistItem = null;
	FloatingActionButton homeFab = null;
	private ActionBar actionBar = null;
	Toolbar toolbar = null;
	LinearLayout showPlayerState = null;

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
		homeFab = (FloatingActionButton) findViewById(R.id.fab_home);
		showPlayerState = (LinearLayout) findViewById(R.id.showPlayerState);
		showPlayerState.setOnClickListener(this);
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
		if (null != gCursor) {
			totalSongs = rootMediaCursorAdapter.getCount();
			songList.setAdapter(rootMediaCursorAdapter);
			songList.setOnItemClickListener(this);
		}
		tvSongsSize.setText(totalSongs + " Songs");
		if (seekBarUpdating != null && !seekBarUpdating.isAlive())
			setSeekBarUpdating();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_playing);
		actionBar = getSupportActionBar();
		actionBar.setTitle(getString(R.string.app_name).toString());
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
				alreadyFetchedAudioFiles();
			} else {
				Playing.this.finish();
			}
		} else {
			Log.i("else SDK", "" + android.os.Build.VERSION.SDK_INT);
			alreadyFetchedAudioFiles();
		}

		homeFab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
				if (findViewById(R.id.now_playing_layout).getVisibility() == View.GONE) {
					findViewById(R.id.home_page_song_list_layout).setVisibility(View.GONE);
					findViewById(R.id.now_playing_layout).setVisibility(View.VISIBLE);
					controlActionBar();
				} else if (findViewById(R.id.home_page_song_list_layout).getVisibility() == View.GONE) {
					findViewById(R.id.now_playing_layout).setVisibility(View.GONE);
					findViewById(R.id.home_page_song_list_layout).setVisibility(View.VISIBLE);
					controlActionBar();
				}
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_current_playlist, menu);
		invalidateOptionsMenu();
		menuSearchItem = menu.findItem(R.id.action_search);
		menuPlaylistItem = menu.findItem(R.id.action_current_playlist);
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
				ArrayAdapter<String> filteredAdapter = new ArrayAdapter<String>(Playing.this, android.R.layout.simple_list_item_1, currentFilteredList);
				songList.setAdapter(filteredAdapter);
				songList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						Log.i("Playing Menu searching", "onItemClicked " + position);
						startPlay(getCurrentFile(filteredIndexes.get(position)));
						updateSongInfoFromDialog(position);
						Log.i("Search result", "" + position);
						if (findViewById(R.id.now_playing_layout).getVisibility() == View.GONE) {
							findViewById(R.id.now_playing_layout).setVisibility(View.VISIBLE);
							findViewById(R.id.parentLayout).setVisibility(View.GONE);
							//findViewById(R.id.current_playlist).setVisibility(View.GONE);
						}
					}
				});
				return true;
			}

			@Override
			public boolean onQueryTextChange(String newText) {
				return false;
			}
		});
		menuPlaylistItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				if (item.getItemId() == R.id.action_current_playlist) {
					Intent intent = new Intent(Playing.this, CurrentPlaylistActivity.class);
					intent.putExtra("currentPosition", songPositionFromList);
					startActivityForResult(intent, 200);
				}
				return false;
			}
		});
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_exit: {
				this.finish();
				return true;
			}
			case android.R.id.home: {
				Log.i("back is clicked", "");
				return true;
			}
			default:
				return super.onOptionsItemSelected(item);
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
		songPositionFromList = lastPlayedSong = lastSong;

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
    /*
    private String getCurrentFileName(int currentPosition) {
		String songName = null;
		Cursor cursor = (Cursor) rootMediaCursorAdapter.getItem(currentPosition);
		songName = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.TITLE));
		return songName;
	}

	private String getCurrentArtistName(int currentPosition) {
		String artistName = null;
		Cursor cursor = (Cursor) rootMediaCursorAdapter.getItem(currentPosition);
		artistName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Artists.ARTIST));
		return artistName;
	}

	private Drawable getCurrentAlbumArt(int currentPosition) {
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

	private String getCurrentFile(int currentPosition) {
		songPositionFromList = currentPosition;
		String songData = null;
		Cursor cursor = (Cursor) rootMediaCursorAdapter.getItem(currentPosition);
		songData = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
		return songData;
	}*/

	private void controlActionBar() {
		if (findViewById(R.id.now_playing_layout).getVisibility() == View.VISIBLE) {
			//homeFab.setVisibility(View.GONE);
			findViewById(R.id.player_layout).setVisibility(View.GONE);
			menuSearchItem.setVisible(false);
			menuPlaylistItem.setVisible(true);
			actionBar.setTitle("" + getCurrentFileName(songPositionFromList));
			actionBar.setSubtitle("" + getCurrentArtistName(songPositionFromList));
			//actionBar.setDisplayHomeAsUpEnabled(true);
			//actionBar.setHomeButtonEnabled(true);

		} else {
			//homeFab.setVisibility(View.VISIBLE);
			findViewById(R.id.player_layout).setVisibility(View.VISIBLE);
			menuSearchItem.setVisible(true);
			menuPlaylistItem.setVisible(false);
			actionBar.setTitle(getString(R.string.app_name));
			actionBar.setSubtitle(null);
			//actionBar.setDisplayHomeAsUpEnabled(false);
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
		currentSong.setText(getCurrentFileName(songPositionFromList));
		currentSongState.setText(getCurrentFileName(songPositionFromList));
		currentSongArtistName.setText(getCurrentArtistName(songPositionFromList));
		currentSongArtistNameState.setText(getCurrentArtistName(songPositionFromList));
		//thumbnail.setImageDrawable(getCurrentAlbumArt(songPositionFromList));
		if (!checkingPreviousSongIndexDuplicity(songPositionFromList)) {
			previousSongPositions.add(songPositionFromList);
			previousSongIndex++;
		}
		controlActionBar();
		updateSongInfo(songPositionFromList);
		setPlayerStates();
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
		if (findViewById(R.id.now_playing_layout).getVisibility() == View.VISIBLE) {
			findViewById(R.id.now_playing_layout).setVisibility(View.GONE);
			findViewById(R.id.home_page_song_list_layout).setVisibility(View.VISIBLE);
			controlActionBar();
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
		songList.setAdapter(rootMediaCursorAdapter);
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
		Log.i("onCompletion called", " " + songPositionFromList);
		startPlay(getCurrentFile(songPositionFromList));
	}

	/*private class MediaCursorAdapter extends SimpleCursorAdapter {

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
	}*/

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

	//updating position from song list dialog
	private void updateSongInfoFromDialog(int positionFromDialog) {
		currentSong.setText(getCurrentFileName(positionFromDialog));
		currentSongState.setText(getCurrentFileName(positionFromDialog));
		currentSongArtistName.setText(getCurrentArtistName(positionFromDialog));
		currentSongArtistNameState.setText(getCurrentArtistName(positionFromDialog));
	}

	private void setPlayerStates() {
		if (player.isPlaying()) {
			playPause.setImageResource(R.drawable.btn_pause);
			playPauseState.setImageResource(R.drawable.ic_action_pause);
		} else {
			playPause.setImageResource(R.drawable.btn_play);
			playPauseState.setImageResource(R.drawable.ic_action_play);
		}
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
			case R.id.playPause: {
				if (songPositionFromList == -1) {
					songPositionFromList = lastPlayedSong;
					Log.i("Play button clicked", " no list item" + songPositionFromList);
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
							setPlayerStates();
						} else {
							Log.i("position here", "" + songPositionFromList);
							player.start();
							updateSongInfo(songPositionFromList);
							setPlayerStates();
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
				/*findViewById(R.id.now_playing_layout).setVisibility(View.GONE);
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
				//searching operation
				SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
				searchView.setQueryHint("Search for songs");
				//searchView.setIconifiedByDefault(false);
				*//*this.searchView.findViewById(searchView.getContext().getResources().getIdentifier("android:id/search_close_btn", null, null)).setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Log.i("searching closed", " clicked");
					}
				});*//*
				*//*searchView.setOnCloseListener(new SearchView.OnCloseListener() {
					@Override
					public boolean onClose() {
						current_playlist_action_bar_activity_content.setVisibility(View.VISIBLE);
						return true;
					}
				});*//*
				searchView.setOnSearchClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Log.i("searching clicked", " yo");
						//playlist_action_bar_back_btn.setVisibility(View.GONE);
						current_playlist_action_bar_activity_content.setVisibility(View.GONE);
						//findViewById(R.id.playlist_actionbar_layout).setVisibility(View.GONE);
					}
				});
				//searchView.setSubmitButtonEnabled(false);
				//searchView.setSuggestionsAdapter(mediaCursorAdapter);
				searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
				searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
					@Override
					public boolean onQueryTextSubmit(String query) {
						{
							filteredIndexes = new ArrayList<Integer>();
							filteredIndexPosition = 0;
							ArrayList<String> currentList = new ArrayList<String>();
							for (gCursor.moveToFirst(); !gCursor.isAfterLast(); gCursor.moveToNext()) {
								if (gCursor.getString(gCursor.getColumnIndex(MediaStore.MediaColumns.TITLE)).toLowerCase().contains(query.toLowerCase()) || gCursor.getString(gCursor.getColumnIndex(MediaStore.Audio.Artists.ARTIST)).toLowerCase().contains(query.toLowerCase())) {
									currentList.add(gCursor.getString(gCursor.getColumnIndex(MediaStore.MediaColumns.TITLE)));
									filteredIndexes.add(gCursor.getPosition());
									filteredIndexPosition++;
								}
							}
							ArrayAdapter<String> filteredAdapter = new ArrayAdapter<String>(Playing.this, android.R.layout.simple_list_item_1, currentList);
							currentPlayList.setAdapter(filteredAdapter);
							currentPlayList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
								@Override
								public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
									startPlay(getCurrentFile(filteredIndexes.get(position)));
									updateSongInfoFromDialog(filteredIndexes.get(position));
									if (findViewById(R.id.now_playing_layout).getVisibility() == View.GONE) {
										if (findViewById(R.id.search_layout).getVisibility() == View.VISIBLE) {
											findViewById(R.id.search_layout).setVisibility(View.GONE);
											findViewById(R.id.playlist_actionbar_layout).setVisibility(View.VISIBLE);
										}
										findViewById(R.id.now_playing_layout).setVisibility(View.VISIBLE);
										findViewById(R.id.parentLayout).setVisibility(View.GONE);
										findViewById(R.id.current_playlist).setVisibility(View.GONE);
									}
									Toast.makeText(Playing.this, position + "" + filteredIndexes.get(position), Toast.LENGTH_SHORT).show();
								}
							});
							return true;
						}
					}

					@Override
					public boolean onQueryTextChange(String newText) {
						mediaCursorAdapter.getFilter().filter(newText);
						return true;
					}
				});

                *//*currentPlayList.setAdapter(mediaCursorAdapter);
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
                });*/
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
							findViewById(R.id.now_playing_layout).setVisibility(View.GONE);
							findViewById(R.id.home_page_song_list_layout).setVisibility(View.VISIBLE);
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
							Log.i("Previous button clicked", "available pre" + previousSongPositions.get(previousSongIndex));
							startPlay(getCurrentFile(previousSongPositions.get(previousSongIndex)));
							//Log.i("seekbarchangedPre+", "" + isSeekBarChangedListenerStarted);
							//setSeekBarChangedListener();
						} else {
							startPlay(getCurrentFile(previousSongPositions.get(0)));
							Log.i("Previous button clicked", "available pre" + previousSongPositions.get(0));
							//setSeekBarChangedListener();
						}
					} else {
						startPlay(getCurrentFile(lastPlayedSong));
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
			case R.id.playlist_action_bar_back_btn: {
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						if (findViewById(R.id.now_playing_layout).getVisibility() == View.GONE) {
							findViewById(R.id.now_playing_layout).setVisibility(View.VISIBLE);
							findViewById(R.id.parentLayout).setVisibility(View.GONE);
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
				//searching songs
                /*search_song_et.addTextChangedListener(new TextWatcher() {
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
                });*/
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
					findViewById(R.id.home_page_song_list_layout).setVisibility(View.GONE);
					findViewById(R.id.now_playing_layout).setVisibility(View.VISIBLE);
					controlActionBar();
				}
				break;
			}
			case R.id.playPauseState: {
				if (songPositionFromList == -1) {
					songPositionFromList = lastPlayedSong;
					Log.i("Play button clicked", " no list item" + songPositionFromList);
					startPlay(getCurrentFile(songPositionFromList));
					if (seekBarUpdating != null && !seekBarUpdating.isAlive())
						setSeekBarUpdating();
				} else {
					try {
						if (player.isPlaying()) {
							Log.i("position here if", "" + songPositionFromList);
							player.pause();
							setPlayerStates();
						} else {
							Log.i("position here", "" + songPositionFromList);
							player.start();
							updateSongInfo(songPositionFromList);
							setPlayerStates();
						}
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (IllegalStateException e) {
						e.printStackTrace();
					}
				}
				break;
			}
			default:
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		songPositionFromList = position;
		Log.i("Home playlist", "item selected" + songPositionFromList);
		if (findViewById(R.id.now_playing_layout).getVisibility() == View.GONE || findViewById(R.id.home_page_song_list_layout).getVisibility() == View.VISIBLE) {
			findViewById(R.id.now_playing_layout).setVisibility(View.VISIBLE);
			findViewById(R.id.home_page_song_list_layout).setVisibility(View.GONE);
		}
		startPlay(getCurrentFile(position));
		updateSongInfoFromDialog(position);
	}
}
