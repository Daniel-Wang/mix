package danielwang.com.musictime;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

// depth first traversal
// node is song
// edge with cost is length of song
//


public class countdownActivity extends Activity implements MediaPlayer.OnCompletionListener {
    final int MINUTE = 60000;

    private int pos = 50;

    File[] twomin;
    File[] fourmin;
    File[] sixmin;

    public int hours;
    public int minutes;
    public int seconds;
    private final TextView[] enteredNum = new TextView[3];

    Button playButton;
    CountDownTimer timer;
    long milliLeft;
    MediaPlayer mp;


    private ArrayList<File> songList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_countdown);

        songList = findSongs(Environment.getExternalStorageDirectory());

        //playButton = (ImageView) findViewById(R.id.pause_button);
        playButton = (Button) findViewById(R.id.pause_button);


        // Here, this is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {


            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission
            } else {
                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        1);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }

            return;
        }

        doCountDown();

        if (mp != null) {
            mp.stop();
            mp.release();
        }

        playSong(pos);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    doCountDown();
                }
            }
        }
    }


    private void doCountDown() {
//        ContentResolver musicResolver = getContentResolver();
//        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
//        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);


        Intent intent = getIntent();

        hours = intent.getIntExtra("hours", 0);
        minutes = intent.getIntExtra("minutes", 0);
        seconds = intent.getIntExtra("seconds", 0);

        enteredNum[0] = (TextView) findViewById(R.id.cd_seconds);
        enteredNum[1] = (TextView) findViewById(R.id.cd_minutes);
        enteredNum[2] = (TextView) findViewById(R.id.cd_hours);

        enteredNum[2].setText(String.valueOf(hours));
        enteredNum[1].setText(String.valueOf(minutes));
        enteredNum[0].setText(String.valueOf(seconds));

        //generatePlaylist(toMilliSec(hours, minutes, seconds), songList);

        timer = new CountDownTimer(toMilliSec(hours, minutes, seconds), 1000) {

            public void onTick(long millisUntilFinished) {
                milliLeft = millisUntilFinished;

                enteredNum[2].setText(String.valueOf(millisUntilFinished / 1000 / 3600));
                enteredNum[1].setText(String.valueOf((millisUntilFinished / 1000 % 3600) / 60));
                enteredNum[0].setText(String.valueOf(millisUntilFinished / 1000 % 60));
            }

            public void onFinish() {
                enteredNum[0].setText(String.valueOf(0));
            }
        }.start();
    }


//    public void generatePlaylist(int millisec, ArrayList<File> songs) {
//        int length = songs.size();
//
//        twomin = new File[length];
//        fourmin = new File[length];
//        sixmin = new File[length];
//        int a = 0;
//        int b = 0;
//        int c = 0;
//
//        for (int i = 0; i < songList.size(); i++) {
//            if (songList.get(i). <= 2 * MINUTE) {
//                twomin[a] = songList.get(i);
//                a++;
//            } else if (songList.get(i).getDuration() <= 4 * MINUTE) {
//                fourmin[b] = songList.get(i);
//                b++;
//            } else {
//                sixmin[c] = songList.get(i);
//                c++;
//            }
//        }
//    }

    public ArrayList<File> findSongs(File root) {
        ArrayList<File> al = new ArrayList<File>();
        File[] files = root.listFiles();

        for (File singleFile : files) {
            if (singleFile.isDirectory() && !singleFile.isHidden()) {
                al.addAll(findSongs(singleFile));
            } else {
                if (singleFile.getName().endsWith(".mp3") ||
                        singleFile.getName().endsWith(".wav")) {
                    al.add(singleFile);
                }
            }
        }
        return al;
    }

    int toMilliSec(int hour, int min, int sec) {
        return (hour * 3600000 + min * 60000 + sec * 1000);
    }

    public void playSong(int songPos) {
        Uri u = Uri.parse(songList.get(songPos).toString());
        mp = MediaPlayer.create(this, u);
        mp.setOnCompletionListener(this);
        mp.start();
    }

    @Override
    public void onCompletion(MediaPlayer arg0) {

        // no repeat or shuffle ON - play next song
        if (pos < songList.size() - 1) {
            pos++;
            playSong(pos);
        }


    }


    public void buttonOnClick(View view) {

        String status = playButton.getText().toString();
        if (status.equals("Pause")) {
            //playButton.setImageResource(R.drawable.ic_play);
            timer.cancel();
            mp.pause();
            playButton.setText("Play");
        } else {
            playButton.setText("Pause");
            mp.start();
            timer = new CountDownTimer(milliLeft, 1000) {

                public void onTick(long millisUntilFinished) {
                    milliLeft = millisUntilFinished;

                    enteredNum[2].setText(String.valueOf(millisUntilFinished / 1000 / 3600));
                    enteredNum[1].setText(String.valueOf((millisUntilFinished / 1000 % 3600) / 60));
                    enteredNum[0].setText(String.valueOf(millisUntilFinished / 1000 % 60));
                }

                public void onFinish() {
                    enteredNum[0].setText(String.valueOf(0));
                }
            }.start();
        }

    }

}
