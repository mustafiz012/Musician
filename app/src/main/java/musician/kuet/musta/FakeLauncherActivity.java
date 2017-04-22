package musician.kuet.musta;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class FakeLauncherActivity extends Activity {

    private boolean isPermissionGranted = false;
    private boolean isPermissionRequested = false;
    private final static int READ_EXTERNAL_STORAGE_REQUEST_CODE = 201;
    private final static int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 202;
    private Button allow_permission_fake = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fake_launcher);
        Toolbar toolbar = (Toolbar) findViewById(R.id.fake_layout_toolbar);
        toolbar.setTitle(getString(R.string.app_name));
        allow_permission_fake = (Button) findViewById(R.id.allow_permission_fake);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //checking runtime permission
            setRuntimePermissionRequest();
            if (isPermissionGranted && !isPermissionRequested) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else {
                allow_permission_fake = (Button) findViewById(R.id.allow_permission_fake);
                findViewById(R.id.application_name_home_screen).setVisibility(View.GONE);
                allow_permission_fake.setVisibility(View.VISIBLE);
                allow_permission_fake.setClickable(true);
                allow_permission_fake.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setRuntimePermissionRequest();
                    }
                });
            }
        }else{
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("Fake", "onDestroy");
    }

    private void setRuntimePermissionRequest() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE_REQUEST_CODE);
                isPermissionRequested = true;
            } else {
                isPermissionGranted = true;
            }
        } else {
            isPermissionGranted = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isPermissionGranted) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case READ_EXTERNAL_STORAGE_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    isPermissionGranted = true;
                } else {
                    isPermissionGranted = false;
                    allow_permission_fake.setClickable(true);
                    allow_permission_fake.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            setRuntimePermissionRequest();
                        }
                    });
                }
                break;
            }
        }
    }
}
