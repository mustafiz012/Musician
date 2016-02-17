package musician.kuet.musta;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Objects;

public class Playing extends ActionBarActivity {

    private static final String TAG = null;
    String[] songItems;
    String testItems;
    ListView songList;
    File internal, external, externalStorageRoot, temp;
    File[] filess;
    int counter =0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playing);
        ArrayList<File> root = new ArrayList<File>();
        songList = (ListView) findViewById(R.id.lvSongList);

        final String state = Environment.getExternalStorageState();

        if ( Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state) ) {  // we can read the External Storage...
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
                            customToast(""+testItems);
                        }
                    }
                }
            }
        }

        

        for (int j=1; j <= 2; j++){

            internal = Environment.getExternalStorageDirectory();
            String temp_External = "/mnt/extSdCard";
            //internal = new File(temp_Internal);
            external = new File(testItems);
            final ArrayList<File> songs = updateSongList(internal);
            final ArrayList<File> song2 = updateSongList(external);
            //final ArrayList<File> temporary = updateSongList(temp);
            songs.addAll(song2);
            //songs.addAll(temporary);

            songItems = new String[songs.size()];
            for (int i = 0; i < songs.size(); i++){
                //customToast(songs.get(i).getName().toString());
                songItems[i] = songs.get(i).getName().toString().replace(".mp3", "").replace(".wav", "");
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.song_layout, R.id.songListText, songItems);
            songList.setAdapter(adapter);
            songList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    startActivity(new Intent(getApplicationContext(), NowPlaying.class).putExtra("pos", position).putExtra("songs", songs));
                }
            });
        }


    }

    public void customToast(String text){
        Toast.makeText(getApplicationContext(),text, Toast.LENGTH_LONG).show();
    }
    //reading all files from sdCard using ArrayList<>
    public ArrayList<File> updateSongList(File root){
        ArrayList<File> arrayList = new ArrayList<File>();
        File[] files = root.listFiles();  //all files from root directory //file array
        for (File singleFile : files){
            if (singleFile.isDirectory() && !singleFile.isHidden()){
                arrayList.addAll(updateSongList(singleFile));
            }else{
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
        finish();
    }


}
