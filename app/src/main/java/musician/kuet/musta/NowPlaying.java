package musician.kuet.musta;

import android.app.Activity;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.os.Handler;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class NowPlaying extends Activity implements View.OnClickListener, MediaPlayer.OnCompletionListener {
    static boolean active = false, shuffleOn = false, isRepeatOneOn = false, isRepeatOn = false, isDialogAOk = false;

    SeekBar bar;
    Button preSong, nextSong, leftSeek, rightSeek, playPause, goToSongList, shuffle, repeat, nowPlayingSongs;
    static MediaPlayer player;
    String[] songItemss;
    ArrayList<File> songs;
    ArrayAdapter<String> adapter;
    int position, shuffleCounter = 0, repeatCounter = 0;
    Uri uri;
    Thread seekBarUpdating;
    TextView currentSong, leftDuration, rightDuration;
    private double startTime = 0;
    private double finalTime = 0;
    long currentPosition = 0;
    Random randomPosition = new Random();
    long totalDuration = 0;
    Bundle gBundle = new Bundle();
    private Handler myHandler = new Handler();

    @Override
    protected void onStart() {
        super.onStart();
        Log.i("start ", "true");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i("stop ", "false");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i("NowPlaying ", "onPause called");
        openActivityNotification(getApplicationContext());
        active = true;
    }

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

        //all objects declaration here
        initialization();


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

        //creating a fresh mediaPlayer
        if (player != null) {
            player.stop();
            player.reset();
            player.release();
        }
        //getting the songs as ArrayList from the array Playing sent here
        Intent intent = getIntent();
        gBundle = intent.getExtras();
        songs = (ArrayList) gBundle.getParcelableArrayList("songs");
        //preparing the media player contents
        position = gBundle.getInt("pos", 0);
        uri = Uri.parse(songs.get(position).toString());
        player = MediaPlayer.create(getApplicationContext(), uri);
        Log.i("position ", "" + position);
        player.start();
        updateSongInfo(position);
        seekBarUpdating.start();


        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // leftDuration.setText((String.valueOf(player.getCurrentPosition() / 1000)));
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

        //song onCompletion actions like playing next song
        player.setOnCompletionListener(this);
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
            }catch (IllegalStateException e){
                e.printStackTrace();
            }catch (IllegalArgumentException e){
                e.printStackTrace();
            }
        }
    };



    public void updateSongInfo(int thisOne){
        playPause.setBackgroundResource(R.mipmap.pause);
        finalTime = player.getDuration();
        bar.setMax((int) finalTime);
        //bar.setProgress(0);
        currentSong.setText(""+songs.get(thisOne).getName().replace(".mp3", "").replace(".MP3", "").replace(".wav", "").replace(".WAV", "").replace("_", " "));

        //setting player button
        if (!player.isPlaying()){
            playPause.setBackgroundResource(R.mipmap.play);
        }
        rightDuration.setText(String.format("%d:%d",
                        TimeUnit.MILLISECONDS.toMinutes((long) finalTime),
                        TimeUnit.MILLISECONDS.toSeconds((long) finalTime) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) finalTime)))
        );
        bar.setProgress((int) startTime);
        myHandler.postDelayed(UpdateSongTimeT, 100);
        if (active){
            openActivityNotification(getApplicationContext());
        }
    }

    //creating a notification through which user can restore the app after leaving it
    public static void openActivityNotification(Context context) {
        NotificationCompat.Builder nc = new NotificationCompat.Builder(context);
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent notifyIntent = new Intent(context, Playing.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        nc.setContentIntent(pendingIntent);

        nc.setSmallIcon(R.mipmap.ic_launcher);
        nc.setAutoCancel(true);
        nc.setContentTitle("Musician");
        nc.setContentText("Touch to Musician");

        nm.notify(1, nc.build());
    }

    public void initialization(){
        bar = (SeekBar) findViewById(R.id.seekBar);
        preSong = (Button) findViewById(R.id.previous);
        nextSong = (Button) findViewById(R.id.next);
//		leftSeek = (Button) findViewById(R.id.leftSeeking);
//		rightSeek = (Button) findViewById(R.id.rightSeeking);
        playPause = (Button) findViewById(R.id.playPause);
        currentSong = (TextView) findViewById(R.id.currentSong);
        leftDuration = (TextView) findViewById(R.id.leftDuratoin);
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
        //player.setOnCompletionListener(this);
        nowPlayingSongs.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            //play-pause button actions
            case R.id.playPause:
                try {
                    if (player.isPlaying()){
                        player.pause();
                        playPause.setBackgroundResource(R.mipmap.play);
                    }else{
                        Log.i("position ",""+position);
                        player.start();
                        updateSongInfo(position);
                    }
                }catch (IllegalArgumentException e){
                    e.printStackTrace();
                } catch (IllegalStateException e){
                    e.printStackTrace();
                }
                break;
            //next button actions
            case R.id.next:
                try {
                    player.stop();
                    player.reset();
                    if (shuffleOn == true){
                        position = randomPosition.nextInt((songs.size() - 0) + 0);
                    }else {
                        position = (position + 1)%songs.size();
                    }
                    uri = Uri.parse(songs.get(position).toString());
                    try {
                        player.setDataSource(getApplicationContext(), uri);
                        player.prepare();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Log.i("position ",""+position);
                    player.start();
                    updateSongInfo(position);
                    playPause.setBackgroundResource(R.mipmap.pause);
                } catch (IllegalArgumentException e){
                    e.printStackTrace();
                } catch (IllegalStateException e){
                    e.printStackTrace();
                }
                break;
            //previous button actions
            case R.id.previous:
                try {
                    player.stop();
                    player.reset();
                    Log.i("Previous ",""+position);
                    if (position-1 < 0){
                        if (shuffleOn == true){
                            position = randomPosition.nextInt((songs.size() - 0) + 0);
                        }else {
                            position = songs.size()-1;
                        }
                    }else {
                        if (shuffleOn == true)
                            position = randomPosition.nextInt((songs.size() - 0) + 0);
                        else
                            position--;
                    }
                    Log.i("Previous ",""+position);
                    uri = Uri.parse(songs.get(position).toString());
                    try {
                        player.setDataSource(getApplicationContext(), uri);
                        player.prepare();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    playPause.setBackgroundResource(R.mipmap.pause);
                    Log.i("position ", "" + position);
                    player.start();
                    updateSongInfo(position);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
                break;
//			case R.id.leftSeeking:
//				player.seekTo((int) (currentPosition - 5000));
//				//player.start();
//				break;
//			case R.id.rightSeeking:
//				try {
//                    player.seekTo((int) (currentPosition + 5000));
//                }catch (IllegalStateException e){
//                    e.printStackTrace();
//                }
//
//                //player.start();
//				break;
            //songList button actions
            case R.id.songList:
                Intent intent = new Intent();
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.setClass(NowPlaying.this, Playing.class);
                startActivity(intent);
                break;
            //shuffling
            case R.id.shuffle:
                shuffleCounter++;
                if (shuffleCounter%2 == 1){
                    shuffle.setBackgroundResource(R.mipmap.shuffle);
                    Toast.makeText(NowPlaying.this, "Shuffle On", Toast.LENGTH_SHORT).show();
                    shuffleOn = true;
                }
                else {
                    shuffle.setBackgroundResource(R.mipmap.shuffle_off);
                    Toast.makeText(NowPlaying.this, "Shuffle Off", Toast.LENGTH_SHORT).show();
                    shuffleOn = false;
                }
                break;
            //repeating
            case R.id.repeat:
                repeatCounter++;
                if (repeatCounter % 2 == 1){
                    if (repeatCounter%3 == 0)
                    {
                        isRepeatOneOn = true;
                        Toast.makeText(NowPlaying.this, "RepeatOne On", Toast.LENGTH_SHORT).show();
                        repeat.setBackgroundResource(R.mipmap.repeat_one);
                    }
                    else{
                        isRepeatOn = true;
                        Toast.makeText(NowPlaying.this, "Repeat On", Toast.LENGTH_SHORT).show();
                        repeat.setBackgroundResource(R.mipmap.repeat);
                    }
                }else {
                    if (repeatCounter%3 == 0)
                    {
                        isRepeatOneOn = true;
                        Toast.makeText(NowPlaying.this, "RepeatOne On", Toast.LENGTH_SHORT).show();
                        repeat.setBackgroundResource(R.mipmap.repeat_one);
                        isRepeatOn = false;
                    }else {
                        repeat.setBackgroundResource(R.mipmap.repeat_off);
                        Toast.makeText(NowPlaying.this, "Repeat Off", Toast.LENGTH_SHORT).show();
                        isRepeatOn = false;
                    }
                }
                break;
            case R.id.imageViewSongList:
                //Toast.makeText(NowPlaying.this, "Under Construction", Toast.LENGTH_SHORT).show();
                final Dialog dialog = new Dialog(NowPlaying.this);
                dialog.setTitle("Your List");
                songItemss = new String[songs.size()];
                for (int i = 0; i < songs.size(); i++){
                    //customToast(songs.get(i).getName().toString());
                    songItemss[i] = songs.get(i).getName().toString().replace(".mp3", "").replace(".wav", "");
                }
                adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.song_layout, R.id.songListText, songItemss);
                dialog.setContentView(R.layout.songs_dialog);

                //songList from the NowPlaying activity
                ListView listView = (ListView) dialog.findViewById(R.id.songsDialog);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                        isDialogAOk = true;
                        player.stop();
                        player.reset();
                        Log.i("position bf ", "" + position);
                        uri = Uri.parse(songs.get(position).toString());
                        try {
                            player.setDataSource(getApplicationContext(), uri);
                            player.prepare();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Log.i("position ", "" + position);
                        player.start();
//                        updateSongInfoFromDialog(position);
                        updateSongInfoFromDialog(position);
//                        onCompletion(player);
//                        //seekBarUpdating.start();

                        dialog.dismiss();
                    }
                });
                dialog.show();
                break;
            default:

                break;
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (active){
            openActivityNotification(getApplicationContext());
        }
        int currentSongPosition = position;
        Log.i("onCompletion: ", "calling");
        //Playing next song automatically
        mp.stop();
        mp.reset();
        //if (isDialogAOk == true)
        updateSongInfoFromDialog(position);
        Log.i("dialog index:", "" + position);

        if (shuffleOn == true && isRepeatOneOn == false){
            position = randomPosition.nextInt((songs.size() - 0) + 0);
        }else if (shuffleOn == true && isRepeatOn == true){
            position = randomPosition.nextInt((songs.size() - 0) + 0);
        }else if ((shuffleOn == false && isRepeatOn == true) || (shuffleOn == false && isRepeatOn == false) || isRepeatOn == true){
            position = (position + 1)%songs.size();
        }else if (isRepeatOneOn == true){
            position = currentSongPosition;
        }
        uri = Uri.parse(songs.get(position).toString());
        try {
            mp.setDataSource(getApplicationContext(), uri);
            mp.prepare();
            Log.i("position ", "" + position);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mp.start();
        updateSongInfo(position);
    }

    //updating position from song list dialog
    private void updateSongInfoFromDialog(int positionFromDialog) {
        position = positionFromDialog;
        currentSong.setText("" + songs.get(positionFromDialog).getName().replace(".mp3", "").replace(".MP3", "").replace(".wav", "").replace(".WAV", "").replace("_", " "));
    }
}
