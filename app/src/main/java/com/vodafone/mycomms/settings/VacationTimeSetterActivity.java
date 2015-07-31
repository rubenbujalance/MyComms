package com.vodafone.mycomms.settings;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.Switch;
import android.widget.TextView;

import com.vodafone.mycomms.R;
import com.vodafone.mycomms.connection.IConnectionCallback;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.view.tab.MyCommsDatePickerFragment;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by str_vig on 10/06/2015.
 */
public class VacationTimeSetterActivity extends FragmentActivity implements IConnectionCallback {
    private MyCommsDatePickerFragment datePickerFragment;
    public static final String EXTRA_HOLIDAY_END_DATE = "EXTRA_VACATION_TIME_ID";
    private long holidayEndDate;
    public boolean isOnHoliday = true;

    private ProfileController profileController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(Constants.TAG, "VacationTimeSetterActivity.onCreate: ");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_vacation_time);

        profileController = new ProfileController(this);
        profileController.setConnectionCallback(this);

        Switch vacationTimeSwitch = (Switch) findViewById(R.id.switch_vacation_time);
        vacationTimeSwitch.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Switch vacationTimeSwitch = (Switch) v;
                if (vacationTimeSwitch.isChecked()) {
                    Calendar holidayEndDateCalendar = Calendar.getInstance();

                    datePickerFragment = new MyCommsDatePickerFragment();

                    if(holidayEndDate > 0L) {
                        try {
                            holidayEndDateCalendar.setTime(Constants.SIMPLE_DATE_FORMAT_DISPLAY.parse(Constants.SIMPLE_DATE_FORMAT_DISPLAY.format(holidayEndDate)));
                            datePickerFragment.setCalendar(holidayEndDateCalendar);
                        } catch (ParseException e) {
                            Log.w(Constants.TAG, "VacationTimeSetterActivity.onClick: ", e);
                        }
                    }
                    datePickerFragment.setOnDateSetListener(onDateSetListener);
                    datePickerFragment.setOnCancelListener(onCancelListener);
                    datePickerFragment.show(VacationTimeSetterActivity.this.getSupportFragmentManager(), "fragment_date_picker");


                } else {
                    VacationTimeSetterActivity.this.findViewById(R.id.settings_textview_vacation_time).setVisibility(View.VISIBLE);
                    VacationTimeSetterActivity.this.findViewById(R.id.vacation_time_ends_layout).setVisibility(View.GONE);
                    isOnHoliday = false;
                    updateHolidayData();
                    //datePickerFragment.setCalendar(Calendar.getInstance());
                }
            }
        });

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            this.holidayEndDate = extras.getLong(EXTRA_HOLIDAY_END_DATE);
            if(this.holidayEndDate > 0L){
                VacationTimeSetterActivity.this.findViewById(R.id.settings_textview_vacation_time).setVisibility(View.GONE);
                VacationTimeSetterActivity.this.findViewById(R.id.vacation_time_ends_layout).setVisibility(View.VISIBLE);
                try {
                    updateDateText(Constants.SIMPLE_DATE_FORMAT_DISPLAY.parse(Constants.SIMPLE_DATE_FORMAT_DISPLAY.format(holidayEndDate)));
                } catch (ParseException e) {
                    Log.w(Constants.TAG, "VacationTimeSetterActivity.onCreate: exception while parsing date", e);
                }
            }else{
                VacationTimeSetterActivity.this.findViewById(R.id.settings_textview_vacation_time).setVisibility(View.VISIBLE);
                VacationTimeSetterActivity.this.findViewById(R.id.vacation_time_ends_layout).setVisibility(View.GONE);
                ((Switch)VacationTimeSetterActivity.this.findViewById(R.id.switch_vacation_time)).setChecked(false);
            }
        }
    }

    @Override
    public void onBackPressed(){
        Log.d(Constants.TAG, "VacationTimeSetterActivity.onBackPressed: ");
        Intent resultIntent = new Intent();
        if(isOnHoliday) {
            resultIntent.putExtra(SettingsMainActivity.VACATION_TIME_END_VALUE, holidayEndDate);
        }else{
            resultIntent.putExtra(SettingsMainActivity.VACATION_TIME_END_VALUE, 0L);
        }
        setResult(Activity.RESULT_OK, resultIntent);
        super.onBackPressed();
    }
    
    private DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            Log.d(Constants.TAG, "VacationTimeSetterActivity.onDateSet: ");
            updateDateText(datePickerFragment.getCalendar().getTime());
            VacationTimeSetterActivity.this.findViewById(R.id.settings_textview_vacation_time).setVisibility(View.GONE);
            VacationTimeSetterActivity.this.findViewById(R.id.vacation_time_ends_layout).setVisibility(View.VISIBLE);
            updateHolidayData();
        }
    };

    private DatePickerDialog.OnCancelListener onCancelListener = new DatePickerDialog.OnCancelListener(){
        @Override
        public void onCancel(DialogInterface dialog) {
            Log.d(Constants.TAG, "VacationTimeSetterActivity.onCancel: ");
            ((Switch)VacationTimeSetterActivity.this.findViewById(R.id.switch_vacation_time)).setChecked(false);

        }
    };

    private void updateDateText(Date date){
        holidayEndDate = date.getTime();
        Log.d(Constants.TAG, "VacationTimeSetterActivity.updateDateText: " + date);
        TextView endDateTextView  =  (TextView)findViewById(R.id.vacation_setter_vacation_date_ends_text);
        endDateTextView.setText(Constants.SIMPLE_DATE_FORMAT_DISPLAY.format(date));

    }


    public void updateHolidayData(){
        Log.d(Constants.TAG, "VacationTimeSetterActivity.updateHolidayData: ");
        HashMap settingsHashMap = new HashMap<>();
        if(holidayEndDate > 0 && isOnHoliday){
            settingsHashMap.put(Constants.PROFILE_HOLIDAY, true);
            settingsHashMap.put(Constants.PROFILE_HOLIDAY_END_DATE,  holidayEndDate);
        }else {
            settingsHashMap.put(Constants.PROFILE_HOLIDAY, false);
        }
        profileController.updateSettingsData(settingsHashMap);
    }

    @Override
    public void onConnectionNotAvailable() {
        Log.d(Constants.TAG, "VacationTimeSetterActivity.onConnectionNotAvailable: ");
        profileController.showToast(getString(R.string.no_internet_connection));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}