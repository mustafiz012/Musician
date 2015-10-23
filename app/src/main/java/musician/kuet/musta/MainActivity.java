package musician.kuet.musta;

import android.app.ListActivity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class Mp3Filter implements FilenameFilter {
	public boolean accept(File dir, String name){
		//return (name.endsWith(".mp3") || name.endsWith(".wma"));

		if (name.endsWith(".mp3") || name.endsWith(".wma") || name.endsWith(".wav") || name.endsWith(".m4a")
					|| name.endsWith(".amr") || name.endsWith(".flac") || name.endsWith(".m4p"))
			return true;
		else
			return false;
	}
}

public class MainActivity extends ListActivity {
	//private static final String MEDIA_PATH = new String(Environment.getExternalStorageDirectory().getPath());
	private static final String MEDIA_PATH = new String("/sdcard/music/");
	private List<String> songs = new ArrayList<String>();
	private MediaPlayer player = new MediaPlayer();
	private int currentPosition = 0;
	Button seek, pause;
	SeekBar seekBar;
	int lenOfCurrentSong, mediaPos, mediaMax;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.songslist);
			updateSongList();
		final Button stop = (Button) findViewById(R.id.stop);
		seek = (Button) findViewById(R.id.seek);
		pause = (Button) findViewById(R.id.pause);
		seekBar = (SeekBar) findViewById(R.id.seekBar);
		mediaPos = player.getCurrentPosition();
		mediaMax = player.getDuration();
		seekBar.setMax(mediaMax);
		seekBar.setProgress(mediaPos);

		stop.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				player.reset();
				if (player.isPlaying() == false)
				{
					pause.setText("Play");
					Toast.makeText(MainActivity.this, songs.get(currentPosition)+" is Stopped", Toast.LENGTH_SHORT).show();
				}
			}
		});
		pause.setOnClickListener(new View.OnClickListener() {
			int counter = 1;
			@Override
			public void onClick(View v) {
				if(counter % 2 == 1){
					player.pause();
					if (player.isPlaying() == false)
					{
						pause.setText("Play");
						Toast.makeText(MainActivity.this, songs.get(currentPosition)+" is Paused", Toast.LENGTH_SHORT).show();
					}
					lenOfCurrentSong = player.getCurrentPosition();
				}
				else{
					player.seekTo(lenOfCurrentSong);
					player.start();
					if (player.isPlaying() == true)
					{
						pause.setText("Pause");
						Toast.makeText(MainActivity.this, songs.get(currentPosition)+" is resumed", Toast.LENGTH_LONG).show();
					}
				}
				counter++;
			}
		});
	}

	public void updateSongList() {
		File home = new File(MEDIA_PATH);
		if(home.listFiles(new Mp3Filter()).length > 0){
			for(File file : home.listFiles(new Mp3Filter())){
				songs.add(file.getName());
			}
			ArrayAdapter<String> songList = new ArrayAdapter<String>(this,R.layout.song_items,songs);
			setListAdapter(songList);
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		currentPosition = position;
		playSong(MEDIA_PATH + songs.get(position));
	}

	private void playSong(String songPath) {
		try {
			player.reset();
			//player.setAudioStreamType(AudioManager.STREAM_MUSIC);
			player.setDataSource(songPath);
			player.prepare();
			player.start();
			if (player.isPlaying() == true){
				pause.setText("Pause");
				Toast.makeText(MainActivity.this, songs.get(currentPosition)+" is Playing", Toast.LENGTH_SHORT).show();
			}
			seek.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					int position = player.getCurrentPosition();
					int i = position+2000;
						player.seekTo(i);
						Toast.makeText(MainActivity.this, "Position: "+i, Toast.LENGTH_SHORT).show();
				}
			});
			player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mp) {
					nextSong();
				}
			});
		} catch (IOException e) {
			Log.v(getString(R.string.app_name), e.getMessage());
		}
	}

	private void nextSong() {
		if(++currentPosition >= songs.size()){
			currentPosition = 0;
		}else{
			playSong(MEDIA_PATH + songs.get(currentPosition));
		}
	}
}
