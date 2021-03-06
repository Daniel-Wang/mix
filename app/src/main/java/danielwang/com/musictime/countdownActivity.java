package danielwang.com.musictime;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

public class countdownActivity extends Activity implements MediaPlayer.OnCompletionListener {

    private int songIndex = -1;
    private int shortestLength = 0;
    private int pos = 0;
    private final int MARGIN = 20000;
    private int progress = 100;
    private boolean paused = false;
    private boolean noSongInFitList = false;
    private boolean songFinished = false;

    public int hours;
    public int minutes;
    public int seconds;
    private final TextView[] enteredNum = new TextView[3];

    ImageView playButton;
    CountDownTimer timer;
    long milliLeft;
    MediaPlayer mp;

    int timeLeft = 0;
    int totalTime = 0;
    int increment = 0;


    private ArrayList<File> songList;
    private ArrayList<MediaPlayer> playlist = new ArrayList<>();

    private CircularFillableLoaders circularFillableLoaders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_countdown);

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

        circularFillableLoaders = (CircularFillableLoaders) findViewById(R.id.circularFillableLoaders);

//        // Set Wave Amplitude (between 0.00f and 0.10f)
        circularFillableLoaders.setAmplitudeRatio(0.03f);

        playButton = (ImageView) findViewById(R.id.pause_button);

        Intent intent = getIntent();

        hours = intent.getIntExtra("hours", 0);
        minutes = intent.getIntExtra("minutes", 0);
        seconds = intent.getIntExtra("seconds", 0);

        songList = (ArrayList<File>) getIntent().getSerializableExtra("songList");
        shortestLength = intent.getIntExtra("shortestLength", 0);

        timeLeft = toMilliSec(hours, minutes, seconds);
        totalTime = toMilliSec(hours, minutes, seconds);

        if (mp != null) {
            mp.stop();
            mp.reset();
        }

        while (timeLeft > MARGIN) {
//            Log.e("songIndex", Integer.toString(songIndex));
            String songName = pickRandSong().toString();
            Uri u = Uri.parse(songName);
            MediaPlayer singleSong = MediaPlayer.create(this, u);

            playlist.add(singleSong);
            songIndex++;

            timeLeft -= singleSong.getDuration();
            if (timeLeft > 0 && timeLeft < MARGIN) {
                //You're done, break out of the loop
//                Log.e("Break", Integer.toString(songIndex));
                break;
            } else if (timeLeft < shortestLength) {
                //Pop off a song and find a fit song and end
//                Log.e("Pop off 1", Integer.toString(songIndex));
//                for (int x = 0; x < playlist.size(); x++){
//                    Log.e("Songs", playlist.get(x).toString());
//                }
                timeLeft += playlist.get(songIndex).getDuration();
                playlist.remove(songIndex);
                songIndex--;
//                Log.e("Pop off 2", Integer.toString(songIndex));
//                for (int x = 0; x < playlist.size(); x++){
//                    Log.e("Songs", playlist.get(x).toString());
//                }
                MediaPlayer mediaP = fitSong(timeLeft);
                if (mediaP != null) {
                    playlist.add(mediaP);
                    songIndex++;
//                Log.e("Add last", Integer.toString(songIndex));
//                for (int x = 0; x < playlist.size(); x++){
//                    Log.e("Songs", playlist.get(x).toString());
//                }
                    break;
                }
            }
        }

        playSong(pos);
        playButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                if (milliLeft != 0) {
                    if (!paused) {
                        playButton.setImageResource(R.mipmap.ic_play);
                        timer.cancel();
                        mp.pause();
                        paused = true;
                    } else {
                        paused = false;
                        playButton.setImageResource(R.mipmap.ic_pause);
                        if(!songFinished){
                            mp.start();
                        }

                        timer = new CountDownTimer(milliLeft, 1000) {

                            public void onTick(long millisUntilFinished) {
                                milliLeft = millisUntilFinished;

                                enteredNum[2].setText(String.valueOf(millisUntilFinished / 1000 / 3600));
                                enteredNum[1].setText(String.valueOf((millisUntilFinished / 1000 % 3600) / 60));
                                enteredNum[0].setText(String.valueOf(millisUntilFinished / 1000 % 60));

                                if (increment > 0 && milliLeft % increment == 0 && progress != 0) {
                                    progress--;
                                } else if (increment == 0 && progress != 0 && milliLeft % Math.ceil((totalTime * 1.0 / 100000.0)) == 0) {
                                    progress--;
                                }
                                circularFillableLoaders.setProgress(progress);

                            }

                            public void onFinish() {
                                milliLeft = 0;
                                enteredNum[0].setText(String.valueOf(0));
                                circularFillableLoaders.setProgress(0);
                                playButton.setImageResource(R.mipmap.ic_replay);
                            }
                        }.start();
                    }
                } else {
                    paused = false;
                    playButton.setImageResource(R.mipmap.ic_pause);
                    pos = 0;
                    milliLeft = totalTime;
                    playSong(pos);
                    doCountDown();
                }
            }
        });
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
        progress = 100;
        circularFillableLoaders.setProgress(progress);

        enteredNum[0] = (TextView) findViewById(R.id.cd_seconds);
        enteredNum[1] = (TextView) findViewById(R.id.cd_minutes);
        enteredNum[2] = (TextView) findViewById(R.id.cd_hours);

        enteredNum[2].setText(String.valueOf(hours));
        enteredNum[1].setText(String.valueOf(minutes));
        enteredNum[0].setText(String.valueOf(seconds));

        increment = totalTime / 100000;

        timer = new CountDownTimer(totalTime, 1000) {

            public void onTick(long millisUntilFinished) {
                milliLeft = millisUntilFinished;

                enteredNum[2].setText(String.valueOf(millisUntilFinished / 1000 / 3600));
                enteredNum[1].setText(String.valueOf((millisUntilFinished / 1000 % 3600) / 60));
                enteredNum[0].setText(String.valueOf(millisUntilFinished / 1000 % 60));

                if (increment > 0 && milliLeft % increment == 0 && progress != 0) {
                    progress--;
                } else if (increment == 0 && progress != 0 && milliLeft % Math.ceil((totalTime * 1.0 / 100000.0)) == 0) {
                    progress--;
                }
                circularFillableLoaders.setProgress(progress);

            }

            public void onFinish() {
                milliLeft = 0;
                enteredNum[0].setText(String.valueOf(0));
                circularFillableLoaders.setProgress(0);
                playButton.setImageResource(R.mipmap.ic_replay);
            }
        }.start();
    }

    public File pickRandSong() {
        Random rand = new Random();

        int n = rand.nextInt(songList.size());
        return songList.get(n);

    }

    public MediaPlayer fitSong(int duration) {
        Boolean arrayComplete = false;
        MediaPlayer myMp;
        ArrayList<MediaPlayer> media = new ArrayList<>();
        int indexSongList = 0;
        int index = -1;

        while (!arrayComplete) {
            Uri u = Uri.parse(songList.get(indexSongList).toString());
//            Log.e("shortest:", songList.get(indexSongList).toString());
            myMp = MediaPlayer.create(this, u);

            if (myMp.getDuration() < duration - MARGIN) {
                indexSongList++;
//                Log.e("indexSongList", Integer.toString(indexSongList));
                continue;
            }
            if (myMp.getDuration() > (duration - MARGIN) && myMp.getDuration() < (duration + MARGIN)) {
                media.add(myMp);
                index++;
                indexSongList++;
//                Log.e("index", Integer.toString(index));
//                Log.e("indexSongList", Integer.toString(indexSongList));

            } else if (myMp.getDuration() > (duration + MARGIN) && index > 0) {
                media.remove(index);
                index--;
                arrayComplete = true;
//                Log.e("Hello", "From the depths of the else if");
            } else if (myMp.getDuration() > (duration + MARGIN)) {
                noSongInFitList = true;
            }
        }
        if (!noSongInFitList) {
            Random rand = new Random();
            int n = rand.nextInt(media.size());
            myMp = media.get(n);
            return myMp;
        } else {
            return null;
        }

    }

    int toMilliSec(int hour, int min, int sec) {
        return (hour * 3600000 + min * 60000 + sec * 1000);
    }

    public void playSong(int songPos) {
        songFinished = false;
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
        } else {
            songFinished = true;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mp != null) {
            mp.stop();
            mp.release();
        }
    }

}
