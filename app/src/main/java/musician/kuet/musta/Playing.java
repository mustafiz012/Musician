package musician.kuet.musta;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class Playing extends ActionBarActivity {

    String[] songItems;
    ListView songList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playing);
        ArrayList<File> root = new ArrayList<File>();
        songList = (ListView) findViewById(R.id.lvSongList);

        final ArrayList<File> songs = updateSongList(Environment.getExternalStorageDirectory());
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

    public void customToast(String text){
        Toast.makeText(getApplicationContext(),text, Toast.LENGTH_SHORT).show();
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

}
