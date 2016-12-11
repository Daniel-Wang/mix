package danielwang.com.musictime;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends Activity implements Button.OnClickListener{
    private final Button[] mNumbers = new Button[10];
    private final int[] mInput = {0, 0, 0, 0, 0, 0};
    private final TextView[] enteredNum = new TextView[6];
    private int mInputPointer = -1;
    private ImageButton mDelete;
    private ArrayList<SongFile> songList;
    private ArrayList<File> fileList = new ArrayList<>();
    private int shortestLength = 0;
    private TextView warningView;
    private TextView loadingMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        warningView = (TextView) findViewById(R.id.tooShortWarning);
        loadingMessage = (TextView) findViewById(R.id.loadingMessage);

        mDelete = (ImageButton) findViewById(R.id.delete);
        mDelete.setOnClickListener(this);

        mNumbers[1] = (Button) findViewById(R.id.key_one);
        mNumbers[2] = (Button) findViewById(R.id.key_two);
        mNumbers[3] = (Button) findViewById(R.id.key_three);
        mNumbers[4] = (Button) findViewById(R.id.key_four);
        mNumbers[5] = (Button) findViewById(R.id.key_five);
        mNumbers[6] = (Button) findViewById(R.id.key_six);
        mNumbers[7] = (Button) findViewById(R.id.key_seven);
        mNumbers[8] = (Button) findViewById(R.id.key_eight);
        mNumbers[9] = (Button) findViewById(R.id.key_nine);
        mNumbers[0] = (Button) findViewById(R.id.key_zero);

        enteredNum[0] = (TextView) findViewById(R.id.seconds_ones);
        enteredNum[1] = (TextView) findViewById(R.id.seconds_tens);
        enteredNum[2] = (TextView) findViewById(R.id.minutes_ones);
        enteredNum[3] = (TextView) findViewById(R.id.minutes_tens);
        enteredNum[4] = (TextView) findViewById(R.id.hours_ones);
        enteredNum[5] = (TextView) findViewById(R.id.hours_tens);


        for (int i = 0; i < mNumbers.length; i++){
            mNumbers[i].setOnClickListener(this);
            mNumbers[i].setTag(i);
        }
        updateTime();

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {


            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0x4);
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

            Intent intent = getIntent();

            fileList = (ArrayList<File>) getIntent().getSerializableExtra("songList");
            shortestLength = intent.getIntExtra("shortestLength", 0);
//            Log.e("Shortest length", "" + fileList.get(0).toString());

            loadingMessage.setVisibility(View.INVISIBLE);
        }

    }

    /** Called when the user clicks the Start button */
    public void startTimer(View view) {
        if(toMilliSec((10 * mInput[5] + mInput[4]), (10 * mInput[3] + mInput[2]), (10 * mInput[1] + mInput[0])) < shortestLength){
            String mmin = "00";
            String msec = "00";
            mmin = Integer.toString((shortestLength/ 1000 % 3600) / 60);
            msec = Integer.toString(shortestLength/ 1000 % 60);

            warningView.setText("Warning! You have no songs shorter than " + mmin + ":" + msec);
            warningView.setVisibility(View.VISIBLE);

            final Animation in = new AlphaAnimation(0.0f, 1.0f);
            final Animation out = new AlphaAnimation(1.0f, 0.0f);

            warningView.startAnimation(in);
            warningView.startAnimation(out);

            in.setDuration(1200);
            in.setFillAfter(true);
            out.setDuration(1200);
            out.setFillAfter(true);
            out.setStartOffset(3000+in.getStartOffset());
        } else{
            Intent intent = new Intent(this, countdownActivity.class);
            intent.putExtra("hours", (10 * mInput[5] + mInput[4]));
            intent.putExtra("minutes", (10 * mInput[3] + mInput[2]));
            intent.putExtra("seconds", (10 * mInput[1] + mInput[0]));


            intent.putExtra("songList", fileList);

            intent.putExtra("shortestLength", shortestLength);

            startActivity(intent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(ActivityCompat.checkSelfPermission(this, permissions[0]) == PackageManager.PERMISSION_GRANTED){

            songList = findSongs(Environment.getExternalStorageDirectory());
            shortestLength = songList.get(0).getDuration();

//            Log.e("Shortest length", "" + songList.get(0).getFile().toString());
            for(int i = 0; i < songList.size(); i++)
                fileList.add(songList.get(i).getFile());

            loadingMessage.setText("Your music is ready to go!");

            final Animation in = new AlphaAnimation(0.0f, 1.0f);
            final Animation out = new AlphaAnimation(1.0f, 0.0f);

            loadingMessage.startAnimation(in);
            loadingMessage.startAnimation(out);

            in.setDuration(1200);
            in.setFillAfter(true);
            out.setDuration(1200);
            out.setFillAfter(true);
            out.setStartOffset(3000+in.getStartOffset());

        } else {
            Toast.makeText(this, "Aw, this app won't work without your permission", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        final Integer n = (Integer) v.getTag();
        // A number was pressed
        if (n != null) {
            // pressing "0" as the first digit does nothing
            if (mInputPointer == -1 && n == 0) {
                return;
            }
            // No space for more digits, so ignore input.
            if (mInputPointer == mInput.length - 1) {
                return;
            }
            // Append the new digit.
            System.arraycopy(mInput, 0, mInput, 1, mInputPointer + 1);
            mInput[0] = n;
            mInputPointer++;
            updateTime();
        }
        // other keys
        if (v == mDelete) {
            if (mInputPointer < 0) {
                // Nothing exists to delete so return.
                return;
            }
            System.arraycopy(mInput, 1, mInput, 0, mInputPointer);
            mInput[mInputPointer] = 0;
            mInputPointer--;
            updateTime();
        }
    }

    private void updateTime() {
        for (int i = 0; i < enteredNum.length; i++)
            enteredNum[i].setText(String.valueOf(mInput[i]));
    }

    int toMilliSec(int hour, int min, int sec) {
        return (hour * 3600000 + min * 60000 + sec * 1000);
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
