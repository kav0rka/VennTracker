package kavorka.venn_tracker.Tutorials;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import kavorka.venn_tracker.MapsActivity;
import kavorka.venn_tracker.R;

public class TutorialsActivity extends AppCompatActivity {


    private Button mCirclesButton;
    private Button mSpawnPointsButton;
    private Button mBackButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorials);

        mCirclesButton = (Button) findViewById(R.id.buttonTutorialsMenuCircles);
        mSpawnPointsButton = (Button) findViewById(R.id.buttonTutorialsMenuSpawnPoints);
        mBackButton = (Button) findViewById(R.id.buttonTutorialsMenuBack);

        mCirclesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(TutorialsActivity.this, TutorialCirclesActivity.class));
            }
        });
        mSpawnPointsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(TutorialsActivity.this, TutorialSpawnPointsActivity.class));

            }
        });
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(TutorialsActivity.this, MapsActivity.class));
            }
        });
    }
}
