package danielwang.com.musictime;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;


public class countdownActivity extends Activity implements MediaPlayer.OnCompletionListener {

    private int songIndex = -1;
    private int shortestLength = 0;
    private int pos = 0;
    private Boolean pickShortest = false;
    private final int MARGIN = 20000;

    public int hours;
    public int minutes;
    public int seconds;
    private final TextView[] enteredNum = new TextView[3];

    Button playButton;
    CountDownTimer timer;
    long milliLeft;
    MediaPlayer mp;

    int timeLeft = 0;


    private ArrayList<File> songList;
    private ArrayList<MediaPlayer> playlist = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_countdown);



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


        Intent intent = getIntent();

        hours = intent.getIntExtra("hours", 0);
        minutes = intent.getIntExtra("minutes", 0);
        seconds = intent.getIntExtra("seconds", 0);

        songList = (ArrayList<File>) getIntent().getSerializableExtra("songList");
        shortestLength = intent.getIntExtra("shortestLength", 0);

        timeLeft = toMilliSec(hours, minutes, seconds);
        Log.e("Hello", Integer.toString(timeLeft));

        if (mp != null) {
            mp.stop();
            mp.release();
        }

        while(timeLeft > MARGIN){
            Log.e("songIndex", Integer.toString(songIndex));
            Uri u = Uri.parse(pickRandSong().toString());
            MediaPlayer singleSong = MediaPlayer.create(this, u);
            playlist.add(singleSong);
            songIndex++;

            timeLeft -= singleSong.getDuration();
            if(timeLeft > 0 && timeLeft < MARGIN){
                //You're done, break out of the loop
                Log.e("Break", Integer.toString(songIndex));
                break;
            } else if(timeLeft < shortestLength){
                //Pop off a song and find a fit song and end
                Log.e("Pop off 1", Integer.toString(songIndex));
                for (int x = 0; x < playlist.size(); x++){
                    Log.e("Songs", playlist.get(x).toString());
                }
                timeLeft += playlist.get(songIndex).getDuration();
                playlist.remove(songIndex);
                songIndex--;
                pickShortest = true;
                Log.e("Pop off 2", Integer.toString(songIndex));
                for (int x = 0; x < playlist.size(); x++){
                    Log.e("Songs", playlist.get(x).toString());
                }

                playlist.add(fitSong(timeLeft));
                songIndex++;
                Log.e("Add last", Integer.toString(songIndex));
                for (int x = 0; x < playlist.size(); x++){
                    Log.e("Songs", playlist.get(x).toString());
                }
                break;
            }
        }

        playSong(pos);
        doCountDown();

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


        enteredNum[0] = (TextView) findViewById(R.id.cd_seconds);
        enteredNum[1] = (TextView) findViewById(R.id.cd_minutes);
        enteredNum[2] = (TextView) findViewById(R.id.cd_hours);

        enteredNum[2].setText(String.valueOf(hours));
        enteredNum[1].setText(String.valueOf(minutes));
        enteredNum[0].setText(String.valueOf(seconds));

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

    public File pickRandSong(){
        Random rand = new Random();

        int n = rand.nextInt(songList.size());
        return songList.get(n);

    }

    public MediaPlayer fitSong(int duration){
        Boolean arrayComplete = false;
        MediaPlayer myMp;
        ArrayList<MediaPlayer> media = new ArrayList<>();
        int indexSongList = 0;
        int index = -1;

        while(!arrayComplete){
            Uri u = Uri.parse(songList.get(indexSongList).toString());
            mp = MediaPlayer.create(this, u);

            if(mp.getDuration() < duration - MARGIN){
                indexSongList++;
                Log.e("indexSongList", Integer.toString(indexSongList));
                continue;
            }
            if(mp.getDuration() > (duration - MARGIN) && mp.getDuration() < (duration + MARGIN)){
                media.add(mp);
                index++;
                indexSongList++;
                Log.e("index", Integer.toString(index));
                Log.e("indexSongList", Integer.toString(indexSongList));

            } else if(mp.getDuration() > (duration + MARGIN)){
                media.remove(index);
                index--;
                arrayComplete = true;
                Log.e("Hello", "For the depths of the else if");
            }
        }
        Random rand = new Random();
        int n = rand.nextInt(media.size());
        myMp = media.get(n);
        Log.e("Song", myMp.toString());
        return myMp;
    }



    int toMilliSec(int hour, int min, int sec) {
        return (hour * 3600000 + min * 60000 + sec * 1000);
    }

    public void playSong(int songPos) {
        mp = playlist.get(songPos);
        mp.setOnCompletionListener(this);
        mp.start();
    }

    @Override
    public void onCompletion(MediaPlayer arg0) {

        // no repeat or shuffle ON - play next song
        if (pos < playlist.size() - 1) {
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
