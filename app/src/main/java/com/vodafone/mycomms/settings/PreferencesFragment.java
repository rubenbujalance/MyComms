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
import android.widget.Switch;

import com.vodafone.mycomms.R;
import com.vodafone.mycomms.login.LoginSignupActivity;
import com.vodafone.mycomms.settings.connection.IProfileConnectionCallback;
import com.vodafone.mycomms.util.Constants;
import com.vodafone.mycomms.util.UserSecurity;

import org.json.JSONObject;

import java.util.HashMap;

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
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private int mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private ProfileController profileController;
    private boolean isSourceDB = true;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PreferencesFragment.
     */
    // TODO: Rename and change types and number of parameters
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

    // TODO: Rename and change types of parameters
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
        profileController = new ProfileController(this);
        profileController.setConnectionCallback(this);
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.d(Constants.TAG, "PreferencesFragment.onResume: ");
//        TextView editProfile = (TextView) getActivity().findViewById(R.id.edit_profile);
//        editProfile.setVisibility(View.INVISIBLE);

        profileController.getProfile();
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

                //Reset user security data
                UserSecurity.resetTokens(getActivity());

                //Reset profile data
                SharedPreferences sp = getActivity().getSharedPreferences(
                        Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);

                SharedPreferences.Editor editor = sp.edit();
                editor.remove(Constants.ACCESS_TOKEN_SHARED_PREF);
                editor.remove(Constants.PROFILE_ID_SHARED_PREF);
                editor.commit();

                //Go to login page as a new task
                Intent in = new Intent(getActivity(), LoginSignupActivity.class);
                in.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK |
                        Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(in);
                getActivity().finish();
            }
        });

        Switch privateTimeZoneSwitch = (Switch) v.findViewById(R.id.setting_share_current_time_switch);
        privateTimeZoneSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isSourceDB) {
                    shareCurrentTime(isChecked);
                }
            }
        });

        Switch doNotDisturbSwitch  = (Switch) v.findViewById(R.id.settings_do_not_disturb_switch);
        doNotDisturbSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isSourceDB) {
                    setDoNotDisturb(isChecked);
                }
                isSourceDB = false;
            }
        });

        return v;
    }

    private void shareCurrentTime(boolean isChecked) {
        HashMap settingsHashMap = new HashMap<>();
        if(isChecked) {
            settingsHashMap.put(Constants.PROFILE_PRIVATE_TIMEZONE, false);
        }else{
            settingsHashMap.put(Constants.PROFILE_PRIVATE_TIMEZONE, true);
        }

        profileController.updateSettingsData(settingsHashMap);
    }


    private void setDoNotDisturb(boolean isChecked) {
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

    @Override
    public void onProfileReceived(UserProfile userProfile) {
        Log.d(Constants.TAG, "PreferencesFragment.onProfileReceived: settings:" + userProfile.getSettings() );

        JSONObject jsonSettings = null;
        boolean privateTimeZone = false;
        boolean doNotDisturb = false;

        try {
            if(userProfile.getSettings() != null && userProfile.getSettings().length() > 0) {
                jsonSettings = new JSONObject(userProfile.getSettings());
                if (jsonSettings.isNull(Constants.PROFILE_PRIVATE_TIMEZONE)){
                    privateTimeZone = true;
                } else{
                    //TODO: Check if this is False (if true is null according To Apiary
                    privateTimeZone = jsonSettings.getBoolean(Constants.PROFILE_PRIVATE_TIMEZONE);
                }
            }
        } catch (Exception e) {
            Log.e(Constants.TAG, "PreferencesFragment.onProfileReceived: timezone " , e);
        }

        try {
            if(userProfile.getSettings() != null && userProfile.getSettings().length() > 0 && jsonSettings != null) {
                if (jsonSettings.isNull(Constants.PROFILE_DONOTDISTURB)){
                    doNotDisturb = true;
                } else{
                    //TODO: Check if this is False (if true is null according To Apiary
                    doNotDisturb = jsonSettings.getBoolean(Constants.PROFILE_DONOTDISTURB);
                }
            }
        } catch (Exception e) {
            Log.e(Constants.TAG, "PreferencesFragment.onProfileReceived, doNotDisturb: " , e);
        }


        if(privateTimeZone){
            Switch shareCurrentTimeSwitch = (Switch) getActivity().findViewById(R.id.setting_share_current_time_switch);
            shareCurrentTimeSwitch.setChecked(false);
        }else {
            Switch shareCurrentTimeSwitch = (Switch) getActivity().findViewById(R.id.setting_share_current_time_switch);
            shareCurrentTimeSwitch.setChecked(true);
        }

        if(doNotDisturb){
            Switch doNotDisturbSwitch = (Switch) getActivity().findViewById(R.id.settings_do_not_disturb_switch);
            doNotDisturbSwitch.setChecked(true);
        }else {
            Switch doNotDisturbSwitch = (Switch) getActivity().findViewById(R.id.settings_do_not_disturb_switch);
            doNotDisturbSwitch.setChecked(false);
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
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }



}
