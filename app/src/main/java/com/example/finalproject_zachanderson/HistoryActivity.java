package com.example.finalproject_zachanderson;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {
    //declare variables
    LinearLayout historyLinearLayout;
    ArrayList<String> history;
    ImageView settingsCog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        //get data passed from previous activities intent
        Bundle b = this.getIntent().getExtras();

        //initialize variables
        historyLinearLayout = (LinearLayout) findViewById(R.id.historyList);
        settingsCog = (ImageView) findViewById(R.id.settingsCog);
        history = b.getStringArrayList("historyArrayList");

        //click listener for cog while ImageView
        settingsCog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //store current history in bundle to pass to next activity
                Bundle b = new Bundle();
                b.putStringArrayList("historyArrayList", history);

                //create and execute intent to start SettingsActivity
                Intent intent = new Intent(HistoryActivity.this, SettingsActivity.class);
                intent.putExtras(b);
                startActivity(intent);
            }
        });

        //call to fill historyLinearLayout with tracking number history
        fillHistory(history);
    }

    //function that fills historyLinearLayout by creating a new TextView for each string in passed ArrayList
    //once all TextViews are added, order is reversed to show most recent tracking numbers at the top
    private void fillHistory(ArrayList<String> history) {
        //inflater to create TextViews based on XML layout
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //creating TextView for each string and adding it to historyLinearLayout
        for (String s: history) {
            TextView textView = (TextView) inflater.inflate(R.layout.history_text_view, null);
            textView.setText("#: " + s);
            historyLinearLayout.addView(textView);
        }

        //secondary ArrayList used to reverse order of TextViews
        ArrayList<View> views = new ArrayList<View>();

        //storing TextViews in secondary ArrayList
        for(int x = 0; x < historyLinearLayout.getChildCount(); x++) {
            views.add(historyLinearLayout.getChildAt(x));
        }

        //clearing original ArrayList
        historyLinearLayout.removeAllViews();

        //adding TextViews back to original ArrayList in reverse order by moving down the secondary ArrayList (back to front)
        for(int x = views.size() - 1; x >= 0; x--) {
            historyLinearLayout.addView(views.get(x));
        }
    }
}