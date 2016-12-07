package musician.kuet.musta;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class Playing extends Activity implements View.OnClickListener {

    private static final String TAG = null;
    String[] songItems;
    String testItems;
    ListView songList;
    File internal, external, externalStorageRoot, temp;
    File[] filess;
    int counter =0;
    Button playingSong;
    NowPlaying status = new NowPlaying();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playing);
        ArrayList<File> root = new ArrayList<File>();
        playingSong = (Button) findViewById(R.id.playingSong);
        TextView songsSize = (TextView) findViewById(R.id.songsSize);
        playingSong.setOnClickListener(this);
        songList = (ListView) findViewById(R.id.lvSongList);

        final String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state) ) {  // we can read the External Storage...
            //Retrieve the primary External Storage:
            final File primaryExternalStorage = Environment.getExternalStorageDirectory();

            //Retrieve the External Storages root directory:
            final String externalStorageRootDir;
            if ( (externalStorageRootDir = primaryExternalStorage.getParent()) == null ) {  // no parent...
                Log.d(TAG, "External Storage: " + primaryExternalStorage + "\n");
            }
            else {
                 externalStorageRoot = new File( externalStorageRootDir );
                 filess = externalStorageRoot.listFiles();
                for ( final File file : filess ) {
                    if ( file.isDirectory() && file.canRead() && (file.listFiles().length > 0) ) {  // it is a real directory (not a USB drive)...
                        Log.d(TAG, "External Storage: " + file.getAbsolutePath() + "\n");
                        //customToast("" + file.getAbsolutePath());
                        counter++;
                        if (counter == 1){
                            testItems = file.getAbsolutePath();
                            //customToast(""+testItems);
                        }
                    }
                }
            }
        }

        

        //select music root storage (if extSdCard present, two roots will be returned)
        for (int j=1; j <= 2; j++){
            //internal storage directory
            internal = Environment.getExternalStorageDirectory();
            //external storage directory
            external = new File(testItems);
            //collecting audio songs from internal storage
            final ArrayList<File> songs = updateSongList(internal);

            if (testItems != null){
                Log.i("Bluestacks ","Working");
                //collecting audio songs from external storage
                final ArrayList<File> song2 = updateSongList(external);
                //all songs getting together
                songs.addAll(song2);
            }

            songsSize.setText(songs.size()+" songs");
            songItems = new String[songs.size()];
            for (int i = 0; i < songs.size(); i++){
                //getting together all songs into a String array
                songItems[i] = songs.get(i).getName().toString().replace(".mp3", "").replace(".wav", "");
            }
            //listing out the songList in Playing activity
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.song_layout, R.id.songListText, songItems);
            songList.setAdapter(adapter);
            //playing specific song by clicking on the the song item
            songList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    startActivity(new Intent(getApplicationContext(), NowPlaying.class).putExtra("pos", position).putExtra("songs", songs));
                }
            });
            //trying to check out the option of the song item by pressing and holding on each
            songList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    customToast("something need to add");
                    return false;
                }
            });
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("playing", "onResumed");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i("playing", "onRestart");
    }

    public void customToast(String text){
        Toast.makeText(getApplicationContext(),text, Toast.LENGTH_LONG).show();
    }
    //reading all music files from sdCard using ArrayList<>
    public ArrayList<File> updateSongList(File root){
        ArrayList<File> arrayList = new ArrayList<File>();
        File[] files = root.listFiles();  //all files from root directory //file array
        for (File singleFile : files){
            if (singleFile.isDirectory() && !singleFile.isHidden()){
                arrayList.addAll(updateSongList(singleFile));
            }else{
                //picking up only .mp3 and .wav format files
                if (singleFile.getName().endsWith(".mp3") || singleFile.getName().endsWith(".wav")){
                    arrayList.add(singleFile);
                }
            }
        }
        return arrayList;
    }

    @Override
    protected void onPause() {
        super.onPause();
		Log.i("onPause:","Playing");
        //finish();
    }


    @Override
    protected void onStop() {
        super.onStop();
        Log.i("Playing ", "stop called");
    }

    @Override
    public void onClick(View v) {
        if (v == playingSong){
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            intent.setClass(Playing.this, NowPlaying.class);
            //checking if the NowPlaying activity is active or not
            if (status.active == true){
                Log.i("NowPlaying ", "active is true");
                startActivity(intent);
            }else {
                Log.i("now ", "false");
                try {
                    Toast.makeText(Playing.this, "Nothing is playing....", Toast.LENGTH_LONG).show();
                }catch (IndexOutOfBoundsException e){
                    e.printStackTrace();
                }
            }
        }
    }
}
