package com.vodafone.mycomms.view.tab;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.widget.DatePicker;

import com.vodafone.mycomms.util.Constants;

import java.util.Calendar;

public class MyCommsDatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    private Calendar calendar = Calendar.getInstance();
    private DatePickerDialog.OnDateSetListener onDateSetListener;
    private DialogInterface.OnCancelListener onCancelListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        if(view.isShown()) {
            this.getCalendar().set(Calendar.YEAR, year);
            this.getCalendar().set(Calendar.MONTH, month);
            this.getCalendar().set(Calendar.DAY_OF_MONTH, day);
            if (onDateSetListener != null) {
                onDateSetListener.onDateSet(view, year, month, day);
            }
        }
    }

    public Calendar getCalendar() {
        return this.calendar;
    }

    public void setCalendar(Calendar calendar) {
        this.calendar = calendar;
    }

    public DatePickerDialog.OnDateSetListener getOnDateSetListener() {
        return this.onDateSetListener;
    }

    public void setOnDateSetListener(DatePickerDialog.OnDateSetListener onDateSetListener) {
        this.onDateSetListener = onDateSetListener;
    }

    @Override
    public void onPause() {
        super.onPause();
        this.dismiss();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        Log.d(Constants.TAG, "MyCommsDatePickerFragment.onCancel:");
        if(this.onCancelListener != null) {
            onCancelListener.onCancel(dialog);
        }
    }

    public DatePickerDialog.OnCancelListener getOnCancelListener() {
        return this.onCancelListener;
    }

    public void setOnCancelListener(DatePickerDialog.OnCancelListener onCancelListener) {
        this.onCancelListener = onCancelListener;
    }
}