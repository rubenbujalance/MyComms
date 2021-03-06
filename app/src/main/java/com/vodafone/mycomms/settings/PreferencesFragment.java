package com.vodafone.mycomms.settings;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.vodafone.mycomms.MycommsApp;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.settings.connection.IProfileConnectionCallback;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.Utils;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import io.realm.Realm;
import model.UserProfile;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PreferencesFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PreferencesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PreferencesFragment extends Fragment implements IProfileConnectionCallback{
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    private OnFragmentInteractionListener mListener;
    public ProfileController profileController;

    public String holidayEndDate = "";
    private boolean doNotDisturb = false;
    private boolean privateTimeZone = false;
    private int commingFrom;

    private TextView vacationTimeEnds;
    private ImageView vacationTimeArrow;

    private View view;

    private Realm realm;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PreferencesFragment.
     */

    public static PreferencesFragment newInstance(String param1, String param2) {
        PreferencesFragment fragment = new PreferencesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public PreferencesFragment() {
        // Required empty public constructor
    }

    public static PreferencesFragment newInstance(int index, String param2) {
        PreferencesFragment fragment = new PreferencesFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, index);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        vacationTimeEnds = (TextView)getActivity().findViewById(
                R.id.settings_preferences_vacation_time_value);
        vacationTimeArrow = (ImageView)getActivity().findViewById(
                R.id.about_arrow_right_top);

        this.realm = Realm.getDefaultInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.layout_set_preferences, container, false);
        view = inflater.inflate(R.layout.layout_set_preferences, container, false);
        vacationTimeEnds = (TextView) v.findViewById(R.id.settings_preferences_vacation_time_value);
        vacationTimeArrow = (ImageView) v.findViewById(R.id.about_arrow_right_top);

        Button btLogout = (Button)v.findViewById(R.id.btLogout);
        btLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(Constants.TAG, "PreferencesFragment.onClick: Logout");
                profileController.logoutToAPI();
                MycommsApp.appIsInitialized = false;
            }
        });

        LinearLayout vacationTimeButton = (LinearLayout) v.findViewById(R.id.button_settings_vacation_time);
        vacationTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), VacationTimeSetterActivity.class);
                intent.putExtra(VacationTimeSetterActivity.EXTRA_HOLIDAY_END_DATE, holidayEndDate);
                startActivityForResult(intent, SettingsMainActivity.VACATION_TIME_SETTER_ID);
            }
        });

        Switch shareCurrentTimeSwitch = (Switch) v.findViewById(R.id.setting_share_current_time_switch);
        shareCurrentTimeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                shareCurrentTime(isChecked);

            }
        });

        Switch doNotDisturbSwitch  = (Switch) v.findViewById(R.id.settings_do_not_disturb_switch);
        doNotDisturbSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setDoNotDisturb(isChecked);
            }
        });

        TextView aboutButton = (TextView) v.findViewById(R.id.btnAbout);
        aboutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AboutActivity.class);
                startActivity(intent);
            }
        });

        profileController = new ProfileController(getActivity());
        profileController.setConnectionCallback(this);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        if(commingFrom!=SettingsMainActivity.VACATION_TIME_SETTER_ID) {
            profileController.setConnectionCallback(this);
            profileController.getProfile(this.realm);
        }

        commingFrom = -1;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(null != realm)
            this.realm.close();
    }

    private void shareCurrentTime(boolean isChecked) {
        this.privateTimeZone = !isChecked;

        updateProfileInDb();

        HashMap<String, Boolean> settingsHashMap = new HashMap<>();
        if(isChecked) {
            settingsHashMap.put(Constants.PROFILE_PRIVATE_TIMEZONE, false);
        }else{
            settingsHashMap.put(Constants.PROFILE_PRIVATE_TIMEZONE, true);
        }

        profileController.updateSettingsData(settingsHashMap);
    }


    private void setDoNotDisturb(boolean isChecked) {
        this.doNotDisturb = isChecked;

        updateProfileInDb();

        HashMap<String, Boolean> settingsHashMap = new HashMap<>();
        if(isChecked) {
            settingsHashMap.put(Constants.PROFILE_DONOTDISTURB, true);
        }else{
            settingsHashMap.put(Constants.PROFILE_DONOTDISTURB, false);
        }

        profileController.updateSettingsData(settingsHashMap);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onProfileReceived(UserProfile userProfile) {
        Log.d(Constants.TAG, "PreferencesFragment.onProfileReceived: settings:" + userProfile.getSettings());

        JSONObject jsonSettings = null;
        boolean privateTimeZone = false;
        boolean doNotDisturb = false;

        try{
            if(userProfile.getSettings() != null && userProfile.getSettings().length() > 0) {
                String test = userProfile.getSettings();
                jsonSettings = new JSONObject(userProfile.getSettings());

                if(jsonSettings.has(Constants.PROFILE_HOLIDAY)) {
                    JSONObject jsonHoliday = jsonSettings.getJSONObject(Constants.PROFILE_HOLIDAY);
                    if (jsonHoliday.getString(Constants.PROFILE_HOLIDAY_END_DATE)!=null) {
                        String endDateStr = jsonHoliday.getString(Constants.PROFILE_HOLIDAY_END_DATE);
                        endDateStr = endDateStr.replaceAll("Z", "+0000");
                        holidayEndDate = Utils.isoDateToTimezone(endDateStr);
                    }

                    if (holidayEndDate != null && holidayEndDate.length()>0) {
                        SimpleDateFormat sdf = new SimpleDateFormat(Constants.API_DATE_FULL_FORMAT);
                        Date endDate = sdf.parse(holidayEndDate);

                        final String holidayDateToSet =
                                Constants.SIMPLE_DATE_FORMAT_DISPLAY.format(endDate.getTime());
                        Log.d(Constants.TAG, "PreferencesFragment.onProfileReceived: setting holidayDate to:" + holidayDateToSet);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                vacationTimeEnds.setText(holidayDateToSet);
                                vacationTimeEnds.setVisibility(View.VISIBLE);
                                vacationTimeArrow.setVisibility(View.GONE);
                            }
                        });
                    }
                }
            }
        } catch (Exception e){
            Log.e(Constants.TAG, "PreferencesFragment.onProfileReceived: ", e);
        }

        try {
            if(userProfile.getSettings() != null && userProfile.getSettings().length() > 0) {
                jsonSettings = new JSONObject(userProfile.getSettings());
                if (jsonSettings.isNull(Constants.PROFILE_PRIVATE_TIMEZONE)){
                    privateTimeZone = false;
                } else{
                    privateTimeZone = jsonSettings.getBoolean(Constants.PROFILE_PRIVATE_TIMEZONE);
                }
            }
        } catch (Exception e) {
            Log.e(Constants.TAG, "PreferencesFragment.onProfileReceived: timezone " , e);
        }

        try {
            if(userProfile.getSettings() != null && userProfile.getSettings().length() > 0 && jsonSettings != null) {
                if (jsonSettings.isNull(Constants.PROFILE_DONOTDISTURB)){
                    doNotDisturb = false;
                } else{
                    doNotDisturb = jsonSettings.getBoolean(Constants.PROFILE_DONOTDISTURB);
                }
            }
        } catch (Exception e) {
            Log.e(Constants.TAG, "PreferencesFragment.onProfileReceived, doNotDisturb: " , e);
        }

        this.doNotDisturb = doNotDisturb;
        this.privateTimeZone = privateTimeZone;

        if(privateTimeZone)
        {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run()
                {
                    Switch shareCurrentTimeSwitch = (Switch) view.findViewById(R.id.setting_share_current_time_switch);
                    shareCurrentTimeSwitch.setChecked(false);
                }
            });

        }else
        {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Switch shareCurrentTimeSwitch = (Switch) view.findViewById(R.id.setting_share_current_time_switch);
                    shareCurrentTimeSwitch.setChecked(true);
                }
            });
        }


        if(doNotDisturb)
        {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Switch doNotDisturbSwitch = (Switch) view.findViewById(R.id.settings_do_not_disturb_switch);
                    doNotDisturbSwitch.setChecked(true);
                }
            });

        }else
        {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Switch doNotDisturbSwitch = (Switch) view.findViewById(R.id.settings_do_not_disturb_switch);
                    doNotDisturbSwitch.setChecked(false);
                }
            });
        }
    }

    @Override
    public void onProfileConnectionError() {

    }

    @Override
    public void onUpdateProfileConnectionError() {

    }

    @Override
    public void onUpdateProfileConnectionCompleted() {

    }

    @Override
    public void onPasswordChangeError(String error) {

    }

    @Override
    public void onPasswordChangeCompleted() {

    }

    @Override
    public void onConnectionNotAvailable() {

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onPause(){
        super.onPause();
        profileController.setConnectionCallback(null);
        Log.d(Constants.TAG, "PreferencesFragment.onPause: ");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(Constants.TAG, "PreferencesFragment.onActivityResult: ");
        commingFrom = requestCode;
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case (SettingsMainActivity.VACATION_TIME_SETTER_ID) : {
                if (resultCode == Activity.RESULT_OK) {
                    Log.d(Constants.TAG, "PreferencesFragment.onActivityResult: ACTIVITY_RESULT_OK");
                    this.holidayEndDate = data.getStringExtra(SettingsMainActivity.VACATION_TIME_END_VALUE);

                    updateHolidayText(holidayEndDate);
                    updateProfileInDb();
                }
                break;
            }
        }
    }

    void updateHolidayText(String holidayEndDate){
        Log.d(Constants.TAG, "PreferencesFragment.updateHolidayText: " + holidayEndDate);
        this.holidayEndDate = holidayEndDate;

        String holidayDateToSet = null;

        try {
            if (holidayEndDate != null && holidayEndDate.length() > 0) {
                SimpleDateFormat sdf = new SimpleDateFormat(Constants.API_DATE_FULL_FORMAT);
                Date endDate = sdf.parse(holidayEndDate);
                holidayDateToSet = Constants.SIMPLE_DATE_FORMAT_DISPLAY.format(endDate.getTime());
            } else {
                holidayDateToSet = getString(R.string.settings_label_vacation_time_not_set);
            }
        } catch (Exception e) {
            Log.e(Constants.TAG, "PreferencesFragment.updateHolidayText: ");
        }

        Log.d(Constants.TAG, "PreferencesFragment.updateHolidayText: setting holidayDate to:" + holidayDateToSet);
        vacationTimeEnds.setText(holidayDateToSet);
        vacationTimeEnds.setVisibility(View.VISIBLE);
        vacationTimeArrow.setVisibility(View.GONE);
    }

    void updateProfileInDb(){
        this.profileController.updateUserProfileSettingsInDB(
                false, this.privateTimeZone, holidayEndDate, this.doNotDisturb);
    }

}
