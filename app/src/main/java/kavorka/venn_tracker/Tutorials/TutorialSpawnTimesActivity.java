package kavorka.venn_tracker.Tutorials;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import kavorka.venn_tracker.R;

public class TutorialSpawnTimesActivity extends AppCompatActivity {
    private Spinner mSpinnerSpawnTimes;
    private ImageButton mArrowLeft;
    private ImageButton mArrowRight;
    private TextView mTextPageNumber;
    private ImageView mBackgroundImage;
    private TextView mTextMain;
    private GestureDetectorCompat mGestureDetector;
    private static final int SWIPE_THRESHOLD = 100;
    private static final int SWIPE_VELOCITY_THRESHOLD = 100;

    private int mPageNumber;
    private final int mPAGEFINAL = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorials_spawn_points);

        mGestureDetector = new GestureDetectorCompat(this, new TutorialSpawnTimesActivity.MyGestureListener());


        mSpinnerSpawnTimes = (Spinner) findViewById(R.id.spinnerTutorialsMenu);
        mTextPageNumber = (TextView) findViewById(R.id.tutorialsPageNumber);
        mTextMain = (TextView) findViewById(R.id.textViewTutorials);
        mBackgroundImage= (ImageView) findViewById(R.id.imageViewTutorials);

        mArrowLeft = (ImageButton) findViewById(R.id.buttonArrowLeft);
        mArrowRight = (ImageButton) findViewById(R.id.buttonArrowRight);

        mArrowLeft.setVisibility(View.INVISIBLE);

        mPageNumber = 1;


        mArrowLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pagePrevious();
            }
        });

        mArrowRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pageNext();
            }
        });


        ArrayAdapter<String> adapter = new ArrayAdapter<>(TutorialSpawnTimesActivity.this,
                android.R.layout.simple_spinner_item,TutorialSpinnerLists.getSpawnTimesList());

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerSpawnTimes.setAdapter(adapter);

        mSpinnerSpawnTimes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextColor(Color.WHITE);
                // Sets page number based on selection so we can set the page contents
                switch (position) {
                    case 0:
                        mPageNumber = 1;
                        break;
                    case 1:
                        if (mPageNumber < 3) {
                            mPageNumber = 3;
                        }
                        break;
                    case 2:
                        startActivity(new Intent(TutorialSpawnTimesActivity.this, TutorialsActivity.class));
                        break;
                }
                setPage();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }


    // Changes all fields for each page
    public void setPage() {
        mTextPageNumber.setText(mPageNumber + "/" + mPAGEFINAL);
        if (mPageNumber == 1) {
            // On page 1, you cant go to a previous page, only show right arrow
            mArrowLeft.setVisibility(View.INVISIBLE);
            mArrowRight.setVisibility(View.VISIBLE);
        }
        else if (mPageNumber == mPAGEFINAL) {
            // On last page, you cant go to next page, only show left arrow
            mArrowRight.setVisibility(View.INVISIBLE);
            mArrowLeft.setVisibility(View.VISIBLE);
        }
        else {
            // if not on first or last page, show both arrows
            mArrowLeft.setVisibility(View.VISIBLE);
            mArrowRight.setVisibility(View.VISIBLE);
        }
        // Checks what page we are on and sets fields accordingly
        switch (mPageNumber) {
            case 1:
                mBackgroundImage.setImageResource(R.drawable.ic_tutorials_spawn_times_1);
                mTextMain.setText(R.string.Tutorials_Spawn_Times_1);
                mSpinnerSpawnTimes.setSelection(0);
                break;
            case 2:
                mBackgroundImage.setImageResource(R.drawable.ic_tutorials_spawn_times_1);
                mTextMain.setText(R.string.Tutorials_Spawn_Times_2);
                mSpinnerSpawnTimes.setSelection(0);
                break;
            case 3:
                mBackgroundImage.setImageResource(R.drawable.ic_tutorials_spawn_times_2);
                mTextMain.setText(R.string.Tutorials_Spawn_Times_3);
                mSpinnerSpawnTimes.setSelection(1);
                break;
            case 4:
                mBackgroundImage.setImageResource(R.drawable.ic_tutorials_spawn_times_3);
                mTextMain.setText(R.string.Tutorials_Spawn_Times_4);
                mSpinnerSpawnTimes.setSelection(1);
                break;
            case 5:
                mBackgroundImage.setImageResource(R.drawable.ic_tutorials_spawn_times_1);
                mTextMain.setText(R.string.Tutorials_Spawn_Times_5);
                mSpinnerSpawnTimes.setSelection(1);
                break;
        }
    }

    // Set methods for previous and next page so we can call on button click and swipe
    public void pagePrevious() {
        // Only decrement page number if it is not the first page
        if (mPageNumber > 1) {
            mPageNumber--;
        }
        setPage();
    }

    public void pageNext() {
        // Only increment page number if it is not the last page
        if (mPageNumber < mPAGEFINAL) {
            mPageNumber++;
        }
        setPage();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        this.mGestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent event) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2,
                               float velocityX, float velocityY) {
            float diffX = event2.getX() - event1.getX();
            if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                if (diffX > 0) {
                    pagePrevious();
                } else {
                    pageNext();
                }
            }
            return true;
        }
    }
}
