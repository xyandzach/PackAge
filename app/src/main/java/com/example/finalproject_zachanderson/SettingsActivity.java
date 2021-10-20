package com.example.finalproject_zachanderson;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

//NOTE: Although UI for limiting history entries has been implemented, functionality for this feature has not been (ran out of time)

public class SettingsActivity extends AppCompatActivity {
    //declaring variables
    ArrayList<String> history = new ArrayList<String>();
    ImageView minusButton;
    TextView recordNumber;
    ImageView plusButton;
    ImageView homeicon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //get data passed from previous activities intent
        Bundle b = this.getIntent().getExtras();

        //initialize variables
        recordNumber = (TextView) findViewById(R.id.recordNumber);
        minusButton = (ImageView) findViewById(R.id.minusButton);
        plusButton = (ImageView) findViewById(R.id.plusButton);
        homeicon = (ImageView) findViewById(R.id.homeicon);
        history = b.getStringArrayList("historyArrayList");

        //click listener for plus ImageView
        plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //increase number of history entries if not currently set to 20
                int increase = Integer.valueOf(recordNumber.getText().toString()) + 1;
                if (increase != 21) {
                    recordNumber.setText(String.valueOf(increase));
                }
            }
        });

        //click listener for minus ImageView
        minusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //decrease number of history entries if not currently set to 0
                int decrease = Integer.valueOf(recordNumber.getText().toString()) - 1;
                if (decrease != -1) {
                    recordNumber.setText(String.valueOf(decrease));
                }
            }
        });

        //click listener for home ImageView
        homeicon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //store current history in bundle to pass to next activity
                Bundle b = new Bundle();
                b.putStringArrayList("historyArrayList", history);

                //create and execute intent to start MainActivity
                Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                intent.putExtras(b);
                startActivity(intent);
            }
        });
    }

    //click function for "My Orders" Button
    public void openMyOrders(View view) {
        //store current history in bundle to pass to next activity
        Bundle b = new Bundle();
        b.putStringArrayList("historyArrayList", history);

        //create and execute intent to start MyOrdersActivity
        Intent intent = new Intent(this, MyOrdersActivity.class);
        intent.putExtras(b);
        startActivity(intent);
    }

    //click function for "History" Button
    public void openHistory(View view) {
        //store current history in bundle to pass to next activity
        Bundle b = new Bundle();
        b.putStringArrayList("historyArrayList", history);

        //create and execute intent to start HistoryActivity
        Intent intent = new Intent(this, HistoryActivity.class);
        intent.putExtras(b);
        startActivity(intent);
    }


}