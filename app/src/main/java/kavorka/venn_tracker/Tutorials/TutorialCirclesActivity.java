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


public class TutorialCirclesActivity extends AppCompatActivity {
    private Spinner mSpinnerCircles;
    private ImageButton mArrowLeft;
    private ImageButton mArrowRight;
    private TextView mTextPageNumber;
    private ImageView mBackgroundImage;
    private TextView mTextMain;
    private GestureDetectorCompat mGestureDetector;
    private static final int SWIPE_THRESHOLD = 100;
    private static final int SWIPE_VELOCITY_THRESHOLD = 100;

    private int mPageNumber;
    private final int mPAGEFINAL = 9;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorials_circles);

        mGestureDetector = new GestureDetectorCompat(this, new MyGestureListener());


        mSpinnerCircles = (Spinner) findViewById(R.id.spinnerTutorialsMenu);
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


        ArrayAdapter<String> adapter = new ArrayAdapter<>(TutorialCirclesActivity.this,
                android.R.layout.simple_spinner_item,TutorialSpinnerLists.getCirclesList());

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerCircles.setAdapter(adapter);

        mSpinnerCircles.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextColor(Color.WHITE);
                // Sets page number based on selection so we can set the page contents
                switch (position) {
                    case 0:
                        mPageNumber = 1;
                        break;
                    case 1:
                        if (mPageNumber <=2 && mPageNumber >=7) {
                            mPageNumber = 2;
                        }
                        break;
                    case 2:
                        if (mPageNumber <= 7 || mPageNumber >=9) {
                        mPageNumber = 7;}
                        break;
                    case 3:
                        mPageNumber = 9;
                        break;
                    case 4:
                        startActivity(new Intent(TutorialCirclesActivity.this, TutorialsActivity.class));
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
                mBackgroundImage.setImageResource(R.drawable.ic_tutorials_circles_1);
                mTextMain.setText(R.string.Tutorials_Circles_1);
                mSpinnerCircles.setSelection(0);
                break;
            case 2:
                mBackgroundImage.setImageResource(R.drawable.ic_tutorials_circles_2);
                mTextMain.setText(R.string.Tutorials_Circles_2);
                mSpinnerCircles.setSelection(1);
                break;
            case 3:
                mBackgroundImage.setImageResource(R.drawable.ic_tutorials_circles_3);
                mTextMain.setText(R.string.Tutorials_Circles_3);
                mSpinnerCircles.setSelection(1);
                break;
            case 4:
                mBackgroundImage.setImageResource(R.drawable.ic_tutorials_circles_4);
                mTextMain.setText(R.string.Tutorials_Circles_4);
                mSpinnerCircles.setSelection(1);
                break;
            case 5:
                mBackgroundImage.setImageResource(R.drawable.ic_tutorials_circles_5);
                mTextMain.setText(R.string.Tutorials_Circles_5);
                mSpinnerCircles.setSelection(1);
                break;
            case 6:
                mBackgroundImage.setImageResource(R.drawable.ic_tutorials_circles_6);
                mTextMain.setText(R.string.Tutorials_Circles_6);
                mSpinnerCircles.setSelection(1);
                break;
            case 7:
                mBackgroundImage.setImageResource(R.drawable.ic_tutorials_circles_7);
                mTextMain.setText(R.string.Tutorials_Circles_7);
                mSpinnerCircles.setSelection(2);
                break;
            case 8:
                mBackgroundImage.setImageResource(R.drawable.ic_tutorials_circles_8);
                mTextMain.setText(R.string.Tutorials_Circles_8);
                mSpinnerCircles.setSelection(2);
                break;
            case 9:
                mBackgroundImage.setImageResource(R.drawable.ic_tutorials_circles_9);
                mTextMain.setText(R.string.Tutorials_Circles_9);
                mSpinnerCircles.setSelection(3);
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
