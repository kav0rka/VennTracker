package kavorka.venn_tracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;


public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences mSharedPreferences;

    private Button mOkButton;
    //private Button mLoadDB;
    private CheckBox mCameraUpdateCheckBox;
    private Boolean mCameraUpdate;
    private CheckBox mOverlayUpdateCheckBox;
    private Boolean mOverlayUpdate;
    private CheckBox mCircleSnapCheckBox;
    private Boolean mCircleSnap;
    private CheckBox mMarkerInCircleCheckBox;
    private static Boolean mMarkerInCircle;
    private CheckBox mLoadDistanceCheckBox;
    private static int mLoadDistanceInt;
    private static Boolean mLoadDistance;
    private EditText mLoadDistanceEditText;
    private int mCircleResolution;
    private Spinner mCircleResolutionSpinner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mSharedPreferences = SettingsActivity.this
                .getSharedPreferences(getString(R.string.PREF_FILE), MODE_PRIVATE);
        mCameraUpdate = mSharedPreferences.getBoolean("camera_update", false);
        mOverlayUpdate = mSharedPreferences.getBoolean("overlay_update", true);
        mCircleSnap = mSharedPreferences.getBoolean("circle_snap", true);
        mMarkerInCircle = mSharedPreferences.getBoolean("marker_in_circle", true);
        mLoadDistance = mSharedPreferences.getBoolean("load_distance", true);
        mLoadDistanceInt = mSharedPreferences.getInt("load_distance_int", 1000);
        mCircleResolution = mSharedPreferences.getInt("circle_resolution", 1);




        mOkButton = (Button) findViewById(R.id.buttonSettingsOk);
        //mLoadDB = (Button) findViewById(R.id.buttonLoadExternalDB);

        mCircleResolutionSpinner = (Spinner) findViewById(R.id.spinnerResolution);

        mCameraUpdateCheckBox = (CheckBox) findViewById(R.id.checkBoxCameraUpdate);
        mOverlayUpdateCheckBox = (CheckBox) findViewById(R.id.checkBoxOverlayUpdate);
        mCircleSnapCheckBox = (CheckBox) findViewById(R.id.checkBoxCircleSnap);
        mMarkerInCircleCheckBox= (CheckBox) findViewById(R.id.checkBoxMarkerInCircle);
        mLoadDistanceCheckBox = (CheckBox) findViewById(R.id.checkBoxSpawnDistance);
        mLoadDistanceEditText = (EditText) findViewById(R.id.editTextDistance);
        mLoadDistanceEditText.setText(mLoadDistanceInt + "");
        mLoadDistanceCheckBox.setChecked(mLoadDistance);
        mCameraUpdateCheckBox.setChecked(mCameraUpdate);
        mOverlayUpdateCheckBox.setChecked(mOverlayUpdate);
        mCircleSnapCheckBox.setChecked(mCircleSnap);
        mMarkerInCircleCheckBox.setChecked(mMarkerInCircle);


        String[] resolutions = {
                "Low", "Medium", "High"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(SettingsActivity.this,
                android.R.layout.simple_spinner_item, resolutions);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCircleResolutionSpinner.setAdapter(adapter);
        mCircleResolutionSpinner.setSelection(mCircleResolution);

        mCircleResolutionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                //((TextView) adapterView.getChildAt(0)).setTextColor(Color.BLACK);
                mCircleResolution = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });




        // Save Settings
        mOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Save camera update settings
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                if (mLoadDistanceEditText.getText().toString().equals(null) || mLoadDistanceEditText.getText().toString().equals("")) {
                    mLoadDistanceEditText.setText("1000");
                }
                if(mCameraUpdateCheckBox.isChecked()) {
                    editor.putBoolean("camera_update", true);
                }
                else {
                    editor.putBoolean("camera_update", false);
                }

                // Save circle snap settings
                if (mCircleSnapCheckBox.isChecked()) {
                    editor.putBoolean("circle_snap", true);
                }
                else {
                    editor.putBoolean("circle_snap", false);
                }

                if (mMarkerInCircleCheckBox.isChecked()) {
                    editor.putBoolean("marker_in_circle", true);
                }
                else {
                    editor.putBoolean("marker_in_circle", false);
                }

                if (mOverlayUpdateCheckBox.isChecked()) {
                    editor.putBoolean("overlay_update", true);
                }
                else {
                    editor.putBoolean("overlay_update", false);
                }

                if (mLoadDistanceCheckBox.isChecked()) {
                    String distanceString = mLoadDistanceEditText.getText().toString();
                    editor.putInt("load_distance_int", Integer.parseInt(distanceString));
                    editor.putBoolean("load_distance", true);
                }
                else {
                    editor.putBoolean("load_distance", false);
                }

                editor.putInt("circle_resolution", mCircleResolution);

                editor.commit();

                // Go back to main activity
                startActivity(new Intent(SettingsActivity.this, MapsActivity.class));
            }
        });
    }
}
