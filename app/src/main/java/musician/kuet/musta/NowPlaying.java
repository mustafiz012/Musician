package musician.kuet.musta;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.os.Handler;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class NowPlaying extends ActionBarActivity implements View.OnClickListener {

	SeekBar bar;
	Button preSong, nextSong, leftSeek, rightSeek, playPause;
	static MediaPlayer player;
	ArrayList<File> songs;
	int position;
	Uri uri;
	Thread seekBarUpdating;
    TextView currentSong, leftDuratoin, rightDuration;
    private double startTime = 0;
    private double finalTime = 0;
    private Handler myHandler = new Handler();
    public static int oneTimeOnly = 0;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        player.reset();
        player.release();
    }


    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_now_playing);

		initialization();

		seekBarUpdating = new Thread(){
			@Override
			public void run() {
				long totalDuration = player.getDuration();
				long currentPosition = 0;
				while (currentPosition+100 < totalDuration){
					try {
                        //Log.e("duration", ""+currentPosition+"\n"+totalDuration);
						sleep(100);
						currentPosition = player.getCurrentPosition();
						bar.setProgress((int) currentPosition);
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (IllegalArgumentException e){
                        e.printStackTrace();
                    }catch (IllegalAccessError e){
                        e.printStackTrace();
                    } catch (IllegalStateException e){
                        e.printStackTrace();
                    }
				}
				//super.run();
			}
		};

		player = new MediaPlayer();

		if (player != null){
			player.stop();
            player.reset();
			player.release();
		}
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		songs =(ArrayList) bundle.getParcelableArrayList("songs");
		position = bundle.getInt("pos", 0);
		uri = Uri.parse(songs.get(position).toString());
		player = MediaPlayer.create(getApplicationContext(), uri);
		player.start();
        updateSongInfo();
		seekBarUpdating.start();


		bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
               // leftDuratoin.setText((String.valueOf(player.getCurrentPosition() / 1000)));
               // rightDuration.setText(String.valueOf(player.getDuration() / 1000));
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
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {

                Log.i("test", "here: " + songs.get(position + 1).getName());
                mp.stop();
                mp.reset();
                mp.release();
                try {
                    position = (position + 1)%songs.size();
                    uri = Uri.parse(songs.get(position).toString());
                    mp = MediaPlayer.create(getApplicationContext(), uri);
                    mp.start();
                    Log.i("test", "here: " + songs.get(position).getName());
                    updateSongInfo();
                } catch (IllegalArgumentException e){
                    e.printStackTrace();
                } catch (IllegalStateException e){
                    e.printStackTrace();
                }
            }
        });

	}
    private Runnable UpdateSongTime = new Runnable() {
        public void run() {
            try {
                startTime = player.getCurrentPosition();
                leftDuratoin.setText(String.format("%d:%d",

                                TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                                TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                                toMinutes((long) startTime)))
                );
                bar.setProgress((int)startTime);
                myHandler.postDelayed(this, 100);
            }catch (IllegalStateException e){
                e.printStackTrace();
            }
        }
    };

    public void updateSongInfo(){
        playPause.setText("||");
        finalTime = player.getDuration();
        startTime = player.getCurrentPosition();
        bar.setMax((int) finalTime);
        bar.setProgress(0);
        currentSong.setText(""+songs.get(position).getName().replace(".mp3", "").replace(".MP3", "").replace(".wav", "").replace(".WAV", ""));

        //setting player button
        if (!player.isPlaying()){
            playPause.setText("|>");
        }

        rightDuration.setText(String.format("%d:%d",
                        TimeUnit.MILLISECONDS.toMinutes((long) finalTime),
                        TimeUnit.MILLISECONDS.toSeconds((long) finalTime) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) finalTime)))
        );
        leftDuratoin.setText(String.format("%d:%d",
                        TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                        TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) startTime)))
        );
        //bar.setProgress((int) startTime);
        myHandler.postDelayed(UpdateSongTime, 100);
    }

	public void initialization(){
		bar = (SeekBar) findViewById(R.id.seekBar);
		preSong = (Button) findViewById(R.id.previous);
		nextSong = (Button) findViewById(R.id.next);
		leftSeek = (Button) findViewById(R.id.leftSeeking);
		rightSeek = (Button) findViewById(R.id.rightSeeking);
		playPause = (Button) findViewById(R.id.playPause);
        currentSong = (TextView) findViewById(R.id.currentSong);
        leftDuratoin = (TextView) findViewById(R.id.leftDuratoin);
        rightDuration = (TextView) findViewById(R.id.rightDuration);

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
				try {
                    if (player.isPlaying()){
                        player.pause();
                        playPause.setText("|>");
                    }else{
                        player.start();
                        updateSongInfo();
                    }
                }catch (IllegalArgumentException e){
                    e.printStackTrace();
                } catch (IllegalStateException e){
                    e.printStackTrace();
                }
                break;
			case R.id.next:
				try {
                    player.stop();
                    player.reset();
                    player.release();
                    playPause.setText("*");
                    position = (position + 1)%songs.size();
                    uri = Uri.parse(songs.get(position).toString());
                    player = MediaPlayer.create(getApplicationContext(), uri);
                    player.start();
                    updateSongInfo();
                    playPause.setText("||");
                } catch (IllegalArgumentException e){
                    e.printStackTrace();
                } catch (IllegalStateException e){
                    e.printStackTrace();
                }
				break;
			case R.id.previous:
				try {
                    player.stop();
                    player.reset();
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
                    updateSongInfo();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
				break;
			case R.id.leftSeeking:
				player.seekTo(player.getCurrentPosition() - 5000);
				//player.start();
				break;
			case R.id.rightSeeking:
				try {
                    player.seekTo(player.getCurrentPosition() + 5000);
                }catch (IllegalStateException e){
                    e.printStackTrace();
                }

                //player.start();
				break;
			default:

				break;


		}
	}

}
