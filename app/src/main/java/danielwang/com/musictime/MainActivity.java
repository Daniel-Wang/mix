package danielwang.com.musictime;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

public class MainActivity extends Activity implements Button.OnClickListener{
    private final Button[] mNumbers = new Button[10];
    private final int[] mInput = {0, 0, 0, 0, 0, 0};
    private final TextView[] enteredNum = new TextView[6];
    private int mInputPointer = -1;
    private ImageButton mDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
    }

    /** Called when the user clicks the Start button */
    public void startTimer(View view) {
        Intent intent = new Intent(this, countdownActivity.class);
        intent.putExtra("hours", (10 * mInput[5] + mInput[4]));
        intent.putExtra("minutes", (10 * mInput[3] + mInput[2]));
        intent.putExtra("seconds", (10 * mInput[1] + mInput[0]));
        startActivity(intent);
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
}
