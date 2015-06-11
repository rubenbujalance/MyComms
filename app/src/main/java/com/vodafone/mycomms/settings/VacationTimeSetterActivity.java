package com.vodafone.mycomms.settings;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.Switch;
import android.widget.TextView;

import com.vodafone.mycomms.R;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.view.tab.MyCommsDatePickerFragment;

import java.util.Date;

/**
 * Created by str_vig on 10/06/2015.
 */
public class VacationTimeSetterActivity extends ActionBarActivity {

    private MyCommsDatePickerFragment datePickerFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(Constants.TAG, "VacationTimeSetterActivity.onCreate: ");
        super.onCreate(savedInstanceState);

        setTitle(getString(R.string.settings_vacation_time_activity_title));
        datePickerFragment = new MyCommsDatePickerFragment();
        datePickerFragment.setOnDateSetListener(onDateSetListener);
        datePickerFragment.setOnCancelListener(onCancelListener);


        setContentView(R.layout.activity_vacation_time);

        Switch vacationTimeSwitch = (Switch) findViewById(R.id.switch_vacation_time);
        vacationTimeSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Switch vacationTimeSwitch = (Switch) v;
                if (vacationTimeSwitch.isChecked()) {
                    datePickerFragment.show(VacationTimeSetterActivity.this.getSupportFragmentManager(), "fragment_date_picker");

                } else {
                    VacationTimeSetterActivity.this.findViewById(R.id.settings_textview_vacation_time).setVisibility(View.VISIBLE);
                    VacationTimeSetterActivity.this.findViewById(R.id.vacation_time_ends_layout).setVisibility(View.GONE);
                }
            }
        });

    }

    private DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            Log.d(Constants.TAG, "VacationTimeSetterActivity.onDateSet: ");
            datePickerFragment.onDateSet(view, year, monthOfYear, dayOfMonth);
            updateDateText(datePickerFragment.getCalendar().getTime());
            VacationTimeSetterActivity.this.findViewById(R.id.settings_textview_vacation_time).setVisibility(View.GONE);
            VacationTimeSetterActivity.this.findViewById(R.id.vacation_time_ends_layout).setVisibility(View.VISIBLE);
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
        Log.d(Constants.TAG, "VacationTimeSetterActivity.updateDateText: " + date);
        TextView endDateTextView  =  (TextView)findViewById(R.id.vacation_setter_vacation_date_ends_text);
        endDateTextView.setText(Constants.SIMPLE_DATE_FORMAT_DISPLAY.format(date));
    }

}