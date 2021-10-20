package com.example.finalproject_zachanderson;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MyOrdersActivity extends AppCompatActivity {
    //declaring variables
    ArrayList<String> savedNumbers = new ArrayList<String>();
    ArrayList<String> history = new ArrayList<String>();
    LinearLayout orderList;
    ImageView settingsCog;
    Database database;
    int counter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_orders);

        //get data passed from previous activities intent
        Bundle b = this.getIntent().getExtras();

        //initializing variables
        settingsCog = (ImageView) findViewById(R.id.settingsCog);
        orderList = (LinearLayout) findViewById(R.id.orderList);
        database = new Database(this);
        history = b.getStringArrayList("historyArrayList");

        //click listener for cog wheel ImageView
        settingsCog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //store current history in bundle to pass to next activity
                Bundle b = new Bundle();
                b.putStringArrayList("historyArrayList", history);

                //create and execute intent to start SettingsActivity
                Intent intent = new Intent(MyOrdersActivity.this, SettingsActivity.class);
                intent.putExtras(b);
                startActivity(intent);
            }
        });

        //gather all saved tracking numbers from database
        SQLiteDatabase db = database.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM savedOrders", null);
        while (cursor.moveToNext()) {
            savedNumbers.add(cursor.getString(0));
        }
        db.close();

        //Start new GetTimelineTrackTask, async task, to retrieve full timeline for each tracking number
        for (int i = 0; i < savedNumbers.size() ;i++) {
            new GetTimelineTrackTask().execute(savedNumbers.get(i));
        }

        //reset counter (keeps track of which tracking number is currently being used)
        counter = 0;
    }

    //function that populates orderList LinearLayout, called in onPostExecute() for GetTimelineTrackTask
    private void fillList(ArrayList<String> list) {
        //inflater for creating LinearLayouts based on XML layout
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //create new LinearLayout for each tracking number, reminder fillList() is called after each GetTimelineTrackTask and therefore for each saved tracking number
        LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.my_orders_list_entry, null);

        //store ImageView found inside new LinearLayout, see my_orders_list_entry.xml for details on linearLayout's layout
        ImageView imageView = (ImageView) linearLayout.getChildAt(0);

        //store TextView found inside new LinearLayout
        TextView textView = (TextView) linearLayout.getChildAt(1);

        //store spinner found inside new LinearLayout
        Spinner spinner = (Spinner) linearLayout.getChildAt(2);

        //create new ArrayAdapter for spinner using passed ArrayList containing data retrieved in GetTimelineTrackTask
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, list);
        spinner.setAdapter(dataAdapter);

        //check if package has been delivered using keywords, checks are made on most recent tracking timeline update located at passed ArrayList index 1
        //must be edited for tracking service provider
        //formulate new LinearLayout's TextView and ImageView based on if delivered or not
        if (list.get(1).contains("delivered") || list.get(1).contains("supplied") || list.get(1).contains("DELIVERED") || list.get(1).contains("Supplied")) {
            textView.setText("DELIVERED: \n" + savedNumbers.get(counter));
            imageView.setImageResource(R.drawable.closedboxicon);
        } else {
            textView.setText("IN-TRANSIT: \n" + savedNumbers.get(counter));
            imageView.setImageResource(R.drawable.truckicon);
        }

        //set click listener for each new LinearLayout
        linearLayout.setOnClickListener(v -> deletePrompt(linearLayout, linearLayout.getContext()));

        //now that LinearLayout has been properly formatted we can add it to main LinearLayout "orderList"
        orderList.addView(linearLayout);
        counter++;
    }

    //function that displays deletion prompt when a saved order is clicked, and acts accordingly to user input
    private void deletePrompt(LinearLayout linearLayout, Context mContext) {
        //new AlertDialog Builder to create prompt
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setCancelable(true);
        builder.setTitle("Delete Order?");

        //lambda for "Yes" option on prompt
        builder.setPositiveButton("Yes",
                (dialog, which) -> {
                    //remove clicked order from main LinearLayout "orderList"
                    orderList.removeView(linearLayout);

                    //get text from clicked order containing tracking number
                    TextView tempTextView = (TextView) linearLayout.getChildAt(1);

                    //string processing to isolate tracking number
                    String temp;
                    if (tempTextView.getText().toString().contains("DELIVERED:")) {
                        temp = tempTextView.getText().toString().replace("DELIVERED: \n", "");
                    } else {
                        temp = tempTextView.getText().toString().replace("IN-TRANSIT: \n", "");
                    }

                    //delete click order's tracking number entry from "savedOrders" table
                    SQLiteDatabase db = database.getWritableDatabase();
                    int result = db.delete("savedOrders", "number=?", new String[] {temp});
                    if (result == 0) {
                        Context context = getApplicationContext();
                        Toast unsuccessfulToast = Toast.makeText(context, temp, Toast.LENGTH_LONG);
                        unsuccessfulToast.show();
                    } else {
                        //error handling
                        Context context = getApplicationContext();
                        Toast successToast = Toast.makeText(context, "Delete Successful.", Toast.LENGTH_LONG);
                        successToast.show();
                    }

                    //close database
                    db.close();
                });

        //lambda for "Cancel" option on prompt, no additional code needed as option is set to android.R.string.cancel
        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> {
        });

        //create and show new AlertDialog using Builder
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    //asynctask class to handle complete timeline retrieval requests for "My Orders" page
    class GetTimelineTrackTask extends AsyncTask<String, String, String> {
        List<String> timeline = new ArrayList<String>();

        @Override
        protected String doInBackground(String... strings) {
            Document doc = null;

            //attempt to retrieve HTML form from url using jsoup and store entire tracking timeline as a List<String>
            try {
                doc = Jsoup.connect("url" + strings[0]).get();
                Elements data = doc.getElementsByClass("package-route-box-content");
                timeline = data.eachText();
            } catch (IOException e){
                //error handling
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Context context = getApplicationContext();
                        Toast errorToast = Toast.makeText(context, "Error detected while loading saved orders, please try again later.", Toast.LENGTH_LONG);
                        errorToast.show();
                    }
                });
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String result) {
            //processing strings in timeLine List<String> to show numbers for each timeline entry
            int counter = 1;
            int entryCounter = 1;
            ArrayList<String> resultData = new ArrayList<String>();
            for(String s: timeline) {
                if (counter % 2 != 0) {
                    resultData.add(entryCounter + ". " + s + ":\n");
                    entryCounter++;
                } else {
                    resultData.add("    " + s + "\n");
                }
                counter++;
            }

            //call to fillList() passing processed strings/data
            fillList(resultData);
        }
    }
}