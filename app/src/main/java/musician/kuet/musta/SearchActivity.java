package musician.kuet.musta;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListView;

public class SearchActivity extends RootMediaActivity {

    private static final String TAG = "SearchActivity";
    private InputMethodManager mImm;
    private ListView recyclerView;
    private ComplexListAdapter complexListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Log.i(TAG, "onCreate: ");
        mImm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = (ListView) findViewById(R.id.recyclerView);
        recyclerView.setAdapter(rootMediaCursorAdapter);
    }
}
