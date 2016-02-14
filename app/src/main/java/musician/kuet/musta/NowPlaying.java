package musician.kuet.musta;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

public class NowPlaying extends ActionBarActivity implements View.OnClickListener {

	SeekBar bar;
	Button preSong, nextSong, leftSeek, rightSeek, playPause;
	static MediaPlayer player = new MediaPlayer();
	ArrayList<File> songs;
	int position;
	Uri uri;
	Thread seekBarUpdating;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_now_playing);

		initialization();

		seekBarUpdating = new Thread(){
			@Override
			public void run() {
				int totalDuration = player.getDuration();
				int currentPosition = 0;
				while (currentPosition < totalDuration){
					try {
						sleep(500);
						currentPosition = player.getCurrentPosition();
						bar.setProgress(currentPosition);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				//super.run();
			}
		};

		if (player != null){
			player.stop();
			player.release();
		}
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		songs =(ArrayList) bundle.getParcelableArrayList("songs");
		position = bundle.getInt("pos", 0);
		uri = Uri.parse(songs.get(position).toString());
		player = MediaPlayer.create(getApplicationContext(), uri);
		player.start();
		bar.setMax(player.getDuration());
		seekBarUpdating.start();

		bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				player.seekTo(seekBar.getProgress());
			}
		});
	}

	public void initialization(){
		bar = (SeekBar) findViewById(R.id.seekBar);
		preSong = (Button) findViewById(R.id.previous);
		nextSong = (Button) findViewById(R.id.next);
		leftSeek = (Button) findViewById(R.id.leftSeeking);
		rightSeek = (Button) findViewById(R.id.rightSeeking);
		playPause = (Button) findViewById(R.id.playPause);

		preSong.setOnClickListener(this);
		nextSong.setOnClickListener(this);
		leftSeek.setOnClickListener(this);
		rightSeek.setOnClickListener(this);
		playPause.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id){
			case R.id.playPause:
				if (player.isPlaying()){
					player.pause();
					playPause.setText("|>");
				}else{
					player.start();
					playPause.setText("||");
				}
				break;
			case R.id.next:
				player.stop();
				player.release();
				playPause.setText("*");
				position = (position + 1)%songs.size();
				uri = Uri.parse(songs.get(position).toString());
				player = MediaPlayer.create(getApplicationContext(), uri);
				player.start();
				bar.setMax(player.getDuration());
				playPause.setText("||");
				break;
			case R.id.previous:
				player.stop();
				player.release();
				playPause.setText("*");
				if (position-1 < 0){
					position = songs.size()-1;
				}else {
					position--;
				}
				uri = Uri.parse(songs.get(position).toString());
				player = MediaPlayer.create(getApplicationContext(),uri );player.start();
				playPause.setText("||");
				player.start();
				bar.setMax(player.getDuration());
				break;
			case R.id.leftSeeking:
				player.seekTo(player.getCurrentPosition()-5000);
				player.start();
				break;
			case R.id.rightSeeking:
				player.seekTo(player.getCurrentPosition()+5000);
				player.start();
				break;
			default:

				break;


		}
	}
}
