package com.vodafone.mycomms.settings;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.Switch;
import android.widget.TextView;

import com.vodafone.mycomms.R;
import com.vodafone.mycomms.connection.IConnectionCallback;
import com.vodafone.mycomms.main.SplashScreenActivity;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.UncaughtExceptionHandlerController;
import com.vodafone.mycomms.util.Utils;
import com.vodafone.mycomms.view.tab.MyCommsDatePickerFragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by str_vig on 10/06/2015.
 */
public class VacationTimeSetterActivity extends FragmentActivity implements IConnectionCallback {
    private MyCommsDatePickerFragment datePickerFragment;
    public static final String EXTRA_HOLIDAY_END_DATE = "EXTRA_VACATION_TIME_ID";
    private String holidayEndDate = "";
    private String initialHolidayEndDate = "";

    private ProfileController profileController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(Constants.TAG, "VacationTimeSetterActivity.onCreate: ");
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler
                (
                        new UncaughtExceptionHandlerController(this, SplashScreenActivity.class)
                );
        setContentView(R.layout.activity_vacation_time);

        profileController = new ProfileController(this);
        profileController.setConnectionCallback(this);

        Switch vacationTimeSwitch = (Switch) findViewById(R.id.switch_vacation_time);
        vacationTimeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Calendar holidayEndDateCalendar = Calendar.getInstance();

                    datePickerFragment = new MyCommsDatePickerFragment();

                    if (holidayEndDate != null && holidayEndDate.length()>0) {

                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat(Constants.API_DATE_FULL_FORMAT);
                            Date endDate = sdf.parse(holidayEndDate);
                            holidayEndDateCalendar.setTime(endDate);
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
                    holidayEndDate = "";
                }
            }
        });

        if (getIntent().getExtras() != null) {
            holidayEndDate = getIntent().getExtras().getString(EXTRA_HOLIDAY_END_DATE);
            initialHolidayEndDate = getIntent().getExtras().getString(EXTRA_HOLIDAY_END_DATE);

            if(holidayEndDate != null && holidayEndDate.length()>0){
                VacationTimeSetterActivity.this.findViewById(R.id.settings_textview_vacation_time).setVisibility(View.GONE);
                VacationTimeSetterActivity.this.findViewById(R.id.vacation_time_ends_layout).setVisibility(View.VISIBLE);
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat(Constants.API_DATE_FULL_FORMAT);
                    Date endDate = sdf.parse(holidayEndDate);
                    updateDateText(endDate);
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

        //Send data to server
        if(initialHolidayEndDate.compareTo(holidayEndDate)!=0)
            updateHolidayData();

        //Send info to previous screen
        Intent resultIntent = new Intent();
        resultIntent.putExtra(SettingsMainActivity.VACATION_TIME_END_VALUE, holidayEndDate);

        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }
    
    private DatePickerDialog.OnDateSetListener onDateSetListener =
            new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    Log.d(Constants.TAG, "VacationTimeSetterActivity.onDateSet: ");
                    datePickerFragment.getCalendar().set(Calendar.HOUR_OF_DAY, 0);
                    datePickerFragment.getCalendar().set(Calendar.MINUTE, 0);
                    datePickerFragment.getCalendar().set(Calendar.SECOND, 0);
                    datePickerFragment.getCalendar().set(Calendar.MILLISECOND, 0);
                    Date selectedDate = datePickerFragment.getCalendar().getTime();
                    holidayEndDate = Utils.timestampToFormatedString(
                            selectedDate.getTime(), Constants.API_DATE_FULL_FORMAT);
                    updateDateText(selectedDate);
                    VacationTimeSetterActivity.this.findViewById(
                            R.id.settings_textview_vacation_time).setVisibility(View.GONE);
                    VacationTimeSetterActivity.this.findViewById
                            (R.id.vacation_time_ends_layout).setVisibility(View.VISIBLE);
                }
            };

    public void updateHolidayData(){
        Log.d(Constants.TAG, "VacationTimeSetterActivity.updateHolidayData: ");

        //RBM: Changes to adapt Android to new backend date format:
        // - Don't send "true"
        // - Change dateFormat to yyyy-MM-dd'T'HH:mm:ss.SSSZ
        try {
            HashMap holidayHashMap = new HashMap<>();
            HashMap settingsHashMap = new HashMap<>();
            if (holidayEndDate != null && holidayEndDate.length() > 0) {
//                String holidayEndDateUTC = Utils.isoDateToUTC(holidayEndDate);
//                holidayEndDateUTC = holidayEndDateUTC.substring(
//                        0, holidayEndDateUTC.length()-5)+"Z";
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                String holidayStartDate = Utils.timestampToFormatedString(
                        cal.getTime().getTime(), Constants.API_DATE_FULL_FORMAT);

                holidayHashMap.put(Constants.PROFILE_HOLIDAY_START_DATE, holidayStartDate);
                holidayHashMap.put(Constants.PROFILE_HOLIDAY_END_DATE, holidayEndDate);
            }

            settingsHashMap.put(Constants.PROFILE_HOLIDAY, holidayHashMap);
            profileController.updateSettingsData(settingsHashMap);
        } catch(Exception e) {
            Log.e(Constants.TAG, "VacationTimeSetterActivity.updateHolidayData: ");
        }
    }

    private DatePickerDialog.OnCancelListener onCancelListener =
            new DatePickerDialog.OnCancelListener(){
                @Override
                public void onCancel(DialogInterface dialog) {
                    Log.d(Constants.TAG, "VacationTimeSetterActivity.onCancel: ");
                    ((Switch)VacationTimeSetterActivity.this
                            .findViewById(R.id.switch_vacation_time)).setChecked(false);
                }
            };

    private void updateDateText(Date date){
        Log.d(Constants.TAG, "VacationTimeSetterActivity.updateDateText: " + date);
        TextView endDateTextView  =  (TextView)findViewById(R.id.vacation_setter_vacation_date_ends_text);
        endDateTextView.setText(Constants.SIMPLE_DATE_FORMAT_DISPLAY.format(date));

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