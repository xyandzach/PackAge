package com.example.finalproject_zachanderson;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    //declare variables
    ArrayList<String> history = new ArrayList<String>();
    TextView outputTextView;
    LinearLayout outputView;
    ImageView settingsCog;
    Button outputButton;
    Database database;
    EditText input;
    ImageView logo;
    String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //get data passed from previous activities intent, important to check null for MainActivity as on startup no bundle will be passed
        if (this.getIntent().getExtras() != null) {
            Bundle b = this.getIntent().getExtras();
            history = b.getStringArrayList("historyArrayList");
        }

        //initialize variables
        outputTextView = (TextView) findViewById(R.id.outputTextView);
        outputButton = (Button) findViewById(R.id.outputButton);
        settingsCog = (ImageView) findViewById(R.id.settingsCog);
        outputView = (LinearLayout) findViewById(R.id.outputView);
        database = new Database(this);
        input = (EditText) findViewById(R.id.trackingNumberInput);
        logo = (ImageView) findViewById(R.id.logo);

        //click listener for cog wheel ImageView
        settingsCog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //store current history in bundle to pass to next activity
                Bundle b = new Bundle();
                b.putStringArrayList("historyArrayList", history);

                //create and execute intent to start SettingsActivity
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                intent.putExtras(b);
                startActivity(intent);
            }
        });
    }

    //reset certain fields on resume of MainActivity
    @Override
    public void onResume(){
        super.onResume();

        //reset inputs and outputs
        input.setText("");
        outputButton.setText("");
        outputTextView.setText("");

        //hide result element for quick track and re-display logo
        outputView.setVisibility(View.INVISIBLE);
        logo.setVisibility(View.VISIBLE);
    }

    //click function for track button
    public void quickTrack(View view) {
        //close keyboard
        input.onEditorAction(EditorInfo.IME_ACTION_DONE);

        //check for valid number before adding to history
        String trackingNumber = input.getText().toString();
        if (trackingNumber != "" || trackingNumber != null) {
            history.add(trackingNumber);
        }

        //formulate url for jsoup
        url = "url" + trackingNumber;

        //start new QuickTrackTask, async task, to retrieve tracking data
        new QuickTrackTask().execute();
    }

    //click function for "Save to My Orders" button that adds current tracking number to saved orders page
    public void addToMyOrders(View view) {
        //insert new tracking number into database
        SQLiteDatabase db = database.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("number", input.getText().toString());
        long result = db.insert("savedOrders", null, values);

        //error handling
        if (result == -1) {
            Context context = getApplicationContext();
            Toast badInsert = Toast.makeText(context, "Error adding "  + input.getText().toString() + " to my orders. Check if number is already saved. ", Toast.LENGTH_LONG);
            badInsert.show();
        } else {
            Context context = getApplicationContext();
            Toast goodInsert = Toast.makeText(context, input.getText().toString() + " added to my orders.", Toast.LENGTH_LONG);
            goodInsert.show();
        }

        //close database
        db.close();
    }

    //asynctask class to handle quick track requests
    class QuickTrackTask extends AsyncTask<String, String, String> {
        String mostRecentUpdate;
        String updateDateTime;

        @Override
        protected String doInBackground(String... strings) {
            Document doc = null;

            //attempt to retrieve HTML form from url using jsoup and store more recent update data
            try {
                doc = Jsoup.connect(url).get();
                Thread.sleep(1000);
                Elements data = doc.getElementsByClass("package-route-box-content");
                if (data != null) {
                    mostRecentUpdate = data.eq(1).text();
                    updateDateTime = data.eq(0).text();
                }
            } catch (IOException | InterruptedException e){
                //error handling
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Context context = getApplicationContext();
                        Toast errorToast = Toast.makeText(context, "Error detected, make sure you're tracking number is correct. If problem persists, please try again later.", Toast.LENGTH_LONG);
                        errorToast.show();
                    }
                });
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String result) {
            //validity check
            if (mostRecentUpdate != "" && mostRecentUpdate != null) {
                //string processing
                String processedUpdate = mostRecentUpdate.replace("Supplied", "Delivered");
                if (processedUpdate.length() >= 102) {
                    processedUpdate = processedUpdate.substring(0, 101);
                }

                //post data, hide logo, and present result
                outputButton.setText(updateDateTime);
                outputTextView.setText(processedUpdate);
                logo.setVisibility(View.INVISIBLE);
                outputView.setVisibility(View.VISIBLE);
            } else {
                //error handling
                Context context = getApplicationContext();
                Toast errorToast = Toast.makeText(context, "Error detected, make sure you're tracking number is correct. If problem persists, please try again later.", Toast.LENGTH_LONG);
                errorToast.show();
            }
        }
    }

    //click function for "My Orders" button
    public void openMyOrders(View view) {
        //store current history in bundle to pass to next activity
        Bundle b = new Bundle();
        b.putStringArrayList("historyArrayList", history);

        //create and execute intent to start MyOrdersActivity
        Intent intent = new Intent(this, MyOrdersActivity.class);
        intent.putExtras(b);
        startActivity(intent);
    }

    //click function for "History" button
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