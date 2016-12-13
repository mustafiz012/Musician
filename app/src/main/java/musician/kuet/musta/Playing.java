package musician.kuet.musta;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
	File internal, external, externalStorageRoot;
	File[] filess;
	int counter = 0;
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
		registerForContextMenu(songList);
		final String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {  // we can read the External Storage...
			//Retrieve the primary External Storage:
			final File primaryExternalStorage = Environment.getExternalStorageDirectory();

			//Retrieve the External Storage root directories:
			final String externalStorageRootDir;
			if ((externalStorageRootDir = primaryExternalStorage.getParent()) == null) {  // no parent...
				Log.d(TAG, "External Storage: " + primaryExternalStorage + "\n");
			} else {
				externalStorageRoot = new File(externalStorageRootDir);
				filess = externalStorageRoot.listFiles();
				for (final File file : filess) {
					if (file.isDirectory() && file.canRead() && (file.listFiles().length > 0)) {  // it is a real directory (not a USB drive)...
						Log.d(TAG, "External Storage: " + file.getAbsolutePath() + "\n");
						//customToast("" + file.getAbsolutePath());
						counter++;
						if (counter == 1) {
							testItems = file.getAbsolutePath();
							//customToast(""+testItems);
						}
					}
				}
			}
		}


		//select music root storage (if extSdCard present, two roots will be returned)
		for (int j = 1; j <= 2; j++) {
			//internal storage directory
			internal = Environment.getExternalStorageDirectory();
			//external storage directory
			external = new File(testItems);
			//collecting audio songs from internal storage
			final ArrayList<File> songs = updateSongList(internal);

			if (testItems != null) {
				//Log.i("Bluestacks ", "Working");
				//collecting audio songs from external storage
				final ArrayList<File> song2 = updateSongList(external);
				//all songs getting together
				songs.addAll(song2);
			}

			songsSize.setText(songs.size() + " songs");
			songItems = new String[songs.size()];
			for (int i = 0; i < songs.size(); i++) {
				//getting together all songs into a String array
				songItems[i] = songs.get(i).getName().toString().replace(".mp3", "").replace(".wav", "");
			}
			//listing out the songList in Playing activity
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.playlist_item, R.id.songTitle, songItems);
			songList.setAdapter(adapter);
			//playing specific song by clicking on the the song item
			songList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					startActivity(new Intent(getApplicationContext(), NowPlaying.class).putExtra("pos", position).putExtra("songs", songs));
				}
			});
			/*//trying to check out the option of the song item by pressing and holding on each
			songList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
				@Override
				public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
					customToast(""+songs.get(position).getName().toString().replace(".mp3", ""));

					return false;
				}
			});*/
		}


	}

	public void addDialog() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
		//builder.setTitle(""+songs.get(position).getName().toString().replace(".mp3", ""));
		builder.setItems(R.array.song_properties, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (which == 4)
					customToast("Deletion will be implemented soon :P");
				else if (which == 3)
					customToast("Quick list will be implemented soon :P");
				else if (which == 2)
					customToast("Playlist will be implemented soon :P");
				else if (which == 1)
					customToast("SET AS functionality will be implemented soon :P");
				else if (which == 0)
					customToast("Sharing functionality will be implemented soon :P");
			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	boolean doubleBackToExitPressedOnce = false;

	@Override
	public void onBackPressed() {
		if (doubleBackToExitPressedOnce) {
			moveTaskToBack(true);
		}

		this.doubleBackToExitPressedOnce = true;
		Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

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

	@Override
	protected void onPause() {
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
		if (v.getId() == R.id.lvSongList) {
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
		Log.i("Playing ", "onStop");
	}

	@Override
	public void onClick(View v) {
		if (v == playingSong) {
			Intent intent = new Intent();
			intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			intent.setClass(Playing.this, NowPlaying.class);
			//checking if the NowPlaying activity is active or not
			if (status.active == true) {
				Log.i("NowPlaying ", "active is true");
				startActivity(intent);
			} else {
				//Log.i("Playing ", "false");
				try {
					Toast.makeText(Playing.this, "Nothing is playing....", Toast.LENGTH_LONG).show();
				} catch (IndexOutOfBoundsException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
