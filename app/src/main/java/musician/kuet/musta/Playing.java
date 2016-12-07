package musician.kuet.musta;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
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
import java.util.Arrays;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;


public class Playing extends Activity implements View.OnClickListener {

	private static final String TAG = null;
	String[] songItems;
	String testItems;
	ListView songList;
	File internal, external, externalStorageRoot, temp;
	File[] filess;
	int counter = 0;
	Button playingSong;
	NowPlaying status = new NowPlaying();
	CallbackManager callbackManager;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		FacebookSdk.sdkInitialize(this.getApplicationContext());
		/*AppEventsLogger.activateApp(getApplicationContext());
		FacebookSdk.sdkInitialize(getApplicationContext());*/
		callbackManager = CallbackManager.Factory.create();
		//LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
		LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
			@Override
			public void onSuccess(LoginResult loginResult) {
				Toast.makeText(getApplicationContext(), "Successfully logged in!!", Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onCancel() {
				Toast.makeText(getApplicationContext(), "Cancelled!!", Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onError(FacebookException error) {
				Toast.makeText(getApplicationContext(), "Error occurred!!", Toast.LENGTH_SHORT).show();
			}
		});


		LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile"));
		setContentView(R.layout.activity_playing);
		ArrayList<File> root = new ArrayList<File>();
		playingSong = (Button) findViewById(R.id.playingSong);
		TextView songsSize = (TextView) findViewById(R.id.songsSize);
		playingSong.setOnClickListener(this);
		songList = (ListView) findViewById(R.id.lvSongList);
		/*

		 */
		if (status.active){
			findViewById(R.id.playingLayoutInn).setVisibility(View.VISIBLE);
			findViewById(R.id.playPause).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					try{
						status.setPlayPause();
					}catch (NullPointerException e){
						e.printStackTrace();
					}
				}
			});
		}

		final String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {  // we can read the External Storage...
			//Retrieve the primary External Storage:
			final File primaryExternalStorage = Environment.getExternalStorageDirectory();

			//Retrieve the External Storages root directory:
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
				Log.i("Bluestacks ", "Working");
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
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		callbackManager.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_playing, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()){
			case R.id.action_exit:{
				if (status.active)
					status.finish();
				finish();
				break;
			}
			default:{

			}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onResume() {
		super.onResume();
		AppEventsLogger.activateApp(this);
		Log.i("playing", "onResumed");
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		Log.i("playing", "onRestart");
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
		//finish();
		AppEventsLogger.deactivateApp(this);
	}


	@Override
	protected void onStop() {
		super.onStop();
		Log.i("Playing ", "stop called");
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
				try {
					startActivity(intent);
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
			} else {
				Log.i("now ", "false");
				try {
					Toast.makeText(Playing.this, "Nothing is playing....", Toast.LENGTH_LONG).show();
				} catch (IndexOutOfBoundsException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
