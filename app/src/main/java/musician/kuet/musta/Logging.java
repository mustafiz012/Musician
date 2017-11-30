package musician.kuet.musta;

import android.util.Log;

/**
 * Created by musta on 3/18/2017.
 */

public class Logging {
	private static final Logging ourInstance = new Logging();

	private Logging() {
	}

	public static Logging getInstance() {
		return ourInstance;
	}

	public void I(String classes, String s) {
		Log.i(classes + " ", "" + s);
	}
}
