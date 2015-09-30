package com.vodafone.mycomms.util;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import model.Contact;

/**
 * Created by str_oan on 05/06/2015.
 */
public class InternalContactSearch
{
    private Context context;
    private String profileId;

    public  InternalContactSearch(Context context, String profileId)
    {
        this.context = context;
        this.profileId = profileId;
    }

    /**
     * Get All local contacts
     * @author str_vdf01
     * @return (ArrayList Contact ) -> list of found contacts
     */
    public ArrayList<Contact> getAllLocalContact() {
        ArrayList<String> ids = getAllContactsIds();
        ArrayList<Contact> contacts = new ArrayList<>();
        Contact contact;
        SharedPreferences sp = context.getSharedPreferences(
                Constants.MYCOMMS_SHARED_PREFS, Context.MODE_PRIVATE);
        String deviceId = sp.getString(Constants.DEVICE_ID_SHARED_PREF,"");
        for(String id : ids)
        {
            contact = new Contact("");
            contact.setId(Constants.CONTACT_LOCAL_CONTENT + "_" + profileId + "_" + deviceId + "_" + id);
            contact.setContactId(Constants.CONTACT_LOCAL_CONTENT + "_" + profileId + "_" + deviceId + "_" + id);
            contact.setPlatform(Constants.PLATFORM_LOCAL);
            contact.setLongField1(Utils.setPlatformOrder(Constants.PLATFORM_LOCAL));
            contact.setProfileId(profileId);
            contact = setContactsCompanyDataByContactsIds(id, contact);
            contact = setContactsBasicDataByContactsIds(id, contact);
            contact = setContactsEmailDataByContactsIds(id, contact);
            contact = setContactsPhoneDataByContactsIds(id, contact);
            contact.setSearchHelper
                    ((
                                    Utils.normalizeStringNFD(contact.getFirstName()) + " " +
                                    Utils.normalizeStringNFD(contact.getLastName()) + " " +
                                    Utils.normalizeStringNFD(contact.getCompany()) + " " +
                                    Utils.normalizeStringNFD(contact.getEmails())).trim()
                    );

            contact.setSortHelper
                    ((
                                contact.getLongField1() + " " +
                                    Utils.normalizeStringNFD(contact.getFirstName()) + " " +
                                    Utils.normalizeStringNFD(contact.getLastName()) + " " +
                                    Utils.normalizeStringNFD(contact.getCompany())).trim()
                    );
            contacts.add(contact);
        }
        return contacts;
    }

    /**
     * Sets Contact Company data (if any) into the passed Contact
     * @author str_oan
     * @param id (String)-> contact id
     * @param contact (Contact) -> contact where data will be added
     * @return (Contact) -> the input Contact with new info
     */
    private Contact setContactsCompanyDataByContactsIds(String id, Contact contact)
    {
        try
        {
            Cursor cursor = getCompanyDataByContactId(id);
            if(cursor.moveToFirst())
            {
                do
                {
                    contact.setCompany(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Organization.COMPANY)));
                    contact.setOfficeLocation(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Organization.OFFICE_LOCATION)));
                    contact.setPosition(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Organization.JOB_DESCRIPTION)));
                }
                while(cursor.moveToNext());
            }
            cursor.close();
            return contact;
        }
        catch (Exception ex)
        {
            String message = ex.getMessage();
            Log.e(Constants.TAG, "setContactsCompanyByContactsIds() -> ERROR: " + message);
            return contact;
        }
    }

    /**
     * Sets Contact Basic data (if any) into the passed Contact
     * @author str_oan
     * @param id (String)-> contact id
     * @param contact (Contact) -> contact where data will be added
     * @return (Contact) -> the input Contact with new info
     */
    private Contact setContactsBasicDataByContactsIds(String id, Contact contact)
    {
        try
        {
            Cursor cursor = getContactBasicDataByContactId(id);
            if(cursor.moveToFirst())
            {
                do
                {
                    String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    String[] splitName = new String[10];
                    if (name!=null)
                        splitName = name.split(" ");
                    else
                        splitName[0] = " ";

                    if(splitName.length > 1)
                    {
                        contact.setFirstName(splitName[0]);
                        contact.setLastName(splitName[1]);
                    }
                    else
                    {
                        contact.setFirstName(splitName[0]);
                    }

                    contact.setLastSeen(Long.parseLong(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.LAST_TIME_CONTACTED))));

                    String avatar = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_URI));
                    contact.setAvatar(avatar);
                }
                while (cursor.moveToNext());
            }
            cursor.close();
            return contact;
        }
        catch (Exception ex)
        {
            String message = ex.getMessage();
            Log.e(Constants.TAG, "setContactsBasicDataByContactsIds() -> ERROR: " + message);
            return contact;
        }
    }

    /**
     * Sets Contact Email data (if any) into the passed Contact
     * @author str_oan
     * @param id (String)-> contact id
     * @param contact (Contact) -> contact where data will be added
     * @return (Contact) -> the input Contact with new info
     */
    private Contact setContactsEmailDataByContactsIds(String id, Contact contact)
    {
        try
        {
            Cursor cursor = getContactEmailDataByContactId(id);
            if(cursor.moveToFirst())
            {
                do
                {
                    contact.setEmails(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)));
                }
                while(cursor.moveToNext());
            }
            cursor.close();
            return contact;
        }
        catch (Exception ex)
        {
            String message = ex.getMessage();
            Log.e(Constants.TAG, "setContactsEmailDataByContactsIds() -> ERROR: " + message);
            return contact;
        }
    }

    /**
     * Sets Contact Phone data (if any) into the passed Contact
     * @author str_oan
     * @param id (String)-> contact id
     * @param contact (Contact) -> contact where data will be added
     * @return (Contact) -> the input Contact with new info
     */
    private Contact setContactsPhoneDataByContactsIds(String id, Contact contact)
    {
        try
        {
            Cursor cursor = getContactPhoneDataByContactId(id);
            if(cursor.moveToFirst())
            {
                do
                {
                    ContentResolver cr = this.context.getContentResolver();
                    Cursor phones = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id, null, null);
                    HashMap<String, String> body = new HashMap<>();
                    while (phones.moveToNext()) {
                        //TODO: Get all numbers and save them into a JSON (and show them correctly on detail)
                        String number = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).trim();
                        int type = phones.getInt(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
                        body.put(Constants.CONTACT_PHONE, number);
                        switch (type) {
                            case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
                                body.put(Constants.CONTACT_PHONE_HOME, number);
                                break;
                            case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
                                body.put(Constants.CONTACT_PHONE_MOBILE, number);
                                break;
                            case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
                                body.put(Constants.CONTACT_PHONE_WORK, number);
                                break;
                            default:
                                body.put(Constants.CONTACT_PHONE, number);
                                break;
                        }
                    }
                    phones.close();
                    if (body!=null && !body.isEmpty()) {
                        JSONObject json = new JSONObject(body);
                        contact.setPhones(json.toString());
                    }
                }
                while(cursor.moveToNext());
            }
            cursor.close();
            return contact;
        }
        catch (Exception ex)
        {
            String message = ex.getMessage();
            Log.e(Constants.TAG, "setContactsPhoneDataByContactsIds() -> ERROR: " + message);
            return contact;
        }
    }

    /**
     * Gets all contact ids from Contact Name
     * @author str_vdf01
     * @return (ArrayList String ) -> list of unique contact IDS
     */
    private ArrayList<String> getAllContactsIds()
    {
        Cursor cursorByName = getAllContactsIdsFromName();

        ArrayList<String> distinctIds = new ArrayList<>();

        if(null != cursorByName && cursorByName.moveToFirst())
        {
            do
            {
                String id = cursorByName
                        .getString(cursorByName.getColumnIndex(ContactsContract.Data.CONTACT_ID));
                if(!distinctIds.contains(id))
                {
                    distinctIds.add(id);
                }
            }
            while (cursorByName.moveToNext());
        }
        if(null != cursorByName && !cursorByName.isClosed())
        {
            cursorByName.close();
        }


        return distinctIds;
    }

    /**
     * Gets Cursor of all contact_ids from ContactsContract.Contacts table
     * @author str_vdf01
     * @return (Cursor) -> ids of contacts if any, otherwise empty Cursor
     */
    private Cursor getAllContactsIdsFromName()
    {
        ContentResolver cr = this.context.getContentResolver();
        try
        {
            Uri uri = ContactsContract.Data.CONTENT_URI;
            String[] projection = new String[]
                    {
                            ContactsContract.Data.CONTACT_ID
                    };

            return cr.query(uri, projection, null, null, ContactsContract.Data.CONTACT_ID+" ASC");
        }
        catch (Exception ex)
        {
            String message = ex.getMessage();
            Log.e(Constants.TAG, "getAllContactsIdsFromName() -> ERROR: " + message);
            return null;
        }
    }

    /**
     * Gets Company Info by Contact ID
     * @author str_oan
     * @param id (String) -> contact id passed as a String
     * @return (Cursor) -> company data stored in the Cursor
     */
    private Cursor getCompanyDataByContactId(String id)
    {
        ContentResolver cr = this.context.getContentResolver();
        Uri uri = ContactsContract.Data.CONTENT_URI;
        String[] projection = new String[]
                {
                          ContactsContract.CommonDataKinds.Organization.COMPANY
                        , ContactsContract.CommonDataKinds.Organization.OFFICE_LOCATION
                        , ContactsContract.CommonDataKinds.Organization.DEPARTMENT
                        , ContactsContract.CommonDataKinds.Organization.JOB_DESCRIPTION
                        , ContactsContract.CommonDataKinds.Organization.TITLE
                        , ContactsContract.CommonDataKinds.Organization.DATA
                };

        String selection = ContactsContract.Data.CONTACT_ID + " = ? AND " +
                ContactsContract.Data.MIMETYPE + " = ?";
        String[] selectionArgs = new String[]{
                id,
                ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE};

        return cr.query(uri,
                projection, selection, selectionArgs, ContactsContract.Data.CONTACT_ID + " ASC");

    }

    /**
     * Gets Contact Data Info by Contact ID
     * @author str_oan
     * @param id (String) -> contact id passed as a String
     * @return (Cursor) -> company data stored in the Cursor
     */
    private Cursor getContactBasicDataByContactId(String id)
    {
        ContentResolver cr = this.context.getContentResolver();
        Uri uri = ContactsContract.Data.CONTENT_URI;
        String[] projection = new String[]
                {
                          ContactsContract.Data.DISPLAY_NAME
                        , ContactsContract.Data.LAST_TIME_CONTACTED
                        , ContactsContract.Data.TIMES_CONTACTED
                        , ContactsContract.Data.PHOTO_URI
                };

        String selection = ContactsContract.Data.CONTACT_ID +" =? ";

        String[] selectionArgs = new String[]
                {
                        id
                };
        return cr.query
                (
                        uri
                        , projection
                        , selection
                        , selectionArgs
                        , ContactsContract.Data.CONTACT_ID + " ASC"
                );
    }

    /**
     * Gets Contact Email Info by Contact ID
     * @author str_oan
     * @param id (String) -> contact id passed as a String
     * @return (Cursor) -> company data stored in the Cursor
     */
    private Cursor getContactEmailDataByContactId(String id)
    {
        ContentResolver cr = this.context.getContentResolver();
        Uri uri = ContactsContract.Data.CONTENT_URI;
        String[] projection = new String[]
                {
                          ContactsContract.CommonDataKinds.Email.ADDRESS
                };

        String selection = ContactsContract.Data.CONTACT_ID +" =? "
                + " AND "+ContactsContract.Data.MIMETYPE+" = ?";
        String[] selectionArgs = new String[]
                {
                        id
                        , ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE
                };

        return cr.query
                (
                        uri
                        , projection
                        , selection
                        , selectionArgs
                        , ContactsContract.Data.CONTACT_ID + " " + "ASC"
                );
    }

    /**
     * Gets Contact Phone Info by Contact ID
     * @author str_oan
     * @param id (String) -> contact id passed as a String
     * @return (Cursor) -> company data stored in the Cursor
     */
    private Cursor getContactPhoneDataByContactId(String id)
    {
        ContentResolver cr = this.context.getContentResolver();
        Uri uri = ContactsContract.Data.CONTENT_URI;
        String[] projection = new String[]
                {
                          ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER
                };

        String selection = ContactsContract.Data.CONTACT_ID +" =? "
                + " AND "+ContactsContract.Data.MIMETYPE+" = ?";
        String[] selectionArgs = new String[]
                {
                        id
                        , ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
                };

        return cr.query
                (
                        uri
                        , null
                        , selection
                        , selectionArgs
                        , ContactsContract.Data.CONTACT_ID + " " + "ASC"
                );
    }
}
