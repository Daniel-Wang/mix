package danielwang.com.musictime;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Daniel on 2016-11-28.
 */

public class SplashActivity extends AppCompatActivity {
    private ArrayList<SongFile> songList;
    private int shortestLength = 0;
    private ArrayList<File> fileList = new ArrayList<>();
    final String PREFS_NAME = "MyPrefsFile";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

        if (settings.getBoolean("my_first_time", true)) {
            //the app is being launched for first time, do something
            Log.d("Comments", "First time");

            // first time task

            // record the fact that the app has been started at least once
            settings.edit().putBoolean("my_first_time", false).commit();

            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();

        } else {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {


                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {

                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission
                    ActivityCompat.requestPermissions(SplashActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0x4);
                } else {
                    // No explanation needed, we can request the permission.

                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            0x4);

                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.

                }

            } else{
                songList = findSongs(Environment.getExternalStorageDirectory());
                shortestLength = songList.get(0).getDuration();
                Log.e("This is in", "the else");
                Log.e("Shortest length", "" + songList.get(0).toString());

                for(int i = 0; i < songList.size(); i++)
                    fileList.add(songList.get(i).getFile());

                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("songList", fileList);

                intent.putExtra("shortestLength", shortestLength);
                startActivity(intent);
                finish();
            }
        }
        // Here, this is the current activity


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(ActivityCompat.checkSelfPermission(this, permissions[0]) == PackageManager.PERMISSION_GRANTED){
            songList = findSongs(Environment.getExternalStorageDirectory());
            shortestLength = songList.get(0).getDuration();

            Log.e("Shortest length", "" + songList.get(0).getFile().toString());
            for(int i = 0; i < songList.size(); i++)
                fileList.add(songList.get(i).getFile());

        } else {
            Toast.makeText(this, "Aw, this app won't work without your permission", Toast.LENGTH_SHORT).show();
        }
    }

    public ArrayList<SongFile> findSongs(File root) {
        ArrayList<SongFile> al = new ArrayList<>();
        File[] files = root.listFiles();

        for (File singleFile : files) {
            if (singleFile.isDirectory() && !singleFile.isHidden()) {
                al.addAll(findSongs(singleFile));
            } else {
                if (singleFile.getName().endsWith(".mp3") ||
                        singleFile.getName().endsWith(".wav")) {
                    final SongFile song = new SongFile(singleFile.getName(), singleFile, this);
                    al.add(song);
                }
            }
        }

        Collections.sort(al);
        return al;
    }
}
