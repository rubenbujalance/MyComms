package com.vodafone.mycomms.settings;

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
import android.widget.ImageView;
import android.widget.TextView;

import com.vodafone.mycomms.R;
import com.vodafone.mycomms.contacts.connection.DownloadLocalContacts;
import com.vodafone.mycomms.realm.RealmContactTransactions;
import com.vodafone.mycomms.settings.globalcontacts.AddGlobalContactsActivity;
import com.vodafone.mycomms.util.Constants;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AccountsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AccountsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AccountsFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private TextView tvAddLocalContacts, tvAddGlobalContacts;
    private ImageView imgCheckLocalContacts, imgCheckGlobalContacts;
    private SharedPreferences sp;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PreferencesFragment.
     */

    public static AccountsFragment newInstance(String param1, String param2) {
        AccountsFragment fragment = new AccountsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public AccountsFragment() {
        // Required empty public constructor
    }

    public static AccountsFragment newInstance(int index, String param2) {
        AccountsFragment fragment = new AccountsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, index);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume(){
        super.onResume();
        checkAccountButtonVisibility();
        Log.d(Constants.TAG, "AccountsFragment.onResume: ");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.layout_set_accounts, container, false);
        sp = getActivity().getSharedPreferences(Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);
        setViewsAndEvents(v);
        return v;
    }


    private void setViewsAndEvents(View v)
    {
        tvAddLocalContacts = (TextView) v.findViewById(R.id.btn_add_local_contacts);
        tvAddGlobalContacts = (TextView) v.findViewById(R.id.btn_add_vodafone_global);
        imgCheckLocalContacts = (ImageView) v.findViewById(R.id.acc_check_local_contacts);
        imgCheckGlobalContacts = (ImageView) v.findViewById(R.id.acc_check_vodafone_global);
        checkAccountButtonVisibility();
        tvAddGlobalContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(getActivity(), AddGlobalContactsActivity.class);
                startActivity(in);
            }
        });
        tvAddLocalContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sp.edit().putBoolean(Constants.IS_LOCAL_CONTACTS_LOADING_ENABLED, true).apply();
                checkAccountButtonVisibility();
                String profileId = sp.getString(Constants.PROFILE_ID_SHARED_PREF, "");
                DownloadLocalContacts downloadLocalContacts =
                        new DownloadLocalContacts(getActivity(), profileId, true);
                downloadLocalContacts.downloadAndStore();
            }
        });
    }

    private void checkAccountButtonVisibility()
    {
        if(sp.getBoolean(Constants.IS_LOCAL_CONTACTS_LOADING_ENABLED,false)
                || validateLocalContactsRealm())
        {
            sp.edit().putBoolean(Constants.IS_LOCAL_CONTACTS_LOADING_ENABLED, true).apply();
            tvAddLocalContacts.setVisibility(View.GONE);
            imgCheckLocalContacts.setVisibility(View.VISIBLE);
        }
        else
        {
            tvAddLocalContacts.setVisibility(View.VISIBLE);
            imgCheckLocalContacts.setVisibility(View.GONE);
        }
        if(sp.getBoolean(Constants.IS_GLOBAL_CONTACTS_LOADING_ENABLED,false))
        {
            tvAddGlobalContacts.setVisibility(View.GONE);
            imgCheckGlobalContacts.setVisibility(View.VISIBLE);
        }
        else
        {
            tvAddGlobalContacts.setVisibility(View.VISIBLE);
            imgCheckGlobalContacts.setVisibility(View.GONE);
        }
    }

    private boolean validateLocalContactsRealm() {
        String profile = sp.getString(Constants.PROFILE_ID_SHARED_PREF, "");
        new RealmContactTransactions(profile);
        return RealmContactTransactions.validateContactPlatformExists(null, Constants.PLATFORM_LOCAL);
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

}
