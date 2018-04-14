package com.capstonegyg.gyg.UI.PostGyg;

import android.app.DatePickerDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationListener;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.app.AlertDialog;
import android.os.Handler;
import android.widget.Toast;
import java.util.Date;

import com.capstonegyg.gyg.R;
import com.capstonegyg.gyg.StartScreen;
import com.capstonegyg.gyg.UI.Profile.ProfileActivity;
import com.capstonegyg.gyg.UI.Profile.ProfileData;
import com.capstonegyg.gyg.UI.Profile.ProfileFirebaseAdapter;
import com.capstonegyg.gyg.UI.Profile.ProfileViewHolder;
import com.capstonegyg.gyg.UI.Profile.Settings;
import com.capstonegyg.gyg.UI.ViewGyg.ViewGygActivity;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *  Written by Jonathan Luetze.
 *
 *  PostGyg allows a user to create and post a Gyg to Firebase
 */

// TO DO:
// option to add picture for a gyg
    // location should suggest current location/ ability on a map to look it up

public class PostGygActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    int year;
    int month;
    int day;
    String gygEndDate;
    Boolean  gygVolunteer;

    SwitchCompat sw;

    ArrayList<String> times;

    Spinner timeSpinner;

    EditText gygName;
    EditText gygCategory;
    EditText gygLocation;
    EditText gygDescription;

    double gygFee;

    String gygTime;
    String gygPosterName;
    String gygPostedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_gyg_screen);


     //   Intent i = new Intent(PostGygActivity.this, MyLocationUsingHelper.class);
     //   startActivity(i);

    //    MyLocationUsingHelper myLocation;
    //    myLocation=new MyLocationUsingHelper(this);



        gygLocation = findViewById(R.id.gyg_area);

        //Set views
        sw = findViewById(R.id.switch2);
        timeSpinner = findViewById(R.id.time_spinner);

        gygEndDate = "NONE";

        /* Initialize data */

        initTimeList();

        initTimeSpinner();

        initDatePicker();

        initVolunteerSwitch();

        setDesign();

        /* Declaring and setting Submit button to send info to Firebase */
        Button Submit = findViewById(R.id.submit_gyg);
        Submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Boolean goodInput = checkInput();

                if(goodInput) {getInput();}

                if (gygVolunteer && gygFee != 0.00 && goodInput) {
                    showAlert("Volunteer Alert", "Please turn Volunteering off before setting a payment");
                    resetVolunteer();
                }
                else if (goodInput) {

                    /* Pop-Up Box to verify that the User wants to post the Gyg */
                    AlertDialog.Builder builder;

                    builder = new AlertDialog.Builder(PostGygActivity.this, android.R.style.Theme_Material_Dialog_Alert);
                    builder.setTitle("Post Gyg");
                    builder.setMessage("Are you sure you want to post this Gyg?");

                    /* If user definitely wants to post the gyg, get data and send it to Firebase */
                    builder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            getInput();

                            pushToFirebase();

                            showPostSuccess();
                        }
                    });

                    /* If user doesn't want to post the gyg */
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
                    builder.create();
                    builder.show();
                }
            }
        });
    }

    /* Sets information collected from date picker */
    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {

        this.year = year;
        this.month = month;
        this.day = day;

        String dateString = "Deadline: "+ month + "/" + day + "/" + year; // creates displayed date

        this.gygEndDate = month + "/" + day + "/" + year; // saves date that's pushed to Firebase

        TextView V = findViewById(R.id.display_date);
        V.setText(dateString);

        Button W = findViewById(R.id.date_button);
        W.setText(R.string.change_deadline);

        V.setTextSize(18);
        V.setAllCaps(true);
    }

    /* Private methods */
    void initTimeList() {
        /* Declaring and filling Arraylist and Spinner for time */
        times = new ArrayList<>();
        times.add("Hourly");
        times.add("Daily");
        times.add("Weekly");
        times.add("Monthly");
        times.add("Yearly");
    }

    /* Initializes the Time Spinner */
    void initTimeSpinner() {
        ArrayAdapter<String> timeSpinnerArrayAdapter = new ArrayAdapter<>
                (this, android.R.layout.simple_spinner_dropdown_item, times);
        timeSpinner.setAdapter(timeSpinnerArrayAdapter);
    }

    void setDesign() {
        /* Setting the background color for the pay section */
        ImageView payBackground = findViewById(R.id.pay_background);
        payBackground.setBackgroundColor(Color.rgb(220,220, 220));
    }

    /* Initializes the date picker and controls its behavior */
    void initDatePicker() {
        /* Gyg Deadline Date Picker Information */
        Button date = findViewById(R.id.date_button);
        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new DatePickerFragment();
                newFragment.show(getFragmentManager(), "datePicker");
            }
        });
    }

    /* Initializes the volunteer switch and controls its behavior */
    void initVolunteerSwitch() {
        gygVolunteer = false;

        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if (isChecked) {
                    gygVolunteer = true;
                    gygFee = 0.00;
                    String free = "0.00";
                    EditText edt = findViewById(R.id.gyg_pay);
                    edt.setText(free);
                }
                else {
                    gygVolunteer = false;
                    EditText edt = findViewById(R.id.gyg_pay);
                    edt.setText("");
                }
            }
        });
    }

    /* Sets remainder of variables not set by checkInput
     * Checks for no payment
     */
    void getInput() {

        EditText edt = findViewById(R.id.gyg_pay);
        gygFee = Double.parseDouble(edt.getText().toString());

        if(gygFee == 0.00) {
            gygVolunteer = true;    // switch volunteer to true if it's free
        }

        //Spinner s = findViewById(R.id.time_spinner);
        gygTime = timeSpinner.getSelectedItem().toString();

        gygPosterName = "testName";
        gygPostedDate = new Date().toString();
    }

    /* Creates Gyg object and pushes it to Firebase */
    void pushToFirebase() {
        // Declaring Firebase object
        final FirebaseDatabase[] database = {FirebaseDatabase.getInstance()};
        DatabaseReference postDBR = database[0].getReference();

        /* Formatting and Pushing of data */
        PostGygData gyg = new PostGygData(format(gygName), format(gygCategory), format(gygLocation), gygFee,
                format(gygDescription), gygTime, gygPosterName, gygPostedDate, gygEndDate, gygVolunteer);
        postDBR.child("gygs").push().setValue(gyg);
    }

    /* Function to format the input */
    private String format(EditText E) {
        return E.getText().toString();
    }

    /* Function to return if EditText element is empty */
    Boolean isEmpty(EditText E) {
        return E.getText().toString().equals("");
    }

    /* Function to display an Alert box with OK button based on title and message (parameters) */
    void showAlert(String title, String message) {
        AlertDialog.Builder b2 = new AlertDialog.Builder(PostGygActivity.this,android.R.style.Theme_Material_Dialog_Alert);
        b2.setTitle(title);
        b2.setMessage(message);

        b2.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
            }
        });

        b2.create();
        b2.show();
    }

    /* Function to reset payment as volunteer (back to 0.00) */
    void resetVolunteer() {
        String free = "0.00";
        EditText edt = findViewById(R.id.gyg_pay);
        edt.setText(free);
    }

    /* Displays a success message after posting Gyg to Firebase */
    void showPostSuccess() {
        /* Posted Successfully Message */
        AlertDialog.Builder newB = new AlertDialog.Builder(PostGygActivity.this);
        newB.setMessage("Posted Successfully");

        newB.create();
        newB.show();

        delayMessage();
    }

    /* Delays message before disappearing */
    void delayMessage() {
        /* Handler delays message from disappearing */
        Handler mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            public void run() {
                finish();
            }
        }, 3000);
    }

    /*  Sets variables needed for checking and checks for empty input
     *  Throws error if empty input is found
     */
    Boolean checkInput() {

        /* Getting and formatting user input */
        gygName        = findViewById(R.id.gyg_title);
        gygCategory    = findViewById(R.id.gyg_category);
        gygLocation    = findViewById(R.id.gyg_area);
        gygDescription = findViewById(R.id.gyg_description);

        EditText edt   = findViewById(R.id.gyg_pay);

        if(isEmpty(gygName)) {
            showAlert("No Title", "Please enter a title for this Gyg");
            return false;
        }
        else if(isEmpty(gygCategory)) {
            showAlert("No Category", "Please specify a category for this Gyg");
            return false;
        }
        else if(isEmpty(gygLocation)) {
            showAlert("No Location", "Please specify a Location for this Gyg");
            return false;
        }
        else if(isEmpty(edt)) {
            showAlert("No Payment", "Please enter a valid amount for payment or mark as volunteer");
            return false;
        }
        else if(!checkCurrency(edt)) {
            showAlert("Incorrect Payment Input", "Please enter a valid currency amount");
            return false;
        }
        else if(isEmpty(gygDescription)) {
            showAlert("No Description", "Please enter a Description for this Gyg");
            return false;
        }
        else {
            return true;
        }
    }

    /* Function to check accurate currency input using a regular expression */
    Boolean checkCurrency(EditText E) {
        String s = format(E);
        String pattern;

        pattern = "[0-9]*((\\.[0-9]{0," + 2 + "})?)||(\\.)?";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(s);
        return m.matches();
    }

}
