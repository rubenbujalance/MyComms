package com.vodafone.mycomms.settings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.vodafone.mycomms.MycommsApp;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.login.LoginSignupActivity;
import com.vodafone.mycomms.realm.RealmProfileTransactions;
import com.vodafone.mycomms.settings.connection.IProfileConnectionCallback;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.UserSecurity;
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

    private int mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private ProfileController profileController;
    private boolean isSourceDB = true;

    private long holidayEndDate = 0L;
    private boolean isFirstLoad = true;
    private boolean doNotDisturb = false;
    private boolean privateTimeZone = false;

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
        if (getArguments() != null) {
            mParam1 = getArguments().getInt(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        this.realm = Realm.getInstance(getActivity());
        this.realm.setAutoRefresh(true);
        profileController = new ProfileController(getActivity());
        profileController.setConnectionCallback(this);
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.d(Constants.TAG, "PreferencesFragment.onResume: ");
//        TextView editProfile = (TextView) getActivity().findViewById(R.id.edit_profile);
//        editProfile.setVisibility(View.INVISIBLE);

        profileController.setConnectionCallback(this);
        profileController.getProfile(this.realm);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.layout_preferences, container, false);

        Button btLogout = (Button)v.findViewById(R.id.btLogout);
        btLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(Constants.TAG, "PreferencesFragment.onClick: Logout");

                SharedPreferences sp = getActivity().getSharedPreferences(
                        Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);
                String profileId = sp.getString(Constants.PROFILE_ID_SHARED_PREF, null);

                //Logout on server
                profileController.logoutToAPI();

                //Remove cookies if Sales Force login
                Utils.removeCookies();

                //Reset user security data
                UserSecurity.resetTokens(getActivity());

                //Reset profile data
                SharedPreferences.Editor editor = sp.edit();
                editor.remove(Constants.ACCESS_TOKEN_SHARED_PREF);
                editor.remove(Constants.PROFILE_ID_SHARED_PREF);
                editor.commit();

                //Remove User from DB
                if(profileId!=null) {
                    RealmProfileTransactions profileTx = new RealmProfileTransactions(getActivity());
                    profileTx.removeUserProfile(profileId, null);
                }

                ((MycommsApp)getActivity().getApplication()).appIsInitialized = false;

                //Go to login page as a new task
                Intent in = new Intent(getActivity(), LoginSignupActivity.class);
                in.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK |
                        Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(in);
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
        return v;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.realm.close();
    }

    private void shareCurrentTime(boolean isChecked) {
        this.privateTimeZone = !isChecked;

        updateProfileInDb();

        HashMap settingsHashMap = new HashMap<>();
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

        HashMap settingsHashMap = new HashMap<>();
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

    public void onStop(){
        super.onStop();
    }


    @Override
    public void onProfileReceived(UserProfile userProfile) {
        Log.d(Constants.TAG, "PreferencesFragment.onProfileReceived: settings:" + userProfile.getSettings());


        JSONObject jsonSettings = null;
        boolean privateTimeZone = false;
        boolean doNotDisturb = false;

        try{
            if(userProfile.getSettings() != null && userProfile.getSettings().length() > 0) {
                jsonSettings = new JSONObject(userProfile.getSettings());

                if(jsonSettings.has("holiday")) {
                    JSONObject jsonHoliday = jsonSettings.getJSONObject("holiday");
                    String endDateISO = jsonHoliday.getString(Constants.PROFILE_HOLIDAY_END_DATE);
                    SimpleDateFormat sdf = new SimpleDateFormat(Constants.API_DATE_FORMAT);
                    Date endDate = sdf.parse(endDateISO);
                    this.holidayEndDate = endDate.getTime();
                    TextView vacationTimeEnds = (TextView) getActivity().findViewById(R.id.settings_preferences_vacation_time_value);

                    if (holidayEndDate > 0) {
                        String holidayDateToSet = Constants.SIMPLE_DATE_FORMAT_DISPLAY.format(holidayEndDate);
                        Log.d(Constants.TAG, "PreferencesFragment.onProfileReceived: setting holidayDate to:" + holidayDateToSet);
                        vacationTimeEnds.setText(holidayDateToSet);
                    }
                }
            }
        } catch (Exception e){
            Log.e(Constants.TAG, "PreferencesFragment.onProfileReceived: ");
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
                    Switch shareCurrentTimeSwitch = (Switch) getActivity().findViewById(R.id.setting_share_current_time_switch);
                    shareCurrentTimeSwitch.setChecked(false);
                }
            });

        }else
        {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Switch shareCurrentTimeSwitch = (Switch) getActivity().findViewById(R.id.setting_share_current_time_switch);
                    shareCurrentTimeSwitch.setChecked(true);
                }
            });
        }


        if(doNotDisturb)
        {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Switch doNotDisturbSwitch = (Switch) getActivity().findViewById(R.id.settings_do_not_disturb_switch);
                    doNotDisturbSwitch.setChecked(true);
                }
            });

        }else
        {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Switch doNotDisturbSwitch = (Switch) getActivity().findViewById(R.id.settings_do_not_disturb_switch);
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
        public void onFragmentInteraction(Uri uri);
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
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case (SettingsMainActivity.VACATION_TIME_SETTER_ID) : {
                if (resultCode == Activity.RESULT_OK) {
                    Log.d(Constants.TAG, "PreferencesFragment.onActivityResult: ACTIVITY_RESULT_OK");
                    this.holidayEndDate = data.getLongExtra(SettingsMainActivity.VACATION_TIME_END_VALUE, 0L);

                    updateHolidayText(holidayEndDate);
                    updateProfileInDb();


                }
                break;
            }
        }
    }

    void updateHolidayText(long holidayEndDate){
        Log.d(Constants.TAG, "PreferencesFragment.updateHolidayText: " + holidayEndDate);
        this.holidayEndDate = holidayEndDate;
        TextView vacationTimeEnds = (TextView) getActivity().findViewById(R.id.settings_preferences_vacation_time_value);

        String holidayDateToSet = null;
        if(holidayEndDate > 0){
            holidayDateToSet = Constants.SIMPLE_DATE_FORMAT_DISPLAY.format(holidayEndDate);
        }else {
            holidayDateToSet = getString(R.string.settings_label_vacation_time_not_set);
        }

        Log.d(Constants.TAG, "PreferencesFragment.updateHolidayText: setting holidayDate to:" + holidayDateToSet);
        vacationTimeEnds.setText(holidayDateToSet);
    }

    void updateProfileInDb(){
        this.profileController.updateUserProfileSettingsInDB(false, this.privateTimeZone, this.holidayEndDate, this.doNotDisturb);
    }

}
